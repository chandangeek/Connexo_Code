package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;
import com.energyict.protocols.impl.channels.VoidComChannel;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of the {@link ConnectionType} interface specific for inbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class InboundProximusSmsConnectionType extends ConnectionTypeImpl {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public InboundProximusSmsConnectionType(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERVLET);
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new InboundProximusCustomPropertySet(this.thesaurus, propertySpecService));
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for InboundProximusSmsConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-28 16:52:59 +0200 (Fre, 28 Jun 2013) $";
    }

}