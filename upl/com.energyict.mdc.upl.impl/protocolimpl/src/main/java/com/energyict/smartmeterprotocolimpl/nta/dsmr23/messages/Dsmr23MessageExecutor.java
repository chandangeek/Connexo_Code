package com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.AutoConnect;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.genericprotocolimpl.webrtu.common.MbusProvider;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.UserFileShadow;
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
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.generic.csvhandling.CSVParser;
import com.energyict.protocolimpl.generic.csvhandling.TestObject;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.NTAMessageHandler;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 13:42:50
 */
public class Dsmr23MessageExecutor extends MessageParser {

    protected final DlmsSession dlmsSession;
    protected final AbstractSmartNtaProtocol protocol;
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");

    private static final byte[] defaultMonitoredAttribute = new byte[]{1, 0, 90, 7, 0, (byte) 255};    // Total current, instantaneous value
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public Dsmr23MessageExecutor(final AbstractSmartNtaProtocol protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        this.protocol = protocol;
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.dlmsSession = this.protocol.getDlmsSession();
    }

    protected TariffCalendarFinder getCalendarFinder() {
        return calendarFinder;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
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

                /* All eMeter related messages */
                boolean xmlConfig = messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
                boolean firmware = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);
                boolean p1Text = messageHandler.getType().equals(RtuMessageConstant.P1TEXTMESSAGE);
                boolean p1Code = messageHandler.getType().equals(RtuMessageConstant.P1CODEMESSAGE);
                boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
                boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
                boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
                boolean llConfig = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
                boolean llClear = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);
                boolean llSetGrId = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
                boolean touCalendar = messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
                boolean touSpecialDays = messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS);
                boolean specialDelEntry = messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
                boolean setTime = messageHandler.getType().equals(RtuMessageConstant.SET_TIME);
                boolean fillUpDB = messageHandler.getType().equals(RtuMessageConstant.ME_MAKING_ENTRIES);
                boolean gprsParameters = messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_SETUP);
                boolean gprsCredentials = messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
                boolean testMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);
                boolean testSecurityMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_SECURITY_MESSAGE);
                boolean globalReset = messageHandler.getType().equals(RtuMessageConstant.GLOBAL_METER_RESET);
                boolean factorySettings = messageHandler.getType().equals(RtuMessageConstant.RESTORE_FACTORY_SETTINGS);
                boolean wakeUpWhiteList = messageHandler.getType().equals(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
                boolean changeHLSSecret = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_HLS_SECRET);
                boolean changeLLSSecret = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_LLS_SECRET);
                boolean changeGlobalkey = messageHandler.getType().equals(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY);
                boolean changeAuthkey = messageHandler.getType().equals(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY);
                boolean activateSMS = messageHandler.getType().equals(RtuMessageConstant.WAKEUP_ACTIVATE);
                boolean deActivateSMS = messageHandler.getType().equals(RtuMessageConstant.WAKEUP_DEACTIVATE);
                boolean actSecuritLevel = messageHandler.getType().equals(RtuMessageConstant.AEE_ACTIVATE_SECURITY);
                boolean serviceKeyHlsSecret = messageHandler.getType().equals(RtuMessageConstant.SERVICEKEY_HLSSECRET);
                boolean serviceKeyAK = messageHandler.getType().equals(RtuMessageConstant.SERVICEKEY_AK);
                boolean serviceKeyEK = messageHandler.getType().equals(RtuMessageConstant.SERVICEKEY_EK);
                boolean changeAuthLevel = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
                boolean enableAuthLevelP0 = messageHandler.getType().equals(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P0);
                boolean disableAuthLevelP0 = messageHandler.getType().equals(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P0);
                boolean enableAuthLevelP3 = messageHandler.getType().equals(RtuMessageConstant.AEE_ENABLE_AUTHENTICATION_LEVEL_P3);
                boolean disableAuthLevelP3 = messageHandler.getType().equals(RtuMessageConstant.AEE_DISABLE_AUTHENTICATION_LEVEL_P3);
                boolean partialLoadProfile = messageHandler.getType().equals(LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag());
                boolean loadProfileRegisterRequest = messageHandler.getType().equals(LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag());
                boolean resetAlarmRegisterRequest = messageHandler.getType().equals(RtuMessageConstant.RESET_ALARM_REGISTER);
                boolean isChangeDefaultResetWindow = messageHandler.getType().equals(RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW);
                boolean isChangeAdministrativeStatus = messageHandler.getType().equals(RtuMessageConstant.CHANGE_ADMINISTRATIVE_STATUS);
                boolean enableDiscoveryOnPowerUp = messageHandler.getType().equals(RtuMessageConstant.ENABLE_DISCOVERY_ON_POWER_UP);
                boolean disableDiscoveryOnPowerUp = messageHandler.getType().equals(RtuMessageConstant.DISABLE_DISCOVERY_ON_POWER_UP);
                boolean installMbus = messageHandler.getType().equals(RtuMessageConstant.MBUS_INSTALL);
                boolean resetMbusClient = messageHandler.getType().equals(RtuMessageConstant.RESET_MBUS_CLIENT);
                boolean isMbusClientRemoteCommission = messageHandler.getType().equals(RtuMessageConstant.MBUS_REMOTE_COMMISSION);
                //Messages for changing the MBus Client Object Attributes
                boolean isChangeMbusClientAttributes = messageHandler.getType().equals(RtuMessageConstant.CHANGE_MBUS_CLIENT_ATTRIBUTES);
                /* All MbusMeter related messages */
                if (xmlConfig) {
                    doXmlConfig(content);
                } else if (firmware) {
                    doFirmwareUpgrade(messageHandler, msgEntry);
                } else if (p1Code) {
                    setP1Code(messageHandler);
                } else if (p1Text) {
                    setP1Text(messageHandler);
                } else if (connect) {
                    doConnect(messageHandler);
                } else if (disconnect) {
                    doDisconnect(messageHandler);
                } else if (connectMode) {
                    setConnectMode(messageHandler);
                } else if (llConfig) {
                    loadLimitConfiguration(messageHandler);
                } else if (llClear) {
                    clearLoadLimiting(messageHandler);
                } else if (llSetGrId) {
                    setLoadLimitGroupId(messageHandler);
                } else if (touCalendar) {
                    upgradeCalendar(messageHandler);
                } else if (touSpecialDays) {
                    upgradeSpecialDays(messageHandler);
                } else if (specialDelEntry) {
                    deleteSpecialDay(messageHandler);
                } else if (setTime) {
                    setTime(messageHandler);
                } else if (fillUpDB) {
                    createDataBaseEntries(messageHandler);
                } else if (gprsParameters) {
                    setGPRSParameters(messageHandler);
                } else if (gprsCredentials) {
                    setGPRSCredentials(messageHandler);
                } else if (testMessage) {
                    testMessage(messageHandler);
                } else if (testSecurityMessage) {
                    msgResult = testSecurityMessage(messageHandler, msgEntry);
                } else if (globalReset) {
                    doGlobalReset();
                } else if (factorySettings) {
                    restoreFactorySettings();
                } else if (wakeUpWhiteList) {
                    setWakeUpWhiteList(messageHandler);
                } else if (changeHLSSecret) {
                    changeHLSSecret(messageHandler);
                } else if (changeLLSSecret) {
                    changeLLSSecret(messageHandler);
                } else if (changeAuthkey) {
                    changeAuthenticationKey(messageHandler);
                } else if (changeGlobalkey) {
                    changeGlobalKey(messageHandler);
                } else if (activateSMS) {
                    activateSms();
                } else if (deActivateSMS) {
                    deactivateSms();
                } else if (serviceKeyHlsSecret) {
                    serviceKeyHlsSecret(messageHandler);
                } else if (serviceKeyAK) {
                    serviceKeyAK(messageHandler);
                } else if (serviceKeyEK) {
                    serviceKeyEK(messageHandler);
                } else if (actSecuritLevel) {
                    getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(messageHandler.getSecurityLevel()));
                } else if (changeAuthLevel) {
                    msgResult = changeAuthenticationLevel(msgEntry, messageHandler);
                } else if (enableAuthLevelP0) {
                    msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 0, true);
                } else if (disableAuthLevelP0) {
                    msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 0, false);
                } else if (enableAuthLevelP3) {
                    msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 3, true);
                } else if (disableAuthLevelP3) {
                    msgResult = changeAuthenticationLevel(msgEntry, messageHandler, 3, false);
                } else if (partialLoadProfile) {
                    msgResult = doReadPartialLoadProfile(msgEntry);
                } else if (loadProfileRegisterRequest) {
                    msgResult = doReadLoadProfileRegisters(msgEntry);
                } else if (resetAlarmRegisterRequest) {
                    resetAlarmRegister();
                } else if (isChangeDefaultResetWindow) {
                    changeDefaultResetWindow(messageHandler);
                } else if (isChangeAdministrativeStatus) {
                    changeAdministrativeStatus(messageHandler);
                } else if (enableDiscoveryOnPowerUp) {
                    msgResult = changeDiscoveryOnPowerUp(msgEntry, 1);
                } else if (disableDiscoveryOnPowerUp) {
                    msgResult = changeDiscoveryOnPowerUp(msgEntry, 0);
                } else if (installMbus) {
                    installMbus(messageHandler);
                } else if (resetMbusClient) {
                    resetMbusClient(messageHandler);
                } else if (isMbusClientRemoteCommission){
                    mBusClientRemoteCommissioning(messageHandler);
                } else if (isChangeMbusClientAttributes) {
                    changeMBusClientAttributes(messageHandler);
                }else {
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

            } catch (ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            } catch (NestedIOException e) {
                Throwable rootCause = getRootCause(e);
                if (rootCause.getClass().equals(ConnectionException.class)) {
                    throw new NestedIOException(rootCause);
                }
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            } catch (BusinessException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            } catch (IOException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            } catch (InterruptedException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (SQLException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            }
            return msgResult;
        }
    }


    private void installMbus(MessageHandler messageHandler) throws IOException {
        int installChannel = messageHandler.getMbusInstallChannel();
        int primaryAddress = getMBusPhysicalAddress(installChannel);
        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) (primaryAddress - 1));
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(mbusClientObisCode, MbusClientAttributes.VERSION9);
        mbusClient.installSlave(primaryAddress);
    }

    private int getMBusPhysicalAddress(int installChannel) throws ProtocolException {
        int physicalAddress;
        if(installChannel == 0){
            physicalAddress = (byte)this.protocol.getMeterTopology().searchNextFreePhysicalAddress();
            log(Level.INFO, "Channel: " + physicalAddress + " will be used as MBUS install channel.");
        }else{
            physicalAddress = installChannel;
        }
        return physicalAddress;
    }
    /**
     * Not supported for the DSMR 2.3 protocols.
     * Sub classes can override.
     */
    protected void resetMbusClient(MessageHandler messageHandler) throws IOException {
        throw new IOException("Message to reset the MBus client is only supported in the IBM Kaifa protocol");
    }

    protected Dsmr23MbusMessageExecutor getMbusMessageExecutor() {
        return new Dsmr23MbusMessageExecutor(protocol);
    }

    //Cryptoserver protocol overrides this implemenation
    protected void serviceKeyHlsSecret(MessageHandler messageHandler) throws IOException {
        throw new IOException("Received message to write the service HLS secret, but Cryptoserver usage is not supported in this protocol");
    }

    protected void serviceKeyAK(MessageHandler messageHandler) throws IOException {
        throw new IOException("Received message to write the service authentication key, but Cryptoserver usage is not supported in this protocol");
    }

    protected void serviceKeyEK(MessageHandler messageHandler) throws IOException {
        throw new IOException("Received message to write the service encryption key, but Cryptoserver usage is not supported in this protocol");
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
            builder = (LegacyLoadProfileRegisterMessageBuilder) builder.fromXml(msgEntry.getContent());
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!");
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
                        int rtuRegisterId = builder.getRtuRegisterIdForRegister(register);
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
            builder = (LegacyPartialLoadProfileMessageBuilder) builder.fromXml(msgEntry.getContent());

            LoadProfileReader lpr = builder.getLoadProfileReader();

            lpr = checkLoadProfileReader(lpr, msgEntry);
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

    private MessageResult testSecurityMessage(MessageHandler messageHandler, MessageEntry messageEntry) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling message TestSecurityMessage");
        String userFileId = messageHandler.getTestUserFileId();

        UserFile uf;
        try {
            int id = Integer.parseInt(userFileId);
            uf = mw().getUserFileFactory().find(id);
        } catch (NumberFormatException e) {
            log(Level.INFO, "Cannot find userfile with ID '" + userFileId + "' in the database... aborting.");
            return MessageResult.createFailed(messageEntry, "Cannot find userfile with ID '" + userFileId + "' in the database... aborting.");
        }
        if (uf == null) {
            log(Level.INFO, "Cannot find userfile with ID '" + userFileId + "' in the database... aborting.");
            return MessageResult.createFailed(messageEntry, "Cannot find userfile with ID '" + userFileId + "' in the database... aborting.");
        }

        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fstream = new FileInputStream(uf.getShadow().getFile());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                String trim = strLine.trim();
                String line = trim.replaceAll(" ", "");
                byte[] frame = ProtocolTools.getBytesFromHexString(line, "");
                DLMSConnection dlmsConnection = dlmsSession.getDLMSConnection();
                if (dlmsConnection instanceof TCPIPConnection || dlmsConnection instanceof SecureConnection) {
                    try {
                        byte[] bytes = dlmsConnection.sendRawBytes(frame);
                        sb.append(ProtocolTools.getHexStringFromBytes(bytes, " ").trim());
                    } catch (IOException | DLMSConnectionException e) {
                        String msg = "Error while sending [" + strLine + "]:" + e.getMessage();
                        log(Level.WARNING, msg);
                        sb.append(msg);
                    }
                    sb.append("\n");
                } else {
                    log(Level.SEVERE, "Error while sending bytes to meter, expected a TCP/IP connection, but was " + dlmsConnection.getClass().getSimpleName());
                    in.close();
                    return MessageResult.createFailed(messageEntry, "Error while sending bytes to meter, expected a TCP/IP connection, but was " + dlmsConnection.getClass().getSimpleName());
                }
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {
            log(Level.SEVERE, "Error while parsing or sending user file: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, "Error while parsing or sending user file: " + e.getMessage());
        }

        UserFileShadow userFileShadow = ProtocolTools.createUserFileShadow(uf.getName() + "_results_" + String.valueOf(new Date().getTime()), sb.toString().getBytes(), uf.getFolderId(), "txt");
        mw().getUserFileFactory().create(userFileShadow);

        return MessageResult.createSuccess(messageEntry);
    }

    private void testMessage(MessageHandler messageHandler) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling message TestMessage");
        int failures = 0;
        String userFileId = messageHandler.getTestUserFileId();
        Date currentTime;
        if (!userFileId.equalsIgnoreCase("")) {
            if (ParseUtils.isInteger(userFileId)) {
                UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileId));
                if (uf != null) {
                    byte[] data = uf.loadFileInByteArray();
                    CSVParser csvParser = new CSVParser(this.messageFileExtractor);
                    csvParser.parse(data);
                    boolean hasWritten;
                    TestObject to = new TestObject("");
                    for (int i = 0; i < csvParser.size(); i++) {
                        to = csvParser.getTestObject(i);
                        if (csvParser.isValidLine(to)) {
                            currentTime = new Date(System.currentTimeMillis());
                            hasWritten = false;
                            try {
                                switch (to.getType()) {
                                    case 0: { // GET
                                        GenericRead gr = getCosemObjectFactory().getGenericRead(to.getObisCode(), DLMSUtils.attrLN2SN(to.getAttribute()), to.getClassId());
                                        to.setResult("0x" + ParseUtils.decimalByteToString(gr.getResponseData()));
                                        hasWritten = true;
                                    }
                                    break;
                                    case 1: { // SET
                                        GenericWrite gw = getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
                                        gw.write(ParseUtils.hexStringToByteArray(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 2: { // ACTION
                                        GenericInvoke gi = getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
                                        if ("".equalsIgnoreCase(to.getData())) {
                                            gi.invoke();
                                        } else {
                                            gi.invoke(ParseUtils.hexStringToByteArray(to.getData()));
                                        }
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 3: { // MESSAGE
                                        // do nothing, no longer supported
                                        //OldDeviceMessageShadow rms = new OldDeviceMessageShadow();
                                        //rms.setContents(csvParser.getTestObject(i).getData());
                                        //rms.setRtuId(getRtuFromDatabaseBySerialNumber().getId());
                                        //OldDeviceMessage rm = mw().getRtuMessageFactory().create(rms);
                                        //doMessage(rm);
                                        //if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                        //    to.setResult("OK");
                                        //} else {
                                        //    to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        //}
                                        //hasWritten = true;
                                    }
                                    break;
                                    case 4: { // WAIT
                                        waitCyclus(Integer.parseInt(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 5: {
                                        // do nothing, it's no valid line
                                    }
                                    break;
                                    default: {
                                        throw new ApplicationException("Row " + i + " of the CSV file does not contain a valid type.");
                                    }
                                }
                                to.setTime(currentTime.getTime());

                                // Check if the expected value is the same as the result
                                if ((to.getExpected() == null) || (!to.getExpected().equalsIgnoreCase(to.getResult()))) {
                                    to.setResult("Failed - " + to.getResult());
                                    failures++;
                                    log(Level.INFO, "Test " + i + " has successfully finished, but the result didn't match the expected value.");
                                } else {
                                    log(Level.INFO, "Test " + i + " has successfully finished.");
                                }

                            } catch (Exception e) {
                                if (!hasWritten) {
                                    if ((to.getExpected() != null) && (e.getMessage().contains(to.getExpected()))) {
                                        to.setResult(e.getMessage());
                                        log(Level.INFO, "Test " + i + " has successfully finished.");
                                        hasWritten = true;
                                    } else {
                                        log(Level.INFO, "Test " + i + " has failed.");
                                        String eMessage;
                                        if (e.getMessage().contains("\r\n")) {
                                            eMessage = e.getMessage().substring(0, e.getMessage().indexOf("\r\n")) + "...";
                                        } else {
                                            eMessage = e.getMessage();
                                        }
                                        to.setResult("Failed. " + eMessage);
                                        hasWritten = true;
                                        failures++;
                                    }
                                    to.setTime(currentTime.getTime());
                                }
                            } finally {
                                if (!hasWritten) {
                                    to.setResult("Failed - Unknow exception ...");
                                    failures++;
                                    to.setTime(currentTime.getTime());
                                }
                            }
                        }
                    }
                    if (failures == 0) {
                        csvParser.addLine("All the tests are successfully finished.");
                    } else {
                        csvParser.addLine("" + failures + " of the " + csvParser.getValidSize() + " tests " + ((failures == 1) ? "has" : "have") + " failed.");
                    }
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getRtuFromDatabaseBySerialNumber().getFolderId()));
                } else {
                    throw new ApplicationException("Userfile with ID " + userFileId + " does not exist.");
                }
            } else {
                throw new IOException("UserFileId is not a valid number");
            }
        } else {
            throw new IOException("No userfile id is given.");
        }
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
            Long millis = Long.parseLong(messageHandler.getMEStartDate()) * 1000;
            Date startTime = new Date(Long.parseLong(messageHandler.getMEStartDate()) * 1000);
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
            TariffCalendar tariffCalendar = this.calendarFinder.from(codeTable).orElseThrow(() -> new IllegalArgumentException("No CodeTable defined with id '" + codeTable + "'"));
            Array sdArray = new Array();
            SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
            List<TariffCalendarExtractor.CalendarRule> rules = this.calendarExtractor.rules(tariffCalendar);
            for (int i = 0; i < rules.size(); i++) {
                TariffCalendarExtractor.CalendarRule rule = rules.get(i);
                if (!rule.seasonId().isPresent()) {
                    OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((rule.year() == -1) ? 0xff : ((rule.year() >> 8) & 0xFF)), (byte) ((rule.year() == -1) ? 0xff : (rule.year()) & 0xFF),
                            (byte) ((rule.month() == -1) ? 0xFF : rule.month()), (byte) ((rule.day() == -1) ? 0xFF : rule.day()),
                            (byte) ((rule.dayOfWeek() == -1) ? 0xFF : rule.dayOfWeek())});
                    Unsigned8 dayType = new Unsigned8(Integer.parseInt(rule.dayTypeId()));
                    Structure struct = new Structure();
                    AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((rule.year() == -1) ? 0x07 : ((rule.year() >> 8) & 0xFF)), (byte) ((rule.year() == -1) ? 0xB2 : (rule.year()) & 0xFF),
                            (byte) ((rule.month() == -1) ? 0xFF : rule.month()), (byte) ((rule.day() == -1) ? 0xFF : rule.day()),
                            (byte) ((rule.dayOfWeek() == -1) ? 0xFF : rule.dayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
                    long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
                    struct.addDataType(new Unsigned16((int) days));
                    struct.addDataType(os);
                    struct.addDataType(dayType);
//								sdt.insert(struct);
                    sdArray.addDataType(struct);
                }
            }

            if (sdArray.nrOfDataTypes() != 0) {
                sdt.writeSpecialDays(sdArray);
            }
        }
    }

    protected void upgradeCalendar(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Activity calendar");

        String name = messageHandler.getTOUCalendarName();
        String activateDate = messageHandler.getTOUActivationDate();
        String codeTable = messageHandler.getTOUCodeTable();
        String userFile = messageHandler.getTOUUserFile();

        if ((codeTable == null) && (userFile == null)) {
            throw new IllegalArgumentException("CodeTable-ID AND UserFile-ID can not be both empty.");
        } else if ((codeTable != null) && (userFile != null)) {
            throw new IllegalArgumentException("CodeTable-ID AND UserFile-ID can not be both filled in.");
        }

        if (codeTable != null) {
            TariffCalendar tariffCalendar = this.calendarFinder.from(codeTable).orElseThrow(() -> new IllegalArgumentException("No CodeTable defined with id '" + codeTable + "'"));
            ActivityCalendarMessage acm = new ActivityCalendarMessage(tariffCalendar, calendarExtractor, getMeterConfig());
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
//							ac.writeActivatePassiveCalendarTime(new OctetString(convertStringToDateTimeOctetString(activateDate).getBEREncodedByteArray(), 0, true));
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
            Lookup lut = mw().getLookupFactory().find(Integer.parseInt(messageHandler.getEpGroupIdListLookupTableId()));
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
            if (e.getMessage().indexOf("Could not write the emergencyProfile structure.Cosem Data-Access-Result exception Type unmatched") != -1) { // do it oure way
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

        if (!messageHandler.getDisconnectDate().equals("")) { // use the disconnectControlScheduler

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
        if (!messageHandler.getConnectDate().equals("")) {    // use the disconnectControlScheduler

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

    protected void doFirmwareUpgrade(MessageHandler messageHandler, MessageEntry messageEntry) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();

        if (!ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }
        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
        if (!(uf instanceof UserFile)) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }

        byte[] imageData = uf.loadFileInByteArray();
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(imageData);
        if (messageHandler.getActivationDate().equalsIgnoreCase("")) { // Do an execute now
            it.imageActivation();

            //Below is a solution for not immediately activating the image so the current connection isn't lost
//					Calendar cal = Calendar.getInstance();
//					cal.add(Calendar.MINUTE, 2);
//					SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
//					String strDate = Long.toString(cal.getTimeInMillis()/1000);
//					Array dateArray = convertUnixToDateTimeArray(strDate);
//
//					sas.writeExecutionTime(dateArray);


        } else if (!messageHandler.getActivationDate().equalsIgnoreCase("")) {
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

    private void changeAdministrativeStatus(MessageHandler messageHandler) throws IOException {
        int status = messageHandler.getAdministrativeStatus();
        log(Level.INFO, "Changing administrative status to " + status);
        getCosemObjectFactory().getData(DSMR40RegisterFactory.AdministrativeStatusObisCode).setValueAttr(new TypeEnum(status));
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
        OctetString os = OctetString.fromByteArray(defaultMonitoredAttribute);
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

        if (getMeterConfig().getClassId(vdt.getObisCode()) == Register.CLASSID) {
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
                return new BooleanObject(value.equalsIgnoreCase("1"));
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
        if (type.equalsIgnoreCase("15")) {
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
        } else if (type.equalsIgnoreCase("day")) {
            cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime()) / 3600000)));
            cal1.set(Calendar.MINUTE, 59);
            cal1.set(Calendar.SECOND, 40);
            return cal1.getTime();
        } else if (type.equalsIgnoreCase("month")) {
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
        int zoneOffset = 0;
        if (type.equalsIgnoreCase("15")) {
            cal1.add(Calendar.MINUTE, 15);
            return cal1.getTime();
        } else if (type.equalsIgnoreCase("day")) {
            zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
            cal1.add(Calendar.DAY_OF_MONTH, 1);
            zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
            cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
            return cal1.getTime();
        } else if (type.equalsIgnoreCase("month")) {
            zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
            cal1.add(Calendar.MONTH, 1);
            cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
            zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
            cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
            return cal1.getTime();
        }

        throw new IOException("Invalid intervaltype.");
    }

    private void waitCyclus(int delay) throws IOException {
        try {
            int nrOfPolls = (delay / (20)) + (delay % (20) == 0 ? 0 : 1);
            for (int i = 0; i < nrOfPolls; i++) {
                if (i < nrOfPolls - 1) {
                    ProtocolTools.delay(20000);
                } else {
                    ProtocolTools.delay((delay - (i * (20))) * 1000);
                }
                log(Level.INFO, "Keeping connection alive");
                getCosemObjectFactory().getClock().getDateTime();
            }
        } catch (IOException e) {
            throw new IOException("Could not keep connection alive." + e.getMessage());
        }
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

    /**
     * *************************************************************************
     */
    /* These methods require database access ...
    /*****************************************************************************/
    private Device getRtuFromDatabaseBySerialNumber() {
        String serial = this.protocol.getSerialNumber();
        Device rtu = mw().getDeviceFactory().findBySerialNumber(serial).get(0);
        ProtocolTools.closeConnection();
        return rtu;
    }

    protected MeteringWarehouse mw() {
        return ProtocolTools.mw();
    }

    private void mBusClientRemoteCommissioning(MessageHandler messageHandler) throws IOException{
        int installChannel = messageHandler.getMbusInstallChannel();
        int physicalAddress = getMBusPhysicalAddress(installChannel);
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(physicalAddress - 1).getObisCode(), 9);
        String shortId = messageHandler.getMbusShortId();
        MbusProvider mbusProvider = new MbusProvider(getCosemObjectFactory(),((Dsmr23Properties)getProtocol().getProperties()).getFixMbusHexShortId());
        try {
            mbusClient.setManufacturerID(mbusProvider.getManufacturerID(shortId));
            mbusClient.setIdentificationNumber(mbusProvider.getIdentificationNumber(shortId));
            mbusClient.setVersion(mbusProvider.getVersion(shortId));
            mbusClient.setDeviceType(mbusProvider.getDeviceType(shortId));
        }catch(ProtocolException e){
            log(Level.SEVERE,"Invalid short id value.");
        }
    }

    private void changeMBusClientAttributes(MessageHandler messageHandler) throws IOException {
        int installChannel = messageHandler.getMbusInstallChannel();
        int physicalAddress = getMBusPhysicalAddress(installChannel);
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(physicalAddress - 1).getObisCode(),9);
        try {
            mbusClient.setManufacturerID(messageHandler.getMbusClientManufacturerID());
            mbusClient.setIdentificationNumber(messageHandler.getMbusClientIdentificationNumber(((Dsmr23Properties)getProtocol().getProperties()).getFixMbusHexShortId()));
            mbusClient.setDeviceType(messageHandler.getMbusDeviceType());
            mbusClient.setVersion(messageHandler.getMbusClientVersion());
        }catch(ProtocolException e){
            log(Level.SEVERE,"Invalid short id value.");
        }
    }
}
