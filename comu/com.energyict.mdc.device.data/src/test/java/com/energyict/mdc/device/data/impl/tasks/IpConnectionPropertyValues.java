package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:27)
 */
public class IpConnectionPropertyValues implements PersistentDomainExtension<ConnectionProvider> {

    @SuppressWarnings("unused")
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @Size(max=15)
    private String ipAddress;
    private int port;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues) {
        this.connectionProvider.set(connectionProvider);
        this.ipAddress = (String) propertyValues.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName());
        BigDecimal port = (BigDecimal) propertyValues.getProperty(IpConnectionProperties.PORT.propertyName());
        if (port != null) {
            this.port = port.intValue();
        }
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), this.ipAddress);
        propertySetValues.setProperty(IpConnectionProperties.PORT.propertyName(), new BigDecimal(this.port));
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}