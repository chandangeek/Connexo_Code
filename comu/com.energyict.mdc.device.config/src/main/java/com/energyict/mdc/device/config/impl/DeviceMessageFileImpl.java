package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.FileBlob;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

import com.google.common.base.MoreObjects;

import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Provides an implementation for the {@link DeviceMessageFile} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-11 (13:52)
 */
public class DeviceMessageFileImpl implements DeviceMessageFile {

    enum Fields {
        NAME("name"),
        DEVICE_TYPE("deviceType"),
        CONTENTS("contents"),
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
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Blob contents = FileBlob.empty();

    DeviceMessageFileImpl init(DeviceType deviceType, Path path) {
        this.deviceType.set(deviceType);
        this.name = path.getFileName().toString();
        this.contents = FileBlob.from(path);
        return this;
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