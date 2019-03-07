/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.lifecycle.DefaultMicroCheck;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DeviceMicroCheckFactoryImpl implements DeviceMicroCheckFactory {
    private final DataModel dataModel;
    private final Map<String, Class<? extends MicroCheckNew>> microCheckMapping = new HashMap<>();
    private final EnumMap<DefaultMicroCheck, Class<? extends MicroCheckNew>> defaultCheckMapping = new EnumMap<>(DefaultMicroCheck.class);

    @Inject
    public DeviceMicroCheckFactoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        addMicroCheckMappings();
    }

    @Override
    public Optional<? extends MicroCheckNew> from(String microCheckKey) {
        return Optional.ofNullable(microCheckMapping.get(microCheckKey))
                .map(dataModel::getInstance);
    }

    public MicroCheckNew from(DefaultMicroCheck defaultMicroCheck) {
        return Optional.ofNullable(defaultCheckMapping.get(Objects.requireNonNull(defaultMicroCheck)))
                .map(dataModel::getInstance)
                .orElseThrow(() -> new IllegalStateException("There is no check for " + DefaultMicroCheck.class.getSimpleName() + '#' + defaultMicroCheck.name()));
    }

    @Override
    public Set<? extends MicroCheckNew> getAllChecks() {
        return microCheckMapping.values().stream()
                .map(dataModel::getInstance)
                .collect(Collectors.toSet());
    }

    private void addMicroCheckMappings() {
        addMicroCheckMapping(DefaultMicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE, ActiveConnectionAvailable.class);
        addMicroCheckMapping(DefaultMicroCheck.ALL_DATA_VALID, AllDataValid.class);
        addMicroCheckMapping(DefaultMicroCheck.ALL_DATA_VALIDATED, AllDataValidated.class);
        addMicroCheckMapping(DefaultMicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, AllIssuesAreClosed.class);
        addMicroCheckMapping(DefaultMicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED, AllLoadProfileDataCollected.class);
        addMicroCheckMapping(DefaultMicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID, ConnectionPropertiesAreValid.class);
        addMicroCheckMapping(DefaultMicroCheck.DEFAULT_CONNECTION_AVAILABLE, DefaultConnectionTaskAvailable.class);
        addMicroCheckMapping(DefaultMicroCheck.LINKED_WITH_USAGE_POINT, DeviceIsLinkedWithUsagePoint.class);
        addMicroCheckMapping(DefaultMicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID, GeneralProtocolPropertiesAreValid.class);
        addMicroCheckMapping(DefaultMicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY, MetrologyConfigurationInCorrectStateIfAny.class);
        addMicroCheckMapping(DefaultMicroCheck.NO_ACTIVE_SERVICE_CALLS, NoActiveServiceCalls.class);
        addMicroCheckMapping(DefaultMicroCheck.NO_LINKED_MULTI_ELEMENT_SLAVES, NoLinkedOperationalMultiElementSlaves.class);
        addMicroCheckMapping(DefaultMicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID, ProtocolDialectPropertiesAreValid.class);
        addMicroCheckMapping(DefaultMicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, ScheduledCommunicationTaskAvailable.class);
        addMicroCheckMapping(DefaultMicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID, SecurityPropertiesAreValid.class);
        addMicroCheckMapping(DefaultMicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE, SharedScheduledCommunicationTaskAvailable.class);
        addMicroCheckMapping(DefaultMicroCheck.SLAVE_DEVICE_HAS_GATEWAY, SlaveDeviceHasGateway.class);
        addMicroCheckMapping(DefaultMicroCheck.AT_LEAST_ONE_ZONE_LINKED, ZonesLinkedToDevice.class);
    }

    private void addMicroCheckMapping(DefaultMicroCheck defaultMicroCheck, Class<? extends MicroCheckNew> clazz) {
        microCheckMapping.put(clazz.getSimpleName(), clazz);
        defaultCheckMapping.put(defaultMicroCheck, clazz);
    }
}
