package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ServerConnectionType;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Specific ConnectionType used for the EIWeb plus Protocol.
 *
 * Copyrights EnergyICT
 * Date: 13/12/12
 * Time: 15:46
 */
@XmlRootElement
public class EIWebPlusConnectionType implements ServerConnectionType {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public EIWebPlusConnectionType(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebPlusConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new EIWebPlusCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-07-08 10:38:12 +0200 (Tue, 08 Jul 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // No implementation needed
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}