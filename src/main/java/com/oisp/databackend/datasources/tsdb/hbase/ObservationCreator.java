package com.oisp.databackend.datasources.tsdb.hbase;

import com.oisp.databackend.datasources.DataFormatter;
import com.oisp.databackend.datastructures.Observation;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ObservationCreator {

    public static final short GPS_COLUMN_SIZE = 3;
    private Observation observation;
    private Result result;
    private static final Logger logger = LoggerFactory.getLogger(ObservationCreator.class);
    private final String accountId;
    private final String componentId;
    private String componentType;
    private Boolean hasGps;
    private Set<String> attributes;

    public ObservationCreator(String accountId, String componentId) {
        this.accountId = accountId;
        this.componentId = componentId;
        this.componentType = null;
        this.hasGps = false;
    }

    public ObservationCreator withGps(boolean hasGps) {
        this.hasGps = hasGps;
        return this;
    }

    public ObservationCreator withComponentType(String componentType) {
        this.componentType = componentType;
        return this;
    }

    public ObservationCreator withAttributes(Set<String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Observation create(Result result) {
        observation = new Observation();
        this.result = result;
        addBasicInformation();
        addAdditionalInformation();
        return observation;
    }

    private void addBasicInformation() {
        String key = Bytes.toString(result.getRow());
        if ("ByteArray".equals(componentType)) {
            byte[] bValue = result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.DATA_COLUMN));
            observation.setbValue(bValue);
        } else {
            String value = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.DATA_COLUMN)));
            observation.setValue(value);
        }
        observation.setCid(componentId);
        observation.setAid(accountId);
        observation.setOn(HbaseDataFormatter.getTimeFromKey(key)); //0L;
        observation.setAttributes(new HashMap<String, String>());
        observation.setDataType(componentType);
    }

    private void addAdditionalInformation() {

        if (hasGps) {
            addLocationData();
        }
        if (attributes != null) {
            addAttributesData(attributes);
        }

    }

    private void addAttributesData(Set<String> attributes) {
        for (String a : attributes) {
            String attribute = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY,
                    Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + a)));
            observation.getAttributes().put(a, attribute);
        }
    }

    private void addLocationData() {
        try {
            String[] coordinate = new String[GPS_COLUMN_SIZE];
            observation.setLoc(new ArrayList<Double>());
            for (int i = 0; i < GPS_COLUMN_SIZE; i++) {
                coordinate[i] = Bytes.toString(result.getValue(Columns.BYTES_COLUMN_FAMILY,
                        Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + DataFormatter.gpsValueToString(i))));
                if (coordinate[i] != null) {
                    observation.getLoc().add(Double.parseDouble(coordinate[i]));
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("problem with parsing GPS coords... not a Double?");
        }
    }
}
