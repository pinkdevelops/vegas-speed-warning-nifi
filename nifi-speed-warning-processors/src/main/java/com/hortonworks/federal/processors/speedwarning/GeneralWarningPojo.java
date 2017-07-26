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

import java.util.HashMap;

public class GeneralWarningPojo {

    private String eventId;
    private long timestamp;
    private String[] vehicleIds;
    private HashMap<String, String> optional = new HashMap<String, String>();

    public GeneralWarningPojo(String eventId, long timestamp, String[] vehicleIds, HashMap<String, String> optional) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.vehicleIds = vehicleIds;
        this.optional = optional;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String[] getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(String[] vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    public HashMap<String, String> getOptional() {
        return optional;
    }

    public void setOptional(HashMap<String, String> optional) {
        this.optional = optional;
    }
}
