package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;


import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Protocol;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums.LTEPingAddress;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

@Deprecated
public class ESMR50MessageExecutor extends Dsmr40MessageExecutor {
    public static final String MBUS_DAILY_BILLING_OBISCODE = "0.x.99.2.0.255";
    public static final String MBUS_MONTHLY_BILLING_OBISCODE = "0.x.98.1.0.255";
    public static final String MBUS_LOAD_PROFILE_PERIOD_1 = "0.x.24.3.0.255";
    public static final String MBUS_CONFIGURATION_OBJECT = "1.1.94.31.3.255";

    private static final ObisCode LTE_IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.5.44.0.0.255");

    public ESMR50MessageExecutor(AbstractSmartNtaProtocol protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, NumberLookupExtractor numberLookupExtractor, NumberLookupFinder numberLookupFinder) {
        super(protocol, calendarFinder, extractor, messageFileFinder, messageFileExtractor, numberLookupExtractor, numberLookupFinder);
    }


    @Override
    public MessageResult executeMessageEntry(MessageEntry msgEntry) throws ConnectionException, NestedIOException {
        try { //todo try catch added
            if (!this.protocol.getMeterSerialNumber().equalsIgnoreCase(msgEntry.getSerialNumber())) {
                //Execute messages for MBus device
                ESMR50MbusMessageExecutor mbusMessageExecutor = getMbusMessageExecutor();
                return mbusMessageExecutor.executeMessageEntry(msgEntry);
            } else {
                MessageResult result = handleESMR5Messages(msgEntry);
                if (result != null){
                    return result;
                }
                return super.executeMessageEntry(msgEntry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MessageResult handleESMR5Messages(MessageEntry msgEntry)  {
        MessageHandler messageHandler = getMessageHandler();
//        try {
//            importMessage(msgEntry.getContent(), messageHandler);
//            if (messageHandler.getType().equals(RtuMessageConstant.MBUS_CHANGE_CONFIGURATION_OBJECT)) {
//                return doMbusChangeConfigurationObject(msgEntry, messageHandler.getMBusChangeConfiguration_bit11());
//            }
//            if (messageHandler.getType().equals(RtuMessageConstant.SET_LTE_PING_ADDRESS)){
//                return doSetLtePingAddress(msgEntry, messageHandler.getLTEPingAddress());
//            }
//
//            if (messageHandler.getType().equals(RtuMessageConstant.SET_LTE_APN)){
//                return doSetLteApn(msgEntry, messageHandler.getLteApn());
//            }
//
//        }catch (Exception ex){
//            protocol.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
//        }

        return null;
    }

    private MessageResult doSetLteApn(MessageEntry msgEntry, String lteApn) {
        try {
            log(Level.INFO, "Changing LTE APN to: ["+lteApn+"]");

            PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);

            if(lteApn != null){
//                getCosemObjectFactory().getLTEModemSetup(LTEModemSetup.getDefaultObisCode()).writeAPN(lteApn);
            }
        }catch (Exception ex){
            return MessageResult.createFailed(msgEntry, ex.getMessage());
        }
        return MessageResult.createSuccess(msgEntry, lteApn);
    }

    private MessageResult doSetLtePingAddress(MessageEntry msgEntry, String ltePingAddressFull) {
        try {
            String[] parts = ltePingAddressFull.split(":");
            if (parts.length!=2){
                throw  new Exception("LTE Ping Address must have format xx.xx.xx.xx:port");
            }
            LTEPingAddress ltePingAddress = new LTEPingAddress(parts[0], parts[1]);
            Data ltePingObject = getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_PING_ADDRESS);
            ltePingObject.setValueAttr(ltePingAddress.getStructure());
        }catch (Exception ex){
            return MessageResult.createFailed(msgEntry, ex.getMessage());
        }
        return MessageResult.createSuccess(msgEntry,ltePingAddressFull);
    }

    private MessageResult doMbusChangeConfigurationObject(MessageEntry msgEntry, int bit11) {
        ObisCode configObject = ObisCode.fromString(MBUS_CONFIGURATION_OBJECT);

        protocol.getLogger().finest("Writing MBus Change Configuration object bit 11 to {"+bit11+"}");

        try {
            Data data = getCosemObjectFactory().getData(configObject);
            protocol.getLogger().finest("Reading existing value ...");
            AbstractDataType abstractDataType = data.getValueAttr();
            if (abstractDataType.isStructure()){
                Structure structure = abstractDataType.getStructure();
                BitString flags = structure.getNextDataType().getBitString();
                protocol.getLogger().finest(" - existing flags: 0x"+Long.toHexString(flags.longValue()));
                flags.set(11, bit11==1);
                structure.setDataType(0, flags);
                protocol.getLogger().finest(" - new flags are: 0x"+Long.toHexString(flags.longValue())+" - saving back to the meter ...");
                data.setValueAttr(structure);
                protocol.getLogger().finest(" - done!");
                return MessageResult.createSuccess(msgEntry, "New flags: 0x"+Long.toHexString(flags.longValue()));
            } else {
                protocol.getLogger().finest(" - not a structure!" + abstractDataType.toString());
                return MessageResult.createFailed(msgEntry, "Value of "+configObject+" is not a structure:"+abstractDataType.toString());
            }
        } catch (IOException e) {
            protocol.getLogger().warning("Could not set MBusConfiguration object " + configObject.toString() + ": " + e.getMessage());
            return MessageResult.createFailed(msgEntry, e.getMessage());
        }

    }

    @Override
    protected ESMR50MbusMessageExecutor getMbusMessageExecutor() {
        return new ESMR50MbusMessageExecutor(protocol);
    }

    @Override
    protected MessageResult changeAuthenticationLevel(MessageEntry msgEntry, MessageHandler messageHandler, int type, boolean enable) throws IOException {
        int newAuthLevel = messageHandler.getAuthenticationLevel();
        if (newAuthLevel != -1) {
            Data config = getCosemObjectFactory().getData(null); //todo see old OBISCODE_CONFIGURATION_OBJECT value
            Structure value;
            BitString flags;
            try {
                value = (Structure) config.getValueAttr();
                try {
                    AbstractDataType dataType = value.getDataType(0);
                    flags = dataType.getBitString();
                    if (flags == null){
                        return MessageResult.createFailed(msgEntry, "Couldn't read existing authentication level configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                    }
                } catch (IndexOutOfBoundsException e) {//todo see OBISCODE_CONFIGURATION_OBJECT in expected value log
                    return MessageResult.createFailed(msgEntry, "Couldn't read existing authentication level configuration. Expected structure value of [" +  "] to have 2 elements.");
                } catch (ClassCastException e) {
                    return MessageResult.createFailed(msgEntry, "Couldn't read existing authentication level configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                }

                /*
                HLS_3_on_P0 and P3_enable (bit 4)   Indicates whether authentication via HLS method 3 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                HLS_4_on_P0 and P3_enable (bit 5)   Indicates whether authentication via HLS method 4 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                HLS_5_on_P0 and P3_enable (bit 6)   Indicates whether authentication via HLS method 5 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                 */
//                getLogger().finest("- configuration object flags: " + getStringFromBitString(flags) +" - before update");
                switch (messageHandler.getAuthenticationLevel()){
                    case 3:
                    case 4:
                    case 5:
                        flags.set(1 + messageHandler.getAuthenticationLevel(), enable);
                        break;
                    default:
                        String msg = "Unexpected authentication level, should be 3,4 or 5 but received: "+messageHandler.getAuthenticationLevel();
//                        getLogger().severe(msg);
                        return MessageResult.createFailed(msgEntry, msg);
                }

//                getLogger().finest("- configuration object flags: " + getStringFromBitString(flags) +" - after update");
                config.setValueAttr(value);
//                getLogger().info("Authentication level for P0 and P3 set to "+messageHandler.getAuthenticationLevel()+" = "+enable);
                return MessageResult.createSuccess(msgEntry);
            } catch (ClassCastException e) {
                //todo see OBISCODE_CONFIGURATION_OBJECT in expected value log
                return MessageResult.createFailed(msgEntry, "Couldn't write configuration. Expected value of [" + "" + "] to be of type 'Structure', but was of type '" + config.getValueAttr().getClass().getSimpleName() + "'.");
            }
        } else {
            return MessageResult.createFailed(msgEntry, "Message contained an invalid authenticationLevel.");
        }
    }

    private String getStringFromBitString(BitString bitString) {
        Iterator<Boolean> iterator = bitString.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()){
            Boolean bit = iterator.next();
            sb.append(bit?"1":"0");
        }
        return "MSB-> "+sb.toString()+" <-LSB";
    }

    /**
     * The Mbus billing period1 gasProfile needs to change the B-field in the ObisCode to readout the correct profile. Herefor we use the serialNumber of the Message.
     *
     * @param lpr      the reader to change
     * @param msgEntry the message which was triggered
     * @return the addapted LoadProfileReader
     */
    //todo override was here
    protected LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, final MessageEntry msgEntry) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_DAILY_BILLING_OBISCODE))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), msgEntry.getSerialNumber(), lpr.getChannelInfos());
        }

        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_MONTHLY_BILLING_OBISCODE))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), msgEntry.getSerialNumber(), lpr.getChannelInfos());
        }

        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_LOAD_PROFILE_PERIOD_1))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), msgEntry.getSerialNumber(), lpr.getChannelInfos());
        }

        return lpr;

    }

    public void setLTEFWLocation(MessageHandler messageHandler) throws IOException {
//        String lteFWLocationFileId = messageHandler.getLTEFWLocation();
//        if(lteFWLocationFileId == null || lteFWLocationFileId != "")
//            if (!ParseUtils.isInteger(lteFWLocationFileId)) {
//                String str = "Not a valid entry for the userFile.";
//                throw new IOException(str);
//            }
//        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(lteFWLocationFileId));
//        if (!(uf instanceof UserFile)) {
//            String str = "Not a valid entry for the userfileID " + lteFWLocationFileId;
//            throw new IOException(str);
//        }
//        log(Level.INFO, "Setting LTE Firmware location by loading file with ID: " + lteFWLocationFileId);
//        byte[] data = uf.loadFileInByteArray();
//        log(Level.INFO, " > file content (hex): " + ProtocolTools.getHexStringFromBytes(data));
//        if(data != null) {
//            log(Level.INFO, " > converting to OctetString and writing to "+ESMR50RegisterFactory.LTE_FW_LOCATION.toString());
//            OctetString octetString = new OctetString(data);
//            Data fwLocation = getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_LOCATION);
//            fwLocation.setValueAttr(octetString);
//            log(Level.INFO, "LTE Firmware location package send successfully!");
//        }else{
//            log(Level.SEVERE, "LTE FW location is empty.");
//        }
    }

    public void setLTEFWDownloadTime(MessageHandler messageHandler) throws IOException {
        int lteFWDownloadTime = 0; //todo get real value
                //messageHandler.getLTEFWDownloadTime();
        log(Level.INFO, "Setting LTE Firmware download time to " + lteFWDownloadTime + " seconds.");
        getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_DOWNLOAD_TIME).setValueAttr(new Unsigned32(lteFWDownloadTime));
    }

    public void doActivateLTEImageTransfer(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Activating LTE Firmware image.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
        imageTransfer.imageActivation();
    }

    public void doInitiateLTEImageTransfer(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Initiating LTE Firmware image transfer.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
//        imageTransfer.initializeFOTA();
    }

    protected MessageResult doModemFirmwareUpgrade(MessageEntry msgEntry, MessageHandler messageHandler) throws IOException {
        setLTEFWLocation(messageHandler);
        setLTEFWDownloadTime(messageHandler);
        doInitiateLTEImageTransfer(messageHandler);
        return MessageResult.createSuccess(msgEntry, "Successfully upgraded the LTE modem firmware.");
    }

    protected void resetAlarmRegister() throws IOException {
        log(Level.INFO, "Handling message Reset Alarm register.");
        //todo See Unsigned64 replacement
//        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned64(-1L));
    }

    /**
     * Will change the Global Encryption Key, and immediately reset frame-counter to zero
     *
     * @throws IOException
     */
    //todo override was here
    protected void changeGlobalKey() throws IOException {
        log(Level.INFO, "Handling message Change global encryption key.");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: global unicast encryption key
//        keyData.addDataType(OctetString.fromByteArray(this.protocol.getDlmsSession().getProperties().getSecurityProvider().getNEWGlobalKey()));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);

//        getLogger().info(" > resetting FrameCounter to 1");
        ((ESMR50Protocol)getProtocol()).resetFrameCounter(1);
    }
}