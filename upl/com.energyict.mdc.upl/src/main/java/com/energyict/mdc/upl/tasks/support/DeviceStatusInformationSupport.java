package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;

/**
 * Defines proper functionality to collect Status information of a Device.
 */
@ProviderType
public interface DeviceStatusInformationSupport extends DeviceFirmwareSupport, DeviceBreakerStatusSupport, DeviceCreditAmountSupport, CalendarSupport {

}