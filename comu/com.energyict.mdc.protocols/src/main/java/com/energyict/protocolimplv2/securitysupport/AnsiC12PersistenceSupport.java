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
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link AnsiC12SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:35)
 */
public class AnsiC12PersistenceSupport extends CommonBaseDeviceSecuritySupport<AnsiC12SecurityProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P21.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_ANSI_C12_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_ANSIC12_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_ANSIC12_SECPROV";
    }

    @Override
    public Class<AnsiC12SecurityProperties> persistenceClass() {
        return AnsiC12SecurityProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(AnsiC12SecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}