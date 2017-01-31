/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ChannelType extends MeasurementType {

    TimeDuration getInterval();

    RegisterType getTemplateRegister();
}
