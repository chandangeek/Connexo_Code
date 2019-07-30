/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.impl.FirmwareVersionState;

import java.util.ArrayList;
import java.util.List;

public class FirmwareVersionState {

    /*enum Fields {
        FIRMWAREVERSION("firmwareVersion"),
        FIRMWARETYPE("firmwareType"),
        FIRMWARESTATUS("firmwareStatus"),
        IMAGEIDENTIFIER("imageIdentifier"),
        RANK("rank"),
        METER_FW_DEP("meterFirmwareDependency"),
        COM_FW_DEP("communicationFirmwareDependency");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }*/

    private String firmwareVersion;
    private String firmwareType;
    private String firmwareStatus;
    private String imageIdentifier;
    private String rank;
    private String meterFirmwareDependency;
    private String communicationFirmwareDependency;


    public FirmwareVersionState(FirmwareVersionInfo firmwareVersionInfo){
        this.firmwareVersion = firmwareVersionInfo.firmwareVersion;
        this.firmwareType = firmwareVersionInfo.firmwareType.localizedValue;
        this.firmwareStatus = firmwareVersionInfo.firmwareStatus.localizedValue;
        this.imageIdentifier = firmwareVersionInfo.imageIdentifier;
        this.rank = String.valueOf(firmwareVersionInfo.rank);
        this.meterFirmwareDependency = firmwareVersionInfo.meterFirmwareDependency.name;//???
        this.communicationFirmwareDependency = firmwareVersionInfo.communicationFirmwareDependency.name;//???
    }

    public List<FirmwareVersionState> getStatesOfFirmwareVersions(List<FirmwareVersionInfo> firmwareVersionInfos){
        List<FirmwareVersionState> firmwareVersionStates = new ArrayList<>();
        firmwareVersionInfos.forEach(fvInfo -> firmwareVersionStates.add(new FirmwareVersionState(fvInfo)));
        return firmwareVersionStates;
    }



}
