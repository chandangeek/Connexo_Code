/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.SimpleBlob;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionBuilder;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

@UniqueFirmwareVersionByType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
@IsValidStatusTransfer(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.STATE_TRANSFER_NOT_ALLOWED + "}")
@IsFileRequired(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
@IsStatusRequired(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public final class FirmwareVersionImpl implements FirmwareVersion {

    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    private final EventService eventService;
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
    private Blob firmwareFile = SimpleBlob.empty();
    @Max(value = FirmwareService.MAX_FIRMWARE_FILE_SIZE, message = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED + "}")
    private Long firmwareFileSize = null; // set this size for validation reason
    private boolean hasFirmwareFile = false; // boolean indicating whether or not the firmware file has been set/updated
    @SuppressWarnings("unused")
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    private FirmwareStatus oldFirmwareStatus;

    @Inject
    public FirmwareVersionImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public void save() {
        doPersist();
    }

    private FirmwareVersionImpl init(DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType) {
        this.deviceType.set(deviceType);
        setFirmwareVersion(firmwareVersion);
        this.firmwareStatus = firmwareStatus;
        this.firmwareType = firmwareType;
        return this;
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        this.notifyCreated();
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

    public void setId(long id) {
        this.id = id;
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
        this.oldFirmwareStatus = this.firmwareStatus;
        this.firmwareStatus = firmwareStatus;
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
    public byte[] getFirmwareFile() {
        return readFirmwareVersionBytes(firmwareFile.getBinaryStream());
    }

    @Override
    public void initFirmwareFile(byte[] firmwareFile) {
        try {
            this.hasFirmwareFile = true;
            this.firmwareFileSize = (long) firmwareFile.length;
            BufferedOutputStream firmwareBlobWriter = new BufferedOutputStream(this.firmwareFile.setBinaryStream());
            firmwareBlobWriter.write(firmwareFile);
            firmwareBlobWriter.close();
        } catch (IOException e) {
            throw new FirmwareIOException(thesaurus, e);
        }
    }

    @Override
    public void setFirmwareFile(byte[] firmwareFile) {
        try {
            this.hasFirmwareFile = true;
            this.firmwareFileSize = (long) firmwareFile.length;
            this.firmwareFile.clear();
            BufferedOutputStream firmwareBlobWriter = new BufferedOutputStream(this.firmwareFile.setBinaryStream());
            firmwareBlobWriter.write(firmwareFile);
            firmwareBlobWriter.close();
            this.dataModel.update(this, "firmwareFile");
        } catch (IOException e) {
            throw new FirmwareIOException(thesaurus, e);
        }
    }

    @Override
    public InputStream getFirmwareFileAsStream() {
        return firmwareFile.getBinaryStream();
    }

    @Override
    public void setExpectedFirmwareSize(long fileSize) {
        this.firmwareFileSize = fileSize;
        this.hasFirmwareFile = true;
    }

    @Override
    public void validate() {
        Save.CREATE.validate(dataModel, this);
    }

    @Override
    public void deprecate() {
        setFirmwareStatus(FirmwareStatus.DEPRECATED);
        dropFirmwareFile();
        update();
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
        this.notifyUpdated();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    public byte[] readFirmwareVersionBytes(InputStream input) {
        try {
            byte[] buffer = new byte[input.available()];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new FirmwareIOException(thesaurus, e);
        }
    }

    public Instant getModTime() {
        return modTime;
    }

    /**
     * @return boolean indicating whether or not the FirmwareFile of this FirmwareVersion object has been set/updated or not
     */
    public boolean hasFirmwareFile() {
        return this.hasFirmwareFile;
    }

    /**
     * Tests if the firmware file of this {@link FirmwareVersion} is empty or not<br/>
     * <b>Remark:</b> in case no firmware file was set, this method will return false!
     *
     * @return true in case the firmware file of this FirmwareVersion is empty
     * false in case no firmware file has been set, or in case the set firmware file is not empty
     */
    public boolean isEmptyFile() {
        return this.firmwareFileSize != null && this.firmwareFileSize == 0;
    }

    public void dropFirmwareFile() {
        this.hasFirmwareFile = false;
        this.firmwareFileSize = null;
        this.firmwareFile.clear();
        this.dataModel.update(this, "firmwareFile");
    }

    public FirmwareStatus getOldFirmwareStatus() {
        return this.oldFirmwareStatus;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FirmwareVersionImpl that = (FirmwareVersionImpl) o;
        return id == that.id;

    }

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

    public static class FirmwareVersionImplBuilder implements FirmwareVersionBuilder {

        private final FirmwareVersionImpl underConstruction;

        public FirmwareVersionImplBuilder(FirmwareVersionImpl underConstruction, DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType) {
            this.underConstruction = underConstruction;
            this.underConstruction.init(deviceType, firmwareVersion, firmwareStatus, firmwareType);
        }

        @Override
        public FirmwareVersionBuilder setExpectedFirmwareSize(Integer fileSize) {
            underConstruction.setExpectedFirmwareSize(fileSize);
            return this;
        }

        @Override
        public FirmwareVersionBuilder initFirmwareFile(byte[] firmwareFile) {
            underConstruction.initFirmwareFile(firmwareFile);
            return this;
        }

        @Override
        public FirmwareVersion create() {
            underConstruction.save();
            return underConstruction;
        }

        @Override
        public void validate() {
            underConstruction.validate();
        }
    }
}
