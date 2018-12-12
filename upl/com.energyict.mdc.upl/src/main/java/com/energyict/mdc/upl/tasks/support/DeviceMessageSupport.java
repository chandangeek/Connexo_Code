package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to handle the {@link DeviceMessageSpec}s.
 */
public interface DeviceMessageSupport {

    /**
     * @return a <code>List</code> of protocol supported {@link DeviceMessageSpec deviceMessages}
     */
    List<DeviceMessageSpec> getSupportedMessages();

    /**
     * Handle all given <code>OfflineDeviceMessage</code>.
     * Each OfflineDeviceMessage should result in a proper CollectedMessage.
     * The CollectedMessage should be added to the returning CollectedMessageList.
     * <p/>
     * If an OfflineDeviceMessage fails due to Business logic <i>(ex. disconnect on a Device which doesn't have a breaker,
     * firmwareUpgrade fails due to receiving incorrect blocks, ...)</i>, then make sure the
     * {@link CollectedMessage#getNewDeviceMessageStatus()
     * CollectedMessage#getNewDeviceMessageStatus()} returns the proper new status (ex. failure).
     * If possible also return a proper {@link CollectedMessage#getDeviceProtocolInformation()
     * CollectedMessage#getDeviceProtocolInformation()}.
     * <p/>
     * If an OfflineDeviceMessage fails for any other reason <i>(ex. timeout, closed connection, ...)</i>,
     * which could lead to failing all other messages, then set the proper failure information
     * on the returning {@link CollectedMessageList}, the individual messages should <i>not</i> be
     * marked as failed.
     *
     * @param pendingMessages the pending messages on a Device
     * @return Message results for the provided pending messages.
     */
    CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages);

    /**
     * Handle all given {@link OfflineDeviceMessage} which have previously been sent to the device.
     * Each OfflineDeviceMessage should result in a proper CollectedMessage.
     * The CollectedMessage should be added to the returning CollectedMessageList.
     * <p/>
     * If an OfflineDeviceMessage fails due to Business logic <i>(ex. disconnect on a Device which doesn't have a breaker,
     * firmwareUpgrade fails due to receiving incorrect blocks, ...)</i>, then make sure the
     * {@link CollectedMessage#getNewDeviceMessageStatus()
     * CollectedMessage#getNewDeviceMessageStatus()} returns the proper new status (ex. failure).
     * If possible also return a proper {@link CollectedMessage#getDeviceProtocolInformation()
     * CollectedMessage#getDeviceProtocolInformation()}.
     * <p/>
     * If an OfflineDeviceMessage fails for any other reason <i>(ex. timeout, closed connection, ...)</i>,
     * which could lead to failing all other messages, then set the proper failure information
     * on the returning {@link CollectedMessageList}, the individual messages should <i>not</i> be
     * marked as failed.
     *
     * @param sentMessages the sent messages
     * @return CollectedData containing update information about the sent messages
     */
    CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages);

    /**
     * Requests to format the given messageAttribute to a proper format which is <b>known</b>
     * by the {@link com.energyict.mdc.upl.DeviceProtocol}.
     * When the framework will request to perform a certain message, then the formatted
     * values will be delivered to the DeviceProtocol.
     *
     * @param offlineDevice        the offlineDevice for which the message will be executed
     * @param offlineDeviceMessage the offline message that
     * @param propertySpec         the spec defining the type of the attribute
     * @param messageAttribute     the messageAttribute value that needs to be formatted.
     * @return a properly formatted version of the given messageAttribute
     */
    String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute);

    /**
     * Request this protocol to prepare {@link OfflineDeviceMessage#getPreparedContext() extra context} for the message
     * that is being taken offline.
     *
     * @param device The Device for which the DeviceMessage is intended
     * @param offlineDevice The offline version of the Device
     * @param deviceMessage The DeviceMessage
     * @return The prepared context or <code>Optional.empty()</code> if there was nothing to prepare
     */
    Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage);

}