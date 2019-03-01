/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.time.Instant;

public class GetMeterConfigItemDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("SERVICE_CALL", "SERVICE_CALL"),
        METER_MRID("METER_MRID", "METER_MRID"),
        METER_NAME("METER_NAME", "METER_NAME"),
        FROM_DATE("FROM_DATE", "FROM_DATE"),
        TO_DATE("TO_DATE", "TO_DATE"),
        ERROR_CODE("ERROR_CODE", "ERROR_CODE"),
        ERROR_MESSAGE("ERROR_MESSAGE", "ERROR_MESSAGE");

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
    private String meterMrid;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterName;
    private Instant fromDate;
    private Instant toDate;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;

    public GetMeterConfigItemDomainExtension() {
        super();
    }

    public String getMeterMrid() {
        return meterMrid;
    }

    public void setMeterMrid(String meterMrid) {
        this.meterMrid = meterMrid;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }
    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setMeterMrid((String) propertyValues.getProperty(FieldNames.METER_MRID.javaName()));
        this.setMeterName((String) propertyValues.getProperty(FieldNames.METER_NAME.javaName()));
        this.setFromDate((Instant) propertyValues.getProperty(FieldNames.FROM_DATE.javaName()));
        this.setToDate((Instant) propertyValues.getProperty(FieldNames.TO_DATE.javaName()));
        this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.METER_MRID.javaName(), this.getMeterMrid());
        propertySetValues.setProperty(FieldNames.METER_NAME.javaName(), this.getMeterName());
        propertySetValues.setProperty(FieldNames.FROM_DATE.javaName(), this.getFromDate());
        propertySetValues.setProperty(FieldNames.TO_DATE.javaName(), this.getToDate());
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
    }

    @Override
    public void validateDelete() {
    }
}
