/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SubParentGetMeterReadingsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        END_DEVICE_NAME("endDeviceName", "END_DEVICE_NAME"),
        END_DEVICE_MRID("endDeviceMrid", "END_DEVICE_MRID"),
        END_DEVICE_SERIAL_NUMBER("endDeviceSerialNumber", "END_DEVICE_SERIAL_NUMBER");

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

    @IsPresent
    private Reference<ServiceCall> serviceCall = Reference.empty();

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endDeviceName;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endDeviceMrid;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endDeviceSerialNumber;

    public SubParentGetMeterReadingsDomainExtension() {
        super();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public void setServiceCall(Reference<ServiceCall> serviceCall) {
        this.serviceCall = serviceCall;
    }

    public String getEndDeviceName() {
        return endDeviceName;
    }

    public void setEndDeviceName(String endDeviceName) {
        this.endDeviceName = endDeviceName;
    }

    public String getEndDeviceMrid() {
        return endDeviceMrid;
    }

    public void setEndDeviceMrid(String endDevice) {
        this.endDeviceMrid = endDevice;
    }

    public String getEndDeviceSerialNumber() {
        return endDeviceSerialNumber;
    }

    public void setEndDeviceSerialNumber(String endDeviceSerialNumber) {
        this.endDeviceSerialNumber = endDeviceSerialNumber;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setEndDeviceName((String) propertyValues.getProperty(FieldNames.END_DEVICE_NAME.javaName()));
        this.setEndDeviceMrid((String) propertyValues.getProperty(FieldNames.END_DEVICE_MRID.javaName()));
        this.setEndDeviceSerialNumber((String) propertyValues.getProperty(FieldNames.END_DEVICE_SERIAL_NUMBER.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.END_DEVICE_NAME.javaName(), this.getEndDeviceName());
        propertySetValues.setProperty(FieldNames.END_DEVICE_MRID.javaName(), this.getEndDeviceMrid());
        propertySetValues.setProperty(FieldNames.END_DEVICE_SERIAL_NUMBER.javaName(), this.getEndDeviceSerialNumber());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }
}
