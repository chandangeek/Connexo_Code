package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.util.Collection;

/**
 * Structure to specify DeviceMessageQuery
 */
public interface DeviceMessageQueryFilter {
    /**
     * List of device groups whose devices will be used in the query.
     * If {@link EndDeviceGroup}s are returned by this method, only DeviceMessages associated with a device in any of the
     * device groups will be returned
     * @return EndDeviceGroups to be used in DeviceMessage query
     */
    Collection<EndDeviceGroup> getDeviceGroups();

    /**
     * List of device message categories used in the query.
     */
    Collection<DeviceMessageCategory> getMessageCategories();

    /**
     * List of specific device messages to be used in the query. If a message category is specified without further
     * selection of deviceMessages from that category, all device messages from the category are used in the query. If a
     * DeviceMessageId in present in this list, the corresponding DeviceMessageCategory has to be present in
     * getMessageCategories()
     * @return
     */
    Collection<DeviceMessageId> getDeviceMessages();

    /**
     * List of {@link DeviceMessageStatus}s to be used in the filter. Only DeviceMessages in the requested status will
     * be returned
     * @return
     */
    Collection<DeviceMessageStatus> getStatuses();
}
