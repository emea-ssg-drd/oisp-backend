package com.oisp.databackend.config.oisp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JaegerConfig {
    private String serviceName;
    private String agentHost;
    private int agentPort;
    private boolean logSpans;
    private String samplerType;
    private Number samplerParam;
    private boolean tracing;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAgentHost() {
        return agentHost;
    }

    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(int agentPort) {
        this.agentPort = agentPort;
    }

    public boolean isLogSpans() {
        return logSpans;
    }

    public void setLogSpans(boolean logSpans) {
        this.logSpans = logSpans;
    }

    public String getSamplerType() {
        return samplerType;
    }

    public void setSamplerType(String samplerType) {
        this.samplerType = samplerType;
    }

    public Number getSamplerParam() {
        return samplerParam;
    }

    public void setSamplerParam(Number samplerParam) {
        this.samplerParam = samplerParam;
    }

    public boolean isTracing() {
        return tracing;
    }

    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }
}
