package com.energyict.protocolimplv2.nta.esmr50.messages;

import com.energyict.common.CommonCryptoMbusMessageExecutor;
import com.energyict.common.CryptoMBusClient;
import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.crypto.KeyRenewalMBusResponse;
import com.energyict.mdc.upl.crypto.MacResponse;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.HsmException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusClient;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MbusMessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.MBusKeyID;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;

public class CryptoESMR50MbusMessageExecutor extends ESMR50MbusMessageExecutor {

    private static final int BLOCK_SIZE = 64 * 1024;
    private CommonCryptoMbusMessageExecutor mbusCryptoMessageExecutor;

    public CryptoESMR50MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        mbusCryptoMessageExecutor = new CommonCryptoMbusMessageExecutor(isUsingCryptoServer(), getProtocol(), collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages){
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            try {
                if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.MBUS_TRANSFER_FUAK)) {
                    collectedMessage = doTransferCryptoFUAK(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.MBUS_TRANSFER_P2KEY)) {
                    collectedMessage = doTransferCryptoP2Key(pendingMessage);
                } else if (pendingMessage.getSpecification()
                        .equals(MBusSetupDeviceMessage.MBUS_ESMR5_FIRMWARE_UPGRADE)) {
                    collectedMessage = doFirmwareUpgradeCrypto(pendingMessage);
                } else if (pendingMessage.getSpecification()
                        .equals(MBusSetupDeviceMessage.MBUS_READ_DETAILED_VERSION_INFORMATION_TAG)) {
                    collectedMessage = doMbusReadDetailedData(pendingMessage);
                } else { // try parent
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession()
                        .getProperties()
                        .getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            } catch (ConfigurationException e) {
                journal(Level.SEVERE, "Configuration Exception failed : " + e.getCause() + e.getMessage());
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.getMessage());

            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }
        // Then delegate all other messages to the Dsmr 4.0 message executor
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }

    private CollectedMessage doMbusReadDetailedData(OfflineDeviceMessage pendingMessage) {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            String serialNumber = pendingMessage.getDeviceSerialNumber();
            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
            ESMR50MbusClient mBusClient5 = new ESMR50MbusClient(mbusClient.getProtocolLink(), mbusClient.getObjectReference(), MbusClientAttributes.VERSION9);

            int dataParameter = 0;
            Unsigned8 data = new Unsigned8(dataParameter);
            byte[] response = mBusClient5.readDetailedVersionInformation(data);
            String msg = "Method response: " + ProtocolTools.getHexStringFromBytes(response, "");
            journal(Level.INFO, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            collectedMessage.setDeviceProtocolInformation(msg);
        } catch (IOException e) {
            String msg = "doTransferMBusKeyCrypto exception:" + e.getCause() + " " + e.getMessage();
            journal(Level.WARNING, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(msg);
        }
        return collectedMessage;
    }

    @Override
    protected void setCryptoserverMbusEncryptionKeys(OfflineDeviceMessage pendingMessage) throws IOException {
        mbusCryptoMessageExecutor.setCryptoserverMbusEncryptionKeys(pendingMessage);
    }

    private CollectedMessage doTransferCryptoFUAK(OfflineDeviceMessage pendingMessage) {
        return doTransferMBusKeyCrypto(MBusKeyID.FUAK, pendingMessage);
    }

    private CollectedMessage doTransferCryptoP2Key(OfflineDeviceMessage pendingMessage) {
        return doTransferMBusKeyCrypto(MBusKeyID.P2, pendingMessage);
    }

    private CollectedMessage doTransferMBusKeyCrypto(MBusKeyID keyID, OfflineDeviceMessage pendingMessage) {
        String serialNumber = pendingMessage.getDeviceSerialNumber();

        if (!isUsingCryptoServer()) {
            return super.doTransferMbusKeyPlain(keyID, pendingMessage);
        }
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);

        String newOpenKey = "";
        String newKeyEncrypted = "";
        byte[] smartMeterKey = new byte[0];
        byte[] authenticationTag = new byte[0];
        byte[] keyData = new byte[0];

        try {
            List<byte[]> response = getKeyData(keyID, serialNumber, pendingMessage);
            keyData = response.get(0);
            if (MBusKeyID.FUAK.equals(keyID)) {
                byte[] keyLabel = response.get(1);
                byte[] mdmSmWK = response.get(2);
                newKeyEncrypted = ProtocolTools.getHexStringFromBytes(keyLabel, "") + ":" + ProtocolTools.getHexStringFromBytes(mdmSmWK, "");
            } else if (MBusKeyID.P2.equals(keyID)) {
                smartMeterKey = response.get(1);
                authenticationTag = response.get(2);
            } else {
                newKeyEncrypted = "";
            }
        } catch (ConfigurationException | IOException e) {
            String msg = "doTransferMBusKeyCrypto exception:" + e.getCause() + " " + e.getMessage();
            journal(Level.SEVERE, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }

        return doSendKeys(pendingMessage, serialNumber, keyID, keyData, smartMeterKey, authenticationTag, newOpenKey, newKeyEncrypted);
    }

    private CollectedMessage doSendKeys(OfflineDeviceMessage pendingMessage, String serialNumber, MBusKeyID keyID,
                                          byte[] keyData, byte[] smartMeterKey, byte[] authenticationTag, String newOpenKey, String newKeyEncrypted) {
        journal(Level.INFO, "Complete key data " + ProtocolTools.getHexStringFromBytes(keyData));
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
            ESMR50MbusClient mBusClient5 = new ESMR50MbusClient(mbusClient.getProtocolLink(), mbusClient.getObjectReference(), MbusClientAttributes.VERSION9);

            if (MBusKeyID.FUAK.equals(keyID)) {
                journal(Level.INFO, "Invoking FUAK method");
                mBusClient5.transferFUAK(keyData);
                journal(Level.INFO, "Successfully wrote the new MBus FUAK");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                collectedMessage.setDeviceProtocolInformation(newKeyEncrypted);
            } else if (MBusKeyID.P2.equals(keyID)) {

                if (isUsingCryptoServer()) {
                    CryptoMBusClient cryptoMBusClient = new CryptoMBusClient(mbusClient, MbusClientAttributes.VERSION9);

                    journal(Level.INFO, "Invoking transportKey method with the full key data");
                    // this is to pass the key to the g-meter
                    cryptoMBusClient.setTransportKey(keyData);

                    journal(Level.FINEST, "AuthenticationTag: " + ProtocolTools.getHexStringFromBytes(authenticationTag));
                    journal(Level.FINEST, "Wrapping smartMeterKey.");

                    byte[] fullRequest = mbusCryptoMessageExecutor.wrap(smartMeterKey, authenticationTag);

                    journal(Level.FINEST, "Full request to send: " + ProtocolTools.getHexStringFromBytes(fullRequest));
                    cryptoMBusClient.sendSetEncryptionKeyRequest(fullRequest);

                    journal(Level.INFO, "Successfully wrote the new MBus P2 Key");
                    journal(Level.INFO, "Saving the smartMeterKey: " + ProtocolTools.getHexStringFromBytes(smartMeterKey));
                    //return MessageResult.createSuccess(msgEntry, ProtocolTools.getHexStringFromBytes(smartMeterKey));
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                    collectedMessage.setDeviceProtocolInformation(ProtocolTools.getHexStringFromBytes(smartMeterKey));
                } else {
                    journal(Level.INFO, "Invoking transportKey method with the full key data");
                    // this is to pass the key to the g-meter
                    mBusClient5.setTransportKey(keyData);

                    journal(Level.INFO, "Invoking encryptionKey method to send the openKey: " + newOpenKey);
                    // this is to store the key
                    mBusClient5.setEncryptionKey(ProtocolTools.getBytesFromHexString(newOpenKey, 2));

                    journal(Level.INFO, "Successfully wrote the new MBus P2 Key");
                    journal(Level.INFO, "Saving the messageInfo: " + newKeyEncrypted);
                    //return MessageResult.createSuccess(msgEntry, newKeyEncrypted);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                    collectedMessage.setDeviceProtocolInformation(newKeyEncrypted);
                }
            } else {
                //return MessageResult.createFailed(msgEntry, "Only FUAK change and MBus P2 key options available.");
                String msg = "Only FUAK change and MBus P2 key options available.";
                journal(Level.SEVERE, msg);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
            }
        } catch (IOException e) {
            String msg = "IO Exception while writing key:" + e.getCause() + e.getMessage();
            journal(Level.SEVERE, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(msg);
        }
        return collectedMessage;
    }

    private List<byte[]> getKeyData(MBusKeyID keyID, String serialNumber, OfflineDeviceMessage pendingMessage)
            throws HsmException, ConfigurationException, IOException {
        journal(Level.INFO, "Preparing key data for phase 2 ... ");

        byte[] kcc = getKCC();
        byte[] mbusIV = getInitializationVector(kcc, serialNumber);
        byte[] eMeterIV = mbusCryptoMessageExecutor.getNextInitializationVector();

        String defaultKeyProperty = getMBusDefaultKey(pendingMessage);
        //IrreversibleKeyImpl encryptedKeyPhase2DefaultKey = EncryptedKeyPhase2.fromDataBaseString(defaultKeyProperty);
        //ProtectedSessionKey defaultKey = encryptedKeyPhase2DefaultKey.toProtectedSessionKey();
        IrreversibleKey defaultKey = IrreversibleKeyImpl.fromByteArray(ProtocolTools.getBytesFromHexString(defaultKeyProperty));

        KeyRenewalMBusResponse response;
        if (MBusKeyID.FUAK.equals(keyID)) {
            journal(Level.INFO, "Calling renewMBusFuakWithGCM ...");

            AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol) getProtocol();
            String workingKeyLabel = ((ESMR50Properties) protocol.getProperties()).getWorkingKeyLabelPhase2();


            journal(Level.FINEST, " - mbusIV [" + mbusIV + "]: " + ProtocolTools.getHexStringFromBytes(mbusIV));

            response = Services.hsmService().renewMBusFuakWithGCM(workingKeyLabel, defaultKey, mbusIV);

            journal(Level.FINEST, "Atos response:" + response.toString());

            List<byte[]> ret = new ArrayList<>();
            //the first 8 bytes of the mBusAuthTag

            byte[] authTag = ProtocolUtils.getSubArray(response.getMBusAuthTag(), 0, 7);
            ret.add(ProtocolTools.concatByteArrays(kcc, response.getMbusDeviceKey(), authTag));
            ret.add(workingKeyLabel.getBytes(StandardCharsets.US_ASCII));
            ret.add(response.getMdmSmWK().getEncryptedKey());
            return ret;
        } else if (MBusKeyID.P2.equals(keyID)) {
            journal(Level.INFO, "Calling renewMBusUserKeyWithGCM ...");

            SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
            SecurityProvider securityProvider = getProtocol().getDlmsSession().getProperties().getSecurityProvider();
            IrreversibleKey encrKey = IrreversibleKeyImpl.fromByteArray(securityContext.getEncryptionKey(false));
            IrreversibleKey authKey = IrreversibleKeyImpl.fromByteArray(securityProvider.getAuthenticationKey());

            byte[] apduTemplate = mbusCryptoMessageExecutor.createApduTemplate(serialNumber);


            journal(Level.FINEST, " - apduTemplate [" + apduTemplate + "]: " + ProtocolTools.getHexStringFromBytes(apduTemplate));
            journal(Level.FINEST, " - eMeterIV [" + eMeterIV + "]: " + ProtocolTools.getHexStringFromBytes(eMeterIV));

            journal(Level.FINEST, " - defaultKey : " + defaultKey);
            journal(Level.FINEST, " - mbusIV [" + mbusIV + "]: " + ProtocolTools.getHexStringFromBytes(mbusIV));

            response = Services.hsmService().renewMBusUserKeyWithGCM(encrKey,
                    apduTemplate,
                    eMeterIV, //e-meter fc
                    authKey,
                    defaultKey,
                    mbusIV,
                    getProtocol().getDlmsSession().getProperties().getSecuritySuite());//mbus
            try {
                journal(Level.FINEST, "Atos response:" + response.toString());
            } catch (NullPointerException e) {
                journal(Level.FINEST, "smWK key is null");
            }

            journal(Level.FINEST, "KEY content:");

            byte[] keyEncrypted = response.getMbusDeviceKey();
            journal(Level.FINEST, " - keyEncrypted [" + keyEncrypted.length + "]: " + ProtocolTools.getHexStringFromBytes(keyEncrypted));

            byte[] keyTag = response.getMBusAuthTag();
            //only the 8 Most Significant Bytes should be used
            keyTag = ProtocolUtils.getSubArray(keyTag, 0, 7);
            journal(Level.FINEST, " - gcmTag [" + keyTag.length + "]: " + ProtocolTools.getHexStringFromBytes(keyTag));

            byte[] keyInfo = response.getSmartMeterKey();
            byte[] authenticationTag = response.getAuthenticationTag();

            journal(Level.FINEST, " - keyInfo [" + keyInfo.length + "]: " + ProtocolTools.getHexStringFromBytes(keyInfo));

            journal(Level.FINEST, " - kcc [" + kcc.length + "]: " + ProtocolTools.getHexStringFromBytes(kcc));

            List<byte[]> ret = new ArrayList<>();
            ret.add(ProtocolTools.concatByteArrays(kcc, keyEncrypted, keyTag));
            ret.add(keyInfo); // this needs to be published to protocolInfo
            ret.add(authenticationTag);
            return ret;
        }

        return null;
    }

    private boolean isUsingCryptoServer() {
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol) getProtocol();
        return ((CryptoESMR50Properties) protocol.getDlmsSessionProperties()).useCryptoServer();
    }

    private CollectedMessage doFirmwareUpgradeCrypto(OfflineDeviceMessage pendingMessage) throws IOException, ConfigurationException {
        journal(Level.INFO, "Handling message Firmware upgrade (crypto)");
        String serialNumber = pendingMessage.getDeviceSerialNumber();

        if (!isUsingCryptoServer()) {
            return super.doFirmwareUpgrade(pendingMessage, super.getMBusFUAK());
        }

        // crypto phase 2
        journal(Level.INFO, "Firmware Update using crypto phase 2 algorithm");
        ObisCode imageTransferObisCode = getImageTransferObisCode(serialNumber);
        journal(Level.INFO, "Firmware Update: image transfer obis code:" + imageTransferObisCode);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(imageTransferObisCode);
        String path = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateFileAttributeName);
        byte[] imageData = loadUserFile(path);

        //String activationDateStr = messageHandler.getActivationDate();
        String activationDateStr = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();   // Will return empty string if the MessageAttribute could not be found
        Calendar activationDate = null;
        journal(Level.FINE, "Initial Data:");
        if (activationDateStr != null && !"".equals(activationDateStr)) {
            activationDate = Calendar.getInstance();
            activationDate.setTimeInMillis(Long.parseLong(activationDateStr) * 1000);
            journal(Level.FINE, "-activationDate: " + activationDate.toString());
        } else {
            journal(Level.FINE, "-activationDate: " + activationDateStr);
        }

        String fuakRaw = super.getMBusFUAK();
        journal(Level.FINE, "-imageData length:" + imageData.length);
        journal(Level.FINE, "-FUAK key:" + fuakRaw);

        ArrayList<byte[]> preparedImageInfo = super.prepareFirmwareUpgradeImage(imageData, serialNumber, activationDate);
        byte[] clearImageData = preparedImageInfo.get(0);
        byte[] iv = preparedImageInfo.get(1);
        byte[] shortId = preparedImageInfo.get(2);

        byte[] encryptedImage;

        //ProtectedSessionKey protectedFUAK = EncryptedKeyPhase2.fromDataBaseString(fuakRaw).toProtectedSessionKey();
        IrreversibleKey irreversibleFUAK = IrreversibleKeyImpl.fromByteArray(ProtocolTools.getBytesFromHexString(fuakRaw));
        journal(Level.INFO, "protectedFUAK value: " + irreversibleFUAK);

        MacResponse macResponse;

        if (clearImageData.length < BLOCK_SIZE) {
            journal(Level.INFO, "Image size [" + clearImageData.length + "] <= blockSize=" + BLOCK_SIZE + ". Will calculate MAC in a single step!");
            macResponse = generateMacSingleBlock(irreversibleFUAK, clearImageData, iv);
        } else {
            journal(Level.INFO, "Image size [" + clearImageData.length + "] > blockSize=" + BLOCK_SIZE + ". Will calculate MAC in steps");
            macResponse = generateMacFirstBlock(irreversibleFUAK, ProtocolUtils.getSubArray(clearImageData, BLOCK_SIZE));
            journal(Level.FINEST, "Encrypting using blockSize=" + BLOCK_SIZE);
            for (int i = 0; i <= clearImageData.length / BLOCK_SIZE; i++) {
                macResponse = generateMacMiddleBlock(irreversibleFUAK, ProtocolUtils.getSubArray(clearImageData, BLOCK_SIZE * i, BLOCK_SIZE), macResponse
                        .getData());
            }
            macResponse = generateMacLastBlock(irreversibleFUAK, ProtocolUtils.getSubArray(clearImageData, clearImageData.length), macResponse
                    .getInitVector(), macResponse.getData());
        }
        byte[] MAC = macResponse.getData();
        journal(Level.FINE, " - MAC: " + ProtocolTools.getHexStringFromBytes(MAC));

        // TODO: encrypt more?
        encryptedImage = ProtocolTools.concatByteArrays(clearImageData, MAC);
        journal(Level.FINE, " - encryptedData length (image+MAC) " + encryptedImage.length);

        journal(Level.FINEST, "CRC16 calculation is done on: " + ProtocolTools.getHexStringFromBytes(encryptedImage));

        int crcVal = CRCGenerator.calcCRCDirect(encryptedImage);
        byte[] crc = ProtocolTools.getBytesFromInt(crcVal, 2);
        journal(Level.FINE, " - CRC = " + crcVal + ": " + ProtocolTools.getHexStringFromBytes(crc));

        String imageIdentifier = getFirmwareImageIdentifier(crc);
        journal(Level.FINE, " - imageIdentifier = " + imageIdentifier);

        byte[] finalImageData = ProtocolTools.concatByteArrays(encryptedImage);

        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }
        handleFWUpgrade(activationDateStr, it, finalImageData, imageIdentifier);
        journal(Level.INFO, "Firmware upgrade successful.");
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Firmware upgrade successful.");
        return collectedMessage;
    }

    private void handleFWUpgrade(String activationDate, ImageTransfer it, byte[] imageData, String imageIdentifier) throws
            IOException {
        //it seems it's no different than non crypto in eiserver
        handleFWUpgradePhase0(activationDate, it, imageData, imageIdentifier);
    }

    private MacResponse generateMacSingleBlock(IrreversibleKey FUAK, byte[] clearData, byte[] icv) throws HsmException {
        journal(Level.INFO, "generateMacSingleBlockBlock...");

        MacResponse macResponse = Services.hsmService().generateMacSingleBlock(FUAK, clearData, icv);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();

        journal(Level.INFO, "generateMacSingleBlockBlock - Init vector value: " + initVector);
        journal(Level.INFO, "generateMacSingleBlockBlock - Mac value: " + macValue);
        return macResponse;
    }

    private MacResponse generateMacFirstBlock(IrreversibleKey FUAK, byte[] clearData) throws HsmException {
        journal(Level.INFO, "generateMacFirstBlock...");
        MacResponse macResponse = Services.hsmService().generateMacFirstBlock(FUAK, clearData);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, "generateMacFirstBlock - Init vector value: " + initVector);
        journal(Level.INFO, "generateMacFirstBlock - Mac value: " + macValue);
        return macResponse;
    }

    private MacResponse generateMacMiddleBlock(IrreversibleKey FUAK, byte[] clearData, byte[] state) throws
            HsmException {
        journal(Level.INFO, "generateMacMiddleBlock...");
        MacResponse macResponse = Services.hsmService().generateMacMiddleBlock(FUAK, clearData, state);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, "generateMacMiddleBlock - Init vector value: " + initVector);
        journal(Level.INFO, "generateMacMiddleBlock - Mac value: " + macValue);
        return macResponse;
    }

    private MacResponse generateMacLastBlock(IrreversibleKey FUAK, byte[] clearData, byte[] icv, byte[] state) throws
            HsmException {
        journal(Level.INFO, "generateMacLastBlock...");
        MacResponse macResponse = Services.hsmService().generateMacLastBlock(FUAK, clearData, icv, state);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, "generateMacLastBlock - Init vector value: " + initVector);
        journal(Level.INFO, "generateMacLastBlock - Mac value: " + macValue);
        return macResponse;
    }

    @Override
    protected CollectedMessage upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) {
        return upgradeFirmwareWithActivationDate(pendingMessage);
    }

    protected CollectedMessage upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) {
        try {
            return doFirmwareUpgradeCrypto(pendingMessage);
        } catch (Exception ex) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation(ex.getLocalizedMessage());
            return collectedMessage;
        }
    }

}
