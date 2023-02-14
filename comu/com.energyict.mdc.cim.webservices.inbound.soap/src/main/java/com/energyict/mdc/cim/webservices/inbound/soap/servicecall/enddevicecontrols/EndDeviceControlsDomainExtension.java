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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class EndDeviceControlsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        DEVICE_NAME("deviceName", "DEVICE_NAME"),
        DEVICE_MRID("deviceMrid", "DEVICE_MRID"),
        DEVICE_SERIAL_NUMBER("deviceSerialNumber", "DEVICE_SERIAL_NUMBER"),
        TRIGGER_DATE("triggerDate", "TRIGGER_DATE"),
        ERROR("error", "ERROR"),
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
    private String deviceSerialNumber;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String cancellationReason = CancellationReason.NOT_CANCELLED.getName();

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant triggerDate;

    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String error;

    public Instant getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Instant triggerDate) {
        this.triggerDate = triggerDate;
    }

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

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String endDevice) {
        this.deviceSerialNumber = endDevice;
    }

    public CancellationReason getCancellationReason() {
        return CancellationReason.valueFor(cancellationReason);
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason.getName();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceName((String) propertyValues.getProperty(FieldNames.DEVICE_NAME.javaName()));
        this.setDeviceMrid((String) propertyValues.getProperty(FieldNames.DEVICE_MRID.javaName()));
        this.setDeviceSerialNumber((String) propertyValues.getProperty(FieldNames.DEVICE_SERIAL_NUMBER.javaName()));
        this.setCancellationReason(CancellationReason.valueFor((String) propertyValues.getProperty(FieldNames.CANCELLATION_REASON.javaName())));
        this.setTriggerDate((Instant) propertyValues.getProperty(FieldNames.TRIGGER_DATE.javaName()));
        this.setError((String) propertyValues.getProperty(FieldNames.ERROR.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_NAME.javaName(), this.getDeviceName());
        propertySetValues.setProperty(FieldNames.DEVICE_MRID.javaName(), this.getDeviceMrid());
        propertySetValues.setProperty(FieldNames.DEVICE_SERIAL_NUMBER.javaName(), this.getDeviceSerialNumber());
        propertySetValues.setProperty(FieldNames.CANCELLATION_REASON.javaName(), this.getCancellationReason().getName());
        propertySetValues.setProperty(FieldNames.TRIGGER_DATE.javaName(), this.getTriggerDate());
        propertySetValues.setProperty(FieldNames.ERROR.javaName(), this.getError());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}
