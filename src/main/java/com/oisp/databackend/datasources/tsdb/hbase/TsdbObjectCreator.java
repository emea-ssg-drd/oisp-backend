package com.oisp.databackend.datasources.tsdb.hbase;

import com.oisp.databackend.datasources.tsdb.opentsdb.TsdbObject;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class TsdbObjectCreator {

    private TsdbObject observation;
    private static final Logger logger = LoggerFactory.getLogger(TsdbObjectCreator.class);
    private Result result;
    private Set<String> attributes;

    TsdbObjectCreator() {
    }


    public TsdbObjectCreator withAttributes(Set<String> attributes) {
        this.attributes = attributes;
        return this;
    }


    public TsdbObject create(Result result) {
        observation = new TsdbObject();
        this.result = result;
        addBasicInformation();
        addAdditionalInformation();
        logger.error("========================Observation");
        return observation;
    }

    private void addBasicInformation() {
        String key = Bytes.toString(result.getRow());
        String value = new String(result.getValue(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.DATA_COLUMN)));
        observation.setTimestamp(HbaseDataFormatter.getTimeFromKey(key)); //0L;
        observation.setValue(value);
        observation.setAllAttributes(new HashMap<String, String>());
        observation.setMetric(HbaseDataFormatter.getPrefixFromKey(key));
    }

    private void addAdditionalInformation() {
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

}
