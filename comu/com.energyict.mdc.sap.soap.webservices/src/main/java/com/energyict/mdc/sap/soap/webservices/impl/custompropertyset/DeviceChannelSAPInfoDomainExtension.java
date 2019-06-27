/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class DeviceChannelSAPInfoDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ChannelSpec>, Effectivity {

    public enum FieldNames {
        DOMAIN("channelSpec", "CHANNELSPEC"),
        DEVICE_ID("device", "DEVICE"),
        LOGICAL_REGISTER_NUMBER("logicalRegisterNumber", "LOGICAL_REGISTER_NUMBER");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<ChannelSpec> channelSpec = ValueReference.absent();
    private Long device;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String logicalRegisterNumber;

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(ChannelSpec domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.channelSpec.set(domainInstance);
        this.device = additionalPrimaryKeyValues.length > 0 ? (Long) additionalPrimaryKeyValues[0] : null;
        this.logicalRegisterNumber = (String) propertyValues.getProperty(FieldNames.LOGICAL_REGISTER_NUMBER.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        additionalPrimaryKeyValues[0] = this.device;
        propertySetValues.setProperty(FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), this.logicalRegisterNumber);
    }

    @Override
    public void validateDelete() {
        // for future purposes
    }

    public long getDeviceId() {
        return device;
    }

    public void setDeviceId(long id) {
        device = id;
    }

    @Override
    public Interval getInterval() {
        return super.getInterval();
    }

    @Override
    public void setInterval(Interval interval) {
        super.setInterval(interval);
    }

    public ChannelSpec getChannelSpec() {
        return channelSpec.get();
    }

    public Optional<String> getLogicalRegisterNumber() {
        return Optional.ofNullable(logicalRegisterNumber);
    }
}

