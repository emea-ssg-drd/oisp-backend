package com.oisp.databackend.config.oisp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KerberosConfig {
    private String kdc;
    private String kpassword;
    private String krealm;
    private String kuser;

    public String getKdc() {
        return kdc;
    }

    public String getKpassword() {
        return kpassword;
    }

    public String getKrealm() {
        return krealm;
    }

    public String getKuser() {
        return kuser;
    }

    public void setKdc(String kdc) {
        this.kdc = kdc;
    }

    public void setKpassword(String kpassword) {
        this.kpassword = kpassword;
    }

    public void setKrealm(String krealm) {
        this.krealm = krealm;
    }

    public void setKuser(String kuser) {
        this.kuser = kuser;
    }
}
