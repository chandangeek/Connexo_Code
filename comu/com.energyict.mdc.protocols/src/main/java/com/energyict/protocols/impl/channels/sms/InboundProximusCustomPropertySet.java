package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link InboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (17:26)
 */
public class InboundProximusCustomPropertySet implements CustomPropertySet<ConnectionType, InboundProximusConnectionProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public InboundProximusCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(ProximusTranslationKeys.INBOUND_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public Class<ConnectionType> getDomainClass() {
        return ConnectionType.class;
    }

    @Override
    public PersistenceSupport<ConnectionType, InboundProximusConnectionProperties> getPersistenceSupport() {
        return new InboundProximusConnectionPropertiesPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream
                .of(DeviceProtocolProperty.PHONE_NUMBER, DeviceProtocolProperty.CALL_HOME_ID)
                .map(prop -> prop.propertySpec(this.propertySpecService, true))
                .collect(Collectors.toList());
    }

}