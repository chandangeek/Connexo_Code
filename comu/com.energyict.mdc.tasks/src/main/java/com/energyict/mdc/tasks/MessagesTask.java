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

    /**
     * Return a list of {@link DeviceMessageCategory} which <i>can</i> be executed during this task.
     * If no types are defined, then an empty list will be returned.
     *
     * @return the list of {@link DeviceMessageCategory} which are defined for this task
     */
    public List<DeviceMessageCategory> getDeviceMessageCategories();
    public void setDeviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories);

    /**
     * Return a list of {@link DeviceMessageSpec}s which <i>can</i> be executed during this task.
     *
     * @return the list of {@link DeviceMessageSpec}s which are defined for this task
     */
    public List<DeviceMessageSpec> getDeviceMessageSpecs();
    public void setDeviceMessageSpecs(List<DeviceMessageSpec> deviceMessageSpecs);

    /**
     * Returns true if all message categories can be executed during this task
     * @return boolean
     */
    public boolean isAllCategories();
    public void setAllCategories(boolean allCategories);

    interface MessagesTaskBuilder {
        public MessagesTaskBuilder deviceMessageCategories(List<DeviceMessageCategory> deviceMessageCategories);
        public MessagesTaskBuilder deviceMessageSpecs(List<DeviceMessageSpec> deviceMessageSpecs);
        public MessagesTaskBuilder allCategories();
        public MessagesTask add();
    }

}