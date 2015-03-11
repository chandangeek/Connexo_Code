package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;

import javax.inject.Inject;
import java.time.Instant;

public class FirmwareUpgradeOptionsImpl implements FirmwareUpgradeOptions {
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private boolean install;
    private boolean activate;
    private boolean activateOnDate;

    @SuppressWarnings("unused")
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    private long version;

    private final DataModel dataModel;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareUpgradeOptionsImpl(DataModel dataModel, FirmwareService firmwareService) {
        this.dataModel = dataModel;
        this.firmwareService = firmwareService;
    }

    public void save() {
        if (!firmwareService.findFirmwareUpgradeOptionsByDeviceType(this.deviceType.get()).isPresent()) {
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

}
