package com.energyict.protocolimplv2.coap.crest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

/**
 * Maps the given JSON data to a object notation.
 */
@XmlRootElement
public class CrestObject {

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
    private String cId;
    @JsonProperty("BAT")
    private String bat;
    @JsonProperty("CSQ")
    private String csq;
    @JsonProperty("TRY")
    private String tries;
    @JsonProperty("MSI")
    private String msi;
    @JsonProperty("URC")
    private List<String> urc;
    @JsonProperty("A")
    private List<Integer> a;
    @JsonProperty("V1M")
    private String v1m;
    @JsonProperty("V1")
    private List<Integer> v1;
    @JsonProperty("V2M")
    private String v2m;
    @JsonProperty("V2")
    private List<Integer> v2;
    @JsonProperty("MEM")
    private String mem;

    @JsonProperty("FMC")
    private String fmc;

    /**
     * Default no argument constructor.
     */
    public CrestObject() {
    }

    public static void main(String[] args) throws JsonProcessingException {
        String json = "{\"ID\":867787050007305,\"CON\":\"B\",\"FW\":12,\"TEL\":\"T-Mobile\",\"cID\":7106662,\"BAT\":3590,\"CSQ\":14,\"TRY\":1,\"MSI\":7,\"URC\":\"OKAY\",\"A\":[0,0],\"MEM\":0,\"V1M\":\"685656680801726998002177041407003000000C78799300210D7C084449202E747375630A20202020202020202020046D3A0AD125027C09656D6974202E7461626914041584FC0B0004957F000000004415000000000F100E1FCD16\",\"V1\":[785540,634456634],\"V2M\":\"6856566808027269980021770414079B3000000C78699800210D7C084449202E747375630A20202020202020202020046D3A0AD125027C09656D6974202E7461626D1404133005000004937F000000004413480100000F10021F4316\",\"V2\":[1328,634456634],\"FMC\":0}\n";
        String json2 = "{\"ID\":867787050007305,\"CON\":\"B\",\"FW\":12,\"TEL\":\"T-Mobile\",\"cID\":7106662,\"BAT\":3590,\"CSQ\":14,\"TRY\":1,\"MSI\":7,\"URC\":\"OKAY\",\"A\":[0,0],\"MEM\":0,\"V1M\":\"685656680801726998002177041407003000000C78799300210D7C084449202E747375630A20202020202020202020046D3A0AD125027C09656D6974202E7461626914041584FC0B0004957F000000004415000000000F100E1FCD16\",\"V1\":[785540,634456634],\"FMC\":0}\n";
        CrestObject object = new ObjectMapper().readValue(json2, CrestObject.class);
        System.out.println(object.getV2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, con, fw, tel, cId, bat, csq, tries, msi, urc, a, v1, v1m, fmc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrestObject crestObject = (CrestObject) o;
        return Objects.equals(this.id, crestObject.id) &&
                Objects.equals(this.con, crestObject.con) &&
                Objects.equals(this.fw, crestObject.fw) &&
                Objects.equals(this.tel, crestObject.tel) &&
                Objects.equals(this.cId, crestObject.cId) &&
                Objects.equals(this.bat, crestObject.bat) &&
                Objects.equals(this.csq, crestObject.csq) &&
                Objects.equals(this.tries, crestObject.tries) &&
                Objects.equals(this.msi, crestObject.msi) &&
                Objects.equals(this.urc, crestObject.urc) &&
                Objects.equals(this.a, crestObject.a) &&
                Objects.equals(this.v1, crestObject.v1) &&
                Objects.equals(this.v1m, crestObject.v1m) &&
                Objects.equals(this.v2, crestObject.v2) &&
                Objects.equals(this.v2m, crestObject.v2m) &&
                Objects.equals(this.fmc, crestObject.fmc);
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

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getBat() {
        return bat;
    }

    public void setBat(String bat) {
        this.bat = bat;
    }

    public String getCsq() {
        return csq;
    }

    public void setCsq(String csq) {
        this.csq = csq;
    }

    public String getTries() {
        return tries;
    }

    public void setTries(String tries) {
        this.tries = tries;
    }

    public String getMsi() {
        return msi;
    }

    public void setMsi(String msi) {
        this.msi = msi;
    }

    public List<String> getUrc() {
        return urc;
    }

    public void setUrc(List<String> urc) {
        this.urc = urc;
    }

    public List<Integer> getA() {
        return a;
    }

    public void setA(List<Integer> a) {
        this.a = a;
    }

    public String getV1M() {
        return v1m;
    }

    public void setV1M(String v1m) {
        this.v1m = v1m;
    }

    public List<Integer> getV1() {
        return v1;
    }

    public void setV1(List<Integer> v1) {
        this.v1 = v1;
    }

    public String getV2M() {
        return v2m;
    }

    public void setV2M(String v2m) {
        this.v2m = v2m;
    }

    public List<Integer> getV2() {
        return v2;
    }

    public void setV2(List<Integer> v2) {
        this.v2 = v2;
    }

    public String getFmc() {
        return fmc;
    }

    public void setFmc(String fmc) {
        this.fmc = fmc;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }
}

