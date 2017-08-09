package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Structure to specify the attributes to be used when performing a filtered search on DeviceMessages.
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
     * List of device message categories used in the query. If a category is mentioned without further selection of
     * DeviceCommands from that category, all device commands in the category will be used in the search.
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

    /**
     * Indicates filtered results will only contain device messages whose release date is present AND is after the mentioned timestamp
     * @return
     */
    Optional<Instant> getReleaseDateStart();

    /**
     * Indicates filtered results will only contain device messages whose release date is present AND is before the mentioned timestamp
     * @return
     */
    Optional<Instant> getReleaseDateEnd();

    /**
     * Indicates filtered results will only contain device messages whose sent date is present AND is after the mentioned timestamp
     * @return
     */
    Optional<Instant> getSentDateStart();

    /**
     * Indicates filtered results will only contain device messages whose sent date is present AND is before the mentioned timestamp
     * @return
     */
    Optional<Instant> getSentDateEnd();

    /**
     * Indicates filtered results will only contain device messages whose creation date is present AND is after the mentioned timestamp
     * @return
     */
    Optional<Instant> getCreationDateStart();

    /**
     * Indicates filtered results will only contain device messages whose creation date is present AND is before the mentioned timestamp
     * @return
     */
    Optional<Instant> getCreationDateEnd();

}
