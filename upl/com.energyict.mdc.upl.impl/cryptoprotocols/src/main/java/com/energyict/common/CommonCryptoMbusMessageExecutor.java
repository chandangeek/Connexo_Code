package com.energyict.common;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.crypto.KeyRenewalMBusResponse;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityPolicy;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.cosem.methods.MbusClientMethods;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.HsmException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Contains shared functionality for the DSMR 2.3 and 4.0 message executors
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/04/13
 * Time: 13:42
 * Author: khe
 */
public class CommonCryptoMbusMessageExecutor extends AbstractMessageExecutor {

    private final DlmsSession dlmsSession;
    private AbstractDlmsProtocol protocol;
    private boolean usesCryptoServer = false;

    public CommonCryptoMbusMessageExecutor(boolean usesCryptoServer, AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.protocol = protocol;
        this.dlmsSession = protocol.getDlmsSession();
        this.usesCryptoServer = usesCryptoServer;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;//Not used in this class
    }

    public void setCryptoserverMbusEncryptionKeys(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        if (!usesCryptoServer) {
            throw new IOException("Cryptoserver property should be set to true in order to use this message!");
        }
        if (!usesEncryption()) {
            throw new IOException("Security policy should be 'encryption' or 'encryption and authentication' in order to set the MBus encryption keys");
        }
        String serialNumber = offlineDeviceMessage.getDeviceSerialNumber();
        SecurityContext securityContext = dlmsSession.getAso().getSecurityContext();
        SecurityProvider securityProvider = dlmsSession.getProperties().getSecurityProvider();
        IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(securityProvider.getAuthenticationKey());
        IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(securityContext.getEncryptionKey(false));
        //extract new key from message
        String defaultKeyString = getDeviceMessageAttributeValue(offlineDeviceMessage, DeviceMessageConstants.defaultKeyAttributeName);
        IrreversibleKey defaultKey = IrreversibleKeyImpl.fromByteArray(ProtocolTools.getBytesFromHexString(defaultKeyString));

        if (!usesAuthentication()) {
            ak = null;     //Don't use it if no authentication is used
        }

        KeyRenewalMBusResponse response;
        try {
            response = Services.hsmService().renewMBusUserKey(
                    createApduTemplate(serialNumber),
                    getNextInitializationVector(),
                    ak,ek,defaultKey,dlmsSession.getProperties().getSecuritySuite());

        } catch (HsmException e) {
            throw new IOException("Failed to send MBus encryption keys using the cryptoserver: " + e.getMessage());
        }

        CryptoMBusClient cryptoMBusClient = new CryptoMBusClient(getMBusClient(serialNumber), MbusClientAttributes.VERSION9);
        byte[] transportKey = response.getMbusDeviceKey();
        cryptoMBusClient.setTransportKey(transportKey);

        byte[] setEncryptionKeyApdu = response.getSmartMeterKey();  //xAPDU that represents a request to execute method 7 (set MBus key) on the MBus client
        byte[] fullRequest = wrap(setEncryptionKeyApdu, response.getAuthenticationTag());
        cryptoMBusClient.sendSetEncryptionKeyRequest(fullRequest);
    }

    public byte[] wrap(byte[] setEncryptionKeyApdu, byte[] tag) {
        byte[] fc = dlmsSession.getAso().getSecurityContext().getFrameCounterInBytes();
        byte[] header = new byte[6];
        header[0] = (byte) 0xE6; // Destination_LSAP
        header[1] = (byte) 0xE6; // Source_LSAP
        header[2] = 0x00; // LLC_Quality
        header[3] = DLMSCOSEMGlobals.GLO_ACTIOREQUEST;
        header[4] = (byte) (1 + setEncryptionKeyApdu.length + fc.length + (usesAuthentication() ? 12 : 0));
        header[5] = getSecurityPolicyByte();

        byte[] trailer = new byte[0];
        if (usesAuthentication()) {
            trailer = ProtocolTools.getSubArray(tag, 0, 12);    //Only use the first 12 bytes of the tag
        }
        return ProtocolTools.concatByteArrays(header, fc, setEncryptionKeyApdu, trailer);
    }

    /**
     * Returns 0x10, 0x20 or 0x30, depending on the security policy
     */
    private byte getSecurityPolicyByte() {
        return (byte) (dlmsSession.getAso().getSecurityContext().getSecurityPolicy().getDataTransportSecurityLevel() * 16);
    }

    /**
     * Returns an IV with framecounter that is one more than the current frame counter.
     * This will be used in the request after we send the transfer key.
     */
    public byte[] getNextInitializationVector() {
        byte[] systemTitle = dlmsSession.getAso().getSecurityContext().getSystemTitle();
        if (systemTitle == null) {
            throw new IllegalArgumentException("The AssociationRequest did NOT have a client SystemTitle - Encryption can not be applied!");
        }
        long frameCounter = dlmsSession.getAso().getSecurityContext().getFrameCounter() + 1;
        byte[] fc = ProtocolTools.getBytesFromLong(frameCounter, 4);
        byte[] paddedSystemTitle = copyOf(systemTitle, SecurityContext.SYSTEM_TITLE_LENGTH);
        return ProtocolUtils.concatByteArrays(paddedSystemTitle, fc);
    }

    //helper method; it was in eiserver but not connexo version of DLMSUtils
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    /* A template for the request to execute method 7 on the proper mbusClient.
     * The Cryptoserver will fill in the new key, and encrypt the request.
     */
    public byte[] createApduTemplate(String serialNumber) throws IOException {
        byte[] emptyKey = new byte[16];
        byte[] apdu = new byte[5];
        apdu[0] = DLMSCOSEMGlobals.COSEM_ACTIONREQUEST;
        apdu[1] = DLMSCOSEMGlobals.COSEM_ACTIONREQUEST_NORMAL;
        apdu[2] = 0x42;      // InvokeId is fixed
        apdu[3] = (byte) (DLMSClassId.MBUS_CLIENT.getClassId() >> 8);
        apdu[4] = (byte) DLMSClassId.MBUS_CLIENT.getClassId();
        apdu = ProtocolTools.concatByteArrays(apdu, getMbusClientObisCode(serialNumber).getLN());

        byte[] request = new byte[4];
        request[0] = (byte) MbusClientMethods.SET_ENCRYPTION_KEY.getMethodNumber();
        request[1] = 0x01;
        request[2] = AxdrType.OCTET_STRING.getTag();
        request[3] = (byte) emptyKey.length;

        apdu = ProtocolTools.concatByteArrays(apdu, request, emptyKey);
        return apdu;
    }

    private ObisCode getMbusClientObisCode(String serialNumber) throws IOException {
        return dlmsSession.getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode();
    }

    private boolean usesEncryption() {
        return getSecurityPolicyLevel() == SecurityPolicy.SECURITYPOLICY_ENCRYPTION || getSecurityPolicyLevel() == SecurityPolicy.SECURITYPOLICY_BOTH;
    }

    private boolean usesAuthentication() {
        return getSecurityPolicyLevel() == SecurityPolicy.SECURITYPOLICY_AUTHENTICATION || getSecurityPolicyLevel() == SecurityPolicy.SECURITYPOLICY_BOTH;
    }

    private int getSecurityPolicyLevel() {
        return getProtocol().getDlmsSession()
                .getAso()
                .getSecurityContext()
                .getSecurityPolicy()
                .getDataTransportSecurityLevel();
    }

}