package com.oisp.databackend.datasources.tsdb.opentsdb;

import com.oisp.databackend.datasources.DataFormatter;
import com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi.QueryResponse;
import com.oisp.databackend.datastructures.Observation;

import java.util.*;

public final class ObservationBuilder {

    private ObservationBuilder() {

    }

    private static void addTagsFromQuery(SortedMap<Long, Observation> observationsMap, QueryResponse[] queryResponses) {
        for (QueryResponse queryResponse: queryResponses) {
            Map<String, String> types = queryResponse.getTags();
            Map<Long, String> dps = queryResponse.getDps();
            for (Map.Entry<String, String> type : types.entrySet()) {
                String tagK = type.getKey();
                if (tagK.equals(TsdbObjectBuilder.TYPE)) {
                    continue;
                }
                String tagV = type.getValue();

                for (Map.Entry<Long, String> entry : dps.entrySet()) {
                    Long timestamp = entry.getKey();
                    if (observationsMap.get(timestamp) != null) {
                        observationsMap.get(timestamp).getAttributes().put(tagK, tagV);
                    }
                }
            }
        }
    }

    private static void addGpsFromQuery(SortedMap<Long, Observation> observationsMap, QueryResponse[] queryResponses) {
        for (QueryResponse queryResponse : queryResponses) {

            String type = queryResponse.getTags().get(TsdbObjectBuilder.TYPE);
            if (!type.equals(DataFormatter.gpsValueToString(0))
                    && !type.equals(DataFormatter.gpsValueToString(1))
                    && !type.equals(DataFormatter.gpsValueToString(2))) {
                continue;
            }
            Map<Long, String> dps = queryResponse.getDps();
            for (Map.Entry<Long, String> entry : dps.entrySet()) {
                Long timestamp = entry.getKey();
                String value = entry.getValue();
                if (observationsMap.get(timestamp) != null) {
                    //if (observationsMap.get(timestamp).getAttributes() != null)
                    observationsMap.get(timestamp).getAttributes().put(type, value);
                    //}
                }
            }
        }
    }
    private static SortedMap<Long, Observation> createBaseObservations(QueryResponse[] queryResponses) {

        SortedMap<Long, Observation> observationMap = new TreeMap<>();

        for (QueryResponse queryResponse: queryResponses) {
            if (!queryResponse.getTags().get(TsdbObjectBuilder.TYPE).equals(TsdbObjectBuilder.VALUE)) {
                continue;
            }
            String metric = queryResponse.getMetric();
            Map<Long, String> dps = queryResponse.getDps();
            for (Map.Entry<Long, String> entry:  dps.entrySet()) {
                Long timestamp = entry.getKey();
                String value = entry.getValue();
                Observation observation = new Observation(DataFormatter.getAccountFromMetric(metric),
                        DataFormatter.getCidFromMetric(metric),
                        timestamp, value, new ArrayList<Double>(), new HashMap<String, String>());
                observationMap.put(timestamp, observation);
            }
        }
        return observationMap;
    }

    public static Observation[] createObservationFromQueryResponses(QueryResponse[] queryResponses) {

        //first create the base objets with the value types
        SortedMap<Long, Observation> observationMap
                = createBaseObservations(queryResponses);

        //Now add the gps tags if any
        addGpsFromQuery(observationMap, queryResponses);
        convertGpsAttributesToLoc(observationMap);

        //Now add the other tags
        addTagsFromQuery(observationMap, queryResponses);

        // collect finally objects in an array
        return observationMap.values().toArray(new Observation[0]);
    }

    private static void convertGpsAttributesToLoc(SortedMap<Long, Observation> observationMap) {
        for (Observation observation: observationMap.values()) {
            Map<String, String> attributes = observation.getAttributes();
            if (attributes.isEmpty()) {
                continue;
            }
            String[] coord = {
                    attributes.get(DataFormatter.gpsValueToString(0)),
                    attributes.get(DataFormatter.gpsValueToString(1)),
                    attributes.get(DataFormatter.gpsValueToString(2))
            };
            List<Double> loc = new ArrayList<>();
            for (int i = 0; i < DataFormatter.GPS_COLUMN_SIZE;  i++) {
                if (coord[i] != null) {
                    loc.add(Double.parseDouble(coord[i]));
                }
            }
            observation.setLoc(loc);
        }
    }

    public static boolean checkforTagMap(Map<String, String> attributes) {
        boolean keepTagMapEmpty = false;

        if (!attributes.isEmpty()) {
            for (String attribute : attributes.keySet()) {
                if (attribute != DataFormatter.gpsValueToString(0)
                        && attribute != DataFormatter.gpsValueToString(1)
                        && attribute != DataFormatter.gpsValueToString(2)) {
                    keepTagMapEmpty = true;
                    break;
                }
            }
        }

        return keepTagMapEmpty;
    }
}
