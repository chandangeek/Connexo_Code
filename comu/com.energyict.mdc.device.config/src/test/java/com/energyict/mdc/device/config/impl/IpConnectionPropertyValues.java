/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:27)
 */
public class IpConnectionPropertyValues extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ConnectionProvider> {

    @SuppressWarnings("unused")
    private Reference<ConnectionProvider> connectionProvider = Reference.empty();
    @Size(max = 15)
    private String ipAddress;
    private BigDecimal port;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.connectionProvider.set(connectionProvider);
        this.ipAddress = (String) propertyValues.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName());
        this.port = (BigDecimal) propertyValues.getProperty(IpConnectionProperties.PORT.propertyName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), this.ipAddress);
        if (this.port != null) {
            propertySetValues.setProperty(IpConnectionProperties.PORT.propertyName(), this.port);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }
}