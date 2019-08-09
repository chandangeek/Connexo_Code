/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignVersionStateShapshot;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

public class FirmwareCampaignVersionSnapshotImpl implements FirmwareCampaignVersionStateShapshot {

    enum Fields {
        FWRCAMPAIGN("firmwareCampaign"),
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

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> firmwareCampaign = ValueReference.absent();
    private String firmwareVersion;
    private FirmwareType firmwareType;
    private FirmwareStatus firmwareStatus;
    private String imageIdentifier;
    private int rank;
    private String meterFirmwareDependency;
    private String communicationFirmwareDependency;
    private final DataModel dataModel;

    @Inject
    public FirmwareCampaignVersionSnapshotImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FirmwareCampaignVersionSnapshotImpl init(FirmwareCampaign firmwareCampaign, FirmwareVersion firmwareVersion) {
        this.firmwareCampaign.set(firmwareCampaign);
        this.firmwareVersion = firmwareVersion.getFirmwareVersion();
        this.firmwareType = firmwareVersion.getFirmwareType();
        this.firmwareStatus = firmwareVersion.getFirmwareStatus();
        this.imageIdentifier = firmwareVersion.getImageIdentifier();
        this.rank = firmwareVersion.getRank();
        this.meterFirmwareDependency = firmwareVersion.getMeterFirmwareDependency().isPresent()?firmwareVersion.getMeterFirmwareDependency().get().getFirmwareVersion():null;//???
        this.communicationFirmwareDependency = firmwareVersion.getCommunicationFirmwareDependency().isPresent()?firmwareVersion.getCommunicationFirmwareDependency().get().getFirmwareVersion():null;//???
        return this;
    }

    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    FirmwareCampaign getFirmwareCampaign(){
        return this.firmwareCampaign.get();
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public FirmwareType getFirmwareType() {
        return firmwareType;
    }

    public FirmwareStatus getFirmwareStatus() {
        return firmwareStatus;
    }

    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public int getRank() {
        return rank;
    }

    public String getMeterFirmwareDependency() {
        return meterFirmwareDependency;
    }

    public String getCommunicationFirmwareDependency() {
        return communicationFirmwareDependency;
    }
}
