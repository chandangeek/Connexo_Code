package com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeCalendar;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.lookups.Lookup;
import com.energyict.mdc.protocol.api.lookups.LookupEntry;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.Clock;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 13:42:50
 */
public class Dsmr23MessageExecutor extends MessageParser {

    private static final byte[] DEFAULT_MONITORED_ATTRIBUTE = new byte[]{1, 0, 90, 7, 0, (byte) 255};    // Total current, instantaneous value

    protected final DlmsSession dlmsSession;
    protected final AbstractSmartNtaProtocol protocol;
    private final Clock clock;
    private final TopologyService topologyService;

    public Dsmr23MessageExecutor(AbstractSmartNtaProtocol protocol, Clock clock, TopologyService topologyService) {
        this.protocol = protocol;
        this.clock = clock;
        this.dlmsSession = this.protocol.getDlmsSession();
        this.topologyService = topologyService;
    }

    protected Clock getClock() {
        return clock;
    }

    protected TopologyService getTopologyService() {
        return topologyService;
    }

    public MessageResult executeMessageEntry(MessageEntry msgEntry) throws ConnectionException, NestedIOException {
        if (!this.protocol.getSerialNumber().equalsIgnoreCase(msgEntry.getSerialNumber())) {
            //Execute messages for MBus device
            return getMbusMessageExecutor().executeMessageEntry(msgEntry);
        } else {
            MessageResult msgResult = null;
            String content = msgEntry.getContent();
            MessageHandler messageHandler = getMessageHandler();
            try {
                importMessage(content, messageHandler);
                switch (messageHandler.getType()) {
                    case RtuMessageConstant.XMLCONFIG: {
                        doXmlConfig(content);
                        break;
                    }
                    case RtuMessageConstant.FIRMWARE_UPGRADE: {
                        doFirmwareUpgrade(messageHandler, msgEntry);
                        break;
                    }
                    case RtuMessageConstant.P1CODEMESSAGE: {
                        setP1Code(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.P1TEXTMESSAGE: {
                        setP1Text(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.CONNECT_LOAD: {
                        doConnect(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.DISCONNECT_LOAD: {
                        doDisconnect(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.CONNECT_CONTROL_MODE: {
                        setConnectMode(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.LOAD_LIMIT_CONFIGURE: {
                        loadLimitConfiguration(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.LOAD_LIMIT_DISABLE: {
                        clearLoadLimiting(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST: {
                        setLoadLimitGroupId(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.TOU_ACTIVITY_CAL: {
                        upgradeCalendar(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.TOU_SPECIAL_DAYS: {
                        upgradeSpecialDays(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE: {
                        deleteSpecialDay(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.SET_TIME: {
                        setTime(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.ME_MAKING_ENTRIES: {
                        createDataBaseEntries(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.GPRS_MODEM_SETUP: {
                        setGPRSParameters(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.GPRS_MODEM_CREDENTIALS: {
                        setGPRSCredentials(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.GLOBAL_METER_RESET: {
                        doGlobalReset();
                        break;
                    }
                    case RtuMessageConstant.RESTORE_FACTORY_SETTINGS: {
                        restoreFactorySettings();
                        break;
                    }
                    case RtuMessageConstant.WAKEUP_ADD_WHITELIST: {
                        setWakeUpWhiteList(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.AEE_CHANGE_HLS_SECRET: {
                        changeHLSSecret(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY: {
                        changeAuthenticationKey(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY: {
                        changeGlobalKey(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.AEE_CHANGE_LLS_SECRET: {
                        changeLLSSecret(messageHandler);
                        break;
                    }
                    case RtuMessageConstant.WAKEUP_ACTIVATE: {
                        activateSms();
                        break;
                    }
                    case RtuMessageConstant.WAKEUP_DEACTIVATE: {
                        deactivateSms();
                        break;
                    }
                    case RtuMessageConstant.AEE_ACTIVATE_SECURITY: {
                        getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(messageHandler.getSecurityLevel()));
                        break;
                    }
                    case RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL: {
                        msgResult = changeAuthenticationLevel(msgEntry, messageHandler);
                        break;
                    }
                    case RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0: {
                        msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 0, true);
                        break;
                    }
                    case RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0: {
                        msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 0, false);
                        break;
                    }
                    case RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3: {
                        msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 3, true);
                        break;
                    }
                    case RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3: {
                        msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 3, false);
                        break;
                    }
                    case LegacyPartialLoadProfileMessageBuilder.MESSAGETAG: {
                        msgResult = doReadPartialLoadProfile(msgEntry);
                        break;
                    }
                    case LegacyLoadProfileRegisterMessageBuilder.MESSAGETAG: {
                        msgResult = doReadLoadProfileRegisters(msgEntry);
                        break;
                    }
                    case RtuMessageConstant.RESET_ALARM_REGISTER: {
                        resetAlarmRegister();
                        break;
                    }
                    case RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW: {
                        changeDefaultResetWindow(messageHandler);
                        break;
                    }
                    default: {
                        msgResult = MessageResult.createFailed(msgEntry, "Message not supported by the protocol.");
                        log(Level.INFO, "Message not supported : " + content);
                    }
                }

                // Some messages create their own messageResult
                if (msgResult == null) {
                    msgResult = MessageResult.createSuccess(msgEntry);
                    log(Level.INFO, "Message has finished.");
                } else if (msgResult.isFailed()) {
                    log(Level.SEVERE, "Message failed : " + msgResult.getInfo());
                }

            } catch (ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            } catch (NestedIOException e) {
                Throwable rootCause = getRootCause(e);
                if (rootCause.getClass().equals(ConnectionException.class)) {
                    throw new NestedIOException(rootCause);
                }
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            }
            catch (BusinessException | IOException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            }
            return msgResult;
        }
    }

    protected Dsmr23MbusMessageExecutor getMbusMessageExecutor() {
        return new Dsmr23MbusMessageExecutor(protocol, clock);
    }

    protected NTAMessageHandler getMessageHandler() {
        return new NTAMessageHandler();
    }

    protected void deactivateSms() throws IOException {
        getCosemObjectFactory().getAutoConnect().writeMode(1);
    }

    protected void activateSms() throws IOException {
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    protected MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        try {
            log(Level.INFO, "Handling message Read LoadProfile Registers.");
            LegacyLoadProfileRegisterMessageBuilder builder = this.protocol.getLoadProfileRegisterMessageBuilder();
            builder.fromXml(msgEntry.getContent());
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!");
            }

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(this.clock, builder.getLoadProfileReader()), msgEntry);
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

            Register previousRegister = null;
            MeterReadingData mrd = new MeterReadingData();
            for (Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < pd.getChannelInfos().size(); i++) {
                    final ChannelInfo channel = pd.getChannel(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        long rtuRegisterId = builder.getRegisterSpecIdForRegister(register);
                        final RegisterValue registerValue;
                        if (rtuRegisterId != -1) {
                            registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), rtuRegisterId);
                        } else {
                            return MessageResult.createFailed(msgEntry, "Register with obiscode '" + register.getObisCode() + "' is not configured in EIServer on RTU with serial number '" + register.getSerialNumber() + "'");
                        }
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
            return MessageResult.createFailed(msgEntry, "Failed while fetching the LoadProfile data. " + e.getMessage());
        }
    }

    private MessageResult doReadPartialLoadProfile(final MessageEntry msgEntry) {
        try {
            log(Level.INFO, "Handling message Read Partial LoadProfile.");
            LegacyPartialLoadProfileMessageBuilder builder = this.protocol.getPartialLoadProfileMessageBuilder();
            builder.fromXml(msgEntry.getContent());

            LoadProfileReader lpr = builder.getLoadProfileReader();

            lpr = checkLoadProfileReader(lpr, msgEntry);
            final List<LoadProfileConfiguration> loadProfileConfigurations = this.protocol.fetchLoadProfileConfiguration(Arrays.asList(lpr));
            final List<ProfileData> profileData = this.protocol.getLoadProfileData(Arrays.asList(lpr));

            if (profileData.isEmpty()) {
                return MessageResult.createFailed(msgEntry, "LoadProfile returned no data.");
            } else {
                for (ProfileData data : profileData) {
                    if (data.getIntervalDatas().isEmpty()) {
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
            return new LoadProfileReader(
                    this.clock,
                    lpr.getProfileObisCode(),
                    lpr.getStartReadingTime(),
                    lpr.getEndReadingTime(),
                    lpr.getLoadProfileId(),
                    lpr.getDeviceIdentifier(),
                    lpr.getChannelInfos(),
                    msgEntry.getSerialNumber(),
                    lpr.getLoadProfileIdentifier());
        } else {
            return lpr;
        }
    }

    /**
     * Override in DMSR 4.0 implementation
     */
    protected MessageResult changeAuthenticationLevel(MessageEntry msgEntry, MessageHandler messageHandler, int type, boolean enable) throws IOException {
        return MessageResult.createFailed(msgEntry, "Authentication level change specifically for P0 or P3 is not supported in DSMR 2.3");
    }

    private MessageResult changeAuthenticationLevel(MessageEntry msgEntry, MessageHandler messageHandler) throws IOException {
        int newAuthLevel = messageHandler.getAuthenticationLevel();
        MessageResult msgResult;
        if (newAuthLevel != -1) {
            if (this.dlmsSession.getReference() == ProtocolLink.LN_REFERENCE) {
                AssociationLN aln = getCosemObjectFactory().getAssociationLN();
                AbstractDataType adt = aln.readAuthenticationMechanismName();
                if (adt.isOctetString()) {
                    byte[] octets = ((OctetString) adt).getOctetStr();
                    if (octets[octets.length - 1] != newAuthLevel) {
                        octets[octets.length - 1] = (byte) newAuthLevel;
                        aln.writeAuthenticationMechanismName(new OctetString(octets, 0));
                        return MessageResult.createSuccess(msgEntry);
                    } else {
                        msgResult = MessageResult.createSuccess(msgEntry, "New authenticationLevel is the same as the one that is already configured in the device, " +
                                "new level will not be written.");
                        log(Level.INFO, msgResult.getInfo());
                        return msgResult;
                    }
                } else if (adt.isStructure()) {
                    Structure struct = (Structure) adt;
                    Unsigned8 u8 = (Unsigned8) struct.getDataType(struct.nrOfDataTypes() - 1);
                    if (u8.intValue() != newAuthLevel) {
                        u8 = new Unsigned8(newAuthLevel);
                        struct.setDataType(struct.nrOfDataTypes() - 1, u8);
                        aln.writeAuthenticationMechanismName(struct);
                        return MessageResult.createSuccess(msgEntry);
                    } else {
                        msgResult = MessageResult.createSuccess(msgEntry, "New authenticationLevel is the same as the one that is already configured in the device, " +
                                "new level will not be written.");
                        log(Level.INFO, msgResult.getInfo());
                        return msgResult;
                    }
                } else {
                    msgResult = MessageResult.createFailed(msgEntry, "Returned AuthenticationMechanismName is not of the type OctetString, nor Structure.");
                }
            } else {
                msgResult = MessageResult.createFailed(msgEntry, "Changing authenticationLevel using ShortName referencing is not supported.");
                log(Level.WARNING, msgResult.getInfo());
            }

        } else {
            msgResult = MessageResult.createFailed(msgEntry, "Message contained an invalid authenticationLevel.");
            log(Level.WARNING, msgResult.getInfo());
        }
        return msgResult;
    }

    private void changeLLSSecret(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Change LLS secret.");
        // changing the LLS secret in LN_referencing is a set of an attribute
        if (this.dlmsSession.getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();
            aln.writeSecret(OctetString.fromByteArray(messageHandler.getLLSSecret()));

            // changing the LLS secret in SN_referencing is the same action as for the HLS secret
        } else if (this.dlmsSession.getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            //TODO low lever security should set the value directly to the secret attribute of the SNAssociation
            asn.changeSecret(messageHandler.getLLSSecret());
        }
    }

    protected void changeGlobalKey(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Change global encryption key.");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: global unicast encryption key
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewEncryptionKey()));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    protected void changeAuthenticationKey(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Change global authentication key.");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewAuthenticationKey()));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void changeHLSSecret(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Change HLS secret.");
        if (this.dlmsSession.getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            aln.changeHLSSecret(messageHandler.getHLSSecret());
        } else if (this.dlmsSession.getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            //TODO low lever security should set the value directly to the secret attribute of the SNAssociation
            asn.changeSecret(messageHandler.getHLSSecret());
        }
    }

    protected void setWakeUpWhiteList(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Setting whitelist.");
        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();

        Array list = new Array();
        list.addDataType(OctetString.fromString(messageHandler.getNr1()));
        list.addDataType(OctetString.fromString(messageHandler.getNr2()));
        list.addDataType(OctetString.fromString(messageHandler.getNr3()));
        list.addDataType(OctetString.fromString(messageHandler.getNr4()));
        list.addDataType(OctetString.fromString(messageHandler.getNr5()));

        autoConnect.writeDestinationList(list);
    }

    private void doGlobalReset() throws IOException {
        log(Level.INFO, "Handling message Global Meter Reset.");
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
    }

    protected void restoreFactorySettings() throws IOException {
        doGlobalReset();
    }

    private void setGPRSCredentials(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Changing gprs modem credentials");

        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (messageHandler.getGprsUsername() != null) {
            pppat.setUserName(messageHandler.getGprsUsername());
        }
        if (messageHandler.getGprsPassword() != null) {
            pppat.setPassWord(messageHandler.getGprsPassword());
        }
        if ((messageHandler.getGprsUsername() != null) || (messageHandler.getGprsPassword() != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }
    }

    private void setGPRSParameters(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Changing gprs modem parameters");

        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (messageHandler.getGprsUsername() != null) {
            pppat.setUserName(messageHandler.getGprsUsername());
        }
        if (messageHandler.getGprsPassword() != null) {
            pppat.setPassWord(messageHandler.getGprsPassword());
        }
        if ((messageHandler.getGprsUsername() != null) || (messageHandler.getGprsPassword() != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }

        if (messageHandler.getGprsApn() != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(messageHandler.getGprsApn());
        }
    }

    private void createDataBaseEntries(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Making database entries.");
        log(Level.INFO, "(This can take several minutes/houres, depending on the number of entries you want to simulate)");

        if (messageHandler.getMEEntries() > 0) {
            // Start the entry making ...

            int entries = messageHandler.getMEEntries();
            String type = messageHandler.getMEInterval();
            long millis = Long.parseLong(messageHandler.getMEStartDate()) * 1000;
            Date startTime = new Date(millis);
            startTime = getFirstDate(startTime, type);
            while (entries > 0) {
                log(Level.INFO, "Setting meterTime to: " + startTime);
                this.protocol.setTime(startTime);
                waitForCrossingBoundry();
                startTime = setBeforeNextInterval(startTime, type);
                entries--;
            }
        }

        if (messageHandler.getMESyncAtEnd()) {
            Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
            log(Level.INFO, "Synced clock to: " + currentTime);
            this.protocol.setTime(currentTime);
        }
    }

    private void setTime(MessageHandler messageHandler) throws IOException {
        String epochTime = messageHandler.getEpochTime();
        log(Level.INFO, "Handling message Setting the device time to: " + convertUnixToGMTDateTime(epochTime).getValue().getTime());
        getCosemObjectFactory().getClock().setAXDRDateTimeAttr(convertUnixToGMTDateTime(epochTime));
    }

    private void deleteSpecialDay(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Delete Special Days entry");
        try {
            SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            sdt.delete(Integer.parseInt(messageHandler.getSpecialDayDeleteEntry()));
        } catch (NumberFormatException e) {
            throw new IOException("Delete index is not a valid entry");
        }
    }

    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");

        String codeTable = messageHandler.getSpecialDaysCodeTable();

        if (codeTable == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {
            Code ct = this.findCode();
            List calendars = ct.getCalendars();
            Array sdArray = new Array();

            SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            for (int i = 0; i < calendars.size(); i++) {
                CodeCalendar cc = (CodeCalendar) calendars.get(i);
                if (cc.getSeason() == 0) {
                    OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                            (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                            (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                    Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
                    Structure struct = new Structure();
                    AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((cc.getYear() == -1) ? 0x07 : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xB2 : (cc.getYear()) & 0xFF),
                            (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                            (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
                    long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
                    struct.addDataType(new Unsigned16((int) days));
                    struct.addDataType(os);
                    struct.addDataType(dayType);
                    sdArray.addDataType(struct);
                }
            }

            if (sdArray.nrOfDataTypes() != 0) {
                sdt.writeSpecialDays(sdArray);
            }
        }
    }

    private Code findCode() {
        // Todo: port Code to jupiter, return null as the previous code would have returned null too.
        throw new UnsupportedOperationException("Code is not longer supported by Jupiter");
    }

    protected void upgradeCalendar(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Activity calendar");

        String name = messageHandler.getTOUCalendarName();
        String activateDate = messageHandler.getTOUActivationDate();
        String codeTable = messageHandler.getTOUCodeTable();
        String userFile = messageHandler.getTOUUserFile();

        if ((codeTable == null) && (userFile == null)) {
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        } else if ((codeTable != null) && (userFile != null)) {
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both filled in.");
        }

        if (codeTable != null) {
            Code ct = this.findCode();
            ActivityCalendarMessage acm = new ActivityCalendarMessage(ct, getMeterConfig());
            acm.parse();

            ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
            ac.writeSeasonProfilePassive(acm.getSeasonProfile());
            ac.writeWeekProfileTablePassive(acm.getWeekProfile());
            ac.writeDayProfileTablePassive(acm.getDayProfile());

            if (name != null) {
                if (name.length() > 8) {
                    name = name.substring(0, 8);
                }
                ac.writeCalendarNamePassive(OctetString.fromString(name));
            }
            if (activateDate != null) {
                ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToGMTDateTime(activateDate).getBEREncodedByteArray(), 0));
            }
        } else if (userFile != null) {
            throw new IOException("ActivityCalendar by userfile is not supported yet.");
        } else {
            // should never get here
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        }
    }

    private void setLoadLimitGroupId(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set LoadLimit EmergencyProfile group ID's");

        Limiter epdiLimiter = getCosemObjectFactory().getLimiter();
        try {
            Lookup lut = this.findLookup();
            if (lut == null) {
                throw new IOException("No lookuptable defined with id '" + messageHandler.getEpGroupIdListLookupTableId() + "'");
            } else {
                Iterator entriesIt = lut.getEntries().iterator();
                Array idArray = new Array();
                while (entriesIt.hasNext()) {
                    LookupEntry lue = (LookupEntry) entriesIt.next();
                    idArray.addDataType(new Unsigned16(lue.getKey()));
                }
                epdiLimiter.writeEmergencyProfileGroupIdList(idArray);
            }
        } catch (NumberFormatException e) {
            throw new IOException("The given lookupTable id is not a valid entry. Error : " + e.getMessage());
        }
    }

    private Lookup findLookup() {
        // Todo: Lookup will NOT be ported to jupiter
        throw new UnsupportedOperationException("Looku is not longer supported by Jupiter");
    }

    protected void loadLimitConfiguration(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set LoadLimit configuration");

        byte theMonitoredAttributeType = -1;
        Limiter loadLimiter = getCosemObjectFactory().getLimiter();

        if (theMonitoredAttributeType == -1) {    // check for the type of the monitored value
            Limiter.ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
            if (valueDefinitionType.getClassId().getValue() == 0) {
                setMonitoredValue(loadLimiter);
                valueDefinitionType = loadLimiter.readMonitoredValue();
            }
            theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
        }

        // Write the normalThreshold
        if (messageHandler.getNormalThreshold() != null) {
            try {
                loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getNormalThreshold()));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the normalThreshold value to an integer.");
                throw new IOException("Could not pars the normalThreshold value to an integer." + e.getMessage());
            }
        }

        // Write the emergencyThreshold
        if (messageHandler.getEmergencyThreshold() != null) {
            try {
                loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getEmergencyThreshold()));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergencyThreshold value to an integer.");
                throw new IOException("Could not pars the emergencyThreshold value to an integer." + e.getMessage());
            }
        }

        // Write the minimumOverThresholdDuration
        if (messageHandler.getOverThresholdDurtion() != null) {
            try {
                loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(messageHandler.getOverThresholdDurtion())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the minimum over threshold duration value to an integer.");
                throw new IOException("Could not pars the minimum over threshold duration value to an integer." + e.getMessage());
            }
        }

        // Construct the emergencyProfile
        Structure emergencyProfile = new Structure();
        if (messageHandler.getEpProfileId() != null) {    // The EmergencyProfileID
            try {
                emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(messageHandler.getEpProfileId())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile id value to an integer.");
                throw new IOException("Could not pars the emergency profile id value to an integer." + e.getMessage());
            }
        }
        if (messageHandler.getEpActivationTime() != null) {    // The EmergencyProfileActivationTime
            try {
//						emergencyProfile.addDataType(new OctetString(convertStringToDateTimeOctetString(messageHandler.getEpActivationTime()).getBEREncodedByteArray(), 0, true));
                emergencyProfile.addDataType(new OctetString(convertUnixToGMTDateTime(messageHandler.getEpActivationTime()).getBEREncodedByteArray(), 0, true));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile activationTime value to a valid date.");
                throw new IOException("Could not pars the emergency profile activationTime value to a valid date." + e.getMessage());
            }
        }
        if (messageHandler.getEpDuration() != null) {        // The EmergencyProfileDuration
            try {
                emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(messageHandler.getEpDuration())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile duration value to an integer.");
                throw new IOException("Could not pars the emergency profile duration value to an integer." + e.getMessage());
            }
        }
        if ((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)) {    // If all three elements are correct, then send it, otherwise throw error
            throw new IOException("The complete emergecy profile must be filled in before sending it to the meter.");
        } else {
            if (emergencyProfile.nrOfDataTypes() > 0) {
                loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
            }
        }
    }

    protected void clearLoadLimiting(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Clear LoadLimit configuration");

        Limiter clearLLimiter = getCosemObjectFactory().getLimiter();

        // first do it the Iskra way, if it fails do it oure way

        Structure emptyStruct = new Structure();
        emptyStruct.addDataType(new Unsigned16(0));
        emptyStruct.addDataType(OctetString.fromByteArray(new byte[14]));
        emptyStruct.addDataType(new Unsigned32(0));
        try {
            clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
        } catch (IOException e) {
            if (e.getMessage().contains("Could not write the emergencyProfile structure.Cosem Data-Access-Result exception Type unmatched")) { // do it oure way
                emptyStruct = new Structure();
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
            } else {
                throw e;
            }
        }
    }

    private void setConnectMode(MessageHandler messageHandler) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot write connect mode, breaker is not supported!");
        }

        log(Level.INFO, "Handling message ConnectControl mode");
        String mode = messageHandler.getConnectControlMode();
        if (mode != null) {
            try {
                int modeInt = Integer.parseInt(mode);

                if ((modeInt >= 0) && (modeInt <= 6)) {
                    Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
                    connectorMode.writeControlMode(new TypeEnum(modeInt));

                } else {
                    throw new IOException("Mode is not a valid entry, value must be between 0 and 6");
                }

            } catch (NumberFormatException e) {
                throw new IOException("Mode is not a valid entry.");
            }
        } else {
            // should never get to the else, can't leave message empty
            throw new IOException("Message can not be empty");
        }
    }

    private void doDisconnect(MessageHandler messageHandler) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot execute disconnect message, breaker is not supported!");
        }

        log(Level.INFO, "Handling message Disconnect");

        if (!"".equals(messageHandler.getDisconnectDate())) { // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
            SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

            sasDisconnect.writeExecutedScript(scriptStruct);
            sasDisconnect.writeExecutionTime(executionTimeArray);

        } else {     // immediate disconnect
            Disconnector disconnector = getCosemObjectFactory().getDisconnector();
            disconnector.remoteDisconnect();
        }
    }

    private void doConnect(MessageHandler messageHandler) throws IOException {
        if (!protocol.hasBreaker()) {
            throw new IOException("Cannot execute connect message, breaker is not supported!");
        }

        log(Level.INFO, "Handling message Connect");
        if (!"".equals(messageHandler.getConnectDate())) {    // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
            SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

            sasConnect.writeExecutedScript(scriptStruct);
            sasConnect.writeExecutionTime(executionTimeArray);

        } else {    // immediate connect
            Disconnector connector = getCosemObjectFactory().getDisconnector();
            connector.remoteReconnect();
        }
    }

    private void setP1Text(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Consumer message Text");
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Text()));

    }

    private void setP1Code(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Consumer message Code");
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Code()));

    }

    private void doXmlConfig(String content) throws IOException {
        log(Level.INFO, "Handling message XmlConfig");
        String xmlConfigStr = getMessageValue(content, RtuMessageConstant.XMLCONFIG);
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xmlConfigStr));
    }

    protected void doFirmwareUpgrade(MessageHandler messageHandler, MessageEntry messageEntry) throws IOException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String firmwareContent = messageHandler.getFirmwareContent();

        byte[] imageData = GenericMessaging.b64DecodeAndUnZipToOriginalContent(firmwareContent);
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(imageData);
        if ("".equalsIgnoreCase(messageHandler.getActivationDate())) { // Do an execute now
            it.imageActivation();

            //Below is a solution for not immediately activating the image so the current connection isn't lost
//					Calendar cal = Calendar.getInstance();
//					cal.add(Calendar.MINUTE, 2);
//					SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
//					String strDate = Long.toString(cal.getTimeInMillis()/1000);
//					Array dateArray = convertUnixToDateTimeArray(strDate);
//
//					sas.writeExecutionTime(dateArray);


        } else if (!"".equalsIgnoreCase(messageHandler.getActivationDate())) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            String strDate = messageHandler.getActivationDate();
            Array dateArray = convertUnixToDateTimeArray(strDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    private void resetAlarmRegister() throws IOException {
        log(Level.INFO, "Handling message Reset Alarm register.");
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(0));
    }

    private void changeDefaultResetWindow(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Change default reset window.");
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(messageHandler.getDefaultResetWindow()));
    }

    protected MessageResult changeDiscoveryOnPowerUp(MessageEntry msgEntry, int enable) throws IOException {
        return MessageResult.createFailed(msgEntry, "Changing the discovery on power up is not supported in DSMR2.3");
    }

    protected void log(final Level level, final String msg) {
        this.dlmsSession.getLogger().log(level, msg);
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.dlmsSession.getTimeZone();
    }


    public CosemObjectFactory getCosemObjectFactory() {
        return this.dlmsSession.getCosemObjectFactory();
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsSession.getMeterConfig();
    }

    protected void setMonitoredValue(Limiter loadLimiter) throws IOException {
        Limiter.ValueDefinitionType vdt = loadLimiter.new ValueDefinitionType();
        vdt.addDataType(new Unsigned16(3));
        OctetString os = OctetString.fromByteArray(DEFAULT_MONITORED_ATTRIBUTE);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        loadLimiter.writeMonitoredValue(vdt);
    }

    /**
     * Get the monitoredAttributeType
     *
     * @param vdt
     * @return the abstractDataType of the monitored attribute
     * @throws IOException
     */
    protected byte getMonitoredAttributeType(Limiter.ValueDefinitionType vdt) throws IOException {

        if (getMeterConfig().getClassId(vdt.getObisCode()) == com.energyict.dlms.cosem.Register.CLASSID) {
            return getCosemObjectFactory().getRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == ExtendedRegister.CLASSID) {
            return getCosemObjectFactory().getExtendedRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return getCosemObjectFactory().getDemandRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == Data.CLASSID) {
            return getCosemObjectFactory().getData(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else {
            throw new IOException("WebRtuKP, getMonitoredAttributeType, invalid classID " + getMeterConfig().getClassId(vdt.getObisCode()) + " for obisCode " + vdt.getObisCode().toString());
        }
    }

    /**
     * Convert the value to write to the Limiter object to the correct monitored value type ...
     *
     * @param theMonitoredAttributeType
     * @param value
     * @return
     * @throws IOException
     */
    protected AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws IOException {

        final AxdrType axdrType = AxdrType.fromTag(theMonitoredAttributeType);
        switch (axdrType) {
            case NULL: {
                return new NullData();
            }
            case BOOLEAN: {
                return new BooleanObject("1".equalsIgnoreCase(value));
            }
            case BIT_STRING: {
                return new BitString(Integer.parseInt(value));
            }
            case DOUBLE_LONG: {
                return new Integer32(Integer.parseInt(value));
            }
            case DOUBLE_LONG_UNSIGNED: {
                return new Unsigned32(Integer.parseInt(value));
            }
            case OCTET_STRING: {
                return OctetString.fromString(value);
            }
            case VISIBLE_STRING: {
                return new VisibleString(value);
            }
            case INTEGER: {
                return new Integer8(Integer.parseInt(value));
            }
            case LONG: {
                return new Integer16(Integer.parseInt(value));
            }
            case UNSIGNED: {
                return new Unsigned8(Integer.parseInt(value));
            }
            case LONG_UNSIGNED: {
                return new Unsigned16(Integer.parseInt(value));
            }
            case LONG64: {
                return new Integer64(Integer.parseInt(value));
            }
            case ENUM: {
                return new TypeEnum(Integer.parseInt(value));
            }
            default:
                throw new IOException("convertToMonitoredtype error, unknown type.");
        }
    }

    private Date getFirstDate(Date startTime, String type) throws IOException {
        return getFirstDate(startTime, type, getTimeZone());
    }

    private Date getFirstDate(Date startTime, String type, TimeZone timeZone) throws IOException {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.setTime(startTime);
        cal1.getTime();
        if ("15".equalsIgnoreCase(type)) {
            if (cal1.get(Calendar.MINUTE) < 15) {
                cal1.set(Calendar.MINUTE, 14);
                cal1.set(Calendar.SECOND, 40);
            } else if (cal1.get(Calendar.MINUTE) < 30) {
                cal1.set(Calendar.MINUTE, 29);
                cal1.set(Calendar.SECOND, 40);
            } else if (cal1.get(Calendar.MINUTE) < 45) {
                cal1.set(Calendar.MINUTE, 44);
                cal1.set(Calendar.SECOND, 40);
            } else {
                cal1.set(Calendar.MINUTE, 59);
                cal1.set(Calendar.SECOND, 40);
            }
            return cal1.getTime();
        } else if ("day".equalsIgnoreCase(type)) {
            cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime()) / 3600000)));
            cal1.set(Calendar.MINUTE, 59);
            cal1.set(Calendar.SECOND, 40);
            return cal1.getTime();
        } else if ("month".equalsIgnoreCase(type)) {
            cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime()) / 3600000)));
            cal1.set(Calendar.MINUTE, 59);
            cal1.set(Calendar.SECOND, 40);
            return cal1.getTime();
        }

        throw new IOException("Invalid intervaltype.");
    }

    private void waitForCrossingBoundry() throws IOException {
        try {
            for (int i = 0; i < 3; i++) {
                ProtocolTools.delay(15000);
                log(Level.INFO, "Keeping connection alive");
                getCosemObjectFactory().getClock().getDateTime();
            }
        } catch (IOException e) {
            throw new IOException("Could not keep connection alive." + e.getMessage());
        }
    }

    private Date setBeforeNextInterval(Date startTime, String type) throws IOException {
        return setBeforeNextInterval(startTime, type, getTimeZone());
    }

    private Date setBeforeNextInterval(Date startTime, String type, TimeZone timeZone) throws IOException {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.setTime(startTime);
        int zoneOffset;
        if ("15".equalsIgnoreCase(type)) {
            cal1.add(Calendar.MINUTE, 15);
            return cal1.getTime();
        } else if ("day".equalsIgnoreCase(type)) {
            zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
            cal1.add(Calendar.DAY_OF_MONTH, 1);
            zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
            cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
            return cal1.getTime();
        } else if ("month".equalsIgnoreCase(type)) {
            zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
            cal1.add(Calendar.MONTH, 1);
            cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
            zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
            cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
            return cal1.getTime();
        }

        throw new IOException("Invalid intervaltype.");
    }

    protected Throwable getRootCause(NestedIOException e) {
        Throwable throwable = e.getCause();
        while (throwable.getClass().equals(NestedIOException.class)) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    public AbstractSmartNtaProtocol getProtocol() {
        return protocol;
    }

}