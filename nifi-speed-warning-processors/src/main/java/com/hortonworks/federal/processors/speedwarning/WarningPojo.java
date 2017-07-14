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

public class WarningPojo {

    private int eventOverSpeed;
    private long timestamp;
    private String vehicleId;
    private int eventSpeedLimit;

    public WarningPojo(int eventOverSpeed, long timestamp, String vehicleId, int eventSpeedLimit) {
        this.eventOverSpeed = eventOverSpeed;
        this.timestamp = timestamp;
        this.vehicleId = vehicleId;
        this.eventSpeedLimit = eventSpeedLimit;
    }

    public int getEventOverSpeed() {
        return eventOverSpeed;
    }

    public void setEventOverSpeed(int eventOverSpeed) {
        this.eventOverSpeed = eventOverSpeed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getEventSpeedLimit() {
        return eventSpeedLimit;
    }

    public void setEventSpeedLimit(int eventSpeedLimit) {
        this.eventSpeedLimit = eventSpeedLimit;
    }
}
