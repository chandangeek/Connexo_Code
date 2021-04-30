package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ConsumerType;

/**
 * Defines proper functionality to collect Status information of a Device.
 */
@ConsumerType
public interface DeviceStatusInformationSupport extends DeviceFirmwareSupport, DeviceBreakerStatusSupport, DeviceCreditAmountSupport, CalendarSupport {

}
