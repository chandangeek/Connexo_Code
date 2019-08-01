/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.firmware.FirmwareCampaignVersionState;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

class FirmwareVersionState {

    enum Fields {
        FWRCVERSIONSTATE("firmwareCampaignVersionState"),
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
    }


    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaignVersionState> firmwareCampaignVersionState = ValueReference.absent();
    private String firmwareVersion;
    private String firmwareType;
    private String firmwareStatus;
    private String imageIdentifier;
    private String rank;
    private String meterFirmwareDependency;
    private String communicationFirmwareDependency;
    private final DataModel dataModel;

    @Inject
    public FirmwareVersionState(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void init(FirmwareVersion firmwareVersion){
        this.firmwareVersion = firmwareVersion.getFirmwareVersion();
        this.firmwareType = firmwareVersion.getFirmwareType().getType();
        this.firmwareStatus = firmwareVersion.getFirmwareStatus().getStatus();
        this.imageIdentifier = firmwareVersion.getImageIdentifier();
        this.rank = String.valueOf(firmwareVersion.getRank());
        this.meterFirmwareDependency = firmwareVersion.getMeterFirmwareDependency().isPresent()?firmwareVersion.getMeterFirmwareDependency().get().getFirmwareVersion():null;//???
        this.communicationFirmwareDependency = firmwareVersion.getCommunicationFirmwareDependency().isPresent()?firmwareVersion.getCommunicationFirmwareDependency().get().getFirmwareVersion():null;//???
    }

    public void delete() {
        dataModel.remove(this);
    }

    public void save() {
        if (dataModel.mapper(FirmwareVersionState.class).getUnique("firmwareCampaignVersionState", firmwareCampaignVersionState.get()).isPresent()) {
            doUpdate();
        } else {
            doPersist();
        }
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public String getFirmwareStatus() {
        return firmwareStatus;
    }

    public void setFirmwareStatus(String firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public void setImageIdentifier(String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getMeterFirmwareDependency() {
        return meterFirmwareDependency;
    }

    public void setMeterFirmwareDependency(String meterFirmwareDependency) {
        this.meterFirmwareDependency = meterFirmwareDependency;
    }

    public String getCommunicationFirmwareDependency() {
        return communicationFirmwareDependency;
    }

    public void setCommunicationFirmwareDependency(String communicationFirmwareDependency) {
        this.communicationFirmwareDependency = communicationFirmwareDependency;
    }
}