package com.energyict.mdc.device.data;

import java.util.Date;

/**
 * Created by tgr on 9/09/2014.
 */
public interface DeviceValidation {

    Device getDevice();

    boolean isValidationActive(Date when);

    boolean isValidationActive(Channel channel, Date when);
}
