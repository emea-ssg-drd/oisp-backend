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

package com.oisp.databackend.datasources.tsdb.hbase;

import com.oisp.databackend.config.oisp.TsdbHBaseCondition;
import com.oisp.databackend.datasources.tsdb.TsdbQuery;
import com.oisp.databackend.datastructures.Observation;
import com.oisp.databackend.datasources.tsdb.TsdbAccess;
import com.oisp.databackend.datasources.DataFormatter;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Repository
@Conditional(TsdbHBaseCondition.class)
public class TsdbAccessHBase implements TsdbAccess {
    private static final Logger logger = LoggerFactory.getLogger(TsdbAccessHBase.class);
    private final String tableName;
    private final byte[] tableNameBytes;
    private static final String DEVICE_MEASUREMENT = "_DEVICE_MEASUREMENT";
    private static final String SEPARATOR = ".";

    private Connection connection;
    @Autowired
    private HbaseConnManger hbaseConnManger;

    private Connection getHbaseConnection() throws IOException {
        try {
            if (connection == null || connection.isClosed()) {
                logger.info("Creating connection");
                connection = hbaseConnManger.create();
            }
            return connection;
        } catch (LoginException e) {
            logger.error("Unable to login into hbase", e);
            throw new IOException(e);
        }
    }

    private Table getHbaseTable() throws IOException {
        return getHbaseConnection().getTable(TableName.valueOf(tableNameBytes));
    }

    @Autowired
    public TsdbAccessHBase(@Value("local") String hbasePrefix) {
        logger.info("Creating HBase. Zookeeper: ");

        this.tableName = hbasePrefix.toUpperCase() + DEVICE_MEASUREMENT;
        this.tableNameBytes = Bytes.toBytes(tableName);
    }

    @PostConstruct
    public boolean createTables() throws IOException {
        Admin admin = null;
        logger.info("Try to create {} in HBase.", tableName);
        try {
            admin = getHbaseConnection().getAdmin();
            TableManager tableManager = new TableManager(admin, TableName.valueOf(tableNameBytes));
            return tableManager.createTables();
        } catch (IOException e) {
            logger.warn("Initialization of HBase table failed.", e);
            return false;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    @PreDestroy
    public void closeHbaseConnection() throws IOException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public boolean put(List<Observation> observations) {

        try (Table table = getHbaseTable()) {

            List<Put> puts = new ArrayList<Put>();
            for (Observation obs : observations) {
                puts.add(getPutForObservation(obs));
            }
            table.put(puts);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean put(Observation observation) {

        List<Observation> puts = new ArrayList<Observation>();
        puts.add(observation);
        return put(puts);
    }


    byte[] getRowKey(Observation observation) {
        return Bytes.toBytes(observation.getAid() + SEPARATOR + observation.getCid() + SEPARATOR + DataFormatter.zeroPrefixedTimestamp(observation.getOn()));
    }

    byte[] getRowPrefix(TsdbQuery tsdbQuery) {
        return Bytes.toBytes(tsdbQuery.getAid() + SEPARATOR + tsdbQuery.getCid());
    }

    private Put getPutForObservation(Observation observation) {
        Put put = new Put(getRowKey(observation));
        if (observation.isBinary()) {
            put.addColumn(Columns.BYTES_COLUMN_FAMILY, Columns.BYTES_DATA_COLUMN, observation.getbValue());
        } else {
            put.addColumn(Columns.BYTES_COLUMN_FAMILY, Columns.BYTES_DATA_COLUMN, Bytes.toBytes((String) observation.getValue()));
        }
        Map<String, String> attributes = observation.getAttributes();
        // In hbase, we treat gps coordinates as special columns
        // like the attributes. So we add all gps coordinates to attributes
        if (observation.getLoc() != null) {
            if (attributes == null) { // no attributes?, but we need the structure for location data as well
                attributes = new HashMap<String, String>();
                observation.setAttributes(attributes);
            }
            for (int i = 0; i < observation.getLoc().size() && i < ObservationCreator.GPS_COLUMN_SIZE; i++) {
                String gpsAttributeName = DataFormatter.gpsValueToString(i);
                if (observation.getLoc().get(i) != null) {
                    observation.getAttributes().put(gpsAttributeName, observation.getLoc().get(i).toString());
                }
            }
        }
        if (attributes != null) {
            for (String k : attributes.keySet()) {
                put.addColumn(Columns.BYTES_COLUMN_FAMILY, Bytes.toBytes(Columns.ATTRIBUTE_COLUMN_PREFIX + k),
                        Bytes.toBytes(attributes.get(k)));
            }
        }
        return put;
    }

    private void addLocationColumns(List<String> attributeList) {
        attributeList.add(DataFormatter.gpsValueToString(0));
        attributeList.add(DataFormatter.gpsValueToString(1));
        attributeList.add(DataFormatter.gpsValueToString(2));
    }

    @Override
    public Observation[] scan(TsdbQuery tsdbQuery) {
        logger.debug("Scanning HBase: aid: {} cid: {} start: {} stop: {}", tsdbQuery.getAid(), tsdbQuery.getCid(), tsdbQuery.getStart(), tsdbQuery.getStop());
        if (tsdbQuery.isLocationInfo()) {
            addLocationColumns(tsdbQuery.getAttributes());
        }
        Scan scan = new HbaseScanManager(
                new String(getRowPrefix(tsdbQuery)))
                .create(tsdbQuery.getStart(), tsdbQuery.getStop())
                .askForData(tsdbQuery.getAttributes().stream().collect(Collectors.toSet()))
                .getScan();
        return getObservations(tsdbQuery, scan);
    }


    public Observation[] scan(TsdbQuery tsdbQuery, boolean forward, int limit) {
        logger.debug("Scanning HBase: aid: {} cid: {} start: {} stop: {} with limit: {}",
                tsdbQuery.getAid(), tsdbQuery.getCid(), tsdbQuery.getStart(), tsdbQuery.getStop(), limit);
        HbaseScanManager scanManager = new HbaseScanManager(new String(getRowPrefix(tsdbQuery)));
        Set<String> attributesSet = tsdbQuery.getAttributes().stream().collect(Collectors.toSet());
        if (forward) {
            scanManager.create(tsdbQuery.getStart(), tsdbQuery.getStop());
        } else {
            scanManager.create(tsdbQuery.getStop(), tsdbQuery.getStart()).setReversed();
        }
        scanManager.askForData(attributesSet);

        logger.debug("Scanning with limit: {}", limit);
        Scan scan = scanManager.setCaching(limit)
                .setFilter(new PageFilter(limit))
                .getScan();
        return getObservations(tsdbQuery, scan);
    }


    private Observation[] getObservations(TsdbQuery tsdbQuery, Scan scan) {
        try (Table table = getHbaseTable(); ResultScanner scanner = table.getScanner(scan)) {
            List<Observation> observations = new ArrayList<>();
            for (Result result : scanner) {
                boolean gps = tsdbQuery.isLocationInfo();
                Observation observation = new ObservationCreator(tsdbQuery.getAid(), tsdbQuery.getCid())
                        .withAttributes(tsdbQuery.getAttributes().stream().collect(Collectors.toSet()))
                        .withGps(gps)
                        .withComponentType(tsdbQuery.getComponentType())
                        .create(result);
                observations.add(observation);
            }
            return observations.toArray(new Observation[observations.size()]);
        } catch (IOException ex) {
            logger.error("Unable to find observation in hbase", ex);
            return null;
        }
    }

    public String[] scanForAttributeNames(TsdbQuery tsdbQuery) throws IOException {

        logger.debug("Scanning HBase: accountId: {} componentId: {} start: {} stop: {}", tsdbQuery.getAid(), tsdbQuery.getCid(), tsdbQuery.getStart(), tsdbQuery.getStop());

        Scan scan = new HbaseScanManager(
                DataFormatter.createMetric(tsdbQuery.getAid(), tsdbQuery.getCid()))
                .create(tsdbQuery.getStart(), tsdbQuery.getStop())
                .setFilter(new ColumnPrefixFilter(Columns.BYTES_ATTRIBUTE_COLUMN_PREFIX))
                .getScan();

        return retrieveAttributeNames(scan)
                .stream()
                .filter((String s) ->
                        !s.equals(DataFormatter.gpsValueToString(0))
                        && !s.equals(DataFormatter.gpsValueToString(1))
                        && !s.equals(DataFormatter.gpsValueToString(2)))
                .toArray(String[]::new);
    }

    private Set<String> retrieveAttributeNames(Scan scan) throws IOException {
        Set<String> attributes = new HashSet<>();
        try (Table table = getHbaseTable(); ResultScanner scanner = table.getScanner(scan)) {
            for (Result result = scanner.next(); result != null; result = scanner.next()) {
                List<Cell> cells = result.listCells();
                for (Cell cell : cells) {
                    String attrName = HbaseDataFormatter.getAttrNameFromCell(cell);
                    attributes.add(attrName);
                }
            }
        }
        return attributes;
    }

    @Override
    public List<String> getSupportedDataTypes() {
        return Arrays.asList("Number", "String", "Boolean", "ByteArray");
    }
}
