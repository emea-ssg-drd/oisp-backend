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

package com.oisp.databackend.datasources.tsdb.opentsdb;

import com.oisp.databackend.config.oisp.OispConfig;
import com.oisp.databackend.datasources.DataFormatter;
import com.oisp.databackend.datasources.tsdb.TsdbAccess;
import com.oisp.databackend.datasources.tsdb.TsdbQuery;
import com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi.Query;
import com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi.QueryResponse;
import com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi.RestApi;
import com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi.SubQuery;

import com.oisp.databackend.datastructures.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Repository
public class TsdbAccessOpenTsdb implements TsdbAccess {

    private static final String OR = "|";
    private RestApi api;
    @Autowired
    private OispConfig oispConfig;


    @PostConstruct
    public void init() throws URISyntaxException {
        //Make sure that this bean is only initiated when needed
        if (!oispConfig.getBackendConfig()
                .getTsdbName()
                .equals(oispConfig.OISP_BACKEND_TSDB_NAME_OPENTSDB)) {
            return;
        }
        api = new RestApi(oispConfig);
    }

    @Override
    public boolean put(List<Observation> observations) {
        List<TsdbObject> tsdbObjects = TsdbObjectBuilder.createTsdbObjectsFromObservations(observations);

        return api.put(tsdbObjects, true);
    }

    @Override
    public boolean put(Observation observation) {

        List<Observation> list = new ArrayList<Observation>();
        list.add(observation);

        return put(list);
    }

    @Override
    public Observation[] scan(TsdbQuery tsdbQuery) {
        SubQuery subQuery = new SubQuery()
                .withAggregator(SubQuery.AGGREGATOR_NONE)
                .withMetric(DataFormatter.createMetric(tsdbQuery.getAid(),
                        tsdbQuery.getCid()));

        // If other than type tag/attrbiute is requested we have to go with empty tag/attribute list (there is not "or" between tags in
        // openTSDB?)
        if (!tsdbQuery.getAttributes().isEmpty()) {
            StringBuffer tag = new StringBuffer(TsdbObjectBuilder.VALUE);
            if (tsdbQuery.isLocationInfo()) {
                tag.append(
                        OR + DataFormatter.gpsValueToString(0)
                        + OR + DataFormatter.gpsValueToString(1)
                        + OR + DataFormatter.gpsValueToString(2)
                );
            }
            subQuery.withTag(TsdbObjectBuilder.TYPE, tag.toString());
        }
        Query query = new Query().withStart(tsdbQuery.getStart()).withEnd(tsdbQuery.getStop());
        query.addQuery(subQuery);

        QueryResponse[] queryResponses = api.query(query);
        if (queryResponses == null) {
            return null;
        }
        return ObservationBuilder.createObservationFromQueryResponses(queryResponses);
    }



    @Override
    public Observation[] scan(TsdbQuery tsdbQuery, boolean forward, int limit) {
        return new Observation[0];
    }


    public String[] scanForAttributeNames(TsdbQuery tsdbQuery) throws IOException {
        return new String[]{"*"};
    }

    @Override
    public List<String> getSupportedDataTypes() {
        return Arrays.asList("Number", "Boolean");
    }
}
