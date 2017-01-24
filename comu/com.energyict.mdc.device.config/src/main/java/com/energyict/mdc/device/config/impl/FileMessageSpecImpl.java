package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceMessageSpec} interface
 * that will replace all {@link PropertySpec}s of type {@link DeviceMessageFile}
 * with a similar PropertySpec (in terms of name, display name,...)
 * but with {@link PropertySpecPossibleValues} that contain only
 * the DeviceMessageFile that have been added to the {@link DeviceType}.
 * In addition, the {@link ValueFactory} is also replaced to consistently
 * return only DeviceMessageFile that have been added to the DeviceType.
 * It explicitly <strong>does not support</strong> multi values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (10:40)
 */
class FileMessageSpecImpl implements DeviceMessageSpec {
    private final DeviceType deviceType;
    private final DeviceMessageSpec source;
    private final PropertySpecService propertySpecService;

    FileMessageSpecImpl(DeviceType deviceType, DeviceMessageSpec source, PropertySpecService propertySpecService) {
        this.deviceType = deviceType;
        this.source = source;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return this.source.getCategory();
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public DeviceMessageId getId() {
        return this.source.getId();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.source
                .getPropertySpecs()
                .stream()
                .map(this::wrapIfNecessary)
                .collect(Collectors.toList());
    }

    private PropertySpec wrapIfNecessary(PropertySpec propertySpec) {
        if (this.relatesToDeviceMessageFile(propertySpec)) {
            PropertySpecBuilder<DeviceMessageFile> builder =
                    this.propertySpecService
                        .specForValuesOf(new DeviceMessageFileValueFactory(this.deviceType))
                        .named(propertySpec.getName(), propertySpec.getDisplayName())
                        .describedAs(propertySpec.getDescription())
                        .addValues(this.deviceType.getDeviceMessageFiles().stream()
                                .sorted(Comparator.comparing(DeviceMessageFile::getName))
                                .collect(Collectors.toList()));
            if (propertySpec.isRequired()) {
                builder.markRequired();
            }
            this.copyPossibleValueBehavior(propertySpec, builder);
            return builder.finish();
        } else {
            return propertySpec;
        }
    }

    private void copyPossibleValueBehavior(PropertySpec propertySpec, PropertySpecBuilder<DeviceMessageFile> builder) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues != null) {
            if (possibleValues.isExhaustive()) {
                builder.markExhaustive(possibleValues.getSelectionMode());
            }
            if (possibleValues.isEditable()) {
                builder.markEditable();
            }
        }
    }

    private boolean relatesToDeviceMessageFile(PropertySpec propertySpec) {
        return this.relatesToDeviceMessageFile(propertySpec.getValueFactory());
    }

    private boolean relatesToDeviceMessageFile(ValueFactory valueFactory) {
        return com.energyict.mdc.protocol.api.DeviceMessageFile.class.isAssignableFrom(valueFactory.getValueType());
    }

}
