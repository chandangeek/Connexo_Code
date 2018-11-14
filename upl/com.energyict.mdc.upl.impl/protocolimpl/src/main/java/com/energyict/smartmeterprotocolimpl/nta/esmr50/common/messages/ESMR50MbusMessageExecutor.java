package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MbusMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.LoadProfileToRegisterParser;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50MbusClient;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Properties;
import org.xml.sax.SAXException;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

@Deprecated
public class ESMR50MbusMessageExecutor extends Dsmr40MbusMessageExecutor {

    private static final String MBUS_IMAGE_TRANSFER_OBIS_CODE = "0.x.44.0.0.255";
    private static final int TAG_SIZE = 12;
    private static final byte KCC_LEN = 4;
    private static final int SHORT_ID_LEN = 8;
    private static final int KEY_LENGTH = 16;
    protected final DlmsSession dlmsSession;
    protected final AbstractSmartNtaProtocol protocol;

    protected ImageFileHeaderFactory factory = null;


    public ESMR50MbusMessageExecutor(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
        this.protocol = protocol;
        this.dlmsSession = this.protocol.getDlmsSession();
    }



    @Override
    protected MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        //todo doReadLoadProfileRegisters
//        try {
//            log(Level.INFO, "ESMR 5.0 Handling message Read LoadProfile Registers.");
//            LoadProfileRegisterMessageBuilder builder = this.protocol.getLoadProfileRegisterMessageBuilder();
//            builder = (LoadProfileRegisterMessageBuilder) builder.fromXml(msgEntry.getContent());
//
//            String slaveSerialNumber = msgEntry.getSerialNumber();
//            ObisCode rawLoadProfileObisCode = ObisCode.fromString(builder.getProfileObisCode().getValue());
//            ObisCode loadProfileObisCode = getProtocol().getPhysicalAddressCorrectedObisCode(rawLoadProfileObisCode, slaveSerialNumber);
//            try {
//                builder.getLoadProfileReader().getProfileObisCode().setValue(loadProfileObisCode.getValue());
//                log(Level.INFO, "Converting obisCode ["+rawLoadProfileObisCode.toString()+"] for the slave meter ["+slaveSerialNumber+"] => "+loadProfileObisCode.toString());
//            } catch (ParseException e) {
//                log(Level.WARNING, "Cannot set translated obis code: "+e.getMessage());
//            }
//            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
//                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode()+ "!");
//            }
//
//            LoadProfileBuilder loadProfileBuilder = this.protocol.getLoadProfileBuilder();
//            if (loadProfileBuilder instanceof LGLoadProfileBuilder) {
//                ((LGLoadProfileBuilder) loadProfileBuilder).setFixMBusToDate(false);    //Don't subtract 15 minutes from the to date
//            }
//
//            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), msgEntry);
//            final List<LoadProfileConfiguration> loadProfileConfigurations = this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
//            final List<ProfileData> profileDatas = this.protocol.getLoadProfileData(Arrays.asList(lpr));
//
//            if (profileDatas.size() != 1) {
//                return MessageResult.createFailed(msgEntry, "We are supposed to receive 1 LoadProfile configuration in this message, but we received " + profileDatas.size());
//            }
//
//            ProfileData pd = profileDatas.get(0);
//            IntervalData id = null;
//            for (IntervalData intervalData : pd.getIntervalDatas()) {
//                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
//                    id = intervalData;
//                }
//            }
//
//            if (id == null) {
//                return MessageResult.createFailed(msgEntry, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")");
//            }
//
//            com.energyict.protocol.Register previousRegister = null;
//            MeterReadingData mrd = new MeterReadingData();
//            for (com.energyict.protocol.Register register : builder.getRegisters()) {
//                if (register.equals(previousRegister)) {
//                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
//                }
//                for (int i = 0; i < pd.getChannelInfos().size(); i++) {
//                    final ChannelInfo channel = pd.getChannel(i);
//                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
//                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), builder.getRtuRegisterIdForRegister(register));
//                        mrd.add(registerValue);
//                    }
//                }
//                previousRegister = register;
//            }
//
//            MeterData md = new MeterData();
//            md.setMeterReadingData(mrd);
//
//            log(Level.INFO, "Message Read LoadProfile Registers Finished.");
//            MeterDataMessageResult messageResult = MeterDataMessageResult.createSuccess(msgEntry, "", md);
//            return new LoadProfileToRegisterParser(ObisCode.fromString("0.x.24.2.3.255")).parse(messageResult);
//        } catch (SAXException e) {
//            return MessageResult.createFailed(msgEntry, "Could not parse the content of the xml message, probably incorrect message.");
//        } catch (IOException e) {
//            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
//        }

        return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
    }

//    protected MeteringWarehouse mw() {
//        return ProtocolTools.mw(); todo See replacement for metering warehouse
//    }

    protected boolean isResume(MessageEntry messageEntry) {
        return false;
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }

    protected boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    /**
     * Convert the given unix activation date to a proper DateTimeArray
     */
    protected Array convertActivationDateUnixToDateTimeArray(String strDate) throws IOException {
        return convertUnixToDateTimeArray(strDate);
    }


   /** Map the generic Obis Code of Class 18 M-Bus Image Transfer Channel x 0-x:44.0.0.255
            * to the correct channel where this MBus device is associated
    *
            * @return ObisCode with "x" replaced with 1-4 according to channel
    * @throws IOException
    */
    public ObisCode getImageTransferObisCode(String serialNumberMBus) throws IOException {
        ObisCode    genericChannelObisCode = ObisCode.fromString(MBUS_IMAGE_TRANSFER_OBIS_CODE);
        log(Level.INFO, "genericChannelObisCode:" + genericChannelObisCode);
        ObisCode mbusClientObisCode = this.protocol.getPhysicalAddressCorrectedObisCode(genericChannelObisCode, serialNumberMBus);
        log(Level.INFO, "mbusClientObisCode:" + mbusClientObisCode);
        return mbusClientObisCode;
    }

    public MessageResult executeMessageEntry(MessageEntry msgEntry) {
        //todo rewrite executeMessage
//        String content = msgEntry.getContent();
//        MessageHandler messageHandler = getMessageHandler();
        MessageResult msgResult = null;
//        try {
//
//            boolean firmwareUpgrade = content.contains(RtuMessageConstant.MBUS_ESMR5_FIRMWARE_UPGRADE);
//            boolean mbusEncryption = content.contains(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
//            boolean mbusCryptoEncryption = content.contains(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS);
//            boolean mbusTransferFUAK = content.contains(RtuMessageConstant.MBUS_TRANSFER_FUAK);
//            boolean mbusTransferP2Key = content.contains(RtuMessageConstant.MBUS_TRANSFER_P2KEY);
//
//            if (firmwareUpgrade) {
//                String fuakKey = getProtocol().getProperties().getProtocolProperties().getProperty(ESMR50Properties.FIRMWARE_UPGRADE_AUTHENTICATION_KEY);
//                if (fuakKey == null || fuakKey.length()==0){
//                    throw new ConfigurationException("FUAK is empty! Please fill in the FUAK property on the slave MBus meter properties.");
//                }
//                msgResult = doFirmwareUpgrade(messageHandler, msgEntry, fuakKey);
//            }else if (mbusEncryption) {
//                String serialNumber = msgEntry.getSerialNumber();
//                super.setMbusEncryptionKeys(messageHandler, serialNumber);
//            }else if (mbusCryptoEncryption) {
//                String serialNumber = msgEntry.getSerialNumber();
//                setCryptoserverMbusEncryptionKeys(msgEntry, messageHandler, serialNumber);
//            }else if (mbusTransferFUAK) {
//                msgResult = doTransferFUAK(messageHandler, msgEntry);
//            }else if (mbusTransferP2Key) {
//                msgResult = doTransferP2Key(messageHandler, msgEntry);
//            }  else{
//                msgResult = super.executeMessageEntry(msgEntry);
//            }
//
//            // Some message create their own messageResult
//            if (msgResult == null) {
//                msgResult = MessageResult.createSuccess(msgEntry);
//                log(Level.INFO, "Message has finished.");
//            } else if (msgResult.isFailed()) {
//                log(Level.SEVERE, "Message failed : " + msgResult.getInfo());
//            }
//        } catch (IOException e) {
//            msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
//            log(Level.SEVERE, "Message failed : " + e.getMessage());
//        } catch (ConfigurationException e) {
//            msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
//            log(Level.SEVERE, "Message failed : " + e.getMessage());
//        }
        return msgResult;
    }

    protected MessageResult doFirmwareUpgrade(MessageHandler messageHandler, MessageEntry messageEntry, String fuakKey) throws IOException, ConfigurationException {
        log(Level.INFO, "Handling message Firmware upgrade");
            //todo rewrite doFirmwareUpgrade
//        String serialNumber = messageEntry.getSerialNumber();
//        ObisCode imageTransferObisCode = getImageTransferObisCode(messageEntry.getSerialNumber());
//        log(Level.INFO, "Firmware Update: image transfer obis code:" + imageTransferObisCode);
//        ImageTransfer it = getCosemObjectFactory().getImageTransfer(imageTransferObisCode);
//
//        // activationDate will be NULL if immediate activation was requested
//        Calendar activationDate = getActivationDateFromMessage(messageHandler);
//
//        if (activationDate == null){
//            log(Level.FINE, "Empty activation date received, going to ask for immediate activation.");
//        }
//
//        byte[] imageData = loadUserFile(messageHandler.getUserFileId());
//
//        byte[] fuak = ProtocolTools.getBytesFromHexString(fuakKey,2);
//
//        log(Level.FINE, "Initial Data:");
//        if (activationDate==null){
//            log(Level.FINE, "-activationDate: immediate");
//        } else {
//            log(Level.FINE, "-activationDate: " + activationDate.getTime().toString());
//        }
//        log(Level.FINE, "-imageData length:"+imageData.length);
//        log(Level.FINE, "-FUAK key:"+fuakKey);
//
//        ArrayList<byte[]> preparedImageInfo = prepareFirmwareUpgradeImage(imageData, serialNumber, activationDate);
//        byte[] updatedData = preparedImageInfo.get(0);
//        byte[] iv = preparedImageInfo.get(1);
//        byte[] shortId = preparedImageInfo.get(2);
//
//
//        log(Level.FINE, "Calculate FW GCM:");
//        log(Level.FINE, "- parameter 2 = data to authenticate (header+data) length= " + updatedData.length);
//        log(Level.FINE, "- parameter 3 = FUAK: " + ProtocolTools.getHexStringFromBytes(fuak)); // parameter 3 = fuak
//        log(Level.FINE, "- parameter 4 = IV: " + ProtocolTools.getHexStringFromBytes(iv)); // parameter 4 = initializationVector
//
//        AesGcm128 aesGcm128 = new AesGcm128(new BitVector(fuak));
//        aesGcm128.setAdditionalAuthenticationData(new BitVector(updatedData)); //data to authenticate (parameter 2)
//        aesGcm128.setInitializationVector(new BitVector(iv));
//
//        log(Level.FINE, "Calling AesGcm128");
//        aesGcm128.encrypt();
//
//        log(Level.FINE, "Output:");
//
//        byte[] MAC = aesGcm128.getTag().getValue();
//
//        log(Level.FINE, " - MAC: "+ProtocolTools.getHexStringFromBytes(MAC));
//
//        byte[] encryptedData = ProtocolTools.concatByteArrays(updatedData, MAC);
//        log(Level.FINE, " - encryptedData length (image+MAC) "+encryptedData.length);
//
//        int crcVal = CRCGenerator.calcCRCDirect(encryptedData);
//        byte[] crc = ProtocolTools.getBytesFromInt(crcVal, 2);
//
//        log(Level.FINE, " - CRC = "+crcVal+": "+ProtocolTools.getHexStringFromBytes(crc));
//
//        String imageIdentifier = getFirmareImageIdentified(crc);
//
//        log(Level.FINE, " - imageIdentifier = "+imageIdentifier);
//
//        byte[] finalImageData = ProtocolTools.concatByteArrays(encryptedData);
//        log(Level.FINE,"Final image data length (data+mac): " + finalImageData.length);
//
//        handleFWUpgradePhase0(messageHandler.getActivationDate(), it, finalImageData, imageIdentifier);
        return MessageResult.createSuccess(messageEntry, "Firmware upgrade successful.");
    }

    private Calendar getActivationDateFromMessage(MessageHandler messageHandler) {
        Calendar cal = Calendar.getInstance();
        String activationDateParam = messageHandler.getActivationDate();

        if (activationDateParam == null || activationDateParam.equals("")){
            // returning null ==> this is a signal that an immediate activation is requested
            return null;
        }

        long messageValue = Long.parseLong(activationDateParam);
        if (messageValue>9000000000L){
            // the value is in milliseconds
            cal.setTimeInMillis(messageValue);
        } else {
            // the value is in seconds
            cal.setTimeInMillis(messageValue * 1000);
        }
        return cal;
    }

    protected String getFirmareImageIdentified(byte[] crc) {
        return this.factory.getImageIdentifier(crc);
    }

    protected ArrayList<byte[]> prepareFirmwareUpgradeImage(byte[] imageData, String serialNumber, Calendar activationDate) {

        log(Level.FINE, "Loaded and decoding header:");
        this.factory = new ImageFileHeaderFactory(imageData, protocol.getLogger());

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
//todo are use files used in Connexo
//    private byte[] loadUserFile(String userFileID) throws IOException {
//        if (!ParseUtils.isInteger(userFileID)) {
//            String str = "Not a valid entry for the userFile.";
//            throw new IOException(str);
//        }
//        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
//        if (uf == null) {
//            String str = "Not a valid entry for the userfileID " + userFileID;
//            throw new IOException(str);
//        }
//        return uf.loadFileInByteArray();

//    }

    private void handleFWUpgradePhase0(String activationDate, ImageTransfer it, byte[] imageData, String imageIdentifier) throws IOException {
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



    public MessageResult doTransferP2Key(MessageHandler handler, MessageEntry msgEntry) {
        return this.doTransferMbusKeyPlain(MBusKeyID.P2, handler, msgEntry);
    }

    public MessageResult doTransferFUAK(MessageHandler handler, MessageEntry msgEntry) {
        return this.doTransferMbusKeyPlain(MBusKeyID.FUAK, handler, msgEntry);
    }

    public MessageResult doTransferMbusKeyPlain(MBusKeyID keyID, MessageHandler handler, MessageEntry msgEntry) {
        log(Level.INFO, "Handling MbusMessage Transfer "+keyID.getName()+" - plain phase 0");

        String serialNumber = msgEntry.getSerialNumber();
        String newOpenKey = getNewMbusKey(keyID, serialNumber, handler);

        log(Level.FINEST, "New Key: ["+newOpenKey+"]");
        byte[] keyData = new byte[0];

        try {
            String defaultKey = getMBusDefaultKey(serialNumber);

            keyData = getKeyDataPhase0(keyID, defaultKey, newOpenKey, serialNumber);
            log(Level.INFO,"Complete keyData "+ProtocolTools.getHexStringFromBytes(keyData));
        } catch (Exception e){
            String msg =  "General exception while preparing key data:" + e.getMessage();
            log(Level.SEVERE, msg);
            return MessageResult.createFailed(msgEntry, msg);
        }

        return doSendKeys(msgEntry, serialNumber, keyID, keyData, newOpenKey, newOpenKey);


    }

    protected MessageResult doSendKeys(MessageEntry msgEntry, String serialNumber, MBusKeyID keyID, byte[] keyData, String newOpenKey, String msgInfoKeyData) {
        log(Level.INFO,"Complete key data "+ProtocolTools.getHexStringFromBytes(keyData));

        try{
            MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
            ESMR50MbusClient mBusClient5 = new ESMR50MbusClient(mbusClient.getProtocolLink(), mbusClient.getObjectReference(), 0); // todo

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
            return MessageResult.createSuccess(msgEntry, msgInfoKeyData);
        } catch (Exception e) {
            String msg = "IO Exception while writing key:" + e.getCause()+ e.getMessage();
            log(Level.SEVERE, msg);
            return MessageResult.createFailed(msgEntry, msg);
        }
    }


    protected String getNewMbusKey(MBusKeyID keyID, String serialNumber, MessageHandler handler) {
        log(Level.FINEST, "Generating a new "+keyID.getName()+ " random key.");
        if (MBusKeyID.FUAK.equals(keyID)){
            return getMBusNewRandomKey();
        }
        if (MBusKeyID.P2.equals(keyID)){
            return getMBusNewRandomKey();
        }

        return null;
    }

    protected String getMBusDefaultKey(String serialNumber) throws ConfigurationException{
//        List<Rtu> mbusDevices = MeteringWarehouse.getCurrent().getRtuFactory().findBySerialNumber(serialNumber);
//        if (mbusDevices==null){
//            log(Level.SEVERE, "MBus device with serial "+serialNumber+" not found");
//            return null;
//        }
//
//        if (mbusDevices.size()>1){
//            log(Level.SEVERE, "Multiple MBus device with serial "+serialNumber+" found: "+mbusDevices.toString());
//        }
//
//        Rtu mbus = mbusDevices.get(0);
//        String defaultKey = mbus.getProperties().getProperty(ESMR50Properties.DEFAULT_KEY);
//        if (defaultKey == null || defaultKey.length()==0){
//            throw new ConfigurationException("DefaultKey is empty! Please fill in the DefaultKey property on the slave MBus meter properties.");
//        }
//        return defaultKey;
        //todo rewrite getMBusDefaultKey
        return "";
    }


    protected String getMBusFUAK(String serialNumber) throws ConfigurationException{
//        List<Rtu> mbusDevices = MeteringWarehouse.getCurrent().getRtuFactory().findBySerialNumber(serialNumber);
//        if (mbusDevices==null){
//            log(Level.SEVERE, "MBus device with serial "+serialNumber+" not found");
//            return null;
//        }
//
//        if (mbusDevices.size()>1){
//            log(Level.SEVERE, "Multiple MBus device with serial "+serialNumber+" found: "+mbusDevices.toString());
//        }
//
//        Rtu mbus = mbusDevices.get(0);
//        String fuak = mbus.getProperties().getProperty(ESMR50Properties.FIRMWARE_UPGRADE_AUTHENTICATION_KEY);
//        if (fuak == null || fuak.length()==0){
//            throw new ConfigurationException("FirmwareUpgradeAuthenticationKey is empty! Please fill in the FirmwareUpgradeAuthenticationKey property on the slave MBus meter properties.");
//        }
//        return fuak;
        //todo rewrite getMBusFuak
        return "";
    }

    protected String getMBusNewRandomKey(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = secureRandom.getSeed(KEY_LENGTH);

        return new String(ProtocolUtils.outputHexString(key).replace("$",""));
    }

    protected byte[] getKeyDataPhase0(MBusKeyID keyID, String encryptionKey, String newKey, String serialNumber) {
//        byte[] kcc = getKCC();
//        byte[] iv = getInitializationVector(kcc, serialNumber);
//
//        BitVector keyPlain = new BitVector(newKey);
//        BitVector keyPlainTexWithId = new BitVector(getKeyPlainText(keyID.getId(), keyPlain.getValue()));
//
//        AesGcm128 aesGcm128 = new AesGcm128(new BitVector(encryptionKey));
//        aesGcm128.setPlainText(keyPlainTexWithId);
//        aesGcm128.setInitializationVector(new BitVector(iv));
//
//        aesGcm128.encrypt();
//
//        BitVector keyEncrypted = aesGcm128.getCipherText();
//        BitVector keyTag = aesGcm128.getTag();
//        BitVector strippedTag = keyTag.Msb2(8); //only the 8 MSB are to be sent
//
//        log(Level.FINEST, "KEY content:");
//        log(Level.FINEST, " - iv ["+iv.length+"]: "+ProtocolTools.getHexStringFromBytes(iv));
//        log(Level.FINEST, " - kcc ["+kcc.length+"]: "+ProtocolTools.getHexStringFromBytes(kcc));
//        log(Level.FINEST, " - keyWithId ["+keyPlainTexWithId.length()+"]: "+ProtocolTools.getHexStringFromBytes(keyPlainTexWithId.getValue()));
//        log(Level.FINEST, " - keyEncrypted ["+keyEncrypted.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(keyEncrypted.getValue()));
//        log(Level.FINEST, " - gcmTag full ["+keyTag.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(keyTag.getValue()));
//        log(Level.FINEST, " - gcmTag stripped ["+strippedTag.getValue().length+"]: "+ProtocolTools.getHexStringFromBytes(strippedTag.getValue()));
//
//        return ProtocolTools.concatByteArrays(kcc, keyEncrypted.getValue(), strippedTag.getValue());
          //todo write again keyDtaPhase0
          return new byte[0];
    }


    protected byte[] getInitializationVector(byte[] kcc, String serialNumber) {
        MBusShortIdFactory shortIdFactory = new MBusShortIdFactory(serialNumber);
        return ProtocolUtils.concatByteArrays(shortIdFactory.getShortIdForKeyChange(), kcc);
    }

    protected byte[] getKeyPlainText(byte keyId, byte[] key){
        byte keySize = (byte) key.length; // size of p2 key
        return ProtocolUtils.concatByteArrays(new byte[]{keyId, keySize}, key);
    }

    protected byte[] getKCC() {
        Calendar Y2K = Calendar.getInstance(getProtocol().getTimeZone());
        Y2K.clear();
        Y2K.set(2000,0,1,0,0,0);

        long now = Calendar.getInstance().getTime().getTime();
        long diff = (now - Y2K.getTime().getTime()) / 1000; // transform milliseconds to seconds

        return ProtocolTools.getBytesFromLong(diff, KCC_LEN);
    }
}