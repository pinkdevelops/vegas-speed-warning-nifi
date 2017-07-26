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

import org.apache.http.HttpHost;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVegasProcessor extends AbstractProcessor {

    static final String HOST_DEFAULT = "localhost";
    static final String PORT_DEFAULT = "9200";

    protected static final PropertyDescriptor HOST = new PropertyDescriptor.Builder()
            .name("Host")
            .description("ElasticSearch Host - defaults to localhost")
            .required(true)
            .defaultValue(HOST_DEFAULT)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    protected static final PropertyDescriptor PORT = new PropertyDescriptor.Builder()
            .name("Port")
            .description("Transport Port")
            .required(true)
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .defaultValue(PORT_DEFAULT)
            .build();
    protected static final PropertyDescriptor INDEX_NAME = new PropertyDescriptor.Builder()
            .name("ES Index")
            .description("The name of the index to use")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    private List<PropertyDescriptor> descriptors;

    static {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(INDEX_NAME);
        descriptors.add(PORT);
        descriptors.add(HOST);
    }

    protected RestClient restClient;

    @OnScheduled
    protected final RestClient createClient(ProcessContext context) {
        if (restClient != null) {
            closeClient();
        }

        final String host = context.getProperty(HOST).getValue();
        final int port = Integer.parseInt(context.getProperty(PORT).getValue());
        getLogger().info("Creating ES REST Client at: " + host + ":" + port);


        return restClient = RestClient.builder(
                new HttpHost(host, port, "http")).build();

    }

    @OnStopped
    public final void closeClient() {
        if (restClient != null) {
            getLogger().info("Closing ES Rest Client");
            try {
                restClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            restClient = null;
        }
    }
}
