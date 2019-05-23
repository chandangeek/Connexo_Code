package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class ParentGetMeterReadingsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        SOURCE("source", "SOURCE"),
        CALLBACK_URL("callbackUrl", "CALLBACK_URL"),
        TIME_PERIOD_START("timePeriodStart", "TIME_PERIOD_START"),
        TIME_PERIOD_END("timePeriodEnd", "TIME_PERIOD_END"),
        READING_TYPES("readingTypes", "READING_TYPES"),
        END_DEVICES("endDevices", "END_DEVICES");

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
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String source;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackUrl;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant timePeriodStart;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant timePeriodEnd;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String readingTypes;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String endDevices;


    public ParentGetMeterReadingsDomainExtension() {
        super();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public void setServiceCall(Reference<ServiceCall> serviceCall) {
        this.serviceCall = serviceCall;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Instant getTimePeriodStart() {
        return timePeriodStart;
    }

    public void setTimePeriodStart(Instant timePeriodStart) {
        this.timePeriodStart = timePeriodStart;
    }

    public Instant getTimePeriodEnd() {
        return timePeriodEnd;
    }

    public void setTimePeriodEnd(Instant timePeriodEnd) {
        this.timePeriodEnd = timePeriodEnd;
    }

    public String getReadingTypes() {
        return readingTypes;
    }

    public void setReadingTypes(String readingTypes) {
        this.readingTypes = readingTypes;
    }

    public String getEndDevices() {
        return endDevices;
    }

    public void setEndDevices(String endDevices) {
        this.endDevices = endDevices;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setSource((String) propertyValues.getProperty(FieldNames.SOURCE.javaName()));
        this.setCallbackUrl((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        this.setTimePeriodStart((Instant) propertyValues.getProperty(FieldNames.TIME_PERIOD_START.javaName()));
        this.setTimePeriodEnd((Instant) propertyValues.getProperty(FieldNames.TIME_PERIOD_END.javaName()));
        this.setReadingTypes((String) propertyValues.getProperty(FieldNames.READING_TYPES.javaName()));
        this.setEndDevices((String) propertyValues.getProperty(FieldNames.END_DEVICES.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.SOURCE.javaName(), this.getSource());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackUrl());
        propertySetValues.setProperty(FieldNames.TIME_PERIOD_START.javaName(), this.getTimePeriodStart());
        propertySetValues.setProperty(FieldNames.TIME_PERIOD_END.javaName(), this.getTimePeriodEnd());
        propertySetValues.setProperty(FieldNames.READING_TYPES.javaName(), this.getReadingTypes());
        propertySetValues.setProperty(FieldNames.END_DEVICES.javaName(), this.getEndDevices());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }
}