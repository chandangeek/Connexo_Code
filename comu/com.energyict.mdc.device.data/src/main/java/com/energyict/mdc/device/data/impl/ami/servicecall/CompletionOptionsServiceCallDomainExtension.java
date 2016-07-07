package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.Size;

/**
 * @author sva
 * @since 03/06/2016 - 15:39
 */
public class CompletionOptionsServiceCallDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        DESTINATION_SPEC("destinationSpec", "destination_spec"),
        DESTINATION_MESSAGE("destinationMessage", "destination_message");

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

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String destinationSpec;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String destinationMessage;

    public CompletionOptionsServiceCallDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public String getDestinationSpec() {
        return destinationSpec;
    }

    public void setDestinationSpec(String destinationSpec) {
        this.destinationSpec = destinationSpec;
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDestinationSpec((String) propertyValues.getProperty(FieldNames.DESTINATION_SPEC.javaName));
        this.setDestinationMessage((String) propertyValues.getProperty(FieldNames.DESTINATION_MESSAGE.javaName));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DESTINATION_SPEC.javaName(), this.getDestinationSpec());
        propertySetValues.setProperty(FieldNames.DESTINATION_MESSAGE.javaName(), this.getDestinationMessage());
    }

    @Override
    public void validateDelete() {
    }
}