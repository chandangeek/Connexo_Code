package com.energyict.protocols.impl.channels.ip.datagrams;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpCustomPropertySet;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link OutboundTcpIpConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:03)
 */
public class OutboundUdpCustomPropertySet extends OutboundTcpIpCustomPropertySet {

    public OutboundUdpCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_UDP_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType(propertySpecService);
    }
}