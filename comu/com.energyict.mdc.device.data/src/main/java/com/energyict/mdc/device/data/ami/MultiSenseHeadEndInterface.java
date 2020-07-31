/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MultiSense specific variant of the {@link HeadEndInterface}
 *
 * @author sva
 * @since 1/07/2016 - 15:19
 */
@ProviderType
public interface MultiSenseHeadEndInterface extends HeadEndInterface {

    CompletionOptions runCommunicationTask(Device device, List<ComTaskExecution> comTasks, Instant instant);

    CompletionOptions runCommunicationTask(Device device, List<ComTaskExecution> comTasks, Instant instant, ServiceCall parentServiceCall);

    void scheduleRequiredComTasks(Device multiSenseDevice, List<DeviceMessage> deviceMessages);

    Optional<? extends ICommandServiceCallDomainExtension> getCommandServiceCallDomainExtension(ServiceCall serviceCall);

}
