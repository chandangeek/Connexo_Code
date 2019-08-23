/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTask;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface InboundConnectionTask extends ConnectionTask<InboundComPortPool, PartialInboundConnectionTask> {
}