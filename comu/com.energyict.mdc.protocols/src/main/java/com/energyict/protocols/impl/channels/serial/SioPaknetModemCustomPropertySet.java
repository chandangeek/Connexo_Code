package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.AbstractConnectionTypeCustomPropertySet;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import javax.inject.Inject;

public class SioPaknetModemCustomPropertySet extends AbstractConnectionTypeCustomPropertySet implements CustomPropertySet<ConnectionProvider, SioSerialConnectionProperties> {

    @Inject
    public SioPaknetModemCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.SERIAL_IO_PAKNET_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, SioSerialConnectionProperties> getPersistenceSupport() {
        return new SioSerialConnectionPropertiesPersistenceSupport();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new SioPaknetModemConnectionType(propertySpecService);
    }
}