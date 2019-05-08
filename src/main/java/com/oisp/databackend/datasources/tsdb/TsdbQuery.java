package com.oisp.databackend.datasources.tsdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TsdbQuery {
    private String aid;
    private String cid;
    private String componentType;
    private List<String>  attributes;
    private boolean locationInfo;
    private long start;
    private long stop;

    public TsdbQuery(String aid, String cid, List<String> attributes, boolean locationInfo, long start, long stop) {
        this.aid = aid;
        this.cid = cid;
        this.componentType = null;
        this.attributes = attributes;
        this.locationInfo = locationInfo;
        this.start = start;
        this.stop = stop;
    }

    public TsdbQuery() {
        this.locationInfo = false;
        this.componentType = null;
        this.attributes = new ArrayList<>();
    }

    public TsdbQuery withAid(String aid) {
        this.aid = aid;
        return this;
    }

    public TsdbQuery withCid(String cid) {
        this.cid = cid;
        return this;
    }

    public TsdbQuery withComponentType(String componentType) {
        this.componentType = componentType;
        return this;
    }

    public TsdbQuery withAttributes(List<String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public TsdbQuery withAttributes(String[] attributes) {
        if (attributes == null) {
            this.attributes = new ArrayList<>();
            return this;
        }
        this.attributes = new ArrayList<String>(Arrays.asList(attributes));
        return this;
    }

    public TsdbQuery withLocationInfo(boolean locationInfo) {
        this.locationInfo = locationInfo;
        return this;
    }

    public TsdbQuery withStart(long start) {
        this.start = start;
        return this;
    }

    public TsdbQuery withStop(long stop) {
        this.stop = stop;
        return this;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public void setLocationInfo(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    public String getAid() {

        return aid;
    }

    public String getCid() {
        return cid;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public boolean isLocationInfo() {
        return locationInfo;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }
}
