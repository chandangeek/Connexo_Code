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
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link IEC1107SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:27)
 */
public class IEC1107SecuritySupportPersistenceSupport implements PersistenceSupport<BaseDevice, IEC1107SecurityProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return "PR1_IEC1107_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return "PR1_FK_IEC1107SEC_DEV";
    }

    @Override
    public Class<IEC1107SecurityProperties> persistenceClass() {
        return IEC1107SecurityProperties.class;
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
            .column(IEC1107SecurityProperties.ActualFields.PASSWORD.databaseName())
            .varChar()
            .map(IEC1107SecurityProperties.ActualFields.PASSWORD.javaName())
            .add();
    }

}