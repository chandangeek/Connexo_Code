/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.time.Instant;
import java.util.List;

/**
 * MultiSense specific variant of the {@link HeadEndInterface}
 *
 * @author sva
 * @since 1/07/2016 - 15:19
 */
public interface MultiSenseHeadEndInterface extends HeadEndInterface {

    CompletionOptions runCommunicationTask(Device device, List<ComTaskExecution> comTasks, Instant instant);

    CompletionOptions runCommunicationTask(Device device, List<ComTaskExecution> comTasks, Instant instant, ServiceCall parentServiceCall);

}
