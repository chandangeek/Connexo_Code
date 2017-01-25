package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of the {@link ConnectionType} interface
 * that supports outbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class OutboundProximusSmsConnectionType extends ConnectionTypeImpl {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public OutboundProximusSmsConnectionType(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    private String phoneNumberPropertyValue() {
        return (String) this.getProperty(OutboundProximusConnectionProperties.Fields.PHONE_NUMBER.propertySpecName());
    }

    private String connectionURLPropertyValue() {
        return (String) this.getProperty(OutboundProximusConnectionProperties.Fields.CONNECTION_URL.propertySpecName());
    }

    private String sourcePropertyValue() {
        return (String) this.getProperty(OutboundProximusConnectionProperties.Fields.SOURCE.propertySpecName());
    }

    private String authenticationPropertyValue() {
        return (String) this.getProperty(OutboundProximusConnectionProperties.Fields.AUTHENTICATION.propertySpecName());
    }

    private String serviceCodePropertyValue() {
        return (String) this.getProperty(OutboundProximusConnectionProperties.Fields.SERVICE_CODE.propertySpecName());
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
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP, ComPortType.UDP);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        this.copyProperties(properties);
        return new ProximusSmsComChannel(
                this.phoneNumberPropertyValue(),
                this.connectionURLPropertyValue(),
                this.sourcePropertyValue(),
                this.authenticationPropertyValue(),
                this.serviceCodePropertyValue());
    }

    private void copyProperties(List<ConnectionProperty> properties) {
        properties
            .stream()
            .filter(this::hasValue)
            .forEach(this::copyProperty);
    }

    private boolean hasValue(ConnectionProperty property) {
        return property.getName() != null;
    }

    private void copyProperty(ConnectionProperty property) {
        this.setProperty(property.getName(), property.getValue());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for OutboundProximusSmsConnectionType
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new OutboundProximusCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-26 15:15:49 +0200 (Mit, 26 Jun 2013) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

}