package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import java.math.BigDecimal;
import java.util.Optional;

public class UsagePointCommandDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        COMMANDS_EXPECTED("expectedNumberOfCommands", "expected_commands"),
        COMMANDS_SUCCESS("actualNumberOfSuccessfulCommands", "success_commands"),
        COMMANDS_FAILED("actualNumberOfFailedCommands", "failed_commands"),
        CALLBACK_METHOD("callbackHttpMethod", "http_method"),
        CALLBACK_SUCCESS("callbackSuccessURL", "callback_success_url"),
        CALLBACK_PART_SUCCESS("callbackPartialSuccessURL", "callback_part_success_url"),
        CALLBACK_FAILURE("callbackFailureURL", "callback_failure_url");

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
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private BigDecimal expectedNumberOfCommands;
    private BigDecimal actualNumberOfSuccessfulCommands;
    private BigDecimal actualNumberOfFailedCommands;
    private String callbackHttpMethod;
    private String callbackSuccessURL;
    private String callbackPartialSuccessURL;
    private String callbackFailureURL;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getCallbackFailureURL() {
        return callbackFailureURL;
    }

    public void setCallbackFailureURL(String callbackFailureURL) {
        this.callbackFailureURL = callbackFailureURL;
    }

    public BigDecimal getExpectedNumberOfCommands() {
        return expectedNumberOfCommands;
    }

    public void setExpectedNumberOfCommands(BigDecimal expectedNumberOfCommands) {
        this.expectedNumberOfCommands = expectedNumberOfCommands;
    }

    public BigDecimal getActualNumberOfSuccessfulCommands() {
        return actualNumberOfSuccessfulCommands;
    }

    public void setActualNumberOfSuccessfulCommands(BigDecimal actualNumberOfSuccessfulCommands) {
        this.actualNumberOfSuccessfulCommands = actualNumberOfSuccessfulCommands;
    }

    public BigDecimal getActualNumberOfFailedCommands() {
        return actualNumberOfFailedCommands;
    }

    public void setActualNumberOfFailedCommands(BigDecimal actualNumberOfFailedCommands) {
        this.actualNumberOfFailedCommands = actualNumberOfFailedCommands;
    }

    public String getCallbackHttpMethod() {
        return callbackHttpMethod;
    }

    public void setCallbackHttpMethod(String callbackHttpMethod) {
        this.callbackHttpMethod = callbackHttpMethod;
    }

    public String getCallbackSuccessURL() {
        return callbackSuccessURL;
    }

    public void setCallbackSuccessURL(String callbackSuccessURL) {
        this.callbackSuccessURL = callbackSuccessURL;
    }

    public String getCallbackPartialSuccessURL() {
        return callbackPartialSuccessURL;
    }

    public void setCallbackPartialSuccessURL(String callbackPartialSuccessURL) {
        this.callbackPartialSuccessURL = callbackPartialSuccessURL;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setExpectedNumberOfCommands(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.COMMANDS_EXPECTED.javaName())).orElse(0).toString()));
        this.setActualNumberOfSuccessfulCommands(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.COMMANDS_SUCCESS.javaName())).orElse(0).toString()));
        this.setActualNumberOfFailedCommands(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.COMMANDS_FAILED.javaName())).orElse(0).toString()));
        this.setCallbackHttpMethod((String) propertyValues.getProperty(FieldNames.CALLBACK_METHOD.javaName()));
        this.setCallbackSuccessURL((String) propertyValues.getProperty(FieldNames.CALLBACK_SUCCESS.javaName()));
        this.setCallbackPartialSuccessURL((String) propertyValues.getProperty(FieldNames.CALLBACK_PART_SUCCESS.javaName()));
        this.setCallbackFailureURL((String) propertyValues.getProperty(FieldNames.CALLBACK_FAILURE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.COMMANDS_EXPECTED.javaName(), this.getExpectedNumberOfCommands());
        propertySetValues.setProperty(FieldNames.COMMANDS_SUCCESS.javaName(), this.getActualNumberOfSuccessfulCommands());
        propertySetValues.setProperty(FieldNames.COMMANDS_FAILED.javaName(), this.getActualNumberOfFailedCommands());
        propertySetValues.setProperty(FieldNames.CALLBACK_METHOD.javaName(), this.getCallbackHttpMethod());
        propertySetValues.setProperty(FieldNames.CALLBACK_SUCCESS.javaName(), this.getCallbackSuccessURL());
        propertySetValues.setProperty(FieldNames.CALLBACK_PART_SUCCESS.javaName(), this.getCallbackPartialSuccessURL());
        propertySetValues.setProperty(FieldNames.CALLBACK_FAILURE.javaName(), this.getCallbackFailureURL());
    }

    @Override
    public void validateDelete() {
    }

}
