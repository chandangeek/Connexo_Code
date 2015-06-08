package com.energyict.mdc.firmware.rest.impl;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareVersionInfo {
    public Long id;
    public String firmwareVersion;
    public FirmwareTypeInfo firmwareType;
    public FirmwareStatusInfo firmwareStatus;
    public Integer fileSize;
    public Boolean isInUse;

}
