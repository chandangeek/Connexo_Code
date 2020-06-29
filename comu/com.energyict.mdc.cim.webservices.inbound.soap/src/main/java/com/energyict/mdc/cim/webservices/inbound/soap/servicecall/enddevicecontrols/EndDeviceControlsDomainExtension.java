/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.Size;

public class EndDeviceControlsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        DEVICE_NAME("deviceName", "DEVICE_NAME"),
        DEVICE_MRID("deviceMrid", "DEVICE_MRID"),
        CANCELLATION_REASON("cancellationReason", "CANCELLATION_REASON");

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

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceName;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceMrid;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String cancellationReason = CancellationReason.NOT_CANCELLED.getName();

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMrid() {
        return deviceMrid;
    }

    public void setDeviceMrid(String endDevice) {
        this.deviceMrid = endDevice;
    }

    public CancellationReason getCancellationReason() {
        return CancellationReason.valueFor(cancellationReason);
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason.getName();
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceName((String) propertyValues.getProperty(FieldNames.DEVICE_NAME.javaName()));
        this.setDeviceMrid((String) propertyValues.getProperty(FieldNames.DEVICE_MRID.javaName()));
        this.setCancellationReason(CancellationReason.valueFor((String) propertyValues.getProperty(FieldNames.CANCELLATION_REASON.javaName())));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_NAME.javaName(), this.getDeviceName());
        propertySetValues.setProperty(FieldNames.DEVICE_MRID.javaName(), this.getDeviceMrid());
        propertySetValues.setProperty(FieldNames.CANCELLATION_REASON.javaName(), this.getCancellationReason().getName());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }
}
