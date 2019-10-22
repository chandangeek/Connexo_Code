/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import java.util.Objects;

public class DeviceLifeCycleStateFactory {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final MeteringTranslationService meteringTranslationService;

    @Inject
    public DeviceLifeCycleStateFactory(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,MeteringTranslationService meteringTranslationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.meteringTranslationService = meteringTranslationService;
    }

    public DeviceLifeCycleStateInfo from(DeviceLifeCycle deviceLifeCycle, State state) {
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(state);
        return new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, deviceLifeCycle, state, meteringTranslationService);
    }

}