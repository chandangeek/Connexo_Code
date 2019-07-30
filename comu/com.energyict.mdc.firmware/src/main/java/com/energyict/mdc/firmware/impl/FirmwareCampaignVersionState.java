/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.impl.DeviceTypeLoadProfileTypeUsage;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FirmwareCampaignVersionState {

    enum Fields {
        FWRCAMPAIGN("firmwareCampaign"),


        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> firmwareCampaign = ValueReference.absent();

    //private List<FirmwareVersionState> firmwareVersionState = new ArrayList<>()

    private final DataModel dataModel;

    @Inject
    public FirmwareCampaignVersionState(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FirmwareCampaignVersionState init(FirmwareCampaign firmwareCampaign) {
        this.firmwareCampaign.set(firmwareCampaign);
        return this;
    }

    public void delete() {
        dataModel.remove(this);
    }

    public void save() {
        if (dataModel.mapper(FirmwareCampaignManagementOptions.class).getUnique("firmwareCampaign", firmwareCampaign.get()).isPresent()) {
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

    FirmwareCampaign getFirmwareCampaign(){
        return this.firmwareCampaign.get();
    }

}
