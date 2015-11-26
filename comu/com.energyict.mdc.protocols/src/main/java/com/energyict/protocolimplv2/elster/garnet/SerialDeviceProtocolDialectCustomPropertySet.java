package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SerialDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:44)
 */
public class SerialDeviceProtocolDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SerialDeviceProtocolDialectProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public SerialDeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return "com.energyict.protocolimplv2.abnt.common.dialects.SerialDeviceProtocolDialect";
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(DeviceProtocolDialectName.GARNET_SERIAL).format();
    }

    @Override
    public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
        return DeviceProtocolDialectPropertyProvider.class;
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SerialDeviceProtocolDialectProperties> getPersistenceSupport() {
        return new SerialDeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
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
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .map(field -> field.propertySpec(this.propertySpecService))
                .collect(Collectors.toList());
    }

}