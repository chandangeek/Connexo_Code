package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.AbstractConnectionTypeCustomPropertySet;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import javax.inject.Inject;

public class SioPEMPModemCustomPropertySet extends AbstractConnectionTypeCustomPropertySet implements CustomPropertySet<ConnectionProvider, SioSerialConnectionProperties> {

    @Inject
    public SioPEMPModemCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.SERIAL_IO_PEMP_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, SioSerialConnectionProperties> getPersistenceSupport() {
        return new SioSerialConnectionPropertiesPersistenceSupport();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new SioPEMPModemConnectionType(propertySpecService);
    }
}