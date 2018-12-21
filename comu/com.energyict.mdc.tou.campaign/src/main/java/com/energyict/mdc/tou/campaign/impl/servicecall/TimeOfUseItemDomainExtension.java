/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.TimeOfUseItem;

import java.math.BigDecimal;

public class TimeOfUseItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, TimeOfUseItem {

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        DEVICE("device", "device"),
        ;

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

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @IsPresent
    private Reference<Device> device = Reference.empty();
    private BigDecimal parentServiceCallId;

    @Override
    public Device getDevice() {
        return device.get();
    }

    @Override
    public BigDecimal getParentServiceCallId() {
        return parentServiceCallId;
    }

    public void setParentServiceCallId(BigDecimal parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    public TimeOfUseItemDomainExtension() {
        super();
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setDevice((Device) propertyValues.getProperty(FieldNames.DEVICE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE.javaName(), this.getDevice());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}