package com.elster.us.protocolimplv2.sel.profiles;

import com.elster.us.protocolimplv2.sel.profiles.structure.LDPError;
import com.elster.us.protocolimplv2.sel.profiles.structure.LPData;
import com.elster.us.protocolimplv2.sel.profiles.structure.MeterConfiguration;
import com.elster.us.protocolimplv2.sel.profiles.structure.MeterStatus;
import com.elster.us.protocolimplv2.sel.profiles.structure.SERData;

import java.util.List;


public class LDPData {

  private MeterConfiguration meterConfig;
  private Long[] presentValues;
  private MeterStatus meterStatus;
  private List<LPData> lpData;
  private List<SERData> serData;
  private List<LDPError> ldpError;

  public MeterConfiguration getMeterConfig() {
    return meterConfig;
  }
  public void setMeterConfig(MeterConfiguration meterConfig) {
    this.meterConfig = meterConfig;
  }
  public Long[] getPresentValues() {
    return presentValues;
  }
  public void setPresentValues(Long[] presentValues) {
    this.presentValues = presentValues;
  }
  public MeterStatus getMeterStatus() {
    return meterStatus;
  }
  public void setMeterStatus(MeterStatus meterStatus) {
    this.meterStatus = meterStatus;
  }
  public List<LPData> getLpData() {
    return lpData;
  }
  public void setLpData(List<LPData> lpData) {
    this.lpData = lpData;
  }
  public List<SERData> getSerData() {
    return serData;
  }
  public void setSerData(List<SERData> serData) {
    this.serData = serData;
  }
  public List<LDPError> getLdpError() {
    return ldpError;
  }
  public void setLdpError(List<LDPError> ldpError) {
    this.ldpError = ldpError;
  }

}
