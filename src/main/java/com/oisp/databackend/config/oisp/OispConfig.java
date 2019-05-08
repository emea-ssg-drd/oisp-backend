package com.oisp.databackend.config.oisp;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class OispConfig {
    public static final String OISP_BACKEND_TSDB_NAME_DUMMY = "dummy";
    public static final String OISP_BACKEND_TSDB_NAME_HBASE = "hbase";
    public static final String OISP_BACKEND_TSDB_NAME_OPENTSDB = "openTSDB";
    public static final String OISP_HBASE_TABLE_PREFIX = "oisp";
    public static final String OISP_BACKEND_TSDB_URI = "uri";
    public static final String OISP_BACKEND_TSDB_PORT = "port";

    private static final String OISP_BACKEND_CONFIG = "OISP_BACKEND_CONFIG";
    private static final String OISP_KAFKA_CONFIG = "OISP_KAFKA_CONFIG";
    private static final String OISP_ZOOKEEPER_CONFIG = "OISP_ZOOKEEPER_CONFIG";
    private static final String OISP_KERBEROS_CONFIG = "OISP_KERBEROS_CONFIG";
    private static final String OISP_HBASE_CONFIG = "OISP_HBASE_CONFIG";
    private static final String OISP_BACKEND_JAEGER_CONFIG = "OISP_BACKEND_JAEGER_CONFIG";

    private static final String OISP_LINK_PREFIX = "@@";
    private static final String OISP_PROPERTY_PREFIX = "%%";
    private static final String SET = "set";

    private static final Map<String, String> varClass = ImmutableMap.<String, String>builder()
        .put(OISP_BACKEND_CONFIG, "com.oisp.databackend.config.oisp.BackendConfig")
        .put(OISP_KAFKA_CONFIG, "com.oisp.databackend.config.oisp.KafkaConfig")
        .put(OISP_ZOOKEEPER_CONFIG, "com.oisp.databackend.config.oisp.ZookeeperConfig")
        .put(OISP_KERBEROS_CONFIG, "com.oisp.databackend.config.oisp.KerberosConfig")
        .put(OISP_HBASE_CONFIG, "com.oisp.databackend.config.oisp.HBaseConfig")
        .put(OISP_BACKEND_JAEGER_CONFIG, "com.oisp.databackend.config.oisp.JaegerConfig")
            .build();

    private BackendConfig backendConfig;
    private Map<String, Object> foundVars;
    private Map<String, Object> foundMaps;

    @PostConstruct
    public void init() throws ConfigEnvironmentException {

        foundVars = new Hashtable<String, Object>();
        foundMaps = new Hashtable<String, Object>();

        backendConfig = (BackendConfig) getObjectFromVar(OISP_BACKEND_CONFIG);

    }


    Object getObjectFromVar(String var) throws ConfigEnvironmentException {

        //Create generic object based on Env Variable
        if (foundVars.containsKey(var)) {
            return foundVars.get(var);
        }

        String rawConfig = System.getenv(var);
        if (rawConfig == null) {
            throw new ConfigEnvironmentException("Could not find environment variable " + var);
        }

        Class<?> classDef = null;
        try {
            classDef = Class.forName(varClass.get(var));
            //myObj = classDef.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigEnvironmentException("Could not instantiate class " + varClass.get(var), e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Object myObj = null;
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(rawConfig);
            myObj = objectMapper.readValue(rawConfig, classDef);
        } catch (IOException e) {
            throw new ConfigEnvironmentException("Could not parse content of " + var + ", =>" + rawConfig, e);
        }

        //search all Env Variabe links and Hashes, i.e. starting with @@ or %%
        insertLinkObjectOrHash(myObj, jsonNode, classDef);

        foundVars.put(var, myObj);
        return myObj;
    }

    void insertLinkObjectOrHash(Object myObj, JsonNode jsonNode, Class<?> classDef) throws ConfigEnvironmentException {
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext();) {
            String key = it.next();
            JsonNode node = jsonNode.get(key);
            if (node.isTextual() && node.asText().startsWith(OISP_LINK_PREFIX)) {
                Object linkObject = getObjectFromVar(node.asText().split(OISP_LINK_PREFIX)[1]);
                try {
                    String methodName = SET + key.substring(0, 1).toUpperCase() + key.substring(1);
                    Method method = classDef.getMethod(methodName, linkObject.getClass());
                    method.invoke(myObj, linkObject);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new ConfigEnvironmentException("Could not instantiate linked object " + node.asText(), e);
                }
            } else if (node.isTextual() && node.asText().startsWith(OISP_PROPERTY_PREFIX)) {
                Properties property = getPropertyMap(node.asText().split(OISP_PROPERTY_PREFIX)[1]);
                try {
                    String methodName = SET + key.substring(0, 1).toUpperCase() + key.substring(1);
                    Method method = classDef.getMethod(methodName, property.getClass());
                    method.invoke(myObj, property);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new ConfigEnvironmentException("Could not instantiate property map " + node.asText(), e);
                }
            }
        }
    }

    Properties getPropertyMap(String var) throws ConfigEnvironmentException {
        //Create generic PropertyMap based on Env Variable
        if (foundMaps.containsKey(var)) {
            return (Properties) foundMaps.get(var);
        }

        String rawConfig = System.getenv(var);
        if (rawConfig == null) {
            throw new ConfigEnvironmentException("Could not find environment variable for property " + var);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(rawConfig);
        } catch (IOException e) {
            throw new ConfigEnvironmentException("Could not parse content of property " + var + ">" + rawConfig, e);
        }

        //Loop through all fields and create property map
        Properties propertyMap = new Properties();
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext();) {
            String key = it.next();
            JsonNode node = jsonNode.get(key);
            propertyMap.put(key, node.asText());
        }
        return propertyMap;
    }

    public BackendConfig getBackendConfig() {
        return backendConfig;
    }

    public void setBackendConfig(BackendConfig backendConfig) {
        this.backendConfig = backendConfig;
    }

}
