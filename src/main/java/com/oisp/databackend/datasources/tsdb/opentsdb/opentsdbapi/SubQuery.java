package com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi;

import java.util.HashMap;
import java.util.Map;

public class SubQuery {

    public static final String AGGREGATOR_MAX = "max";
    public static final String AGGREGATOR_MIN = "min";
    public static final String AGGREGATOR_NONE = "none";

    private String aggregator;
    private String metric;
    private Map<String, String> tags;


    public SubQuery() {
        tags = new HashMap<String, String>();
    }

    public SubQuery withMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public SubQuery withAggregator(String aggregator) {
        this.aggregator = aggregator;
        return this;
    }

    public SubQuery withTag(String tagK, String tagV) {
        this.tags.put(tagK, tagV);
        return this;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getAggregator() {
        return aggregator;
    }

    public String getMetric() {
        return metric;
    }
}
