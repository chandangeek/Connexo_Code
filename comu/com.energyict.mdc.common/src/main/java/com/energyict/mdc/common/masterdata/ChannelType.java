/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.masterdata;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface ChannelType extends MeasurementType {

    TimeDuration getInterval();

    RegisterType getTemplateRegister();
}
