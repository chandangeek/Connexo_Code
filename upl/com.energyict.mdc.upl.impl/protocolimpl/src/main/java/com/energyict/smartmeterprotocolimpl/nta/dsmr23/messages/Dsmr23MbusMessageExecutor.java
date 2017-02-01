package com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterData;
import com.energyict.protocol.MeterDataMessageResult;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 18-jul-2011
 * Time: 8:38:27
 */
public class Dsmr23MbusMessageExecutor extends MessageParser {

    private final AbstractSmartNtaProtocol protocol;
    private final DlmsSession dlmsSession;

    public Dsmr23MbusMessageExecutor(final AbstractSmartNtaProtocol protocol) {
        this.protocol = protocol;
        this.dlmsSession = this.protocol.getDlmsSession();
    }

    public AbstractSmartNtaProtocol getProtocol() {
        return protocol;
    }

    public MessageResult executeMessageEntry(MessageEntry msgEntry) {
        String content = msgEntry.getContent();
        MessageHandler messageHandler = getMessageHandler();
        String serialNumber = msgEntry.getSerialNumber();
        MessageResult msgResult = null;
        try {
            importMessage(content, messageHandler);

            boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
            boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
            boolean decommission = messageHandler.getType().equals(RtuMessageConstant.MBUS_DECOMMISSION);
            boolean mbusEncryption = messageHandler.getType().equals(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
            boolean mbusCryptoserverEncryption = messageHandler.getType().equals(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS);
            boolean mbusCorrected = messageHandler.getType().equals(RtuMessageConstant.MBUS_CORRECTED_VALUES);
            boolean mbusUnCorrected = messageHandler.getType().equals(RtuMessageConstant.MBUS_UNCORRECTED_VALUES);
            boolean partialLoadProfile = messageHandler.getType().equals(LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag());
            boolean loadProfileRegisterRequest = messageHandler.getType().equals(LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag());

            if (connect) {
                doConnectMessage(messageHandler, serialNumber);
            } else if (disconnect) {
                doDisconnectMessage(messageHandler, serialNumber);
            } else if (connectMode) {
                setConnectMode(messageHandler, serialNumber);
            } else if (decommission) {
                doDecommission(messageHandler, serialNumber);
            } else if (mbusEncryption) {
                setMbusEncryptionKeys(messageHandler, serialNumber);
            } else if (mbusCryptoserverEncryption) {
                setCryptoserverMbusEncryptionKeys(msgEntry, messageHandler, serialNumber);
            } else if (mbusCorrected) {
                setMbusCorrected(messageHandler, serialNumber);
            } else if (mbusUnCorrected) {
                setMbusUncorrected(messageHandler, serialNumber);
            } else if (partialLoadProfile) {
                msgResult = doReadPartialLoadProfile(msgEntry);
            } else if (loadProfileRegisterRequest) {
                msgResult = doReadLoadProfileRegisters(msgEntry);
            } else {
                msgResult = MessageResult.createFailed(msgEntry, "Message not supported by the protocol.");
                log(Level.INFO, "Message not supported : " + content);
            }

            // Some message create their own messageResult
            if (msgResult == null) {
                msgResult = MessageResult.createSuccess(msgEntry);
                log(Level.INFO, "Message has finished.");
            } else if (msgResult.isFailed()) {
                log(Level.SEVERE, "Message failed : " + msgResult.getInfo());
            }
        } catch (BusinessException e) {
            msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
            log(Level.SEVERE, "Message failed : " + e.getMessage());
        } catch (IOException e) {
            msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
            log(Level.SEVERE, "Message failed : " + e.getMessage());
        } catch (SQLException e) {
            msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
            log(Level.SEVERE, "Message failed : " + e.getMessage());
        }
        return msgResult;
    }

    protected NTAMessageHandler getMessageHandler() {
        return new NTAMessageHandler();
    }

    private void setMbusUncorrected(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Set loadprofile to unCorrected values");
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = OctetString.fromByteArray(new byte[]{(byte) 0x0C});
        struct.addDataType(dib);
        OctetString vib = OctetString.fromByteArray(new byte[]{(byte) 0x93, (byte) 0x3A});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        getMBusClient(serialNumber).writeCaptureDefinition(capDef);
    }

    protected MBusClient getMBusClient(String serialNumber) throws IOException {
        return getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
    }

    protected ObisCode getMbusClientObisCode(String serialNumber) throws IOException {
        return getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode();
    }

    private void setMbusCorrected(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage  Set loadprofile to corrected values");
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = OctetString.fromByteArray(new byte[]{0x0C});
        struct.addDataType(dib);
        OctetString vib = OctetString.fromByteArray(new byte[]{0x13});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        getMBusClient(serialNumber).writeCaptureDefinition(capDef);
    }

    protected void setCryptoserverMbusEncryptionKeys(MessageEntry msgEntry, final MessageHandler messageHandler, final String serialNumber) throws IOException {
        throw new IOException("Received message to renew MBus keys using the Cryptoserver, but Cryptoserver usage is not supported in this protocol");
    }

    protected void setMbusEncryptionKeys(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Set encryption keys");

        String openKey = messageHandler.getOpenKey();
        String transferKey = messageHandler.getTransferKey();
        MBusClient mbusClient = getMBusClient(serialNumber);

        if (openKey == null) {
            mbusClient.setEncryptionKey("");
        } else if (transferKey != null) {
            mbusClient.setTransportKey(convertStringToByte(transferKey));
            mbusClient.setEncryptionKey(convertStringToByte(openKey));
        } else {
            throw new IOException("Transfer key may not be empty when setting the encryption keys.");
        }
    }

    private void doDecommission(final MessageHandler messageHandler, final String serialNumber) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling MbusMessage Decommission MBus device");

        getMBusClient(serialNumber).deinstallSlave();
    }

    private void setConnectMode(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot write connect mode, breaker is not supported!");
        }

        log(Level.INFO, "Handling MbusMessage ConnectControl mode");
        String mode = messageHandler.getConnectControlMode();

        if (mode != null) {
            try {
                int modeInt = Integer.parseInt(mode);

                if ((modeInt >= 0) && (modeInt <= 6)) {
                    Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
                    connectorMode.writeControlMode(new TypeEnum(modeInt));

                } else {
                    throw new IOException("Mode is not a valid entry for message, value must be between 0 and 6");
                }

            } catch (NumberFormatException e) {
                throw new IOException("Mode is not a valid entry for message.");
            }
        } else {
            // should never get to the else, can't leave message empty
            throw new IOException("Message can not be empty");
        }
    }

    protected void doDisconnectMessage(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot execute disconnect message, breaker is not supported!");
        }

        log(Level.INFO, "Handling MbusMessage Disconnect");

        if (!messageHandler.getDisconnectDate().equals("")) {    // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
            SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMbusAddress(serialNumber)).getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMbusAddress(serialNumber)).getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

            sasDisconnect.writeExecutedScript(scriptStruct);
            sasDisconnect.writeExecutionTime(executionTimeArray);

        } else { // immediate disconnect
            Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
            connector.remoteDisconnect();
        }
    }

    protected void doConnectMessage(MessageHandler messageHandler, String serialNumber) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot execute connect message, breaker is not supported!");
        }

        log(Level.INFO, "Handling MbusMessage Connect");

        if (!messageHandler.getConnectDate().equals("")) {    // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
            SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMbusAddress(serialNumber)).getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMbusAddress(serialNumber)).getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

            sasConnect.writeExecutedScript(scriptStruct);
            sasConnect.writeExecutionTime(executionTimeArray);

        } else { // immediate connect
            Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
            connector.remoteReconnect();
        }
    }

    protected byte[] convertStringToByte(String string) throws IOException {
        try {
            byte[] b = new byte[string.length() / 2];
            int offset = 0;
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) Integer.parseInt(string.substring(offset, offset += 2), 16);
            }
            return b;
        } catch (NumberFormatException e) {
            throw new IOException("String " + string + " can not be formatted to byteArray");
        }
    }


    protected MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        try {
            log(Level.INFO, "Handling message Read LoadProfile Registers.");
            LegacyLoadProfileRegisterMessageBuilder builder = LegacyLoadProfileRegisterMessageBuilder.fromXml(msgEntry.getContent());
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!");
            }

            LoadProfileBuilder loadProfileBuilder = this.protocol.getLoadProfileBuilder();
            if (loadProfileBuilder instanceof LGLoadProfileBuilder) {
                ((LGLoadProfileBuilder) loadProfileBuilder).setFixMBusToDate(false);    //Don't subtract 15 minutes from the to date
            }

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), msgEntry);
            final List<LoadProfileConfiguration> loadProfileConfigurations = this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
            final List<ProfileData> profileDatas = this.protocol.getLoadProfileData(Arrays.asList(lpr));

            if (profileDatas.size() != 1) {
                return MessageResult.createFailed(msgEntry, "We are supposed to receive 1 LoadProfile configuration in this message, but we received " + profileDatas.size());
            }

            ProfileData pd = profileDatas.get(0);
            IntervalData id = null;
            for (IntervalData intervalData : pd.getIntervalDatas()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    id = intervalData;
                }
            }

            if (id == null) {
                return MessageResult.createFailed(msgEntry, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")");
            }

            com.energyict.protocol.Register previousRegister = null;
            MeterReadingData mrd = new MeterReadingData();
            for (com.energyict.protocol.Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < pd.getChannelInfos().size(); i++) {
                    final ChannelInfo channel = pd.getChannel(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), register.getRtuRegisterId());
                        mrd.add(registerValue);
                    }
                }
                previousRegister = register;
            }

            MeterData md = new MeterData();
            md.setMeterReadingData(mrd);

            log(Level.INFO, "Message Read LoadProfile Registers Finished.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, "Could not parse the content of the xml message, probably incorrect message.");
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
        }
    }

    private MessageResult doReadPartialLoadProfile(final MessageEntry msgEntry) {
        try {
            log(Level.INFO, "Handling message Read Partial LoadProfile.");
            LegacyPartialLoadProfileMessageBuilder builder = LegacyPartialLoadProfileMessageBuilder.fromXml(msgEntry.getContent());

            LoadProfileReader lpr = builder.getLoadProfileReader();

            lpr = checkLoadProfileReader(lpr, msgEntry);

            LoadProfileBuilder loadProfileBuilder = this.protocol.getLoadProfileBuilder();
            if (loadProfileBuilder instanceof LGLoadProfileBuilder) {
                ((LGLoadProfileBuilder) loadProfileBuilder).setFixMBusToDate(false);    //Don't subtract 15 minutes from the to date
            }

            final List<LoadProfileConfiguration> loadProfileConfigurations = this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
            final List<ProfileData> profileData = this.protocol.getLoadProfileData(Arrays.asList(lpr));

            if (profileData.size() == 0) {
                return MessageResult.createFailed(msgEntry, "LoadProfile returned no data.");
            } else {
                for (ProfileData data : profileData) {
                    if (data.getIntervalDatas().size() == 0) {
                        return MessageResult.createFailed(msgEntry, "LoadProfile returned no interval data.");
                    }
                }
            }

            MeterData md = new MeterData();
            for (ProfileData data : profileData) {
                data.sort();
                md.addProfileData(data);
            }
            log(Level.INFO, "Message Read Partial LoadProfile Finished.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, "Could not parse the content of the xml message, probably incorrect message.");
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data.");
        }
    }

    /**
     * The Mbus Hourly gasProfile needs to change the B-field in the ObisCode to readout the correct profile. Herefor we use the serialNumber of the Message.
     *
     * @param lpr      the reader to change
     * @param msgEntry the message which was triggered
     * @return the addapted LoadProfileReader
     */
    private LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, final MessageEntry msgEntry) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), msgEntry.getSerialNumber(), lpr.getChannelInfos());
        } else {
            return lpr;
        }
    }

    protected DLMSMeterConfig getMeterConfig() {
        return this.dlmsSession.getMeterConfig();
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return this.dlmsSession.getCosemObjectFactory();
    }

    protected DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.dlmsSession.getTimeZone();
    }

    protected void log(final Level level, final String msg) {
        this.dlmsSession.getLogger().log(level, msg);
    }

    protected int getMbusAddress(String serialNumber) {
        return this.protocol.getPhysicalAddressFromSerialNumber(serialNumber) - 1;
    }
}
