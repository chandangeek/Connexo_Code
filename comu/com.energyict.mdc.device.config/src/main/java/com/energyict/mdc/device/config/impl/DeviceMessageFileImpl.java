package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.FileBlob;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.google.common.base.MoreObjects;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Provides an implementation for the {@link DeviceMessageFile} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-11 (13:52)
 */
class DeviceMessageFileImpl implements ServerDeviceMessageFile {

    enum Fields {
        NAME("name"),
        DEVICE_TYPE("deviceType"),
        CONTENTS("contents"),
        CREATIONDATE("creationDate"),
        COBSOLETEDATE("obsoleteDate")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    long id;
    private DataModel dataModel;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", groups = {Save.Create.class, Save.Update.class})
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Blob contents = FileBlob.empty();
    @Max(value = DeviceConfigurationService.MAX_DEVICE_MESSAGE_FILE_SIZE_MB, message = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED + "}", groups = {Save.Create.class, Save.Update.class})
    @SuppressWarnings("unused") // For validation only
    private BigDecimal blobSize = BigDecimal.ZERO;
    private Instant obsoleteDate;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    DeviceMessageFileImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }


    DeviceMessageFileImpl init(DeviceType deviceType, Path path) {
        this.deviceType.set(deviceType);
        this.name = path.getFileName().toString();
        this.contents = FileBlob.from(path);
        this.blobSize = this.sizeInMB();
        return this;
    }

    ServerDeviceMessageFile init(DeviceType deviceType, InputStream inputStream, String fileName) {
        this.deviceType.set(deviceType);
        this.name = fileName;
        this.contents = FileBlob.from(inputStream);
        this.blobSize = this.sizeInMB();
        return this;
    }

    private BigDecimal sizeInMB() {
        BigDecimal byteToMBScaler = BigDecimal.valueOf(1024 * 1024);
        return BigDecimal.valueOf(this.contents.length()).divide(byteToMBScaler, 3, BigDecimal.ROUND_CEILING);
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public long getSize() {
        return contents.length();
    }

    @Override
    public void setObsolete(Instant instant) {
        this.dataModel.mapper(DeviceMessageFile.class).lockNoWait(this.getId());
        this.obsoleteDate = instant;
        this.contents.clear();
        this.dataModel.update(this);
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteDate != null;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readWith(Consumer<InputStream> inputStreamConsumer) {
        try (InputStream inputStream = this.contents.getBinaryStream()) {
            inputStreamConsumer.accept(inputStream);
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceMessageFileImpl that = (DeviceMessageFileImpl) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceType", deviceType)
                .add("id", id)
                .add("name", name)
                .toString();
    }

}