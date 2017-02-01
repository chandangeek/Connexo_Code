/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface TimeOfUseOptions {

    void setOptions(Set<ProtocolSupportedCalendarOptions> allowedOptions);

    Set<ProtocolSupportedCalendarOptions> getOptions();

    void save();

    void delete();

    long getVersion();
}
