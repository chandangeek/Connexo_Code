package com.energyict.dlms.protocolimplv2;

import com.energyict.mdc.upl.properties.TypedProperties;

import java.time.Duration;
import java.util.Properties;

/**
 * Contains access to all properties to configure a connection layer (TCP, HDLC,...)
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/02/12
 * Time: 15:08
 */
public interface CommunicationSessionProperties {

    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    long getTimeout();

    /**
     * The number of retries
     */
    int getRetries();

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    long getForcedDelay();

    /**
     * Add properties
     */
    void addProperties(TypedProperties properties);

    /**
     * Return all properties
     */
    TypedProperties getProperties();

    default void setProperties(Properties properties) {
        this.addProperties(com.energyict.mdc.upl.TypedProperties.copyOf(properties));
    }

    /**
     * The client MAC address.
     * This is used as the source field
     */
    int getClientMacAddress();

    /**
     * The logical device address (usually 1).
     * This addresses a separate entity within a physical device.
     * This is used as the destination field for TCP.
     */
    int getServerUpperMacAddress();

    /**
     * The physical device address. Only used for HDLC.
     * This addresses a physical device on the multi-drop.
     */
    int getServerLowerMacAddress();

    /**
     * Used for HDLC only. Can be 1, 2 or 4.
     * It is the number of bytes that is used to represent the upper and lower HDLC addresses in a frame.
     */
    int getAddressingMode();

    /**
     * Used for HDLC only.
     * The size of the information field (the field that contains application data) in a HDLC frame.
     * This will be negotiated with the device.
     */
    int getInformationFieldSize();

    /**
     * Property that swaps the client and server address in the TCP header.
     */
    boolean isSwitchAddresses();

    /**
     * For the HDLC connection only.
     * Use this for meters that require a specific parameter length in the SNRM frame (e.g. Flex, Iskra Mx382)
     * In case of type 1, a specific parameter length will be used, else, the defaults are used.
     */
    int getSNRMType();

    /**
     * The initial baud rate to start the IEC1107 signon with
     */
    int getHHUSignonBaudRateCode();

    /**
     * Boolean indicating whether or not the protocol supports (and can use) general block transfer
     */
    boolean useGeneralBlockTransfer();

    /**
     * Getter for the general block transfer window size<br/>
     * This is the preferred window size of our head-end (ComServer) and should match the devices capabilities.
     */
    int getGeneralBlockTransferWindowSize();

    /**
     * The delay (in milliseconds) that the connection layer should wait between the polling on the inputstream (for responses).
     * If set to 0, no polling will be used. In this case, the reading of responses is event-driven.
     * <p/>
     * E.g. the RTU+Server manages its thread performance itself, and prefers the protocols to avoid polling.
     * <p/>
     * Polling: check inputstream.available periodically, read number of bytes if available.
     * Non polling: use blocking read calls on the inputstream without checking periodically.
     */
    Duration getPollingDelay();

    /**
     * Indicate if a timeout error means that the connection should be considered as 'broken'.
     * This has impact on the next physical slaves that re-use the same connection.
     * <p/>
     * Default is true, specific protocols can override this.
     */
    boolean timeoutMeansBrokenConnection();

    /**
     * Getter for boolean indicating whether the frame counter should be increased for each retry request
     * or whether the same frame counter should be used for all retries
     */
    boolean incrementFrameCounterForRetries();
}
