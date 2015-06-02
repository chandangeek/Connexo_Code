package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

/**
 * Represents a mapping between a {@link com.energyict.mdc.tasks.MessagesTask} and either a
 * <ul>
 *     <li>{@link DeviceMessageCategory}</li>
 *     <li>{@link DeviceMessageSpec}</li>
 * </ul>
 *
 * Copyrights EnergyICT
 * Date: 26/02/13
 * Time: 14:50
 */
public interface MessagesTaskTypeUsage extends HasId {

    public long getId();

    public DeviceMessageCategory getDeviceMessageCategory();

}