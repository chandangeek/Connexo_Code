package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecuritySupport;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link DlmsSecuritySupportPerClient}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (15:08)
 */
public class DlmsSecurityPerClientPersistenceSupport extends CommonBaseDeviceSecuritySupport<DlmsSecurityPerClientProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_DLMS_SECURITY_PER_CLIENT";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_DLMSSEC_PERCLIENT_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_DLMSSECPERCLIENT_SECPROV";
    }

    @Override
    public Class<DlmsSecurityPerClientProperties> persistenceClass() {
        return DlmsSecurityPerClientProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(DlmsSecurityPerClientProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}