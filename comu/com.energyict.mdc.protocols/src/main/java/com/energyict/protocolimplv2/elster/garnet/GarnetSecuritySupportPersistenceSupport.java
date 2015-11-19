package com.energyict.protocolimplv2.elster.garnet;

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
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (16:32)
 */
public class GarnetSecuritySupportPersistenceSupport implements PersistenceSupport<BaseDevice, GarnetSecurityProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return "PR1_GARNET_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return "PR1_FK_GARNETSEC_DEV";
    }

    @Override
    public Class<GarnetSecurityProperties> persistenceClass() {
        return GarnetSecurityProperties.class;
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
        Stream
            .of(GarnetSecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}