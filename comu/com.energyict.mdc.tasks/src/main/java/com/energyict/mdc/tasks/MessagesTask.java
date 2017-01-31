/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.List;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which will execute Messages for a Device.
 * <p>
 * The task can contain an optional list of {@link DeviceMessageCategory deviceMessageCategories} and
 * an optional list of {@link DeviceMessageSpec}s,
 * which means that only {@link DeviceMessage deviceMessages} who are in the defined
 * categories or specs (and if pending on the Device) will be executed during communication. If no lists are provided, then <b>all</b>
 * defined {@link DeviceMessage deviceMessages} which are pending on the Device will be executed.
 * </p>
 *
 * @author gna
 * @since 19/04/12 - 15:09
 */
public interface MessagesTask extends ProtocolTask {

    public enum MessageTaskType {
        NONE,
        ALL,
        SELECTED
    }

    /**
     * Return a list of {@link DeviceMessageCategory} which <i>can</i> be executed during this task.
     * If no types are defined, then an empty list will be returned.
     *
     * @return the list of {@link DeviceMessageCategory} which are defined for this task
     */
    List<DeviceMessageCategory> getDeviceMessageCategories();
    void setDeviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories);

    /**
     * Gets the current MessageTaskType
     *
     * @return the MessageTaskType
     */
    MessageTaskType getMessageTaskType();
    void setMessageTaskType(MessageTaskType messageTaskType);

    interface MessagesTaskBuilder {

        /**
         * Sets the MessageTaskType.
         * Setting the type to {@link MessageTaskType#ALL} or {@link MessageTaskType#NONE} will clear the current categories.
         *
         * @param messageTaskType the current type to set
         * @return this builder
         */
        public MessagesTaskBuilder setMessageTaskType(MessageTaskType messageTaskType);

        /**
         * Sets the given deviceMessageCategories.
         * By setting deviceMessageCategories, you the MessageTaskType will automatically be set to {@link MessageTaskType#SELECTED}
         *
         * @param deviceMessageCategories the categories to set
         * @return the builder
         */
        public MessagesTaskBuilder deviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories);

        /**
         * Returns the object which was <i>constructed</i> with this builder
         *
         * @return the object
         */
        public MessagesTask add();
    }

}