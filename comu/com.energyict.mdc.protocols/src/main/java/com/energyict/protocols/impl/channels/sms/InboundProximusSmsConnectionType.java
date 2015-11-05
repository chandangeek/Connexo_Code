package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of the {@link ConnectionType} interface specific for inbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class InboundProximusSmsConnectionType extends AbstractInboundSmsConnectionType {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public InboundProximusSmsConnectionType(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for InboundProximusSmsConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-28 16:52:59 +0200 (Fre, 28 Jun 2013) $";
    }

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.of(this.newCustomPropertySet());
    }

    private InboundProximusCustomPropertySet newCustomPropertySet() {
        return new InboundProximusCustomPropertySet(this.thesaurus, propertySpecService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.newCustomPropertySet().getPropertySpecs();
    }

}