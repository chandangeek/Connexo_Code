package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@UniqueFirmwareVersionByType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
@IsValidStatusTransfer(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.STATE_TRANSFER_NOT_ALLOWED + "}")
@IsFileRequired(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@IsStatusRequired(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class FirmwareVersionImpl implements FirmwareVersion, PersistenceAware {

    enum Fields {
        FIRMWAREVERSION("firmwareVersion"),
        DEVICETYPE("deviceType"),
        FIRMWARETYPE("firmwareType"),
        FIRMWARESTATUS("firmwareStatus"),
        FIRMWAREFILE("firmwareFileArray");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH + "}")
    private String firmwareVersion;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareType firmwareType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareStatus firmwareStatus;
    private byte[] firmwareFileArray;

    @Max(value = FirmwareService.MAX_FIRMWARE_FILE_SIZE, message = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED + "}")
    private long firmwareFile; // set this name for validation reason


    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    private final DataModel dataModel;
    private final EventService eventService;

    private FirmwareStatus oldFirmwareStatus;

    @Inject
    public FirmwareVersionImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    @Override
    public void deprecate() {
        setFirmwareStatus(FirmwareStatus.DEPRECATED);
        setFirmwareFile(null);
        save();
    }

    private FirmwareVersion init(DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType) {
        this.deviceType.set(deviceType);
        setFirmwareVersion(firmwareVersion);
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
        this.notifyCreated();
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        this.notifyUpdated();
    }

    private void notifyCreated() {
        this.eventService.postEvent(EventType.FIRMWARE_VERSION_CREATED.topic(), this);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(EventType.FIRMWARE_VERSION_UPDATED.topic(), this);
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
    public final void setFirmwareVersion(String firmwareVersion) {
        if (!Checks.is(firmwareVersion).emptyOrOnlyWhiteSpace()) {
            this.firmwareVersion = firmwareVersion.trim();
        } else {
            this.firmwareVersion = null;
        }
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
        return firmwareFileArray;
    }

    public boolean hasFirmwareFile(){
        return this.firmwareFile > 0;
    }

    @Override
    public void setFirmwareFile(byte[] firmwareFile) {
        this.firmwareFileArray = firmwareFile;
        if (this.firmwareFileArray != null) {
            this.firmwareFile = firmwareFileArray.length;
        } else {
            this.firmwareFile = 0;
        }
    }

    @Override
    public void setExpectedFirmwareSize(long fileSize) {
        this.firmwareFile = fileSize;
    }

    @Override
    public void postLoad() {
        if (this.firmwareFileArray != null) {
            this.firmwareFile = firmwareFileArray.length;
        }
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
