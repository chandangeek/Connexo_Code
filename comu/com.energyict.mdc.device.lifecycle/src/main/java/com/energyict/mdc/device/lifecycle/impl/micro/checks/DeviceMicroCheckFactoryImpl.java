/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.device.lifecycle.micro.check.factory",
        service = DeviceMicroCheckFactory.class,
        immediate = true)
@SuppressWarnings("unused")
public class DeviceMicroCheckFactoryImpl implements DeviceMicroCheckFactory {

    private final Map<String, Class<? extends MicroCheckNew>> microCheckMapping = new HashMap<>();

    public DeviceMicroCheckFactoryImpl() {
        addMicroCheckMappings();
    }

    @Override
    public Optional<Class<? extends MicroCheckNew>> from(String microCheckKey) {
        return Optional.ofNullable(microCheckMapping.get(microCheckKey));
    }

    @Override
    public Set<Class<? extends MicroCheckNew>> getAllChecks() {
        return new HashSet<>(microCheckMapping.values());
    }

    private void addMicroCheckMappings() {
        addMicroCheckMapping(ActiveConnectionAvailable.class);
        addMicroCheckMapping(AllDataValid.class);
        addMicroCheckMapping(AllDataValidated.class);
        addMicroCheckMapping(AllIssuesAreClosed.class);
        addMicroCheckMapping(AllLoadProfileDataCollected.class);
        addMicroCheckMapping(ConnectionPropertiesAreValid.class);
        addMicroCheckMapping(DefaultConnectionTaskAvailable.class);
        addMicroCheckMapping(DeviceIsLinkedWithUsagePoint.class);
        addMicroCheckMapping(GeneralProtocolPropertiesAreValid.class);
        addMicroCheckMapping(MetrologyConfigurationInCorrectStateIfAny.class);
        addMicroCheckMapping(NoActiveServiceCalls.class);
        addMicroCheckMapping(NoLinkedOperationalMultiElementSlaves.class);
        addMicroCheckMapping(ProtocolDialectPropertiesAreValid.class);
        addMicroCheckMapping(ScheduledCommunicationTaskAvailable.class);
        addMicroCheckMapping(SecurityPropertiesAreValid.class);
        addMicroCheckMapping(SharedScheduledCommunicationTaskAvailable.class);
        addMicroCheckMapping(SlaveDeviceHasGateway.class);
    }

    private void addMicroCheckMapping(Class<? extends MicroCheckNew> clazz) {
        microCheckMapping.put(clazz.getSimpleName(), clazz);
    }
}