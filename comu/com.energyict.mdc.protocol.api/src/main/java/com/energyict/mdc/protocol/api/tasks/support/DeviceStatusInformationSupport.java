/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import aQute.bnd.annotation.ProviderType;

/**
 * Defines proper functionality to collect Status information of a Device.
 */
@ProviderType
public interface DeviceStatusInformationSupport extends DeviceFirmwareSupport, DeviceBreakerStatusSupport, CalendarSupport {
}