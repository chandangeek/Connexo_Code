package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;
import com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEPingAddress;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;

public class ESMR50MessageExecutor extends Dsmr40MessageExecutor {


    private static final ObisCode LTE_IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.5.44.0.0.255");

    public static final String MBUS_DAILY_BILLING_OBISCODE = "0.x.99.2.0.255";
    public static final String MBUS_MONTHLY_BILLING_OBISCODE = "0.x.98.1.0.255";
    public static final String MBUS_LOAD_PROFILE_PERIOD_1 = "0.x.24.3.0.255";
    public static final String MBUS_CONFIGURATION_OBJECT = "1.1.94.31.3.255";

    public ESMR50MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
    }
    //TODO verify all ESMR50 messages
    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : masterMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME)) {
                    collectedMessage = doSetLteApn(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS)) {
                    collectedMessage = doSetLtePingAddress(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS)) {
                    collectedMessage = doSetLtePingAddress(pendingMessage);
                }else if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_DEFINITION)) {
                    collectedMessage = writeCaptureDefinition(pendingMessage);
                }else if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_PERIOD)) {
                    collectedMessage = writeCapturePeriod(pendingMessage);
                }else if (pendingMessage.getSpecification().equals(MBusConfigurationDeviceMessage.SetMBusConfigBit11)) {
                    collectedMessage = doMbusChangeConfigurationObject(pendingMessage);
                } else {
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);  // These messages are not specific for ESMR 5.0, but can be executed by the super (= Dsmr 4.0) messageExecutor
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 4.0 message executor
        result.addCollectedMessages(super.executePendingMessages(notExecutedDeviceMessages));
        return result;
    }
    @Override
    protected void changeAuthenticationLevel(OfflineDeviceMessage pendingMessage, int type, boolean enable) throws IOException {
        int newAuthLevel = getIntegerAttribute(pendingMessage); //TODO add original logging messages from 8.11 implementation
        if (newAuthLevel != -1) {
            Data config = getCosemObjectFactory().getData(OBISCODE_CONFIGURATION_OBJECT);
            Structure value;
            BitString flags;
            try {
                value = (Structure) config.getValueAttr();
                try {
                    AbstractDataType dataType = value.getDataType(0);
                    flags = dataType.getBitString();
                    if (flags == null){
                        throw new ProtocolException("Couldn't read existing authentication level configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected structure value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to have 2 elements.");
                } catch (ClassCastException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                }

                /*
                HLS_3_on_P0 and P3_enable (bit 4)   Indicates whether authentication via HLS method 3 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                HLS_4_on_P0 and P3_enable (bit 5)   Indicates whether authentication via HLS method 4 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                HLS_5_on_P0 and P3_enable (bit 6)   Indicates whether authentication via HLS method 5 is enabled on P0 and P3 (disabled == 0, enabled ==1)
                 */
                getProtocol().getLogger().finest("- configuration object flags: " + getStringFromBitString(flags) +" - before update");
                switch (newAuthLevel){
                    case 3:
                    case 4:
                    case 5:
                        flags.set(1 + newAuthLevel, enable);
                        break;
                    default:
                        throw new ProtocolException("Unexpected authentication level, should be 3,4 or 5 but received: " + newAuthLevel);
                }
                getProtocol().getLogger().finest("- configuration object flags: " + getStringFromBitString(flags) +" - after update");
                config.setValueAttr(value);
            } catch (ClassCastException e) {
                throw new ProtocolException("Couldn't write configuration. Expected value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to be of type 'Structure', but was of type '" + config.getValueAttr().getClass().getSimpleName() + "'.");
            }
        } else {
            throw new ProtocolException("Message contained an invalid authenticationLevel.");
        }
    }

    private CollectedMessage doSetLteApn(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String lteAPN = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.apnAttributeName);
        try {
            getProtocol().getLogger().info("Changing LTE APN to: ["+lteAPN+"]");

            PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);

            if(lteAPN != null){
                getCosemObjectFactory().getLTEModemSetup(LTEModemSetup.getDefaultObisCode()).writeAPN(lteAPN);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            }
        }catch (Exception ex){
           collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
           collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message to write " + LTEModemSetup.getDefaultObisCode()
                   + "!"));
        }

        return collectedMessage;
    }

    private CollectedMessage doSetLtePingAddress(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String lteAPN = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.uplinkPingDestinationAddress);
        try {
            String[] parts = lteAPN.split(":");
            if (parts.length!=2){
                throw  new ProtocolException("LTE Ping Address must have format xx.xx.xx.xx:port");
            }
            LTEPingAddress ltePingAddress = new LTEPingAddress(parts[0], parts[1]);
            Data ltePingObject = getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_PING_ADDRESS);
            ltePingObject.setValueAttr(ltePingAddress.getStructure());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        }catch (Exception ex){
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message to write " + ESMR50RegisterFactory.LTE_PING_ADDRESS
                    + "!"));
        }
        return collectedMessage;
    }

    private CollectedMessage doMbusChangeConfigurationObject(OfflineDeviceMessage pendingMessage) throws ProtocolException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        int bit11 = getIntegerAttribute(pendingMessage);// TODO check that the field is a number. Change attribute type maybe?
        ObisCode configObject = ObisCode.fromString(MBUS_CONFIGURATION_OBJECT);
        getProtocol().getLogger().finest("Writing MBus Change Configuration object bit 11 to {"+bit11+"}");

        try {
            Data data = getCosemObjectFactory().getData(configObject);
            getProtocol().getLogger().finest("Reading existing value ...");
            AbstractDataType abstractDataType = data.getValueAttr();
            if (abstractDataType.isStructure()){
                Structure structure = abstractDataType.getStructure();
                BitString flags = structure.getNextDataType().getBitString();
                getProtocol().getLogger().finest(" - existing flags: 0x"+Long.toHexString(flags.longValue()));
                flags.set(11, bit11==1);
                structure.setDataType(0, flags);
                getProtocol().getLogger().finest(" - new flags are: 0x"+Long.toHexString(flags.longValue())+" - saving back to the meter ...");
                data.setValueAttr(structure);
                getProtocol().getLogger().finest(" - done!");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                collectedMessage.setDeviceProtocolInformation("New flags: 0x"+Long.toHexString(flags.longValue()));
            } else {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                getProtocol().getLogger().finest(" - not a structure!" + abstractDataType.toString());
                collectedMessage.setDeviceProtocolInformation("Value of "+configObject+" is not a structure:"+abstractDataType.toString());
            }
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            getProtocol().getLogger().warning("Could not set MBusConfiguration object " + configObject.toString() + ": " + e.getMessage());
            collectedMessage.setDeviceProtocolInformation( e.getMessage());
        }
        return collectedMessage;
    }

    @Override
    protected ESMR50MbusMessageExecutor getMbusMessageExecutor() {
        return new ESMR50MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
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
     * @return the addapted LoadProfileReader
     */
    @Override
    protected LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_DAILY_BILLING_OBISCODE))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
        }

        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_MONTHLY_BILLING_OBISCODE))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
        }

        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString(MBUS_LOAD_PROFILE_PERIOD_1))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
        }

        return lpr;

    }

    public void setLTEFWLocation(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] fileAsOctetString = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, "Download_location_user_file"), ""); //TODO create device message with this attribute
        getProtocol().getLogger().info(" > file content (hex): " + ProtocolTools.getHexStringFromBytes(fileAsOctetString));
        if(fileAsOctetString != null) {
            getProtocol().getLogger().info(" > converting to OctetString and writing to "+ESMR50RegisterFactory.LTE_FW_LOCATION.toString());
            OctetString octetString = new OctetString(fileAsOctetString);
            Data fwLocation = getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_LOCATION);
            fwLocation.setValueAttr(octetString);
            getProtocol().getLogger().info( "LTE Firmware location package send successfully!");
        }else{
            getProtocol().getLogger().info("LTE FW location is empty.");
        }
    }

    public void setLTEFWDownloadTime(OfflineDeviceMessage pendingMessage) throws IOException {
        int lteFWDownloadTime = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, "Download_Timeout")); //TODO create device message attribute
        getProtocol().getLogger().info("Setting LTE Firmware download time to " + lteFWDownloadTime + " seconds.");
        getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_DOWNLOAD_TIME).setValueAttr(new Unsigned32(lteFWDownloadTime));
    }

    public void doActivateLTEImageTransfer(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().getLogger().info("Activating LTE Firmware image.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
        imageTransfer.imageActivation();
    }

    public void doInitiateLTEImageTransfer(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().getLogger().info("Initiating LTE Firmware image transfer.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
        imageTransfer.initializeFOTA();
    }

    protected CollectedMessage doModemFirmwareUpgrade(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        setLTEFWLocation(pendingMessage);
        setLTEFWDownloadTime(pendingMessage);
        doInitiateLTEImageTransfer(pendingMessage);
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Successfully upgraded the LTE modem firmware.");
        return collectedMessage;
    }

    protected void resetAlarmRegister() throws IOException {
        getProtocol().getLogger().info("Handling message Reset Alarm register.");
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(-1L)); //TODO Value was originally Unsigned64, must create Unsigned64 data type
    }

    /**
     * Will change the Global Encryption Key, and immediately reset frame-counter to zero
     *
     * @throws IOException
     */
    protected void changeGlobalKey() throws IOException {
        getProtocol().getLogger().info("Handling message Change global encryption key.");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: global unicast encryption key
        NTASecurityProvider ntaSecurityProvider = (NTASecurityProvider) this.getProtocol().getDlmsSession().getProperties().getSecurityProvider();
        keyData.addDataType(OctetString.fromByteArray(ntaSecurityProvider.getGlobalKey()));//TODO use properties global key
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);

        getProtocol().getLogger().info(" > resetting FrameCounter to 1");
        ((ESMR50Protocol)getProtocol()).resetFrameCounter(1);
    }

    protected CollectedMessage writeCaptureDefinition(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        String captureObjects = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.captureObjectListAttributeName);
        //TODO This is just an attempt. Actual code must be adapted to current protocol. Actual code from Dsmr40Messaging in 8.11
        String[] splitCaptureObjects = captureObjects.split(";");
        List <String> capturedObjectDefinitions = Arrays.asList(splitCaptureObjects);
        List <String> filteredCaptureObjects = new ArrayList<>();
        for(String capturedObject : capturedObjectDefinitions){
            filteredCaptureObjects.add( capturedObject.replace("{", "").replace("}", ""));
        }
        if (filteredCaptureObjects.isEmpty()) {

            ProfileGeneric profileGeneric = null;
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE);
            } catch (NotInObjectListException e) {
                e.printStackTrace();
            }
            if (profileGeneric == null) {
                getProtocol().getLogger().log(Level.SEVERE, "Profile for obis code " + ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE.toString() + " is null");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            }
            if(capturedObjectDefinitions.isEmpty()){
                getProtocol().getLogger().log(Level.INFO, "Failed to set definable load profile capture objects.");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            }
            Array capturedObjects = new Array();
            for (String capturedObjectDefinition : capturedObjectDefinitions) {
                String[] definitionParts = capturedObjectDefinition.split(",");
                try {
                    int dlmsClassId = Integer.parseInt(definitionParts[0].substring(0, 1));
                    ObisCode obisCode = ObisCode.fromString(definitionParts[1].replace('-', '.').replace(':', '.'));
                    int attribute = Integer.parseInt(definitionParts[2]);
                    int dataIndex = Integer.parseInt(definitionParts[3].substring(0, 1));
                    Structure definition = new Structure();
                    definition.addDataType(new Unsigned16(dlmsClassId));
                    definition.addDataType(OctetString.fromObisCode(obisCode));
                    definition.addDataType(new Integer8(attribute));
                    definition.addDataType(new Unsigned16(dataIndex));
                    capturedObjects.addDataType(definition);
                } catch (Exception e) {
                    getProtocol().getLogger().log(Level.SEVERE, e.getMessage());
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                }
            }
            profileGeneric.setCaptureObjectsAttr(capturedObjects);
            getProtocol().getLogger().log(Level.INFO, "Successfully set definable load profile capture objects.");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } else {
            getProtocol().getLogger().log(Level.INFO, "Failed to set definable load profile capture objects.");
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        }
        return collectedMessage;
    }

    protected CollectedMessage writeCapturePeriod(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        ObisCode obisCode = ESMR50LoadProfileBuilder.DEFINABLE_LOAD_PROFILE;
        int period  =Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.capturePeriodAttributeName).getValue());
        getProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(obisCode).setCapturePeriodAttr(new Unsigned32(period));
        getProtocol().getLogger().log(Level.INFO, "Successfully set definable load profile capture period to " + period);
        return collectedMessage;
    }

}
