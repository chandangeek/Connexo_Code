package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareUpgradeDeviceStatus;

import javax.inject.Inject;
import java.time.Instant;

public class DeviceInFirmwareCampaignImp implements DeviceInFirmwareCampaign {

    public enum Fields {
        CAMPAIGN ("campaign"),
        DEVICE ("device"),
        STATUS ("status"),
        ;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName(){
            return this.name;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> campaign = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private FirmwareUpgradeDeviceStatus status;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;

    @Inject
    public DeviceInFirmwareCampaignImp(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceInFirmwareCampaign init(FirmwareCampaign campaign, Device device){
        this.campaign.set(campaign);
        this.device.set(device);
        return this;
    }

    @Override
    public FirmwareUpgradeDeviceStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(FirmwareUpgradeDeviceStatus status) {
        this.status = status;
    }

    @Override
    public Device getDevice(){
        return this.device.get();
    }
}
