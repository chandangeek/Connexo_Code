package com.energyict.dlms.protocolimplv2;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.util.TimeZone;

/**
 * Contains access to all properties to configure a DLMS session and its connection layers (TCP, HDLC,...)
 * <p>
 * Copyrights EnergyICT
 * Date: 8/02/12
 * Time: 15:08
 */
public interface DlmsSessionProperties extends HasDynamicProperties, CommunicationSessionProperties {

    /**
     * The device timezone
     */
    TimeZone getTimeZone();

    /**
     * The DLMS reference. Can be SN (short name) or LN (logical name)
     */
    DLMSReference getReference();

    /**
     * The security set of a device. It contains all properties related to security.
     */
    DeviceProtocolSecurityPropertySet getSecurityPropertySet();

    /**
     * Setter for the device's security set
     */
    void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

    /**
     * Getter for the authentication level. Can be 0 (none), 1 (password), 2 (manufacturer specific), 3 (MD5), 4 (SHA1) or 5(GMAC).
     * The authentication is done while setting up the DLMS association to a device.
     */
    int getAuthenticationSecurityLevel();

    /**
     * Getter for the data transport security level. Can be 0 (no security), 1 (authentication), 2 (encryption) or 3 (both)
     */
    int getDataTransportSecurityLevel();

    /**
     * Indicates the manufacturer.
     * This can be used to choose from a set of hard coded object lists instead of reading out the device's full object list.
     */
    String getManufacturer();

    /**
     * Indicates if a wakeup should happen before starting the communication with the device.
     * This can be a CSD call or an SMS to trigger the device's GPRS connection.
     */
    boolean isWakeUp();

    /**
     * Getter for the device ID.
     * This can be used for the HHU sign on.
     */
    String getDeviceId();

    /**
     * The type of ciphering. Can be global or dedicated.
     * Depending on the type, either the global encryption key or the dedicated key is used.
     */
    CipheringType getCipheringType();

    /**
     * Indicates if the device to communicate with is the NTA simulation tool.
     * This will put the configured serial number in as the calledAPtitle in the application association request.
     * This way, the simulation tool knows to which device in its database we want to communicate.
     */
    boolean isNtaSimulationTool();

    /**
     * Indicates if the protocol is allowed to use bulk requests.
     * A bulk request reads out multiple attributes in a single request.
     */
    boolean isBulkRequest();

    /**
     * The conformance block that will be proposed to the device.
     * It is a set of services that will be allowed during communication.
     */
    ConformanceBlock getConformanceBlock();

    /**
     * The system title, usually the default one: "EICTCOMM".
     * This is the callingAPtitle in the application association request.
     */
    byte[] getSystemIdentifier();

    /**
     * Getter for the InvokeIdAndPriorityHandler that will handle the InvokeId and priority byte in the DLMS requests and responses.
     * The handler can check and verify the ID (and can reject a response if it has an invalid ID).
     */
    InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler();

    /**
     * The maximum received PDU size that will be proposed to the device.
     * If accepted, this is the one that will be used in the communication session.
     */
    int getMaxRecPDUSize();

    /**
     * The proposed version of the bluebook
     * Default (6) is usually used.
     */
    int getProposedDLMSVersion();

    /**
     * The proposed quality of service. Default is -1.
     */
    int getProposedQOS();

    /**
     * Indicates if the timezone should be requested from the device.
     * This will then be used to parse the date and time of the device.
     * Otherwise, the device timezone configured in EIServer is used.
     */
    boolean isRequestTimeZone();

    /**
     * This value is used to compensate for the round trip time, e.g. when reading out the clock of a device.
     */
    int getRoundTripCorrection();

    /**
     * A collection of security related objects, e.g. keys, FC handler, ...
     */
    SecurityProvider getSecurityProvider();

    /**
     * Setter for the security provider
     */
    void setSecurityProvider(SecurityProvider securityProvider);

    /**
     * Getter for the configured serial number of the device
     * This is used as calling-ap-title when setting up an association to the NTA simulation tool.
     */
    String getSerialNumber();

    /**
     * Setter for the configured serial number of the device.
     * For V2 protocols, this is received in the init() method in the offlineDevice.
     */
    void setSerialNumber(String serialNumber);

    /**
     * Indicates how to construct the ID of MBus meters.
     */
    boolean getFixMbusHexShortId();

    /**
     * Indicates to ignore the received DST status code (for LP interval data) or not.
     */
    boolean isIgnoreDSTStatusCode();

}