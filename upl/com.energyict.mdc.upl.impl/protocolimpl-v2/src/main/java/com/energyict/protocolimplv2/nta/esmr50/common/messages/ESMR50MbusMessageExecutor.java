package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.encryption.AesGcm;
import com.energyict.encryption.BitVector;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MbusMessageExecutor;
import com.energyict.sercurity.KeyRenewalInfo;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.FUAKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusConfigurationSupport.DEFAULT_KEY;
import static com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusConfigurationSupport.FUAK;

public class ESMR50MbusMessageExecutor extends Dsmr40MbusMessageExecutor {

    private static final String MBUS_IMAGE_TRANSFER_OBIS_CODE = "0.x.44.0.0.255";
    private static final int KEY_LENGTH = 16;
    private static final byte KCC_LEN = 4;
    private final DeviceMasterDataExtractor deviceMasterDataExtractor;

    protected ImageFileHeaderFactory factory = null;

    public ESMR50MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                     IssueFactory issueFactory, DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(protocol, collectedDataFactory, issueFactory);
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
    }

    protected CollectedMessage doTransferMbusKeyPlain(MBusKeyID keyID, OfflineDeviceMessage pendingMessage) throws ProtocolException {

        final String serialNumber = pendingMessage.getDeviceSerialNumber();
        String newOpenKey = getMBusNewRandomKey();

        byte[] keyData;
        try {
            String mBusKey = getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), DEFAULT_KEY);
            if (keyID.equals(MBusKeyID.FUAK)) {
                final KeyRenewalInfo keyRenewalInfo = KeyRenewalInfo.fromJson(getDeviceMessageAttributeValue(pendingMessage, FUAKeyAttributeName));
                newOpenKey = keyRenewalInfo.keyValue;
            }
            keyData = getKeyDataPhase0(keyID, mBusKey, newOpenKey, serialNumber);
            journal(Level.INFO,"Complete keyData " + ProtocolTools.getHexStringFromBytes(keyData));
        } catch (ConfigurationException e) {
            journal(Level.SEVERE, "Configuration Exception while preparing key data: " + e.getCause() + " : " + e.getMessage());
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            return collectedMessage;
        }
        return doSendKeys(pendingMessage, serialNumber, keyID, keyData, newOpenKey, newOpenKey);
    }

    private CollectedMessage doSendKeys(OfflineDeviceMessage pendingMessage, String serialNumber, MBusKeyID keyID, byte[] keyData, String newOpenKey, String msgInfoKeyData) {
        journal(Level.INFO,"Complete key data " + ProtocolTools.getHexStringFromBytes(keyData));
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            MBusClient mbusClient = getMBusClient(serialNumber);

            if (MBusKeyID.FUAK.equals(keyID)) {
                journal(Level.INFO, "Invoking FUAK method");
                mbusClient.transferFUAK(keyData);
                journal(Level.INFO, "Successfully wrote the new MBus FUAK");
            } else if (MBusKeyID.P2.equals(keyID)) {

                journal(Level.INFO, "Invoking transportKey method with the full key data");
                // this is to pass the key to the g-meter
                mbusClient.setTransportKey(keyData);

                journal(Level.INFO, "Invoking encryptionKey method to send the openKey: " + newOpenKey);
                // this is to store the key
                mbusClient.setEncryptionKey(ProtocolTools.getBytesFromHexString(newOpenKey,2));

                journal(Level.INFO, "Successfully wrote the new MBus P2 Key");
            }
            journal(Level.INFO, "Saving the messageInfo: " + msgInfoKeyData);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            collectedMessage.setDeviceProtocolInformation(msgInfoKeyData);
        } catch (IOException e) {
            String msg = "IOException while writing key:" + e.getCause() + e.getMessage();
            journal(Level.SEVERE, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
        }
        return collectedMessage;
    }

    protected String getDeviceProtocolPropertyValue(long mBusDeviceId, String propertyName) throws ConfigurationException {
        final Optional<Device> deviceOptional = deviceMasterDataExtractor.find(mBusDeviceId);

        if (deviceOptional.isPresent()) {
            final Device device = deviceOptional.get();
            final com.energyict.mdc.upl.properties.TypedProperties typedProperties = deviceMasterDataExtractor.protocolProperties(device);

            final String propertyValue = (String) typedProperties.getProperty(propertyName);

            if (propertyValue == null || propertyValue.length() == 0) {
                throw new ConfigurationException(propertyName + " is empty! Please fill in the " + propertyName + " property on the slave MBus meter properties.");
            }

            return propertyValue;
        } else {
            throw new ConfigurationException("Could not find slave device on master configuration. Topology may not be in order.");
        }
    }

    private String getMBusNewRandomKey() {
        byte[] key = SecureRandom.getSeed(KEY_LENGTH);

        return ProtocolUtils.outputHexString(key).replace("$","");
    }

    private byte[] getKeyDataPhase0(MBusKeyID keyID, String encryptionKey, String newKey, String serialNumber) {
        byte[] kcc = getKCC();
        byte[] iv = getInitializationVector(kcc, serialNumber);

        BitVector keyPlain = new BitVector(newKey);
        BitVector keyPlainTexWithId = new BitVector(getKeyPlainText(keyID.getId(), keyPlain.getValue()));

        AesGcm aesGcm128 = new AesGcm(new BitVector(encryptionKey));
        aesGcm128.setPlainText(keyPlainTexWithId);
        aesGcm128.setInitializationVector(new BitVector(iv));
        aesGcm128.encrypt();

        BitVector keyEncrypted = aesGcm128.getCipherText();
        BitVector keyTag = aesGcm128.getTag();
        BitVector strippedTag = keyTag.Msb2(8); //only the 8 MSB are to be sent

//        log(Level.FINEST, "KEY content:");
//        log(Level.FINEST, " - iv ["+iv.length+"]: "+ProtocolTools.getHexStringFromBytes(iv));
//        log(Level.FINEST, " - kcc ["+kcc.length+"]: "+ProtocolTools.getHexStringFromBytes(kcc));
//        log(Level.FINEST, " - keyWithId ["+keyPlainTexWithId.length()+"]: "+ProtocolTools.getHexStringFromBytes(keyPlainTexWithId.getValue()));
//        log(Level.FINEST, " - keyEncrypted ["+keyEncrypted.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(keyEncrypted.getValue()));
//        log(Level.FINEST, " - gcmTag full ["+keyTag.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(keyTag.getValue()));
//        log(Level.FINEST, " - gcmTag stripped ["+strippedTag.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(strippedTag.getValue()));

        return ProtocolTools.concatByteArrays(kcc, keyEncrypted.getValue(), strippedTag.getValue());
    }

    protected byte[] getKCC() {
        Calendar Y2K = Calendar.getInstance(getProtocol().getTimeZone());
        Y2K.clear();
        Y2K.set(2000, Calendar.JANUARY,1,0,0,0);

        long now = Calendar.getInstance().getTime().getTime();
        long diff = (now - Y2K.getTime().getTime()) / 1000; // transform milliseconds to seconds

        return ProtocolTools.getBytesFromLong(diff, KCC_LEN);
    }

    protected byte[] getInitializationVector(byte[] kcc, String serialNumber) {
        MBusShortIdFactory shortIdFactory = new MBusShortIdFactory(serialNumber);
        return ProtocolUtils.concatByteArrays(shortIdFactory.getShortIdForKeyChange(), kcc);
    }

    private byte[] getKeyPlainText(byte keyId, byte[] key){
        byte keySize = (byte) key.length; // size of p2 key
        return ProtocolUtils.concatByteArrays(new byte[]{keyId, keySize}, key);
    }

    protected CollectedMessage doFirmwareUpgrade(OfflineDeviceMessage pendingMessage, String fuakKey) throws IOException {
        journal(Level.INFO, "Handling message Firmware upgrade");

        String serialNumber = pendingMessage.getDeviceSerialNumber();
        ObisCode imageTransferObisCode = getImageTransferObisCode(serialNumber);
        journal(Level.INFO, "Firmware Update: image transfer obis code:" + imageTransferObisCode);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(imageTransferObisCode);

        // activationDate will be NULL if immediate activation was requested
        Calendar activationDate = getActivationDateFromMessage(pendingMessage);

        if (activationDate == null) {
            journal(Level.FINE, "Empty activation date received, going to ask for immediate activation.");
        }
        String path = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateFileAttributeName);
        byte[] imageData = loadUserFile(path);

        byte[] fuak = ProtocolTools.getBytesFromHexString(fuakKey,2);

        journal(Level.FINE, "Initial Data:");
        if (activationDate == null) {
            journal(Level.FINE, "-activationDate: immediate");
        } else {
            journal(Level.FINE, "-activationDate: " + activationDate.getTime().toString());
        }
        journal(Level.FINE, "-imageData length:" + imageData.length);
        journal(Level.FINE, "-FUAK key:" + fuakKey);

        ArrayList<byte[]> preparedImageInfo = prepareFirmwareUpgradeImage(imageData, serialNumber, activationDate);
        byte[] updatedData = preparedImageInfo.get(0);
        byte[] iv = preparedImageInfo.get(1);
        byte[] shortId = preparedImageInfo.get(2);

        journal(Level.FINE, "Calculate FW GCM:");
        journal(Level.FINE, "- parameter 2 = data to authenticate (header+data) length = " + updatedData.length);
        journal(Level.FINE, "- parameter 3 = FUAK: " + ProtocolTools.getHexStringFromBytes(fuak)); // parameter 3 = fuak
        journal(Level.FINE, "- parameter 4 = IV: " + ProtocolTools.getHexStringFromBytes(iv)); // parameter 4 = initializationVector

        AesGcm aesGcm128 = new AesGcm(new BitVector(fuak));
        aesGcm128.setAdditionalAuthenticationData(new BitVector(updatedData)); //data to authenticate (parameter 2)
        aesGcm128.setInitializationVector(new BitVector(iv));

        journal(Level.FINE, "Calling AesGcm128");
        aesGcm128.encrypt();

        journal(Level.FINE, "Output:");

        byte[] MAC = aesGcm128.getTag().getValue();

        journal(Level.FINE, " - MAC: " + ProtocolTools.getHexStringFromBytes(MAC));

        byte[] encryptedData = ProtocolTools.concatByteArrays(updatedData, MAC);
        journal(Level.FINE, " - encryptedData length (image+MAC) " + encryptedData.length);

        int crcVal = CRCGenerator.calcCRCDirect(encryptedData);
        byte[] crc = ProtocolTools.getBytesFromInt(crcVal, 2);

        journal(Level.FINE, " - CRC = " + crcVal + ": " + ProtocolTools.getHexStringFromBytes(crc));

        String imageIdentifier = this.factory.getImageIdentifier(crc);

        journal(Level.FINE, " - imageIdentifier = " + imageIdentifier);

        byte[] finalImageData = ProtocolTools.concatByteArrays(encryptedData);
        journal(Level.FINE,"Final image data length (data+mac): " + finalImageData.length);
        handleFWUpgradePhase0(it, finalImageData, imageIdentifier);
        journal(Level.FINE,"Firmware upgrade successful.");

        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Firmware upgrade successful.");
        return collectedMessage;
    }

    // Phase 0 means non-crypto
    protected void handleFWUpgradePhase0(ImageTransfer it, byte[] imageData, String imageIdentifier) throws IOException {
        it.setBooleanValue(getBooleanValue());
        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);

        if (imageIdentifier != null && imageIdentifier.length() > 0) {
            it.upgrade(imageData, false, imageIdentifier, false);
        } else {
            it.upgrade(imageData, false);
        }
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    private int getBooleanValue() {
        return 0xFF;
    }

    /** Map the generic Obis Code of Class 18 M-Bus Image Transfer Channel x 0-x:44.0.0.255
     * to the correct channel where this MBus device is associated
     *
     * @return ObisCode with "x" replaced with 1-4 according to channel
     */
    protected ObisCode getImageTransferObisCode(String serialNumberMBus) {
        ObisCode  genericChannelObisCode = ObisCode.fromString(MBUS_IMAGE_TRANSFER_OBIS_CODE);
        journal(Level.INFO, "genericChannelObisCode:" + genericChannelObisCode);
        ObisCode mbusClientObisCode = getProtocol().getPhysicalAddressCorrectedObisCode(genericChannelObisCode, serialNumberMBus);
        journal(Level.INFO, "mbusClientObisCode:" + mbusClientObisCode);
        return mbusClientObisCode;
    }

    private Calendar getActivationDateFromMessage(OfflineDeviceMessage pendingMessage) {
        Calendar cal = Calendar.getInstance();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();   // Will return empty string if the MessageAttribute could not be found

        if (activationDate == null || activationDate.equals("")) {
            // returning null ==> this is a signal that an immediate activation is requested
            return null;
        }

        long messageValue = Long.parseLong(activationDate);
        if (messageValue > 9000000000L) {
            // the value is in milliseconds
            cal.setTimeInMillis(messageValue);
        } else {
            // the value is in seconds
            cal.setTimeInMillis(messageValue * 1000);
        }
        return cal;
    }

    protected byte[] loadUserFile(String path) {
        return ProtocolTools.readBytesFromFile(path);
    }

    protected ArrayList<byte[]> prepareFirmwareUpgradeImage(byte[] imageData, String serialNumber, Calendar activationDate) throws
            ProtocolException {
        journal(Level.FINE, "Loaded and decoding header:");
        this.factory = new ImageFileHeaderFactory(imageData, getProtocol().getLogger());

        journal(Level.FINE, factory.toString());

        byte[] kcc = getKCC();
        byte[] reversedKCC = kcc.clone();
        ProtocolTools.reverseByteArray(reversedKCC);
        journal(Level.FINE, "Updating image header with the new values:");

        factory.setImageVersion(reversedKCC); // reversed KCC here as requested by Harry
        factory.setAddressField(serialNumber);
        if (activationDate == null) {
            factory.setActivationType(ImageFileHeaderFactory.ACTIVATION_IMMEDIATE);
            factory.setActivationDate(null);
        } else {
            factory.setActivationType(ImageFileHeaderFactory.ACTIVATION_TIMED);
            factory.setActivationDate(activationDate);
        }

        byte[] updatedData = factory.getUpdatedImageData();

        byte[] shortId = factory.getShortIdFactory().getShortIdForFwUpgrade();
        byte[] iv = ProtocolTools.concatByteArrays(shortId, kcc);

        journal(Level.FINEST, "Prepare firmware image result: " + this.factory.toString());
        ArrayList<byte[]> ret = new ArrayList<>();
        ret.add(updatedData);
        ret.add(iv);
        ret.add(shortId);
        return ret;
    }

    protected String getFirmwareImageIdentifier(byte[] crc) {
        return this.factory.getImageIdentifier(crc);
    }

    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return false;
    }

    @Override
    protected CollectedMessage upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) {
        return upgradeFirmwareWithActivationDate(pendingMessage);
    }

    protected CollectedMessage upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) {
        try {
            return doFirmwareUpgrade(pendingMessage, getDeviceProtocolPropertyValue(pendingMessage.getDeviceId(), FUAK));
        } catch (Exception ex) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation(ex.getLocalizedMessage());
            return collectedMessage;
        }
    }

    protected void journal(Level logLevel, String message) {
        getProtocol().journal(logLevel, message);
    }

    @Override
    protected MBusClient getMBusClient(String serialNumber) throws IOException {
        return getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MBusClient.VERSION.VERSION1);
    }
}
