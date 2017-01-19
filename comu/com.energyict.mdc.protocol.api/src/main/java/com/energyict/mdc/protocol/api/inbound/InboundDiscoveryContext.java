package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.crypto.Cryptographer;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides contextual information to an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (12:55)
 */
public interface InboundDiscoveryContext {

    Logger getLogger();

    void setLogger(Logger logger);

    Cryptographer getCryptographer();

    void setCryptographer(Cryptographer cryptographer);

    ComChannel getComChannel();

    HttpServletRequest getServletRequest();

    void setServletRequest(HttpServletRequest servletRequest);

    HttpServletResponse getServletResponse();

    void setServletResponse(HttpServletResponse servletResponse);

    /**
     * Confirms the correct receipt of previously sent {@link OfflineDeviceMessage}s
     * and returns pending messages that need to be sent.
     *
     * @param deviceIdentifier  The DeviceIdentifier that uniquely identifies the Device
     * @param confirmationCount The number of previously sent message that have been confirmed
     *                          by the device as correctly received (and executed)
     * @return The pending RtuMessages that are waiting to be sent
     */
    List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount);

    /**
     * Gets the {@link SecurityProperty security properties} that have been
     * created against the Device that is currently connected to the ComServer.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The List of SecurityProperty or null if the Device is not ready for inbound communication
     */
    List<SecurityProperty> getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Gets the {@link TypedProperties} that have been
     * created against the ConnectionTask that is currently
     * used to connect the Device to the ComServer.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties or <code>null</code> if the Device is not ready for inbound communication
     */
    TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Gets the {@link TypedProperties} of the Device that relate
     * to the protocol that is used to communicate with that Device.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties that relate to the Device's protocol
     * or <code>null</code> if the Device does not exist
     */
    TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier);

    void markNotAllCollectedDataWasProcessed();

}