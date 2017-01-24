package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecuritySupport;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;
import com.google.inject.Module;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link DlmsSecuritySupport} suite 1 & 2.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:35)
 */
public class DlmsSecuritySuite1And2PersistenceSupport extends CommonBaseDeviceSecuritySupport<DlmsSecuritySuite1And2Properties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P32.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_DLMS_SECURITY_SUITE_1AND2";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_DLMSSEC_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_DLMSSEC_SECPROV";
    }

    @Override
    public Class<DlmsSecuritySuite1And2Properties> persistenceClass() {
        return DlmsSecuritySuite1And2Properties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(DlmsSecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
        Stream
            .of(DlmsSecuritySuite1And2Properties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}