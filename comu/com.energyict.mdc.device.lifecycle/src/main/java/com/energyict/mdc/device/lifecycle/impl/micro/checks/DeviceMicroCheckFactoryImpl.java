/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = "com.energyict.device.lifecycle.micro.check.factory", service = DeviceMicroCheckFactory.class)
@SuppressWarnings("unused")
public class DeviceMicroCheckFactoryImpl implements DeviceMicroCheckFactory {

    private DataModel dataModel;

    private final Map<String, Class<? extends MicroCheckNew>> microCheckMapping = new HashMap<>();

    public DeviceMicroCheckFactoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        addMicroCheckMappings();
    }

    private void addMicroCheckMappings() {
//        addMicroCheckMapping(MetrologyConfigurationIsDefinedCheck.class);
//        addMicroCheckMapping(MeterRolesAreSpecifiedCheck.class);
    }

    private void addMicroCheckMapping(Class<? extends ServerMicroCheck> clazz) {
        this.microCheckMapping.put(clazz.getSimpleName(), clazz);
    }

    @Override
    public Optional<MicroCheckNew> from(String microCheckKey) {
        return Optional.ofNullable(this.microCheckMapping.get(microCheckKey))
                .map(this.dataModel::getInstance);
    }

    @Override
    public Set<MicroCheckNew> getAllChecks() {
        return this.microCheckMapping.values().stream()
                .map(this.dataModel::getInstance)
                .collect(Collectors.toSet());
    }
}