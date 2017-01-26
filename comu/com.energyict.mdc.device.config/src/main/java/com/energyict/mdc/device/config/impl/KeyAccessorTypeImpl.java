package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.energyict.mdc.device.config.DeviceType;

import java.time.Duration;
import java.util.Optional;

public class KeyAccessorTypeImpl implements KeyAccessorType {
    private long id;
    private String name;
    private String description;
    private Duration duration;
    private Reference<KeyType> keyType = Reference.empty();
    private Reference<DeviceType> deviceType = Reference.empty();

    enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        DURATION("duration"),
        KEYTYPE("keyType"),
        DEVICETYPE("deviceType")
        ;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
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
    public Optional<Duration> getDuration() {
        return duration==null?Optional.empty():Optional.of(duration);
    }

    public KeyType getKeyType() {
        return keyType.get();
    }

    public void setKeyType(KeyType keyType) {
        this.keyType.set(keyType);
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
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
