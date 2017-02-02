package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.util.Optional;

public class KeyAccessorTypeImpl implements KeyAccessorType {
    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    private TimeDuration duration;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String keyEncryptionMethod;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<KeyType> keyType = Reference.empty();
    @IsPresent
    private Reference<DeviceType> deviceType = Reference.empty();
    private DataModel dataModel;

    enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        ENCRYPTIONMETHOD("keyEncryptionMethod"),
        DURATION("duration"),
        KEYTYPE("keyType"),
        DEVICETYPE("deviceType");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public KeyAccessorTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Optional<TimeDuration> getDuration() {
        return duration == null ? Optional.empty() : Optional.of(duration);
    }

    @Override
    public KeyType getKeyType() {
        return keyType.get();
    }

    public void setKeyType(KeyType keyType) {
        this.keyType.set(keyType);
    }

    @Override
    public String getKeyEncryptionMethod() {
        return keyEncryptionMethod;
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }

    public void setKeyEncryptionMethod(String keyEncryptionMethod) {
        this.keyEncryptionMethod = keyEncryptionMethod;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setDuration(TimeDuration duration) {
        this.duration = duration;
    }

    public Optional<DeviceType> getDeviceType() {
        return deviceType.getOptional();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }
}
