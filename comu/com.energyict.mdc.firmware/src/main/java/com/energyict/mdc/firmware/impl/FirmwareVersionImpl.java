package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@UniqueFirmwareVersion(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
@IsValidStatusTransfer(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.STATE_TRANSFER_NOT_ALLOWED + "}")
@IsFileRequired(groups = Save.Update.class, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@IsStatusRequired(groups = Save.Update.class, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class FirmwareVersionImpl implements FirmwareVersion {

    enum Fields {
        FIRMWAREVERSION("firmwareVersion"),
        DEVICETYPE("deviceType"),
        FIRMWARETYPE("firmwareType"),
        FIRMWARESTATUS("firmwareStatus"),
        FIRMWAREFILE("firmwareFile");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH + "}")
    private String firmwareVersion;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareType firmwareType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareStatus firmwareStatus;
    @Size(max = FirmwareService.MAX_FIRMWARE_FILE_SIZE, message = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED + "}")
    private byte[] firmwareFile;

    @SuppressWarnings("unused")
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    private long version;

    private final DataModel dataModel;

    private FirmwareStatus oldFirmwareStatus;

    @Inject
    public FirmwareVersionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    public void deprecate() {
        setFirmwareStatus(FirmwareStatus.DEPRECATED);
        setFirmwareFile(null);
        save();
    }

    private FirmwareVersion init(DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType) {
        this.deviceType.set(deviceType);
        this.firmwareVersion = firmwareVersion;
        this.firmwareStatus = firmwareStatus;
        this.firmwareType = firmwareType;
        return this;
    }

    public static FirmwareVersion from(DataModel dataModel, DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType) {
        return dataModel.getInstance(FirmwareVersionImpl.class).init(deviceType, firmwareVersion, firmwareStatus, firmwareType);
    }

    @Override
    public void validate() {
        Save.CREATE.validate(dataModel, this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    public long getId() {
        return id;
    }

    public void setId( long id) {
        this.id = id;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion.trim() : firmwareVersion;
    }

    @Override
    public FirmwareType getFirmwareType() {
        return firmwareType;
    }

    @Override
    public void setFirmwareType(FirmwareType firmwareType) {
        this.firmwareType = firmwareType;
    }

    @Override
    public FirmwareStatus getFirmwareStatus() {
        return firmwareStatus;
    }

    @Override
    public void setFirmwareStatus(FirmwareStatus firmwareStatus) {
        this.oldFirmwareStatus =  this.firmwareStatus;
        this.firmwareStatus = firmwareStatus;
    }

    @Override
    public byte[] getFirmwareFile() {
        return firmwareFile;
    }

    @Override
    public void setFirmwareFile(byte[] firmwareFile) {
        this.firmwareFile = firmwareFile;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    public FirmwareStatus getOldFirmwareStatus() {
        return this.oldFirmwareStatus;
    }
}
