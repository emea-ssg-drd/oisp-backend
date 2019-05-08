package com.oisp.databackend.datasources.tsdb.opentsdb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Main abstraction for TSDB objects
 *
 */
public class TsdbObject implements Serializable {
    private String metric;
    private String value;
    private long timestamp;
    private Map<String, String> attributes;

    public TsdbObject(String metric, String value, long timestamp, Map<String, String> attributes) {
        this.metric = metric;
        this.value  = value;
        this.timestamp = timestamp;
        this.attributes = attributes;
    }

    public TsdbObject(String metric, String value, long timestamp) {
        this.metric = metric;
        this.value  = value;
        this.timestamp = timestamp;
        this.attributes = new HashMap<String, String>();
    }

    public TsdbObject() {
        attributes = new HashMap<String, String>();
    }

    public TsdbObject(TsdbObject o) {
        this.metric = o.getMetric();
        this.value = o.getValue();
        this.timestamp = o.getTimestamp();
        this.attributes = new HashMap<String, String>();
    }

    public TsdbObject withMetric(String metric) {
        this.setMetric(metric);
        return this;
    }

    public TsdbObject withValue(String value) {
        this.value = value;
        return this;
    }

    public TsdbObject withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAttribute(String attributeK, String attributeV) {
        attributes.put(attributeK, attributeV);
    }

    public void setAllAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getMetric() {
        return metric;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
