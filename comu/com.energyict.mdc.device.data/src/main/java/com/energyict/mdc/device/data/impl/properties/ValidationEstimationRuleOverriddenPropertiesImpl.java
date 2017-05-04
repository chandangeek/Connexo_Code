/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.google.common.collect.ImmutableMap;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ValidationEstimationRuleOverriddenPropertiesImpl {

    public static Map<String, Class<? extends ValidationEstimationRuleOverriddenPropertiesImpl>> IMPLEMENTERS = ImmutableMap.of(
            ChannelValidationRuleOverriddenPropertiesImpl.TYPE_IDENTIFIER, ChannelValidationRuleOverriddenPropertiesImpl.class,
            ChannelEstimationRuleOverriddenPropertiesImpl.TYPE_IDENTIFIER, ChannelEstimationRuleOverriddenPropertiesImpl.class
    );

    public enum Fields {

        DEVICE("device"),
        READINGTYPE("readingType"),
        RULE_NAME("ruleName"),
        RULE_IMPL("ruleImpl"),
        PROPERTIES("properties");

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();

    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String ruleName;

    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String ruleImpl;

    @Valid
    private List<ValidationEstimationOverriddenPropertyImpl> properties = new ArrayList<>();

    private final DataModel dataModel;

    ValidationEstimationRuleOverriddenPropertiesImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ValidationEstimationRuleOverriddenPropertiesImpl init(Device device, ReadingType readingType, String ruleName, String ruleImpl) {
        this.device.set(device);
        this.readingType.set(readingType);
        this.ruleName = ruleName;
        this.ruleImpl = ruleImpl;
        return this;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public Device getDevice() {
        return device.get();
    }

    public ReadingType getReadingType() {
        return readingType.get();
    }

    public Map<String, Object> getProperties() {
        return this.properties.stream().collect(Collectors.toMap(
                ValidationEstimationOverriddenPropertyImpl::getName,
                ValidationEstimationOverriddenPropertyImpl::getValue));
    }

    abstract List<PropertySpec> getPropertySpecs();

    abstract PropertySpec getPropertySpec(String propertyName);

    abstract void validateProperties(Map<String, Object> properties);

    String getRuleName() {
        return ruleName;
    }

    String getRuleImpl() {
        return ruleImpl;
    }

    public void setProperties(Map<String, Object> newProperties) {
        if (!this.properties.isEmpty()) {
            this.properties.clear();
        }
        for (Map.Entry<String, Object> property : newProperties.entrySet()) {
            ValidationEstimationOverriddenPropertyImpl overriddenProperty = dataModel.getInstance(ValidationEstimationOverriddenPropertyImpl.class);
            overriddenProperty.init(this, property.getKey(), property.getValue());
            this.properties.add(overriddenProperty);
        }
    }

    public void update() {
        validate();
        if (this.properties.isEmpty()) {
            delete();// no need to keep entity in database if no properties overridden
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public void delete() {
        dataModel.remove(this);
    }

    public void save() {
        validate();
        Save.CREATE.save(dataModel, this);
    }

    public void validate() {
        Save.CREATE.validate(dataModel, this);
        validateProperties(getProperties());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationEstimationRuleOverriddenPropertiesImpl)) {
            return false;
        }
        ValidationEstimationRuleOverriddenPropertiesImpl that = (ValidationEstimationRuleOverriddenPropertiesImpl) o;
        return id == that.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
