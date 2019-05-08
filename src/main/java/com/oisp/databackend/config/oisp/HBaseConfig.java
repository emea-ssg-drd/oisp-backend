package com.oisp.databackend.config.oisp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Properties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HBaseConfig {

    @JsonIgnore
    private Properties hadoopProperties;

    public void setHadoopProperties(Properties hadoopProperties) {
        this.hadoopProperties = hadoopProperties;
    }

    public Properties getHadoopProperties() {
        return hadoopProperties;
    }
}
