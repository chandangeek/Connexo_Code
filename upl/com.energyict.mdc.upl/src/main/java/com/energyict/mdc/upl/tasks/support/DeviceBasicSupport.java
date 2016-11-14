package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.SerialNumberSupport;

/**
 * Defines functionality which is by default supported by a Device. This functionality will not always be fetched.
 */
public interface DeviceBasicSupport extends DeviceBasicTimeSupport, SerialNumberSupport {
}