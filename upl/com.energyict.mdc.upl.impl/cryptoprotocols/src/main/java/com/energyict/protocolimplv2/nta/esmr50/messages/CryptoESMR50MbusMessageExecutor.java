package com.energyict.protocolimplv2.nta.esmr50.messages;

import com.energyict.common.CommonCryptoMbusMessageExecutor;
import com.energyict.common.CryptoMBusClient;
import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ProtocolException;
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
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MbusMessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.MBusKeyID;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimpl.utils.ProtocolTools.getHexStringFromBytes;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusConfigurationSupport.DEFAULT_KEY;
import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusConfigurationSupport.FUAK;

public class CryptoESMR50MbusMessageExecutor extends ESMR50MbusMessageExecutor {

    private static final int BLOCK_SIZE = 64 * 1024;
    private final CommonCryptoMbusMessageExecutor mbusCryptoMessageExecutor;

    public CryptoESMR50MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                           IssueFactory issueFactory, DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(protocol, collectedDataFactory, issueFactory, deviceMasterDataExtractor);
        mbusCryptoMessageExecutor = new CommonCryptoMbusMessageExecutor(isUsingCryptoServer(), getProtocol(), collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            try {
                if (pendingMessage.getSpecification().equals(SecurityMessage.MBUS_TRANSFER_FUAK)) {
                    collectedMessage = doTransferFUAK(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.MBUS_TRANSFER_P2KEY)) {
                    collectedMessage = doTransferP2Key(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.MBUS_ESMR5_FIRMWARE_UPGRADE)) {
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
            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MBusClient.VERSION.VERSION1);

            int dataParameter = 0;
            Unsigned8 data = new Unsigned8(dataParameter);
            byte[] response = mbusClient.readDetailedVersionInformation(data);
            String msg = "Method response: " + getHexStringFromBytes(response, "");
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

    private CollectedMessage doTransferFUAK(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        if (isUsingCryptoServer()) {
            return doTransferCryptoFUAK(pendingMessage);
        } else {
            return doTransferMbusKeyPlain(MBusKeyID.FUAK, pendingMessage);
        }
    }

    private String determineFUAKSecurityAccessorName(String slaveSerialNumber) throws ConfigurationException {
        return getProtocol().getOfflineDevice().getAllSlaveDevices()
                    .stream()
                    .filter(slaveDevice -> slaveDevice.getSerialNumber().equals(slaveSerialNumber))
                    .findFirst()
                    .map(slaveDevice -> slaveDevice.getAllProperties().getTypedProperty(FUAK))
                    .map(Object::toString)
                    .orElseThrow(() -> new ConfigurationException("Property '" + FUAK + "' is not set"));
    }

    private CollectedMessage doTransferCryptoFUAK(OfflineDeviceMessage pendingMessage) {
        final String serialNumber = pendingMessage.getDeviceSerialNumber();

        try {
            final String defaultKeyProperty = getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), DEFAULT_KEY);
            String securityAccessorName = determineFUAKSecurityAccessorName(serialNumber);
            FUAKKeyData fuakKeyData = getFUAKKeyData(serialNumber, defaultKeyProperty);
            String newKeyEncrypted = getHexStringFromBytes(fuakKeyData.getKeyLabel(), "") + ":" + getHexStringFromBytes(fuakKeyData.getMdmSmWK(), "");
            journal(Level.INFO, "Complete key data " + getHexStringFromBytes(fuakKeyData.getKeyData()));
            MBusClient mbusClientESMR5 = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MBusClient.VERSION.VERSION1);
            journal(Level.INFO, "Invoking FUAK method");
            mbusClientESMR5.transferFUAK(fuakKeyData.getKeyData());
            journal(Level.INFO, "Successfully wrote the new MBus FUAK");
            CollectedMessage collectedMessage = createCollectedMessageWithUpdateSecurityProperty(pendingMessage, securityAccessorName, newKeyEncrypted);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (ConfigurationException | IOException e) {
            String msg = "doTransferMBusKeyCrypto exception:" + e.getCause() + " " + e.getMessage();
            journal(Level.SEVERE, msg);
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }
    }

    private FUAKKeyData getFUAKKeyData(String serialNumber, String defaultKeyProperty) throws HsmException {
        journal(Level.INFO, "Preparing key data for phase 2 ... ");

        byte[] kcc = getKCC();
        byte[] mbusIV = getInitializationVector(kcc, serialNumber);

        IrreversibleKey defaultKey = new IrreversibleKeyImpl(defaultKeyProperty);

        KeyRenewalMBusResponse response;
        journal(Level.INFO, "Calling renewMBusFuakWithGCM ...");

        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol) getProtocol();
        String workingKeyLabel = ((ESMR50Properties) protocol.getDlmsSessionProperties()).getWorkingKeyLabelPhase2();

        journal(Level.FINEST, " - mbusIV [" + Arrays.toString(mbusIV) + "]: " + getHexStringFromBytes(mbusIV));
        response = Services.hsmService().renewMBusFuakWithGCM(workingKeyLabel, defaultKey, mbusIV);
        journal(Level.FINEST, "Atos response:" + response.toString());

        //the first 8 bytes of the mBusAuthTag
        byte[] authTag = ProtocolUtils.getSubArray(response.getMBusAuthTag(), 0, 7);

        byte[] keyData = ProtocolTools.concatByteArrays(kcc, response.getMbusDeviceKey(), authTag);
        byte[] keyLabel = workingKeyLabel.getBytes(StandardCharsets.US_ASCII);
        byte[] mdmSmWk = response.getMdmSmWK().getEncryptedKey();
        return new FUAKKeyData(keyData, keyLabel, mdmSmWk);
    }

    private CollectedMessage doTransferP2Key(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        if (isUsingCryptoServer()) {
            return doTransferCryptoP2Key(pendingMessage);
        } else {
            return doTransferMbusKeyPlain(MBusKeyID.P2, pendingMessage);
        }
    }

    private CollectedMessage doTransferCryptoP2Key(OfflineDeviceMessage pendingMessage) {
        final String serialNumber = pendingMessage.getDeviceSerialNumber();
        try {
            final String defaultKeyProperty = getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), DEFAULT_KEY);
            P2KeyData p2KeyData = getP2KeyData(serialNumber, defaultKeyProperty);

            journal(Level.INFO, "Complete key data " + getHexStringFromBytes(p2KeyData.getKeyData()));
            MBusClient mbusClientESMR5 = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MBusClient.VERSION.VERSION1);

            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION);
            CryptoMBusClient cryptoMBusClient = new CryptoMBusClient(mbusClient, MBusClient.VERSION.VERSION0_BLUE_BOOK_9TH_EDITION);

            journal(Level.INFO, "Invoking transportKey method with the full key data");
            // this is to pass the key to the g-meter
            cryptoMBusClient.setTransportKey(p2KeyData.getKeyData());

            journal(Level.FINEST, "AuthenticationTag: " + getHexStringFromBytes(p2KeyData.getAuthenticationTag()));
            journal(Level.FINEST, "Wrapping smartMeterKey.");

            byte[] fullRequest = mbusCryptoMessageExecutor.wrap(p2KeyData.getSmartMeterKey(), p2KeyData.getAuthenticationTag());

            journal(Level.FINEST, "Full request to send: " + getHexStringFromBytes(fullRequest));
            cryptoMBusClient.sendSetEncryptionKeyRequest(fullRequest);

            journal(Level.INFO, "Successfully wrote the new MBus P2 Key");
            journal(Level.FINEST, "Incrementing frame-counter");
            getProtocol().getDlmsSession().getAso().getSecurityContext().incFrameCounter();
            journal(Level.INFO, "Encryption key status is now: " + mbusClientESMR5.readKeyStatusAsText() );
            journal(Level.INFO, "Saving the smartMeterKey: " + getHexStringFromBytes(p2KeyData.getSmartMeterKey()));

            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (ConfigurationException | IOException e) {
            String msg = "doTransferMBusKeyCrypto exception:" + e.getCause() + " " + e.getMessage();
            journal(Level.SEVERE, msg);
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }
    }

    private P2KeyData getP2KeyData(String serialNumber, String defaultKeyProperty) throws HsmException, IOException {
        journal(Level.INFO, "Preparing key data for phase 2 ... ");

        byte[] kcc = getKCC();
        byte[] mbusIV = getInitializationVector(kcc, serialNumber);

        IrreversibleKey defaultKey = new IrreversibleKeyImpl(defaultKeyProperty);

        journal(Level.INFO, "Preparing P2 meter data using HSM");
        journal(Level.INFO, "Reading information required for apduTemplate");
        byte[] apduTemplate = mbusCryptoMessageExecutor.createApduTemplate(serialNumber);

        /* No other meter communication until method8 invocation to keep the frame-counter in sync! */
        byte[] eMeterIV = mbusCryptoMessageExecutor.getNextInitializationVector();
        journal(Level.INFO, "Calling renewMBusUserKeyWithGCM with the parameters:");
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        SecurityProvider securityProvider = getProtocol().getDlmsSession().getProperties().getSecurityProvider();
        IrreversibleKey encrKey = IrreversibleKeyImpl.fromByteArray(securityContext.getEncryptionKey(false));
        IrreversibleKey authKey = IrreversibleKeyImpl.fromByteArray(securityProvider.getAuthenticationKey());

        journal(Level.FINEST, String.format(" - apduTemplate [%s]: %s", Arrays.toString(apduTemplate), getHexStringFromBytes(apduTemplate)));
        journal(Level.FINEST, String.format(" - eMeterIV [%s]: %s", Arrays.toString(eMeterIV), getHexStringFromBytes(eMeterIV)));
        journal(Level.FINEST, String.format(" - defaultKey : %s:%s", defaultKey.getKeyLabel(), getHexStringFromBytes(defaultKey.getEncryptedKey())));
        journal(Level.FINEST, String.format(" - mbusIV [%s]: %s", Arrays.toString(mbusIV), getHexStringFromBytes(mbusIV)));

        KeyRenewalMBusResponse response = Services.hsmService().renewMBusUserKeyWithGCM(encrKey,
                apduTemplate,
                eMeterIV, //e-meter fc
                authKey,
                defaultKey,
                mbusIV,
                getProtocol().getDlmsSession().getProperties().getSecuritySuite());//mbus
        try {
            journal(Level.FINEST, "Atos smartMeterKey: \t" + getHexStringFromBytes(response.getSmartMeterKey()));
            journal(Level.FINEST, "Atos authenticationTag: \t" + getHexStringFromBytes(response.getAuthenticationTag()));
            journal(Level.FINEST, "Atos mbusDeviceKey: \t" + getHexStringFromBytes(response.getMbusDeviceKey()));
            journal(Level.FINEST, "Atos authTag: \t" + getHexStringFromBytes(response.getMBusAuthTag()));
        } catch (NullPointerException e) {
            journal(Level.FINEST, "Null pointer exception while reading the ATOS response");
        }

        journal(Level.FINEST, "KEY content:");

        byte[] keyEncrypted = response.getMbusDeviceKey();
        journal(Level.FINEST, " - keyEncrypted [" + keyEncrypted.length + "]: " + getHexStringFromBytes(keyEncrypted));

        byte[] keyTag = response.getMBusAuthTag();
        //only the 8 Most Significant Bytes should be used
        keyTag = ProtocolUtils.getSubArray(keyTag, 0, 7);
        journal(Level.FINEST, " - gcmTag [" + keyTag.length + "]: " + getHexStringFromBytes(keyTag));

        byte[] keyInfo = response.getSmartMeterKey();
        byte[] authenticationTag = response.getAuthenticationTag();

        journal(Level.FINEST, " - keyInfo [" + keyInfo.length + "]: " + getHexStringFromBytes(keyInfo));
        journal(Level.FINEST, " - kcc [" + kcc.length + "]: " + getHexStringFromBytes(kcc));

        return new P2KeyData(ProtocolTools.concatByteArrays(kcc, keyEncrypted, keyTag), keyInfo, authenticationTag);

    }

    private boolean isUsingCryptoServer() {
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol) getProtocol();
        return ((CryptoESMR50Properties) protocol.getDlmsSessionProperties()).useCryptoServer();
    }

    private CollectedMessage doFirmwareUpgradeCrypto(OfflineDeviceMessage pendingMessage) throws IOException, ConfigurationException {
        journal(Level.INFO, "Handling message Firmware upgrade (crypto)");
        final String serialNumber = pendingMessage.getDeviceSerialNumber();

        if (!isUsingCryptoServer()) {
            return super.doFirmwareUpgrade(pendingMessage, super.getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), FUAK));
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

        String fuakRaw = super.getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), FUAK);
        journal(Level.FINE, "-imageData length:" + imageData.length);
        journal(Level.FINE, "-FUAK key:" + fuakRaw);

        ArrayList<byte[]> preparedImageInfo = super.prepareFirmwareUpgradeImage(imageData, serialNumber, activationDate);
        byte[] clearImageData = preparedImageInfo.get(0);
        byte[] iv = preparedImageInfo.get(1);
        byte[] shortId = preparedImageInfo.get(2);

        byte[] encryptedImage;

        IrreversibleKey irreversibleFUAK = new IrreversibleKeyImpl(fuakRaw);
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
        journal(Level.FINE, " - MAC: " + getHexStringFromBytes(MAC));

        // TODO: encrypt more?
        encryptedImage = ProtocolTools.concatByteArrays(clearImageData, MAC);
        journal(Level.FINE, " - encryptedData length (image+MAC) " + encryptedImage.length);

        journal(Level.FINEST, "CRC16 calculation is done on: " + getHexStringFromBytes(encryptedImage));

        int crcVal = CRCGenerator.calcCRCDirect(encryptedImage);
        byte[] crc = ProtocolTools.getBytesFromInt(crcVal, 2);
        journal(Level.FINE, " - CRC = " + crcVal + ": " + getHexStringFromBytes(crc));

        String imageIdentifier = getFirmwareImageIdentifier(crc);
        journal(Level.FINE, " - imageIdentifier = " + imageIdentifier);

        byte[] finalImageData = ProtocolTools.concatByteArrays(encryptedImage);

        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }
        handleFWUpgrade(it, finalImageData, imageIdentifier);
        journal(Level.INFO, "Firmware upgrade successful.");
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Firmware upgrade successful.");
        return collectedMessage;
    }

    private void handleFWUpgrade(ImageTransfer it, byte[] imageData, String imageIdentifier) throws
            IOException {
        //it seems it's no different than non crypto in eiserver
        handleFWUpgradePhase0(it, imageData, imageIdentifier);
    }

    private MacResponse generateMacSingleBlock(IrreversibleKey FUAK, byte[] clearData, byte[] icv) throws HsmException {
        journal(Level.INFO, "generateMacSingleBlockBlock...");

        MacResponse macResponse = Services.hsmService().generateMacSingleBlock(FUAK, clearData, icv);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();

        journal(Level.INFO, String.format("generateMacSingleBlockBlock - Init vector value: %s", Arrays.toString(initVector)));
        journal(Level.INFO, String.format("generateMacSingleBlockBlock - Mac value: %s", Arrays.toString(macValue)));
        return macResponse;
    }

    private MacResponse generateMacFirstBlock(IrreversibleKey FUAK, byte[] clearData) throws HsmException {
        journal(Level.INFO, "generateMacFirstBlock...");
        MacResponse macResponse = Services.hsmService().generateMacFirstBlock(FUAK, clearData);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, String.format("generateMacFirstBlock - Init vector value: %s", Arrays.toString(initVector)));
        journal(Level.INFO, String.format("generateMacFirstBlock - Mac value: %s", Arrays.toString(macValue)));
        return macResponse;
    }

    private MacResponse generateMacMiddleBlock(IrreversibleKey FUAK, byte[] clearData, byte[] state) throws
            HsmException {
        journal(Level.INFO, "generateMacMiddleBlock...");
        MacResponse macResponse = Services.hsmService().generateMacMiddleBlock(FUAK, clearData, state);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, String.format("generateMacMiddleBlock - Init vector value: %s", Arrays.toString(initVector)));
        journal(Level.INFO, String.format("generateMacMiddleBlock - Mac value: %s", Arrays.toString(macValue)));
        return macResponse;
    }

    private MacResponse generateMacLastBlock(IrreversibleKey FUAK, byte[] clearData, byte[] icv, byte[] state) throws
            HsmException {
        journal(Level.INFO, "generateMacLastBlock...");
        MacResponse macResponse = Services.hsmService().generateMacLastBlock(FUAK, clearData, icv, state);
        byte[] initVector = macResponse.getInitVector();
        byte[] macValue = macResponse.getData();
        journal(Level.INFO, String.format("generateMacLastBlock - Init vector value: %s", Arrays.toString(initVector)));
        journal(Level.INFO, String.format("generateMacLastBlock - Mac value: %s", Arrays.toString(macValue)));
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

    private static class FUAKKeyData {

        private final byte[] keyData;
        private final byte[] keyLabel;
        private final byte[] mdmSmWK;

        FUAKKeyData(byte[] keyData, byte[] keyLabel, byte[] mdmSmWK) {
            this.keyData = keyData;
            this.keyLabel = keyLabel;
            this.mdmSmWK = mdmSmWK;
        }

        public byte[] getKeyData() {
            return keyData;
        }

        public byte[] getKeyLabel() {
            return keyLabel;
        }

        public byte[] getMdmSmWK() {
            return mdmSmWK;
        }
    }

    private static class P2KeyData {

        private final byte[] keyData;
        private final byte[] smartMeterKey;
        private final byte[] authenticationTag;

        P2KeyData(byte[] keyData, byte[] smartMeterKey, byte[] authenticationTag) {
            this.keyData = keyData;
            this.smartMeterKey = smartMeterKey;
            this.authenticationTag = authenticationTag;
        }

        public byte[] getKeyData() {
            return keyData;
        }

        public byte[] getSmartMeterKey() {
            return smartMeterKey;
        }

        public byte[] getAuthenticationTag() {
            return authenticationTag;
        }
    }

}


