package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class UsagePointCommandDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal expectedNumberOfCommands;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal actualNumberOfSuccessfulCommands;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal actualNumberOfFailedCommands;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackHttpMethod;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackSuccessURL;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackPartialSuccessURL;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackFailureURL;

    public UsagePointCommandDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
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
