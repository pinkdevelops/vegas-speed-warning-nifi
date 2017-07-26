/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hortonworks.federal.processors.speedwarning;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.*;

@Tags({"City of Las Vegas, Speed Warning"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@EventDriven
@CapabilityDescription("Uses incoming vehicle gps data to determine if the vehicle is speeding or not")
@SeeAlso({})
public class SpeedWarning extends AbstractVegasProcessor {

    static final PropertyDescriptor VEHICLE_ID = new PropertyDescriptor.Builder()
            .name("Vehicle ID")
            .required(true)
            .description("Name of the attribute for Vehicle ID")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    static final PropertyDescriptor LONGITUDE = new PropertyDescriptor.Builder()
            .name("Longitude")
            .required(true)
            .description("Name of the attribute for longitude")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    static final PropertyDescriptor LATITUDE = new PropertyDescriptor.Builder()
            .name("Latitude")
            .required(true)
            .description("Name of the attribute for latitude")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    static final PropertyDescriptor CURRENT_SPEED = new PropertyDescriptor.Builder()
            .name("Speed")
            .required(true)
            .description("Name of the attribute for speed")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    static final Relationship LOCATION_NOT_AVAILABLE = new Relationship.Builder()
            .name("Speed not available at location")
            .description("There is no speed data for the current location given")
            .build();
    static final Relationship SPEED_WARNING = new Relationship.Builder()
            .name("Speed Warning")
            .description("The driver is over the speed limit")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(VEHICLE_ID);
        descriptors.add(LONGITUDE);
        descriptors.add(LATITUDE);
        descriptors.add(CURRENT_SPEED);
        descriptors.add(INDEX_NAME);
        descriptors.add(HOST);
        descriptors.add(PORT);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(LOCATION_NOT_AVAILABLE);
        relationships.add(SPEED_WARNING);

        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final RestClient restClient = createClient(context);

        final double lon = Double.parseDouble(flowFile.getAttribute(context.getProperty(LONGITUDE).getValue()));
        final double lat = Double.parseDouble(flowFile.getAttribute(context.getProperty(LATITUDE).getValue()));
        final double currentSpeed = Double.parseDouble(flowFile.getAttribute(context.getProperty(CURRENT_SPEED).getValue()));
        final String vehicleID = flowFile.getAttribute(context.getProperty(VEHICLE_ID).getValue());
        final String esIndex = "/" + context.getProperty(INDEX_NAME).getValue() + "/_search";

        HttpEntity entity1 = new NStringEntity(
                "{\"query\":{\"bool\":{\"must\":{\"match_all\":{}},\"filter\":{\"geo_shape\":{\"geometry\":{\"shape\":{\"type\":\"circle\",\"coordinates\":[" + lon + "," + lat + "],\"radius\":\"10m\"},\"relation\":\"intersects\"}}}}}}", ContentType.APPLICATION_JSON);

        Response response = null;
        try {
            response = restClient.performRequest("GET", esIndex, Collections.singletonMap("pretty", "true"),
                    entity1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String results = null;
        try {
            results = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int searchHits = JsonPath.read(results, "$.hits.total");

        if (searchHits > 0) {
            String strSpeed = JsonPath.read(results, "$.hits.hits[0]._source.properties.TITLE");
            int speed = Integer.parseInt(strSpeed.substring(0, 2));
            double eventOverSpeed = currentSpeed - speed;
            long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
            WarningPojo warning = new WarningPojo(eventOverSpeed, timestamp, vehicleID, speed);
            Gson gson = new Gson();
            String json = gson.toJson(warning);

            flowFile = session.write(flowFile, new OutputStreamCallback() {

                @Override
                public void process(OutputStream out) throws IOException {
                    out.write(json.getBytes());
                }
            });
            session.transfer(flowFile, SPEED_WARNING);

        } else if (searchHits <= 0) {
            session.transfer(flowFile, LOCATION_NOT_AVAILABLE);
        }
    }
}
