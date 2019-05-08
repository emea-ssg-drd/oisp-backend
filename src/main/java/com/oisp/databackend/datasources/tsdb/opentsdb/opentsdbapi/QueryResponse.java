package com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse {

    private String metric;
    private Map<String, String> tags;
    private List<String> aggregateTags;
    private Map<Long, String> dps;

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public void setAggregateTags(List<String> aggregateTags) {
        this.aggregateTags = aggregateTags;
    }

    public void setDps(Map<Long, String> dps) {
        this.dps = dps;
    }

    public String getMetric() {
        return metric;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public List<String> getAggregateTags() {
        return aggregateTags;
    }

    public Map<Long, String> getDps() {
        return dps;
    }


}
