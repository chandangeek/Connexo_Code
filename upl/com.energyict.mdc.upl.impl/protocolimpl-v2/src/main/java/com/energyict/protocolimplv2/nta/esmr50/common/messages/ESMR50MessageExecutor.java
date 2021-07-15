package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned64;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.LTEModemSetup;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributeobjects.ImageTransferStatus;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
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
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEPingAddress;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SetMBusConfigBit11AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;

public class ESMR50MessageExecutor extends Dsmr40MessageExecutor {

    private static final ObisCode LTE_IMAGE_TRANSFER_OBIS = ObisCode.fromString("0.5.44.0.0.255");

    private static final String MBUS_DAILY_BILLING_OBISCODE = "0.x.99.2.0.255";
    private static final String MBUS_MONTHLY_BILLING_OBISCODE = "0.x.98.1.0.255";
    private static final String MBUS_LOAD_PROFILE_PERIOD_1 = "0.x.24.3.0.255";
    private static final ObisCode MBUS_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");

    private final DeviceMasterDataExtractor deviceMasterDataExtractor;

    public ESMR50MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory,
                                 IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor,
                                 DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
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
            getProtocol().journal("ESMR50 Message executor processing " + pendingMessage.getSpecification().getName());
            try {
                if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_APN_NAME)) {
                    collectedMessage = doSetLteApn(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_LTE_PING_ADDRESS)) {
                    collectedMessage = doSetLtePingAddress(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.LTE_MODEM_FIRMWARE_UPGRADE)) {
                    collectedMessage = doModemFirmwareUpgrade(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_DEFINITION)) {
                    collectedMessage = writeCaptureDefinition(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.CONFIGURE_CAPTURE_PERIOD)) {
                    collectedMessage = writeCapturePeriod(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(MBusConfigurationDeviceMessage.SetMBusConfigBit11)) {
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
                getProtocol().journal(Level.SEVERE,"Error while executing message " + pendingMessage.getSpecification().getName()+": " + e.getLocalizedMessage());
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
        int newAuthLevel = getIntegerAttribute(pendingMessage, authenticationLevelAttributeName); //TODO add original logging messages from 8.11 implementation
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
                getProtocol().journal("Configuration object flags: " + getStringFromBitString(flags) +" - before update");
                switch (newAuthLevel){
                    case 3:
                    case 4:
                    case 5:
                        flags.set(1 + newAuthLevel, enable);
                        break;
                    default:
                        throw new ProtocolException("Unexpected authentication level, should be 3,4 or 5 but received: " + newAuthLevel);
                }
                getProtocol().journal("Configuration object flags: " + getStringFromBitString(flags) +" - after update");
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
            getProtocol().journal("Changing LTE APN to: ["+lteAPN+"]");

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
        int bit11 = getIntegerAttribute(pendingMessage, SetMBusConfigBit11AttributeName);// TODO check that the field is a number. Change attribute type maybe?
        getProtocol().journal("Writing MBus Change Configuration object bit 11 to {"+bit11+"}");

        try {
            Data data = getCosemObjectFactory().getData(MBUS_CONFIGURATION_OBJECT);
            getProtocol().journal("Reading existing value ...");
            AbstractDataType abstractDataType = data.getValueAttr();
            if (abstractDataType.isStructure()){
                Structure structure = abstractDataType.getStructure();
                BitString flags = structure.getNextDataType().getBitString();
                getProtocol().journal(" - existing flags: 0x"+Long.toHexString(flags.longValue()));
                flags.set(11, bit11==1);
                structure.setDataType(0, flags);
                getProtocol().journal(" - new flags are: 0x"+Long.toHexString(flags.longValue())+" - saving back to the meter ...");
                data.setValueAttr(structure);
                getProtocol().journal(" - done!");
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
                collectedMessage.setDeviceProtocolInformation("New flags: 0x"+Long.toHexString(flags.longValue()));
            } else {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                getProtocol().journal(" - not a structure!" + abstractDataType.toString());
                collectedMessage.setDeviceProtocolInformation("Value of "+MBUS_CONFIGURATION_OBJECT+" is not a structure:"+abstractDataType.toString());
            }
        } catch (IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            getProtocol().journal(Level.WARNING, "Could not set MBusConfiguration object " + MBUS_CONFIGURATION_OBJECT.toString() + ": " + e.getMessage());
            collectedMessage.setDeviceProtocolInformation( e.getMessage());
        }
        return collectedMessage;
    }

    @Override
    protected ESMR50MbusMessageExecutor getMbusMessageExecutor() {
        return new ESMR50MbusMessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory(),
                this.deviceMasterDataExtractor);
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

    private void setLTEFWLocation( OfflineDeviceMessage pendingMessage ) throws IOException {
        String updateFilePath = getDeviceMessageAttributeValue( pendingMessage, DeviceMessageConstants.firmwareUpdateFileAttributeName );
        String content = new String( Files.readAllBytes( Paths.get( updateFilePath ) ) );

        byte[] fileAsOctetString = content.getBytes();

        getProtocol().journal(" > file content (hex): " + ProtocolTools.getHexStringFromBytes(fileAsOctetString));
        if (fileAsOctetString != null) {
            getProtocol().journal(" > converting to OctetString and writing to " + ESMR50RegisterFactory.LTE_FW_LOCATION.toString());
            OctetString octetString = new OctetString(fileAsOctetString);
            Data fwLocation = getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_LOCATION);
            fwLocation.setValueAttr(octetString);
            getProtocol().journal("LTE Firmware location package send successfully!" + fwLocation.getString() + " - " + fwLocation.getText() );
        } else {
            getProtocol().journal("LTE FW location is empty.");
        }
    }

    private void setLTEFWDownloadTime(OfflineDeviceMessage pendingMessage) throws IOException {
        int lteFWDownloadTime = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.LTEModemFirmwareUgradeDownloadTimeoutAttributeName));
        getProtocol().journal("Setting LTE Firmware download time to " + lteFWDownloadTime + " seconds.");
        getCosemObjectFactory().getData(ESMR50RegisterFactory.LTE_FW_DOWNLOAD_TIME).setValueAttr(new Unsigned32(lteFWDownloadTime));
    }

    private void doActivateLTEImageTransfer(OfflineDeviceMessage pendingMessage) throws IOException {
        getProtocol().journal("Activating LTE Firmware image.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
        imageTransfer.imageActivation();
    }

    private void doInitiateLTEImageTransfer(OfflineDeviceMessage pendingMessage) throws IOException, InterruptedException {
        getProtocol().journal("Initiating LTE Firmware image transfer.");
        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer(LTE_IMAGE_TRANSFER_OBIS);
        imageTransfer.initializeFOTA(true);

        final int max_attempt_count = 30; // 5 min wait for firmware downloading
        int attempt_count = 0;
        while( attempt_count < max_attempt_count ) {
            final ImageTransferStatus currentState = imageTransfer.readImageTransferStatus();
            if( ImageTransferStatus.TRANSFER_INITIATED == currentState ) {
                getProtocol().journal(Level.INFO, "Image transfer state says the transfer was initiated, meaning we were in the process of sending data, checking if the image name is the same." + currentState.getInfo() );
            } else {
                getProtocol().journal(Level.INFO, "Image transfer state says the transfer wasn't initiated: " + currentState.getInfo() );
            }
            // 10 sec step wait until we ready to activate fw.
            Thread.sleep(10000L);

            List<ImageTransfer.ImageToActivateInfo> imageInfo = imageTransfer.readImageToActivateInfo();
            if (!imageInfo.isEmpty()) {
                for (ImageTransfer.ImageToActivateInfo a_info : imageInfo) {
                    if (a_info.getImageIdentifier() != null) {
                        getProtocol().journal("Activate firmware");
                        doActivateLTEImageTransfer(pendingMessage);
                        return;
                    }
                }
            }
            ++attempt_count;
        }
    }


    protected CollectedMessage doModemFirmwareUpgrade(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        setLTEFWLocation(pendingMessage);
        setLTEFWDownloadTime(pendingMessage);
        try {
            doInitiateLTEImageTransfer(pendingMessage);
        }
        catch (InterruptedException ex)
        {
            getProtocol().journal( ex.getMessage() );
        }
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        collectedMessage.setDeviceProtocolInformation("Successfully upgraded the LTE modem firmware.");
        getProtocol().journal("Successfully upgraded the LTE modem firmware.");
        return collectedMessage;
    }

    @Override
    protected void resetAlarmRegister() throws IOException {
        getProtocol().journal("Handling message Reset Alarm register.");
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned64(BigInteger.valueOf(-1L)));
    }

    /**
     * Will change the Global Encryption Key, and immediately reset frame-counter to zero
     *
     * @throws IOException
     */
    protected void changeGlobalKey() throws IOException {
        getProtocol().journal("Handling message Change global encryption key.");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: global unicast encryption key
        NTASecurityProvider ntaSecurityProvider = (NTASecurityProvider) this.getProtocol().getDlmsSession().getProperties().getSecurityProvider();
        keyData.addDataType(OctetString.fromByteArray(ntaSecurityProvider.getGlobalKey()));//TODO use properties global key
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);

        getProtocol().journal(" > resetting FrameCounter to 1");
        ((ESMR50Protocol)getProtocol()).resetFrameCounter(1);
    }


    /**
     * ESMR 5.0:
     Value ::= structure {
                    Flags bitstring (16)
                }
     */
    @Override
    protected int getConfigurationObjectFlagsIndex(){
        return 0;
    }

}
