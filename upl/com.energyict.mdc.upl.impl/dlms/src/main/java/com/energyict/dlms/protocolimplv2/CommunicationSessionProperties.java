package com.energyict.dlms.protocolimplv2;

import com.energyict.cpo.TypedProperties;

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
    public long getTimeout();

    /**
     * The number of retries
     */
    public int getRetries();

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    public long getForcedDelay();

    /**
     * Add properties
     */
    public void addProperties(TypedProperties properties);

    /**
     * Return all properties
     */
    public TypedProperties getProperties();

    /**
     * The client MAC address.
     * This is used as the source field
     */
    public int getClientMacAddress();

    /**
     * The logical device address (usually 1).
     * This addresses a separate entity within a physical device.
     * This is used as the destination field for TCP.
     */
    public int getServerUpperMacAddress();

    /**
     * The physical device address. Only used for HDLC.
     * This addresses a physical device on the multi-drop.
     */
    public int getServerLowerMacAddress();

    /**
     * Used for HDLC only. Can be 1, 2 or 4.
     * It is the number of bytes that is used to represent the upper and lower HDLC addresses in a frame.
     */
    public int getAddressingMode();

    /**
     * Used for HDLC only.
     * The size of the information field (the field that contains application data) in a HDLC frame.
     * This will be negotiated with the device.
     */
    public int getInformationFieldSize();

    /**
     * Property that swaps the client and server address in the TCP header.
     */
    public boolean isSwitchAddresses();

    /**
     * For the HDLC connection only.
     * Use this for meters that require a specific parameter length in the SNRM frame (e.g. Flex, Iskra Mx382)
     * In case of type 1, a specific parameter length will be used, else, the defaults are used.
     */
    public int getSNRMType();

    /**
     * The initial baud rate to start the IEC1107 signon with
     */
    public int getHHUSignonBaudRateCode();

    /**
     * Boolean indicating whether or not the protocol supports (and can use) general block transfer
     */
    public boolean useGeneralBlockTransfer();

    /**
     * Getter for the general block transfer window size<br/>
     * This is the preferred window size of our head-end (ComServer) and should match the devices capabilities.
     */
    public int getGeneralBlockTransferWindowSize();

    /**
     * Indicate if the framework executing the protocols prefers the usage of polling on the inputstream or not.
     * E.g. the RTU+Server manages its thread performance itself, and prefers the protocols to avoid polling.
     * <p/>
     * Polling: check inputstream.available periodically, read number of bytes if available.
     * Non polling: use blocking read calls on the inputstream without checking
     */
    public boolean isUsePolling();
}
