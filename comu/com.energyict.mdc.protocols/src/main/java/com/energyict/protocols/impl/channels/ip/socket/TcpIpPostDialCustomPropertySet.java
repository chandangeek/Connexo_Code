package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link TcpIpPostDialConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:04)
 */
public class TcpIpPostDialCustomPropertySet extends OutboundTcpIpCustomPropertySet {

    public TcpIpPostDialCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_TCP_POST_DIAL_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType(propertySpecService);
    }
}