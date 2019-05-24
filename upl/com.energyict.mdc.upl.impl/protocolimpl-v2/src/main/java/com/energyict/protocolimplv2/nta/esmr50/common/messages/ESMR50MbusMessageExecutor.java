package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.encryption.AesGcm;
import com.energyict.encryption.BitVector;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MbusMessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50MbusClient;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Properties;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;

public class ESMR50MbusMessageExecutor extends Dsmr40MbusMessageExecutor {
    private static final String MBUS_IMAGE_TRANSFER_OBIS_CODE = "0.x.44.0.0.255";
    private static final int KEY_LENGTH = 16;
    private static final byte KCC_LEN = 4;

    protected ImageFileHeaderFactory factory = null;

    public ESMR50MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    public CollectedMessage doTransferMbusKeyPlain(MBusKeyID keyID, OfflineDeviceMessage pendingMessage){

        String serialNumber = pendingMessage.getDeviceSerialNumber();
        String newOpenKey = getNewMbusKey(keyID);

        byte[] keyData;
        try {
            String defaultKey = getMBusDefaultKey();
            keyData = getKeyDataPhase0(keyID, defaultKey, newOpenKey, serialNumber);
            log(Level.INFO,"Complete keyData "+ProtocolTools.getHexStringFromBytes(keyData));
        } catch (ConfigurationException e){
            log(Level.SEVERE, "Configuration Exception while preparing key data: " + e.getCause() + " : " + e.getMessage());
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            return collectedMessage;
        }
        return doSendKeys(pendingMessage, serialNumber, keyID, keyData, newOpenKey, newOpenKey);
    }

    protected CollectedMessage doSendKeys(OfflineDeviceMessage pendingMessage, String serialNumber, MBusKeyID keyID, byte[] keyData, String newOpenKey, String msgInfoKeyData) {
        log(Level.INFO,"Complete key data "+ProtocolTools.getHexStringFromBytes(keyData));
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try{
            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
            ESMR50MbusClient mBusClient5 = new ESMR50MbusClient(mbusClient.getProtocolLink(), mbusClient.getObjectReference(), MbusClientAttributes.VERSION9);

            if (MBusKeyID.FUAK.equals(keyID)) {
                log(Level.INFO, "Invoking FUAK method");
                mBusClient5.transferFUAK(keyData);
                log(Level.INFO, "Successfully wrote the new MBus FUAK");
            } else if (MBusKeyID.P2.equals(keyID)) {

                log(Level.INFO, "Invoking transportKey method with the full key data");
                // this is to pass the key to the g-meter
                mBusClient5.setTransportKey(keyData);

                log(Level.INFO, "Invoking encryptionKey method to send the openKey: "+newOpenKey);
                // this is to store the key
                mBusClient5.setEncryptionKey(ProtocolTools.getBytesFromHexString(newOpenKey,2));

                log(Level.INFO, "Successfully wrote the new MBus P2 Key");
            }
            log(Level.INFO, "Saving the messageInfo: "+msgInfoKeyData);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            collectedMessage.setDeviceProtocolInformation(msgInfoKeyData);
        } catch (Exception e) {
            String msg = "IO Exception while writing key:" + e.getCause()+ e.getMessage();
            log(Level.SEVERE, msg);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
        }
        return  collectedMessage;
    }

    protected String getMBusDefaultKey() throws ConfigurationException {

        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol)getProtocol();
        String defaultKey =((ESMR50Properties) protocol.getProperties()).getDefaultKey();

        if (defaultKey == null || defaultKey.length()==0){
            throw new ConfigurationException("DefaultKey is empty! Please fill in the DefaultKey property on the slave MBus meter properties.");
        }
        return defaultKey;
    }

    protected String getMBusFUAK() throws ConfigurationException{
        AbstractSmartNtaProtocol protocol = (AbstractSmartNtaProtocol)getProtocol();
        String fuak = ((ESMR50Properties) protocol.getProperties()).getFUAK();

        if (fuak == null || fuak.length()==0){
            throw new ConfigurationException("FirmwareUpgradeAuthenticationKey is empty! Please fill in the FirmwareUpgradeAuthenticationKey property on the slave MBus meter properties.");
        }
        return fuak;
    }

    protected String getNewMbusKey(MBusKeyID keyID) {
        if (MBusKeyID.FUAK.equals(keyID)){
            return getMBusNewRandomKey();
        }
        if (MBusKeyID.P2.equals(keyID)){
            return getMBusNewRandomKey();
        }

        return null;
    }

    protected String getMBusNewRandomKey(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = secureRandom.getSeed(KEY_LENGTH);

        return new String(ProtocolUtils.outputHexString(key).replace("$",""));
    }

    protected byte[] getKeyDataPhase0(MBusKeyID keyID, String encryptionKey, String newKey, String serialNumber) {
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
        Y2K.set(2000,0,1,0,0,0);

        long now = Calendar.getInstance().getTime().getTime();
        long diff = (now - Y2K.getTime().getTime()) / 1000; // transform milliseconds to seconds

        return ProtocolTools.getBytesFromLong(diff, KCC_LEN);
    }

    protected byte[] getInitializationVector(byte[] kcc, String serialNumber) {
        MBusShortIdFactory shortIdFactory = new MBusShortIdFactory(serialNumber);
        return ProtocolUtils.concatByteArrays(shortIdFactory.getShortIdForKeyChange(), kcc);
    }

    protected byte[] getKeyPlainText(byte keyId, byte[] key){
        byte keySize = (byte) key.length; // size of p2 key
        return ProtocolUtils.concatByteArrays(new byte[]{keyId, keySize}, key);
    }

    protected void log(final Level level, final String msg) {
        getProtocol().getDlmsSession().getLogger().log(level, msg);
    }

    protected CollectedMessage doFirmwareUpgrade(OfflineDeviceMessage pendingMessage, String fuakKey) throws IOException
    {
        log(Level.INFO, "Handling message Firmware upgrade");

        String serialNumber = pendingMessage.getDeviceSerialNumber();
        ObisCode imageTransferObisCode = getImageTransferObisCode(serialNumber);
        log(Level.INFO, "Firmware Update: image transfer obis code:" + imageTransferObisCode);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer(imageTransferObisCode);

        // activationDate will be NULL if immediate activation was requested
        Calendar activationDate = getActivationDateFromMessage(pendingMessage);

        if (activationDate == null){
            log(Level.FINE, "Empty activation date received, going to ask for immediate activation.");
        }
        String path = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateFileAttributeName);
        byte[] imageData = loadUserFile(path);

        byte[] fuak = ProtocolTools.getBytesFromHexString(fuakKey,2);

        log(Level.FINE, "Initial Data:");
        if (activationDate==null){
            log(Level.FINE, "-activationDate: immediate");
        } else {
            log(Level.FINE, "-activationDate: " + activationDate.getTime().toString());
        }
        log(Level.FINE, "-imageData length:"+imageData.length);
        log(Level.FINE, "-FUAK key:"+fuakKey);

        ArrayList<byte[]> preparedImageInfo = prepareFirmwareUpgradeImage(imageData, serialNumber, activationDate);
        byte[] updatedData = preparedImageInfo.get(0);
        byte[] iv = preparedImageInfo.get(1);
        byte[] shortId = preparedImageInfo.get(2);


        log(Level.FINE, "Calculate FW GCM:");
        log(Level.FINE, "- parameter 2 = data to authenticate (header+data) length= " + updatedData.length);
        log(Level.FINE, "- parameter 3 = FUAK: " + ProtocolTools.getHexStringFromBytes(fuak)); // parameter 3 = fuak
        log(Level.FINE, "- parameter 4 = IV: " + ProtocolTools.getHexStringFromBytes(iv)); // parameter 4 = initializationVector

        AesGcm aesGcm128 = new AesGcm(new BitVector(fuak));
        aesGcm128.setAdditionalAuthenticationData(new BitVector(updatedData)); //data to authenticate (parameter 2)
        aesGcm128.setInitializationVector(new BitVector(iv));

        log(Level.FINE, "Calling AesGcm128");
        aesGcm128.encrypt();

        log(Level.FINE, "Output:");

        byte[] MAC = aesGcm128.getTag().getValue();

        log(Level.FINE, " - MAC: "+ProtocolTools.getHexStringFromBytes(MAC));

        byte[] encryptedData = ProtocolTools.concatByteArrays(updatedData, MAC);
        log(Level.FINE, " - encryptedData length (image+MAC) "+encryptedData.length);

        int crcVal = CRCGenerator.calcCRCDirect(encryptedData);
        byte[] crc = ProtocolTools.getBytesFromInt(crcVal, 2);

        log(Level.FINE, " - CRC = "+crcVal+": "+ProtocolTools.getHexStringFromBytes(crc));

        String imageIdentifier = this.factory.getImageIdentifier(crc);

        log(Level.FINE, " - imageIdentifier = "+imageIdentifier);

        byte[] finalImageData = ProtocolTools.concatByteArrays(encryptedData);
        log(Level.FINE,"Final image data length (data+mac): " + finalImageData.length);
        String activationDateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();
        handleFWUpgradePhase0(activationDateString, it, finalImageData, imageIdentifier);
        log(Level.FINE,"Firmware upgrade successful.");
        //return MessageResult.createSuccess(messageEntry, "Firmware upgrade successful.");
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Firmware upgrade successful.");
        return collectedMessage;
    }

    //Phase 0 means non-crypto
    protected void handleFWUpgradePhase0(String activationDate, ImageTransfer it, byte[] imageData, String imageIdentifier) throws IOException {
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
    protected int getBooleanValue() {
        return 0xFF;
    }

    /** Map the generic Obis Code of Class 18 M-Bus Image Transfer Channel x 0-x:44.0.0.255
     * to the correct channel where this MBus device is associated
     *
     * @return ObisCode with "x" replaced with 1-4 according to channel
     * @throws IOException
     */
    public ObisCode getImageTransferObisCode(String serialNumberMBus) throws IOException {
        ObisCode  genericChannelObisCode = ObisCode.fromString(MBUS_IMAGE_TRANSFER_OBIS_CODE);
        log(Level.INFO, "genericChannelObisCode:" + genericChannelObisCode);
        ObisCode mbusClientObisCode = getProtocol().getPhysicalAddressCorrectedObisCode(genericChannelObisCode, serialNumberMBus);
        log(Level.INFO, "mbusClientObisCode:" + mbusClientObisCode);
        return mbusClientObisCode;
    }

    private Calendar getActivationDateFromMessage(OfflineDeviceMessage pendingMessage) {
        Calendar cal = Calendar.getInstance();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName)
                .getValue();   // Will return empty string if the MessageAttribute could not be found

        if (activationDate == null || activationDate.equals("")){
            // returning null ==> this is a signal that an immediate activation is requested
            return null;
        }

        long messageValue = Long.parseLong(activationDate);
        if (messageValue>9000000000L){
            // the value is in milliseconds
            cal.setTimeInMillis(messageValue);
        } else {
            // the value is in seconds
            cal.setTimeInMillis(messageValue * 1000);
        }
        return cal;
    }

    protected byte[] loadUserFile(String path) throws IOException {
        return ProtocolTools.readBytesFromFile(path);
    }

    protected ArrayList<byte[]> prepareFirmwareUpgradeImage(byte[] imageData, String serialNumber, Calendar activationDate) throws
            ProtocolException {
        log(Level.FINE, "Loaded and decoding header:");
        this.factory = new ImageFileHeaderFactory(imageData, getProtocol().getLogger());

        log(Level.FINE, factory.toString());

        byte[] kcc = getKCC();
        byte[] reversedKCC = getKCC();
        ProtocolTools.reverseByteArray(reversedKCC);
        log(Level.FINE, "Updating image header with the new values:");

        factory.setImageVersion(reversedKCC); // reversed KCC here as requested by Harry
        factory.setAddressField(serialNumber);
        if (activationDate == null){
            factory.setActivationType(ImageFileHeaderFactory.ACTIVATION_IMMEDIATE);
            factory.setActivationDate(null);
        } else {
            factory.setActivationType(ImageFileHeaderFactory.ACTIVATION_TIMED);
            factory.setActivationDate(activationDate);
        }


        byte[] updatedData = factory.getUpdatedImageData();

        byte[] shortId = factory.getShortIdFactory().getShortIdForFwUpgrade();
        byte[] iv = ProtocolTools.concatByteArrays(shortId, kcc);

        log(Level.FINEST, "Prepare firmware image result: " + this.factory.toString());
        ArrayList<byte[]> ret = new ArrayList<byte[]>();
        ret.add(updatedData);
        ret.add(iv);
        ret.add(shortId);
        return ret;
    }

    protected String getFirmareImageIdentifier(byte[] crc) {
        return this.factory.getImageIdentifier(crc);
    }

    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return false;
    }
}
