package com.energyict.mdc.task.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.task.ProtocolTask;

/**
 * Represents a mapping between a {@link com.energyict.mdc.task.MessagesTask} and either a
 * <ul>
 *     <li>{@link DeviceMessageCategory}</li>
 *     <li>{@link DeviceMessageSpec}</li>
 * </ul>
 *
 * Copyrights EnergyICT
 * Date: 26/02/13
 * Time: 14:50
 */
public interface MessagesTaskTypeUsage {

    public long getId();

    public DeviceMessageSpec getDeviceMessageSpec();

    public DeviceMessageCategory getDeviceMessageCategory();

    public void setProtocolTask(ProtocolTask protocolTask);

    public void setDeviceMessageSpec(DeviceMessageSpec deviceMessageSpec);

    public void setDeviceMessageCategory(DeviceMessageCategory deviceMessageCategory);

    public ProtocolTask getProtocolTask();

    boolean hasDeviceMessageCategory();

    boolean hasDeviceMessageSpec();
}