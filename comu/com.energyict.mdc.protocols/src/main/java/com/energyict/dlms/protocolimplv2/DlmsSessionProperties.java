/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ConformanceBlock;

import java.util.TimeZone;

public interface DlmsSessionProperties extends HasDynamicProperties, CommunicationSessionProperties {

    String CLIENT_PRIVATE_KEY_AGREEMENT_KEY = "ClientPrivateKeyAgreementKey";
    String CLIENT_SIGNING_CERTIFICATE = "ClientSigningCertificate";
    String CLIENT_PRIVATE_SIGNING_KEY = "ClientPrivateSigningKey";
    String GENERAL_CIPHERING_KEY_TYPE = "GeneralCipheringKeyType";
    String SERVER_TLS_CERTIFICATE = "ServerTLSCertificate";

    /**
     * The device timezone
     */
    public TimeZone getTimeZone();

    /**
     * The DLMS reference. Can be SN (short name) or LN (logical name)
     */
    public DLMSReference getReference();

    /**
     * The security set of a device. It contains all properties related to security.
     */
    public DeviceProtocolSecurityPropertySet getSecurityPropertySet();

    /**
     * Setter for the device's security set
     */
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

    /**
     * Getter for the authentication level. Can be 0 (none), 1 (password), 2 (manufacturer specific), 3 (MD5), 4 (SHA1) or 5(GMAC).
     * The authentication is done while setting up the DLMS association to a device.
     */
    public int getAuthenticationSecurityLevel();

    /**
     * Getter for the data transport security level. Can be 0 (no security), 1 (authentication), 2 (encryption) or 3 (both)
     */
    public int getDataTransportSecurityLevel();

    /**
     * Indicates the manufacturer.
     * This can be used to choose from a set of hard coded object lists instead of reading out the device's full object list.
     */
    public String getManufacturer();

    /**
     * Indicates if a wakeup should happen before starting the communication with the device.
     * This can be a CSD call or an SMS to trigger the device's GPRS connection.
     */
    public boolean isWakeUp();

    /**
     * Getter for the device ID.
     * This can be used for the HHU sign on.
     */
    public String getDeviceId();

    /**
     * The type of ciphering. Can be global or dedicated.
     * Depending on the type, either the global encryption key or the dedicated key is used.
     */
    public CipheringType getCipheringType();

    /**
     * Indicates if the device to communicate with is the NTA simulation tool.
     * This will put the configured serial number in as the calledAPtitle in the application association request.
     * This way, the simulation tool knows to which device in its database we want to communicate.
     */
    public boolean isNtaSimulationTool();

    /**
     * Indicates if the protocol is allowed to use bulk requests.
     * A bulk request reads out multiple attributes in a single request.
     */
    public boolean isBulkRequest();

    /**
     * The conformance block that will be proposed to the device.
     * It is a set of services that will be allowed during communication.
     */
    public ConformanceBlock getConformanceBlock();

    /**
     * The system title, usually the default one: "EICTCOMM".
     * This is the callingAPtitle in the application association request.
     */
    public byte[] getSystemIdentifier();

    /**
     * Getter for the InvokeIdAndPriorityHandler that will handle the InvokeId and priority byte in the DLMS requests and responses.
     * The handler can check and verify the ID (and can reject a response if it has an invalid ID).
     */
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler();

    /**
     * The maximum received PDU size that will be proposed to the device.
     * If accepted, this is the one that will be used in the communication session.
     */
    public int getMaxRecPDUSize();

    /**
     * The proposed version of the bluebook
     * Default (6) is usually used.
     */
    public int getProposedDLMSVersion();

    /**
     * The proposed quality of service. Default is -1.
     */
    public int getProposedQOS();

    /**
     * Indicates if the timezone should be requested from the device.
     * This will then be used to parse the date and time of the device.
     * Otherwise, the device timezone configured in EIServer is used.
     */
    public boolean isRequestTimeZone();

    /**
     * This value is used to compensate for the round trip time, e.g. when reading out the clock of a device.
     */
    public int getRoundTripCorrection();

    /**
     * A collection of security related objects, e.g. keys, FC handler, ...
     */
    public SecurityProvider getSecurityProvider();

    /**
     * Setter for the security provider
     */
    public void setSecurityProvider(SecurityProvider securityProvider);

    /**
     * Getter for the configured serial number of the device
     * This is used as calling-ap-title when setting up an association to the NTA simulation tool.
     */
    public String getSerialNumber();

    /**
     * Setter for the configured serial number of the device.
     * For V2 protocols, this is received in the init() method in the offlineDevice.
     */
    public void setSerialNumber(String serialNumber);

    /**
     * Indicates how to construct the ID of MBus meters.
     */
    public boolean getFixMbusHexShortId();

    /**
     * Indicates to ignore the received DST status code (for LP interval data) or not.
     */
    public boolean isIgnoreDSTStatusCode();

    /**
     * Return true if digital signing is to be used (only possible for DLMS suite 1 and 2)
     */
    boolean isGeneralSigning();

    GeneralCipheringKeyType getGeneralCipheringKeyType();

}