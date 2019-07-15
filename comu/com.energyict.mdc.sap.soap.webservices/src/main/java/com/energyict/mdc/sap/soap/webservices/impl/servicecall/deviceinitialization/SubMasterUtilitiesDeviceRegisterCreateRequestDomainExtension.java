/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "serviceCall"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),

        // provided
        DEVICE_ID("deviceId", "deviceId"),

        //returned
        RETURN_CODE("returnCode", "returnCode"),
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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceId;

    private long returnCode;


    public BigDecimal getParentServiceCallId() {
        return parentServiceCallId;
    }

    public void setParentServiceCallId(BigDecimal parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(long returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setParentServiceCallId(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setDeviceId((String) propertyValues.getProperty(FieldNames.DEVICE_ID.javaName()));
        this.setReturnCode((long) propertyValues.getProperty(FieldNames.RETURN_CODE.javaName()));

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.RETURN_CODE.javaName(), this.getReturnCode());
    }

    @Override
    public void validateDelete() {

    }
}
