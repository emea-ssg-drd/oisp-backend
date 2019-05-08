/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oisp.databackend.datasources;


import com.oisp.databackend.config.oisp.OispConfig;
import com.oisp.databackend.datasources.tsdb.TsdbQuery;
import com.oisp.databackend.datastructures.Observation;
import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import com.oisp.databackend.datasources.tsdb.TsdbAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

@Repository
@Configuration
public class DataDaoImpl implements DataDao {

    private static final Logger logger = LoggerFactory.getLogger(DataDaoImpl.class);

    private TsdbAccess tsdbAccess;

    @Autowired
    private OispConfig oispConfig;

    @Autowired
    private ApplicationContext context;

    @Autowired
    public void selectDAOPlugin() throws ConfigEnvironmentException {
        String tsdbName = oispConfig.getBackendConfig().getTsdbName();
        if (oispConfig.OISP_BACKEND_TSDB_NAME_DUMMY.equals(tsdbName)) {
            logger.info("TSDB backend: dummy");
            this.tsdbAccess = (TsdbAccess) context.getBean("tsdbAccessDummy");
        } else if (oispConfig.OISP_BACKEND_TSDB_NAME_HBASE.equals(tsdbName)) {
            logger.info("TSDB backend: hbase");
            this.tsdbAccess = (TsdbAccess) context.getBean("tsdbAccessHBase");
        } else if (oispConfig.OISP_BACKEND_TSDB_NAME_OPENTSDB.equals(tsdbName)) {
            logger.info("TSDB backend: openTSDB");
            this.tsdbAccess = (TsdbAccess) context.getBean("tsdbAccessOpenTsdb");
        } else {
            throw new ConfigEnvironmentException("Could not find the backend with name " + tsdbName);
        }
    }

    @Override
    public boolean put(final Observation[] observations) {

        tsdbAccess.put(Arrays.asList(observations));
        return true;
    }

    @Override
    public Observation[] scan(String accountId, String componentId, String componentType, long start, long stop, Boolean gps, String[] attributeList) {
        logger.debug("Scanning TSDB: acc: {} cid: {} start: {} stop: {} gps: {}", accountId, componentId, start, stop, gps);
        TsdbQuery tsdbQuery = new TsdbQuery()
                .withAid(accountId)
                .withCid(componentId)
                .withComponentType(componentType)
                .withLocationInfo(gps)
                .withAttributes(attributeList)
                .withStart(start)
                .withStop(stop);
        return tsdbAccess.scan(tsdbQuery);
    }

    @Override
    public Observation[] scan(String accountId, String componentId, String componentType, long start, long stop, Boolean gps,
                              String[] attributeList, boolean forward, int limit) {
        logger.debug("Scanning TSDB: acc: {} cid: {} start: {} stop: {} gps: {} with limit: {}",
                accountId, componentId, start, stop, gps, limit);
        TsdbQuery tsdbQuery = new TsdbQuery()
                .withAid(accountId)
                .withCid(componentId)
                .withComponentType(componentType)
                .withLocationInfo(gps)
                .withAttributes(attributeList)
                .withStart(start)
                .withStop(stop);
        Observation[] observations = tsdbAccess.scan(tsdbQuery, forward, limit);
        //addLocToObservations(observations, gps);
        return observations;
    }

    @Override
    public String[] scanForAttributeNames(String accountId, String componentId, long start, long stop) throws IOException {

        logger.debug("Scanning TSD: acc: {} cid: {} start: {} stop: {}", accountId, componentId, start, stop);
        TsdbQuery tsdbQuery = new TsdbQuery()
                .withAid(accountId)
                .withCid(componentId)
                .withStart(start)
                .withStop(stop);
        String[] attributesArray = tsdbAccess.scanForAttributeNames(tsdbQuery);

        //Remove locX, locY, locZ attributes as these will be processed only when requested by location flag
        /*String[] filteredAttr = Arrays.stream(attributesArray).filter((String s) ->
                !s.equals(DataFormatter.gpsValueToString(0))
                        && !s.equals(DataFormatter.gpsValueToString(1))
                        && !s.equals(DataFormatter.gpsValueToString(2))).toArray(String[]::new);*/
        return attributesArray;
    }

    @Override
    public List<String> getSupportedDataTypes() {
        return tsdbAccess.getSupportedDataTypes();
    }
}
