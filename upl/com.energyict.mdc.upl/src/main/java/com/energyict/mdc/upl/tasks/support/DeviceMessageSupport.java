package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;

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
     * Request the protocol implementation to prepare and add extra context to the message.
     * This context is kept in a String field on the offlineDeviceMessage instance, so it is available to the message executor.
     * <p/>
     * Note that this method is called in the "going offline" phase of the DeviceMessage. This always runs in the online comserver environment,
     * meaning that the implementors can use factories to access extra information from the EIServer database.
     */
    String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage);

}