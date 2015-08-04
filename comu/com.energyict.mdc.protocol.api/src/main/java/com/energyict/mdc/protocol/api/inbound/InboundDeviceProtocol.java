package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import java.util.List;

/**
 * Models the behavior that is expected by the ComServer for inbound
 * communication to detect what device is actually communicating
 * and what it is trying to tell.<p>
 * Currently, the discovery can work on binary data
 * or on data provided by servlet technology.
 * This component will indicate that to the ComServer
 * by returning the appropriate InputDataType.
 * When binary data is required, the component <strong>MUST</strong>
 * implement the {@link BinaryInboundDeviceProtocol}.
 * When servlet technology is used, the component <strong>MUST</strong>
 * implement the {@link ServletBasedInboundDeviceProtocol}.
 *
 * @since 2012-06-21 (13:34)
 */
public interface InboundDeviceProtocol extends Pluggable {

    /**
     * Indicates the type of data that was detected.
     */
    public enum DiscoverResultType {
        /**
         * The protocol detected only information that
         * uniquely identifies the device that is communication.
         */
        IDENTIFIER,

        /**
         * The protocol detected not only information that
         * uniquely identifies the device that is communication
         * but also data that was measured by that device.
         */
        DATA
    }

    /**
     * Indicates the type of data required by this protocol.
     */
    public enum InputDataType {
        /**
         * Indicates that this protocol requires binary data.
         * The protocol<strong>MUST</strong> also implement
         * the {@link BinaryInboundDeviceProtocol} interface.
         */
        BINARY,

        /**
         * Indicates that this protocol requires data provided
         * by servlet technology.
         * The protocol <strong>MUST</strong> also implement
         * the {@link ServletBasedInboundDeviceProtocol} interface.
         */
        SERVLET
    }

    /**
     * DiscoveryResponses are intended to provide a proper response to a Device
     * which initiated a session.
     */
    public enum DiscoverResponseType {

        /**
         * Indicates that the inbound discovery succeeded. If data was provided, it will be processed and stored.
         * If an outbound session should take place, it will happen in just a few moments.
         */
        SUCCESS,
        /**
         * Indicates that the inbound discovery failed. No data was provided and will therefore not be processed nor stored.
         */
        FAILURE,
        /**
         * Indicates that the provided {@link com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier} did not refer to a proper Device in our database.
         * The provided data (if any) will <b>NOT</b> be processed or stored.
         */
        DEVICE_NOT_FOUND,
        /**
         * Indicates that the requested Device was found in the database, but the Device did not expect any
         * inbound data. Processing of this data will <b>NOT</b> be done.
         */
        DEVICE_DOES_NOT_EXPECT_INBOUND,
        /**
         * Indicates that the discovered Device required that the data should have been encrypted
         * Processing of the data will <b>NOT</b> be done.
         */
        ENCRYPTION_REQUIRED,
        /**
         * Indicates that the server is running at full capacity and that their is no free resource anymore to
         * properly handle the incoming data. <b>No</b> processing will be done and the connection will terminate.
         */
        SERVER_BUSY,
        /**
         * Indicates that the server was not able to correctly store all the data
         */
        STORING_FAILURE
    }

    /**
     * Injects {@link InboundDiscoveryContext contextual information}.
     *
     * @param context The InboundDiscoveryContext
     */
    public void initializeDiscoveryContext(InboundDiscoveryContext context);

    /**
     * Gets the {@link InboundDiscoveryContext contextual information}
     * provided to this InboundDeviceProtocol at init time.
     *
     * @return The InboundDiscoveryContext
     * @see #initializeDiscoveryContext
     */
    public InboundDiscoveryContext getContext();

    /**
     * Does the actual discovery and returns the type of the received data to the framework.
     * Note that any exception that is reported by underlying communication
     * mechanisms are wrapped in a com.energyict.mdc.protocol.exceptions.CommunicationException.
     *
     * @return The type of the result, indicating if we just received the device identifier or also extra meter data.
     */
    public DiscoverResultType doDiscovery();

    /**
     * Allows the protocol to provide a descent response/feedback to the actual Device after we processed the data.
     * Based on the given <code>responseType</code> you can provide different feedback.
     *
     * @param responseType the responseType based on the processed data ({@link DiscoverResultType discoverResultType} and additional data)
     */
    public void provideResponse(DiscoverResponseType responseType);

    /**
     * Returns the unique identifier of the device that set up the inbound connection
     *
     * @return The unique device identifier
     */
    public DeviceIdentifier getDeviceIdentifier();

    /**
     * Returns the data (registers, load profile entries, events, ...)
     * that was received (inbound) from the device.
     * Note that when only identification information was detected,
     * the protocol should return an empty list instead of <code>null</code>.
     *
     * @param device the offline version of the device which is discovered
     * @return The CollectedData or an empty list when no data was detected
     */
    public List<CollectedData> getCollectedData(OfflineDevice device);

}