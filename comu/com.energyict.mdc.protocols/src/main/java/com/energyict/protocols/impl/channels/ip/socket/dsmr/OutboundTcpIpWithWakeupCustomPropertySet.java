package com.energyict.protocols.impl.channels.ip.socket.dsmr;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.ip.socket.dsmr.*;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.AbstractConnectionTypeCustomPropertySet;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import javax.inject.Inject;

public class OutboundTcpIpWithWakeupCustomPropertySet
        extends AbstractConnectionTypeCustomPropertySet
        implements CustomPropertySet<ConnectionProvider, OutboundTcpIpWithWakeupConnectionProperties> {

    @Inject
    public OutboundTcpIpWithWakeupCustomPropertySet(Thesaurus thesaurus,
                                                    PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_TCP_WAKEUP_CUSTOM_PROPERTY_SET_NAME).format();
    }

    /**
     * We need this to be false, otherwise the table (and journal) will grow uncontrolled
     * due to frequent change of "host" property, on each call.
     */
    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public PersistenceSupport<ConnectionProvider, OutboundTcpIpWithWakeupConnectionProperties> getPersistenceSupport() {
        return new OutboundTcpIpWithWakeupConnectionPropertiesPersistenceSupport();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new OutboundTcpIpWithWakeUpConnectionType(
                propertySpecService);
    }
}