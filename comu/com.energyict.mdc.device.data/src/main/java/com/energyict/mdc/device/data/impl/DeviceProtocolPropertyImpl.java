/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidDeviceProtocolProperties;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

@ValidDeviceProtocolProperties(groups = {Save.Create.class, Save.Update.class})
public class DeviceProtocolPropertyImpl implements ServerDeviceProtocolPropertyForValidation, Serializable {

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String propertyValue;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String propertyName;
    private transient Optional<PropertySpec> propertySpec = Optional.empty();
    private Reference<Device> device = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public DeviceProtocolPropertyImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    DeviceProtocolPropertyImpl initialize(Device device, Optional<PropertySpec> propertySpec, String stringValue) {
        this.device.set(device);
        if (propertySpec.isPresent()) {
            this.propertySpec = propertySpec;
            this.propertyName = propertySpec.get().getName();
        } else {
            throw DeviceProtocolPropertyException.propertySpecTypeDoesNotExist(stringValue, thesaurus, MessageSeeds.DEVICE_PROPERTY_HAS_NO_SPEC);
        }
        this.propertyValue = stringValue;
        return this;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public PropertySpec getPropertySpec() {
        if (!propertySpec.isPresent()) {
            propertySpec = getPropertySpecForProperty(propertyName);
        }
        return propertySpec.orElseThrow(() -> DeviceProtocolPropertyException.propertySpecTypeDoesNotExist(propertyName, thesaurus, MessageSeeds.DEVICE_PROPERTY_HAS_NO_SPEC));
    }

    private Optional<PropertySpec> getPropertySpecForProperty(String name) {
        if (device.isPresent() && device.get().getDeviceProtocolPluggableClass().isPresent()) {
            return this.device.get()
                    .getDeviceProtocolPluggableClass()
                    .get()
                    .getDeviceProtocol()
                    .getPropertySpecs()
                    .stream()
                    .filter(spec -> spec.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public void setValue(String value) {
        this.propertyValue = value;
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
    }
}