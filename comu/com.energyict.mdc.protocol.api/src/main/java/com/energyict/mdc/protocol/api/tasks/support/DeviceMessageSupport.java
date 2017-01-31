/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;
import java.util.Set;

/**
 * Provides functionality to handle the {@link DeviceMessageSpec}s.
 */
public interface DeviceMessageSupport {

    /**
     * @return a <code>List</code> of protocol supported {@link DeviceMessageSpec deviceMessages}
     */
    public Set<DeviceMessageId> getSupportedMessages();

    /**
     * Handle all given <code>OfflineDeviceMessage</code>.
     * Each OfflineDeviceMessage should result in a proper CollectedMessage.
     * The CollectedMessage should be added to the returning CollectedMessageList.
     * <p/>
     * If an OfflineDeviceMessage fails due to Business logic <i>(ex. disconnect on a Device which doesn't have a breaker,
     * firmwareUpgrade fails due to receiving incorrect blocks, ...)</i>, then make sure the
     * {@link com.energyict.mdc.protocol.api.device.data.CollectedMessage#getNewDeviceMessageStatus()
     * CollectedMessage#getNewDeviceMessageStatus()} returns the proper new status (ex. failure).
     * If possible also return a proper {@link com.energyict.mdc.protocol.api.device.data.CollectedMessage#getDeviceProtocolInformation()
     * CollectedMessage#getDeviceProtocolInformation()}.
     * <p/>
     * If an OfflineDeviceMessage fails for any other reason <i>(ex. timeout, closed connection, ...)</i>,
     * which could lead to failing all other messages, then set the proper failure information
     * on the returning {@link CollectedMessageList}, the individual messages should <i>not</i> be
     * marked as failed.<p/>
     * <b>Note:</b> The provided list of {@link OfflineDeviceMessage}s should already be sorted
     * (by default sorted on release date), so it is crucial to handle the messages in the same
     * order as they are specified in the list.
     *
     * Note2: attributes of type FirmwareVersion will be modified by the ComServer framework!
     * The contents of the FirmwareVersion will be written to a temp file and the messageAttribute
     * will be changed to the path to this temp file!
     *
     * @param pendingMessages the pending messages on a Device
     * @return Message results for the provided pending messages.
     */
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages);

    /**
     * Handle all given {@link OfflineDeviceMessage} which have previously been sent to the device.
     * Each OfflineDeviceMessage should result in a proper CollectedMessage.
     * The CollectedMessage should be added to the returning CollectedMessageList.
     * <p/>
     * If an OfflineDeviceMessage fails due to Business logic <i>(ex. disconnect on a Device which doesn't have a breaker,
     * firmwareUpgrade fails due to receiving incorrect blocks, ...)</i>, then make sure the
     * {@link com.energyict.mdc.protocol.api.device.data.CollectedMessage#getNewDeviceMessageStatus()
     * CollectedMessage#getNewDeviceMessageStatus()} returns the proper new status (ex. failure).
     * If possible also return a proper {@link com.energyict.mdc.protocol.api.device.data.CollectedMessage#getDeviceProtocolInformation()
     * CollectedMessage#getDeviceProtocolInformation()}.
     * <p/>
     * If an OfflineDeviceMessage fails for any other reason <i>(ex. timeout, closed connection, ...)</i>,
     * which could lead to failing all other messages, then set the proper failure information
     * on the returning {@link CollectedMessageList}, the individual messages should <i>not</i> be
     * marked as failed.
     * <p/>
     * <b>Note:</b> The provided list of {@link OfflineDeviceMessage}s should already be sorted
     * (by default sorted on release date), so it is crucial to handle the messages in the same
     * order as they are specified in the list.
     *
     * @param sentMessages the sent messages
     * @return CollectedData containing update information about the sent messages
     */
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages);

    /**
     * Requests to format the given messageAttribute to a proper format which is <b>known</b>
     * by the {@link DeviceProtocol deviceProtocol}.
     * When the framework will request to perform a certain message, then the formatted
     * values will be delivered to the DeviceProtocol.
     *
     * Note: attributes of type FirmwareVersion will be modified by the ComServer framework!
     * The contents of the FirmwareVersion will be written to a temp file and the messageAttribute
     * will be changed to the path to this temp file!
     *
     * @param propertySpec     the spec defining the type of the attribute
     * @param messageAttribute the messageAttribute value that needs to be formatted.
     * @return a properly formatted version of the given messageAttribute
     */
    public String format(PropertySpec propertySpec, Object messageAttribute);

}