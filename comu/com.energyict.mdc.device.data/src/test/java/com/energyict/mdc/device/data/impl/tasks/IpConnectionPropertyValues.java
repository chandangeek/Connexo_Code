package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:27)
 */
public class IpConnectionPropertyValues implements PersistentDomainExtension<ConnectionType> {

    @SuppressWarnings("unused")
    private Reference<ConnectionType> connectionType = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    private String ipAddress;
    private int port;

    @Override
    public void copyFrom(ConnectionType connectionType, CustomPropertySetValues propertyValues) {
        this.connectionType.set(connectionType);
        this.ipAddress = (String) propertyValues.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName());
        Integer port = (Integer) propertyValues.getProperty(IpConnectionProperties.PORT.propertyName());
        if (port != null) {
            this.port = port;
        }
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), this.ipAddress);
        propertySetValues.setProperty(IpConnectionProperties.PORT.propertyName(), this.port);
    }

}