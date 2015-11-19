package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link NoOrPasswordSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:27)
 */
public class NoOrPasswordSecuritySupportPersistenceSupport implements PersistenceSupport<BaseDevice, NoOrPasswordSecurityProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return "PR1_NO_OR_PWD_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return "PR1_FK_NOORPWDSEC_DEV";
    }

    @Override
    public Class<NoOrPasswordSecurityProperties> persistenceClass() {
        return NoOrPasswordSecurityProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table
            .column(NoOrPasswordSecurityProperties.ActualFields.PASSWORD.databaseName())
            .varChar()
            .map(NoOrPasswordSecurityProperties.ActualFields.PASSWORD.javaName())
            .add();
    }

}