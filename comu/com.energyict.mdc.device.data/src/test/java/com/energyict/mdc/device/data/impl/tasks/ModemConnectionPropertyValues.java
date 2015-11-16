package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:27)
 */
public class ModemConnectionPropertyValues implements PersistentDomainExtension<ConnectionProvider> {

    @SuppressWarnings("unused")
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @Size(max=32)
    private String phoneNumber;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues) {
        this.connectionProvider.set(connectionProvider);
        this.phoneNumber = (String) propertyValues.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(ModemConnectionProperties.PHONE_NUMBER.propertyName(), this.phoneNumber);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}