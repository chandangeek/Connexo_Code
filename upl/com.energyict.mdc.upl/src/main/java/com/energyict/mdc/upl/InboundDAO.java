package com.energyict.mdc.upl;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.List;

/**
 * Models the behavior of a component that provides access to
 * data that is relevant to {@link InboundDeviceProtocol}s.
 * Implementation classes will focus on the actual data source containing the data.
 * and therefore hiding these implementation details from the InboundDeviceProtocol.
 * <p/>
 * The implementation classes are allowed to throw com.energyict.comserver.core.interfaces.DataAccessException(s)
 * to report severe problems that relate to the actual data source.
 * <p/>
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
     * @param deviceIdentifier  The DeviceIdentifier that uniquely identifies the Device
     * @param confirmationCount The number of previously sent message that have been confirmed
     *                          by the device as correctly received (and executed)
     * @return The pending RtuMessages that are waiting to be sent
     */
    List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount);

    /**
     * Get the properties of the default (or first if there's no default) outbound connection task
     */
    TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Gets all the {@link TypedProperties} of the Device that relate
     * to the protocol that is used to communicate with that Device.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties that relate to the Device's protocol
     * or <code>null</code> if the Device does not exist
     */
    TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Gets only the local{@link TypedProperties} of the Device that relate
     * to the protocol that is used to communicate with that Device.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties that relate to the Device's protocol
     * or <code>null</code> if the Device does not exist
     */
    TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Creates an offline version of the given device based on the given context.
     * Throw an exception if the device does not uniquely exist.
     */
    OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context);

    /**
     * Return the java class name of the DeviceProtocol of the given device
     */
    String getDeviceProtocolClassName(DeviceIdentifier identifier);

}