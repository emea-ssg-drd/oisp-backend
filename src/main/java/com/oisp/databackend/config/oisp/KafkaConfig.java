package com.oisp.databackend.config.oisp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaConfig {
    private String uri;
    private int partitions;
    private int replication;
    private int timeoutMs;
    private String topicsObservations;
    private String topicsRuleEngine;
    private String topicsHeartbeatName;
    private int topicsHeartbeatInterval;

    public String getUri() {
        return uri;
    }

    public int getPartitions() {
        return partitions;
    }

    public int getReplication() {
        return replication;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public String getTopicsObservations() {
        return topicsObservations;
    }

    public String getTopicsRuleEngine() {
        return topicsRuleEngine;
    }

    public String getTopicsHeartbeatName() {
        return topicsHeartbeatName;
    }

    public int getTopicsHeartbeatInterval() {
        return topicsHeartbeatInterval;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setTopicsObservations(String topicsObservations) {
        this.topicsObservations = topicsObservations;
    }

    public void setTopicsRuleEngine(String topicsRuleEngine) {
        this.topicsRuleEngine = topicsRuleEngine;
    }

    public void setTopicsHeartbeatName(String topicsHeartbeatName) {
        this.topicsHeartbeatName = topicsHeartbeatName;
    }

    public void setTopicsHeartbeatInterval(int topicsHeartbeatInterval) {
        this.topicsHeartbeatInterval = topicsHeartbeatInterval;
    }
}
