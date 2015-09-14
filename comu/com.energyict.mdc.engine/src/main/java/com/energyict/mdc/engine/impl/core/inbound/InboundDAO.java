package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.util.List;
import java.util.Optional;

/**
 * Models the behavior of a component that provides access to
 * data that is relevant to {@link InboundDeviceProtocol}s.
 * Implementation classes will focus on the actual data source containing the data.
 * and therefore hiding these implementation details from the InboundDeviceProtocol.
 * <p>
 * The implementation classes are allowed to throw com.energyict.comserver.core.interfaces.DataAccessException(s)
 * to report severe problems that relate to the actual data source.
 * <p>
 * Refer to java website for a complete discussion on the
 * <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Object design pattern</a>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-06-26 (12:02)
 */
public interface InboundDAO {

    /**
     * Confirms the correct receipt of previously sent {@link OfflineDeviceMessage}s
     * and returns pending messages that need to be sent.
     *
     *
     * @param deviceIdentifier The DeviceIdentifier that uniquely identifies
     *                         the {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     * @param confirmationCount The number of previously sent message that have been confirmed
     *                          by the device as correctly received (and executed)
     * @return The pending RtuMessages that are waiting to be sent
     */
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier<Device> deviceIdentifier, int confirmationCount);

    /**
     * Gets the {@link SecurityProperty security properties} that have been
     * created against the Device that is currently connected to the ComServer
     * via the specified {@link InboundComPort}.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @param inboundComPort The InboundComPort
     * @return The List of SecurityProperty or null if the Device is not ready for inbound communication
     */
    public List<SecurityProperty> getDeviceProtocolSecurityProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);

    /**
     * Gets the {@link TypedProperties} that have been
     * created against the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that is currently used to connect the Device to the ComServer
     * via the specified {@link InboundComPort}.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @param inboundComPort The InboundComPort
     * @return The TypedProperties or <code>null</code> if the Device is not ready for inbound communication
     */
    public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort);

    /**
     * Gets the {@link TypedProperties} of the Device that relate
     * to the protocol that is used to communicate with that Device.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties that relate to the Device's protocol
     *         or <code>null</code> if the Device does not exist
     */
    public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier);

    /**
     * Finds the {@link com.energyict.mdc.protocol.api.device.BaseDevice} that is uniquely identified
     * by the specified {@link DeviceIdentifier}.
     *
     * @param identifier The DeviceIdentifier
     * @return The offline version of the Device that is identified by the DeviceIdentifier
     */
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier<?> identifier);

}