/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.lifecycle.device.microchecks;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SkpProcessNotRunning implements ExecutableMicroCheck {
    private static final String PROCESS_NAME = "Service key provisioning";
    private static final String PROCESS_VERSION = "1.0";
    private final Thesaurus thesaurus;
    private final BpmService bpmService;

    @Inject
    public SkpProcessNotRunning(Thesaurus thesaurus,
                                BpmService bpmService) {
        this.thesaurus = thesaurus;
        this.bpmService = bpmService;
    }

    @Override
    public String getCategory() {
        return CustomMicroCategory.MAINTENANCE.name();
    }

    @Override
    public String getCategoryName() {
        return thesaurus.getFormat(CustomMicroCategory.MAINTENANCE).format();
    }

    @Override
    public String getKey() {
        return CustomMicroCheck.SKP_NOT_RUNNING.getKey();
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(CustomMicroCheck.SKP_NOT_RUNNING).format();
    }

    @Override
    public String getDescription() {
        return thesaurus.getFormat(CustomMicroCheck.SKP_NOT_RUNNING_DESCRIPTION).format(getProcessNameAndVersion());
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return bpmService.getRunningProcesses(null, "?variableid=deviceId&variablevalue=" + device.getmRID()).processes.stream()
                .filter(item -> PROCESS_NAME.equals(item.name) && PROCESS_VERSION.equals(item.version) && item.status.equals("1"))
                .findAny()
                .map(runningProcess -> new ExecutableMicroCheckViolation(this, thesaurus.getSimpleFormat(MessageSeeds.SKP_IS_RUNNING).format(getProcessNameAndVersion())));
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.allOf(DefaultTransition.class);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof SkpProcessNotRunning
                && Objects.equals(getKey(), ((SkpProcessNotRunning) o).getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    private String getProcessNameAndVersion() {
        return PROCESS_NAME + ':' + PROCESS_VERSION;
    }
}
