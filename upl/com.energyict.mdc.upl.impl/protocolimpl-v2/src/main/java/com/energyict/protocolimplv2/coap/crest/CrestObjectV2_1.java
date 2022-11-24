/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

/**
 * Maps the given JSON data to a object notation.
 */
@XmlRootElement
public class CrestObjectV2_1 {

    @JsonProperty("ID")
    private String id;
    @JsonProperty("TS")
    private String ts;
    @JsonProperty("CON")
    private String con;
    @JsonProperty("FW")
    private String fw;
    @JsonProperty("TEL")
    private String tel;
    @JsonProperty("cID")
    private String cID;
    @JsonProperty("BAT")
    private Integer bat;
    @JsonProperty("CSQ")
    private String csq;
    @JsonProperty("TRY")
    private Byte tries;
    @JsonProperty("MSI")
    private Byte msi;
    @JsonProperty("URC")
    private List<String> urc;
    @JsonProperty("A")
    private List<Byte> a;
    @JsonProperty("MEM")
    private Integer mem;
    @JsonProperty("T1")
    private List<Integer> t1;
    @JsonProperty("H1")
    private List<Integer> h1;
    @JsonProperty("V1M")
    private String v1m;
    @JsonProperty("V1")
    private List<Integer> v1;
    @JsonProperty("V2M")
    private String v2m;
    @JsonProperty("V2")
    private List<Integer> v2;
    @JsonProperty("FMC")
    private Integer fmc;

    /**
     * Default no argument constructor.
     */
    public CrestObjectV2_1() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ts, con, fw, tel, cID, bat, csq, tries, msi, urc, a, mem, t1, h1, v1m, v1, v2m, v2, fmc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrestObjectV2_1 that = (CrestObjectV2_1) o;
        return Objects.equals(id, that.id) && Objects.equals(ts, that.ts) && Objects.equals(con, that.con) && Objects.equals(fw, that.fw) && Objects.equals(tel, that.tel) && Objects.equals(cID, that.cID) && Objects.equals(bat, that.bat) && Objects.equals(csq, that.csq) && Objects.equals(tries, that.tries) && Objects.equals(msi, that.msi) && Objects.equals(urc, that.urc) && Objects.equals(a, that.a) && Objects.equals(mem, that.mem) && Objects.equals(t1, that.t1) && Objects.equals(h1, that.h1) && Objects.equals(v1m, that.v1m) && Objects.equals(v1, that.v1) && Objects.equals(v2m, that.v2m) && Objects.equals(v2, that.v2) && Objects.equals(fmc, that.fmc);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCon() {
        return con;
    }

    public void setCon(String con) {
        this.con = con;
    }

    public String getFw() {
        return fw;
    }

    public void setFw(String fw) {
        this.fw = fw;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getCID() {
        return cID;
    }

    public void setCID(String cID) {
        this.cID = cID;
    }

    public Integer getBat() {
        return bat;
    }

    public void setBat(Integer bat) {
        this.bat = bat;
    }

    public String getCsq() {
        return csq;
    }

    public void setCsq(String csq) {
        this.csq = csq;
    }

    public Byte getTries() {
        return tries;
    }

    public void setTries(Byte tries) {
        this.tries = tries;
    }

    public Byte getMsi() {
        return msi;
    }

    public void setMsi(Byte msi) {
        this.msi = msi;
    }

    public List<String> getUrc() {
        return urc;
    }

    public void setUrc(List<String> urc) {
        this.urc = urc;
    }

    public List<Byte> getA() {
        return a;
    }

    public void setA(List<Byte> a) {
        this.a = a;
    }

    public Integer getMem() {
        return mem;
    }

    public void setMem(Integer mem) {
        this.mem = mem;
    }

    public List<Integer> getT1() {
        return t1;
    }

    public void setT1(List<Integer> t1) {
        this.t1 = t1;
    }

    public List<Integer> getH1() {
        return h1;
    }

    public void setH1(List<Integer> h1) {
        this.h1 = h1;
    }

    public String getV1m() {
        return v1m;
    }

    public void setV1m(String v1m) {
        this.v1m = v1m;
    }

    public List<Integer> getV1() {
        return v1;
    }

    public void setV1(List<Integer> v1) {
        this.v1 = v1;
    }

    public String getV2m() {
        return v2m;
    }

    public void setV2m(String v2m) {
        this.v2m = v2m;
    }

    public List<Integer> getV2() {
        return v2;
    }

    public void setV2(List<Integer> v2) {
        this.v2 = v2;
    }

    public Integer getFmc() {
        return fmc;
    }

    public void setFmc(Integer fmc) {
        this.fmc = fmc;
    }
}

