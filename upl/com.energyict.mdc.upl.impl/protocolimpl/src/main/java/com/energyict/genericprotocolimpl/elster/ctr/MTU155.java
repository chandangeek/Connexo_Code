package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.ctr.discover.InstallationDateDiscover;
import com.energyict.genericprotocolimpl.elster.ctr.discover.MTU155Discover;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConfigurationException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRDiscoverException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRExceptionWithProfileData;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ActivateTemporaryKeyMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ChangeDSTMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ChangeExecutionKeyMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ChangeSealStatusMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ChangeTemporaryKeyMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ForceSyncClockMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.MTU155MessageExecutor;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.ReadPartialProfileDataMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.TariffDisablePassiveMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.TariffUploadPassiveMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.TemporaryBreakSealMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.WakeUpFrequency;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.WriteConverterMasterDataMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.WriteGasParametersMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.WriteMeterMasterDataMessage;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.WritePDRMessage;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.profile.ProfileChannel;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.CodeTableBase64Builder;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.FirmwareUpdateMessaging;
import com.energyict.protocol.messaging.FirmwareUpdateMessagingConfig;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocolimpl.base.RtuDiscoveredEvent;
import com.energyict.protocolimpl.debug.DebugUtils;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.protocolimpl.utils.MeterEventUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 12-may-2011
 * Time: 13:16:45
 */
public class MTU155 extends AbstractGenericProtocol implements FirmwareUpdateMessaging {

    private final StoreObject storeObject = new StoreObject();
    private final MTU155Properties properties = new MTU155Properties();
    private RequestFactory requestFactory;
    private boolean isOutboundSmsProfile;
    private OutboundSmsHandler outboundSmsHandler;
    private ObisCodeMapper obisCodeMapper;
    private Device rtu;
    private MeterAmrLogging meterAmrLogging;
    private MTU155Discover mtu155Discover;
    private long startTime = -1;
    private RtuDiscoveredEvent discoveredEvent = null;

    /**
     * Main MTU155 protocol class. This is the class that should be configured in EIServer
     */
    public MTU155() {

    }

    /**
     * The protocol version 2011/08/03
     *
     * @return
     */
    public String getVersion() {
        return "$Date$";
    }

    /**
     * @return
     */
    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

    /**
     * @return
     */
    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }
//
//    @Override
//    public List<PropertySpec> getRequiredProperties() {
//        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
//    }
//
//    @Override
//    public List<PropertySpec> getOptionalProperties() {
//        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
//    }

    /**
     *
     */
    @Override
    public void initProperties() {
        properties.addProperties(getProperties());
    }

    /**
     *
     */
    @Override
    protected void doExecute() throws BusinessException, SQLException {
//
//        try {
//            isOutboundSmsProfile = getOutboundSmsHandler().isOutboundSmsProfile(getCommunicationScheduler());
//            if (isOutboundSmsProfile) {
//                getOutboundSmsHandler().doExecute(getCommunicationScheduler());
//            } else {
//
//                getProtocolProperties().addProperties(getPropertiesFromProtocolClass());
//                log("Incomming TCP connection from: " + getRequestFactory().getIPAddress());
//                updateRequestFactory();
//
//                logMeterInfo();
//
//                this.rtu = identifyAndGetRtu();
//                log("Device with name '" + getRtu().getName() + "' connected successfully.");
//                getProtocolProperties().addProperties(rtu.getOldProtocol().getProperties().toStringProperties());
//                getProtocolProperties().addProperties(rtu.getProperties().toStringProperties());
//                updateRequestFactory();
//                checkSerialNumbers();
//                readDevice();
//            }
//        } catch (CTRException e) {
//            severe(e.getMessage());
//        } finally {
//            try {
//                disconnect();
//            } catch (Exception e) {
//                severe("Error closing connection: " + e.getMessage());
//            }
//        }
//
//        try {
//            getStoreObject().doExecute();
//            if (discoveredEvent != null) {
//                log("Sending new RtuDiscoveredEvent for rtu [" + getRtu() + "]");
//                MeteringWarehouse.getCurrent().signalEvent(discoveredEvent);
//            }
//        } catch (BusinessException e) {
//            e.printStackTrace();
//            severe(e.getMessage());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            severe(e.getMessage());
//        }

    }

    private void logMeterInfo() {
        IdentificationResponseStructure structure = getRequestFactory().getIdentificationStructure();
        CTRAbstractValue<String> pdrObject = structure != null ? structure.getPdr() : null;
        String pdr = pdrObject != null ? pdrObject.getValue() : null;

        MeterInfo meterInfo = getRequestFactory().getMeterInfo();

        log("MTU155 with pdr='" + pdr + "'");
        log("Serial number of the MTU155='" + meterInfo.getMTUSerialNumber() + "'");
        log("Serial number of the converter='" + meterInfo.getConverterSerialNumber() + "'");
        log("Serial number of the gas meter='" + (structure != null ? structure.getMeterSerialNumber() : null) + "'");
    }

    /**
     * Checks if the RTU serial number matches the one in EiServer
     *
     * @throws CTRConfigurationException, when the serial number doesn't match
     */
    private void checkSerialNumbers() throws CTRConfigurationException {
        try {
            validateMeterSerialNumber();
            validateConverterSerialNumber();
        } catch (CTRConfigurationException e) {
//            List<CommunicationScheduler> communicationSchedulers = getRtu() != null ? getRtu().getCommunicationSchedulers() : new ArrayList<CommunicationScheduler>();
//            for (CommunicationScheduler cs : communicationSchedulers) {
//                logConfigurationError(cs);
//            }
            throw e;
        }
    }

    private void validateMeterSerialNumber() throws CTRConfigurationException {
        String meterSerial = getRequestFactory().getIdentificationStructure().getMeterSerialNumber();
        String rtuSerial = getMeterSerialNumberFromRtu();
        if ((meterSerial == null) || ("".equals(meterSerial))) {
            severe("Unable to check the serial number of the device! mtuSerial was 'null'");
        } else if ((rtuSerial == null) || ("".equals(rtuSerial))) {
            severe("Unable to check the serial number of the device! Device serialnumer in EiServer was empty. Meter serial was [" + meterSerial + "]");
        } else if (!meterSerial.trim().equalsIgnoreCase(rtuSerial.trim())) {
            String message = "Serialnumber from meter [" + meterSerial + "] does not match the serialnumber in EiServer [" + rtuSerial + "]";
            severe(message);
            throw new CTRConfigurationException(message);
        } else {
            getLogger().finest("Meter serialnumber [" + meterSerial + "] matches rtu serial [" + rtuSerial + "] in EiServer.");
        }
    }

    private void validateConverterSerialNumber() throws CTRConfigurationException {
        String converterSerial = getRequestFactory().getMeterInfo().getConverterSerialNumber();
        String eiserverConverterSerial = getConverterSerialNumberFromRtu();
        if ((converterSerial == null) || ("".equals(converterSerial))) {
            severe("Unable to check the serial number of the converter! converterSerial was 'null'");
        } else if ((eiserverConverterSerial == null) || ("".equals(eiserverConverterSerial))) {
            severe("Unable to check the serial number of the converter! Converter serialnumer in EiServer was empty. Converter serial was [" + converterSerial + "]");
        } else if (!converterSerial.trim().equalsIgnoreCase(eiserverConverterSerial.trim())) {
            String message = "Serialnumber from converter [" + converterSerial + "] does not match the serialnumber in EiServer [" + eiserverConverterSerial + "]";
            severe(message);
            throw new CTRConfigurationException(message);
        } else {
            getLogger().finest("Converter serialnumber [" + converterSerial + "] matches converter serial [" + eiserverConverterSerial + "] in EiServer.");
        }
    }

    private Properties getPropertiesFromProtocolClass() {
        List<CommunicationProtocol> protocols = CommonUtils.mw().getCommunicationProtocolFactory().findAll();
        for (CommunicationProtocol protocol : protocols) {
            if (protocol.getJavaClassName().equalsIgnoreCase(getClass().getName())) {
                getLogger().info("Using properties from protocol only, because RTU is not discovered yet. " + protocol);
                return protocol.getProperties().toStringProperties();
            }
        }
        warning("No protocol properties found for this protocol. Using defaults!");
        return new Properties();
    }

    private void disconnect() {
        if (getProtocolProperties().isSendEndOfSession()) {
            getRequestFactory().sendEndOfSession();
        }
    }

    private void updateRequestFactory() {
        this.requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties(), getTimeZone(), getRequestFactory().getIdentificationStructure());
    }

    /**
     * Check the device's communication schedules, and execute them.
     */
    private void readDevice() {
//        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();
//        boolean connectionOk = true;
//        for (CommunicationScheduler cs : communicationSchedulers) {
//            String csName = cs.displayString();
//            if (!SmsHandler.isSmsProfile(cs)) {  //If schedule contains sms in the name (both for outbound / inbound) the schedule will be skipped.
//                meterAmrLogging = null;
//                if (cs.getNextCommunication() == null) {
//                    log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
//                } else if (cs.getNextCommunication().after(getNow())) {
//                    log("CommunicationScheduler '" + csName + "' nextCommunication not reached yet. Skipping.");
//                } else {
//                    storeStartTime();
//                    log("CommunicationScheduler '" + csName + "' nextCommunication reached. Executing scheduler.");
//                    try {
//                        if (connectionOk) {
//                            cs.startCommunication();
//                            cs.startReadingNow();
//                            executeCommunicationSchedule(cs);
//                            logSuccess(cs);
//                        } else {
//                            throw new CTRConnectionException("CTR connection to device down.");
//                        }
//                    } catch (CTRConnectionException e) {
//                        connectionOk = false;
//                        severe(e.getMessage());
//                        logFailure(cs);
//                    } catch (CTRException e) {
//                        severe(e.getMessage());
//                        logFailure(cs);
//                    } catch (SQLException e) {
//                        severe(e.getMessage());
//                        logFailure(cs);
//                    } catch (IOException e) {
//                        severe(e.getMessage());
//                        logFailure(cs);
//                    } catch (BusinessException e) {
//                        severe(e.getMessage());
//                        logFailure(cs);
//                    }
//                }
//            } else {
//                log("CommunicationScheduler '" + csName + "' is only ment for SMS. Skipping.");
//            }
//        }
    }

    public void storeStartTime() {
        this.startTime = System.currentTimeMillis();
    }
//
//    /**
//     * Executes the communication schedule. Can set time, read time, read event records, read profile data or read register data.
//     *
//     * @param communicationScheduler: the device's communication schedule
//     * @throws IOException
//     */
//    private void executeCommunicationSchedule(CommunicationScheduler communicationScheduler) throws CTRConfigurationException, CTRConnectionException {
//        CommunicationProfile communicationProfile = communicationScheduler.getCommunicationProfile();
//        String csName = communicationScheduler.displayString();
//        if (communicationProfile == null) {
//            throw new CTRConfigurationException("CommunicationScheduler '" + csName + "' has no communication profile.");
//        }
//
//        //Send the meter messages
//        if (communicationProfile.getSendRtuMessage()) {
//            getLogger().log(Level.INFO, "Sending messages to meter with serial number: " + getMeterSerialNumberFromRtu());
//            sendMeterMessages();
//        }
//
//        // Check if the time is greater then allowed, if so then no data can be stored...
//        // Don't do this when a forceClock is scheduled
//        if (!communicationProfile.getForceClock() && !communicationProfile.getAdHoc()) {
//            // TODO: implement method
//        }
//
//        // Read the clock & set if needed
//        if (communicationProfile.getForceClock()) {
//            try {
//                Date meterTime = getRequestFactory().getMeterInfo().getTime();
//                Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
//                setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
//                severe("Forced to set meterClock to systemTime: " + currentTime);
//                getRequestFactory().getMeterInfo().setTime(currentTime);
//            } catch (CTRConnectionException e) {
//                throw e;
//            } catch (CTRException e) {
//                severe(e.getMessage());
//            }
//        } else {
//            try {
//                verifyAndWriteClock(communicationProfile);
//            } catch (CTRConnectionException e) {
//                throw e;
//            } catch (CTRException e) {
//                severe(e.getMessage());
//            }
//        }
//
//        // Read the events
//        if (communicationProfile.getReadMeterEvents()) {
//            try {
//                getLogger().log(Level.INFO, "Getting events for meter with serial number: " + getMeterSerialNumberFromRtu());
//                CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
//                List<MeterEvent> meterEvents = meterEvent.getMeterEvents(getRtu().getLastLogbook());
//                ProfileData profileData = new ProfileData();
//                profileData.setMeterEvents(meterEvents);
//                storeObject.add(getRtu(), profileData);
//                validateAndGetInstallationDate(meterEvents);
//            } catch (CTRConnectionException e) {
//                throw e;
//            } catch (CTRDiscoverException e) {
//                severe(e.getMessage());
//            } catch (CTRException e) {
//                severe("Unable to read events: " + e.getMessage());
//            }
//        }
//
//        // Read the register values
//        if (communicationProfile.getReadMeterReadings()) {
//            getLogger().log(Level.INFO, "Getting registers for meter with serial number: " + getMeterSerialNumberFromRtu());
//            storeObject.addAll(doReadRegisters(communicationProfile));
//        }
//
//        // Read the profiles
//        if (communicationProfile.getReadDemandValues()) {
//            getLogger().log(Level.INFO, "Getting profile data for meter with serial number: " + getMeterSerialNumberFromRtu());
//            readChannelData();
//        }
//
//    }

    private void validateAndGetInstallationDate(List<MeterEvent> meterEvents) throws CTRDiscoverException {
        ObisCode obis = ObisCode.fromString(ObisCodeMapper.OBIS_INSTALL_DATE);
        Register register = getRtu().getRegister(obis);
        if (register == null) {
            throw new CTRDiscoverException("No register configured for the installation date! [" + obis + "]");
        } else if (register.getLastReading() == null) {
            if (getProtocolProperties().getExtractInstallationDate() == 0) {
                // Use current date & time instead of accessing events.
                severe("No installation date yet! Setting current time as installation date.");
                Date installationDate = Calendar.getInstance(getTimeZone()).getTime();
                Quantity installationQuantity = new Quantity(installationDate.getTime(), Unit.get("ms"));
                RegisterValue registerValue = new RegisterValue(obis, installationQuantity, installationDate, new Date(), new Date(), new Date(), 0, installationDate.toString());
                storeObject.add(register, registerValue);
                severe("Preparing RtuDiscoveredEvent for rtu [" + getRtu().getName() + "]");
                discoveredEvent = new RtuDiscoveredEvent(getRtu());
            } else {
                severe("No installation date yet! Starting installation date detection.");
                List<MeterEvent> allEvents = MeterEventUtils.convertRtuEventToMeterEvent(getRtu().getEvents());
                allEvents.addAll(meterEvents);
                Date installationDate = new InstallationDateDiscover(allEvents).getInstallationDateFromEvents();
                if (installationDate != null) {
                    severe("Found installation date! [" + installationDate + "]");
                    Quantity installationQuantity = new Quantity(installationDate.getTime(), Unit.get("ms"));
                    RegisterValue registerValue = new RegisterValue(obis, installationQuantity, installationDate, new Date(), new Date(), new Date(), 0, installationDate.toString());
                    storeObject.add(register, registerValue);
                    severe("Preparing RtuDiscoveredEvent for rtu [" + getRtu().getName() + "]");
                    discoveredEvent = new RtuDiscoveredEvent(getRtu());
                } else {
                    severe("No installation date found yet. Retrying next time.");
                }
            }

        } else {
            log("No need to determine installation date.");
        }
    }

    private void sendMeterMessages() {
        MTU155MessageExecutor messageExecutor = getMessageExecuter();
        Iterator<OldDeviceMessage> it = getRtu().getOldPendingMessages().iterator();
        OldDeviceMessage rm = null;
        while (it.hasNext()) {
            rm = it.next();
            try {
                messageExecutor.doMessage(rm);
                warning("Message [" + rm.displayString() + "] executed successfully.");
            } catch (BusinessException e) {
                severe("Unable to send message [" + rm.displayString() + "]! " + e.getMessage());
            } catch (SQLException e) {
                severe("Unable to send message [" + rm.displayString() + "]! " + e.getMessage());
            }
        }
    }

    public MTU155MessageExecutor getMessageExecuter() {
        return new MTU155MessageExecutor(getLogger(), getRequestFactory(), getRtu(), getStoreObject());
    }

    /**
     * Read channel data from the meter.
     */
    private void readChannelData() throws CTRConnectionException {
        List<Channel> channelList = getRtu().getChannels();
        for (Channel channel : channelList) {
            ProfileChannel profile = new ProfileChannel(getRequestFactory(), channel);
            getLogger().info("Reading profile for channel [" + channel.getName() + "]");
            ProfileData pd;
            try {
                pd = profile.getProfileData();
            } catch (CTRExceptionWithProfileData e) {
                pd = e.getProfileData();
                if (pd != null) {
                    storeObject.add(channel, pd);
                }
                if (e.getException() instanceof CTRConnectionException) {
                    throw (CTRConnectionException) e.getException();
                }
            }
            storeObject.add(channel, pd);
        }
    }
//
//    /**
//     * Read registers from the meter
//     *
//     * @param cp
//     * @return
//     */
//    private Map<Register, RegisterValue> doReadRegisters(CommunicationProfile cp) throws CTRConnectionException {
//        HashMap<Register, RegisterValue> regValueMap = new HashMap<Register, RegisterValue>();
//        Iterator<com.energyict.mdw.amr.Register> rtuRegisterIterator = getRtu().getRegisters().iterator();
//        List groups = cp.getRtuRegisterGroups();
//        while (rtuRegisterIterator.hasNext()) {
//            ObisCode obisCode = null;
//            try {
//                Register rtuRegister = rtuRegisterIterator.next();
//                if (CommonUtils.isInRegisterGroup(groups, rtuRegister)) {
//                    obisCode = rtuRegister.getRegisterSpec().getDeviceObisCode();
//                    ObisCode obisToRead;
//                    if (obisCode == null) {
//                        obisCode = rtuRegister.getRegisterMapping().getObisCode();
//                        obisToRead = ProtocolTools.setObisCodeField(obisCode, 1, (byte) (rtuRegister.getRegisterSpec().getDeviceChannelIndex() & 0x0FF));
//                    } else {
//                        obisToRead = ObisCode.fromByteArray(obisCode.getLN());
//                    }
//
//                    try {
//                        RegisterValue registerValue = getObisCodeMapper().readRegister(obisToRead);
//                        registerValue.setRtuRegisterId(rtuRegister.getId());
//                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
//                            regValueMap.put(rtuRegister, registerValue);
//                        }
//                    } catch (NoSuchRegisterException e) {
//                        log(Level.FINEST, e.getMessage());
//                        getMeterAmrLogging().logRegisterFailure(e, obisCode);
//                        getLogger().log(Level.INFO, "ObisCode " + obisCode + " is not supported by the meter.");
//                    }
//                }
//            } catch (CTRConnectionException e) {
//                throw e;
//            } catch (CTRException e) {
//                // TODO if the connection is out you should not try and read the others as well...
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.INFO, "Reading register with obisCode " + obisCode + " FAILED.");
//            }
//        }
//        return regValueMap;
//    }
//
//    /**
//     * Write the meter clock
//     *
//     * @param communicationProfile
//     * @throws IOException
//     */
//    private void verifyAndWriteClock(CommunicationProfile communicationProfile) throws CTRException {
//        Date meterTime = getRequestFactory().getMeterInfo().getTime();
//        Date now = Calendar.getInstance(getTimeZone()).getTime();
//
//        setTimeDifference(Math.abs(now.getTime() - meterTime.getTime()));
//        long diff = getTimeDifference() / 1000;
//
//        log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
//        if (communicationProfile.getWriteClock()) {
//            if ((diff < communicationProfile.getMaximumClockDifference()) && (diff > communicationProfile.getMinimumClockDifference())) {
//                severe("Metertime will be set to systemtime: " + now);
//                getRequestFactory().getMeterInfo().setTime(now);
//            } else if (diff > communicationProfile.getMaximumClockDifference()) {
//                severe("Metertime will not be set, timeDifference is too large.");
//            }
//        } else {
//            log("WriteClock is disabled, metertime will not be set.");
//        }
//
//    }

    /**
     * Get the serial from the rtu in EiServer. If Device == null, return null as serial number
     *
     * @return
     */
    public String getMeterSerialNumberFromRtu() {
        if ((getRtu() != null) && (getRtu().getSerialNumber() != null)) {
            return getRtu().getSerialNumber().trim();
        } else {
            return null;
        }
    }

    /**
     * Get the serial from the converter in EiServer. If Device == null, return null as serial number
     *
     * @return
     */
    private String getConverterSerialNumberFromRtu() {
        if ((getRtu() != null) && (getRtu().getName() != null)) {
            return getRtu().getName().trim();
        } else {
            return null;
        }
    }

    /**
     * Get the RTU, by PDR
     *
     * @return
     * @throws CTRException
     */
    private Device identifyAndGetRtu() throws CTRException {
        String pdr = readPdr();
        log("MTU155 with pdr='" + pdr + "' connected.");

//        List<Device> rtus = CommonUtils.mw().getDeviceFactory().findByDialHomeId(pdr);
        List<Device> rtus = new ArrayList<>(0); // TODO: warning - API call no longer exists (cause DialHomeId is no longer managed by device)
        switch (rtus.size()) {
            case 0:
                if (getProtocolProperties().isDisableDSTForKnockingDevices()) {
                    getDiscover().disableDSTForKnockingDevice(pdr);
                }
                if (getProtocolProperties().isFastDeployment()) {
                    log("Device not found in EIServer. Starting fast discover.");
                    return getDiscover().doDiscover();
                } else {
                    throw new CTRConfigurationException("No rtu found in EiServer with callhomeId='" + pdr + "' and FastDeployment is disabled.");
                }
            case 1:
                return rtus.get(0);
            default:
                throw new CTRConfigurationException("Found " + rtus.size() + " rtu's in EiServer with callhomeId='" + pdr + "', but only one allowed. Skipping communication until fixed.");
        }

    }

    private MTU155Discover getDiscover() {
        if (mtu155Discover == null) {
            mtu155Discover = new MTU155Discover(this);
        }
        return mtu155Discover;
    }

    /**
     * @return the pdr value as String
     * @throws CTRException
     * @throws IndexOutOfBoundsException
     */
    private String readPdr() throws CTRException {
        IdentificationResponseStructure identStruct = getRequestFactory().getIdentificationStructure();
        CTRAbstractValue<String> pdrObject = identStruct != null ? identStruct.getPdr() : null;
        String pdr = pdrObject != null ? pdrObject.getValue() : null;
        if (pdr == null) {
            throw new CTRException("Unable to detect meter. PDR value was 'null'!");
        }
        return pdr;
    }

    public MTU155Properties getProtocolProperties() {
        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException, BusinessException, SQLException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        int baudRate = 9600;
        int dataBits = SerialCommunicationChannel.DATABITS_8;
        int parity = SerialCommunicationChannel.PARITY_NONE;
        int stopBits = SerialCommunicationChannel.STOPBITS_1;

        Dialer dialer = DebugUtils.getConnectedDirectDialer("COM1", baudRate, dataBits, parity, stopBits);

        MTU155 mtu155 = new MTU155();
//        mtu155.execute(null, dialer, Logger.getLogger(MTU155.class.getName()));

    }

    public Device getRtu() {
        return rtu;
    }

    public void setRtu(Device rtu) {
        this.rtu = rtu;
    }

    public int getNetworkID() {
        try {
            if (rtu != null) {
                // get the network ID from a proper property value
//                return Integer.parseInt(getRtu().getNetworkId());
                return -1;

            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setNetworkID(int ID) throws BusinessException, SQLException {
        DeviceShadow shadow = getRtu().getShadow();
//        shadow.setNetworkId(Integer.toString(ID));
        getRtu().update(shadow);
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

    private Date getNow() {
        return new Date();
    }

//    /**
//     * Log a successful event
//     *
//     * @param commSchedule
//     */
//    public void logSuccess(CommunicationScheduler commSchedule) {
//        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
//        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
//        journal.addAll(getMeterAmrLogging().getJournalEntries());
//        try {
//            commSchedule.journal(journal);
//            commSchedule.logSuccess(new Date());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (BusinessException e) {
//            e.printStackTrace();
//        }
//    }

    private String getConnectTime() {
        String connectTimeStr = "0";
        if (startTime != -1) {
            long connectTime = (System.currentTimeMillis() - startTime) / 1000;
            if (connectTime > 0) {
                connectTimeStr = "" + connectTime;
            }
        }
        startTime = -1;
        return connectTimeStr;
    }

//    /**
//     * Log a failed event
//     *
//     * @param commSchedule
//     */
//    private void logFailure(CommunicationScheduler commSchedule) {
//        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.START, "" + getNow().getTime()));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
//        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_PROTOCOLERROR));
//        try {
//            // If the schedule has an SMS fallback schedule set, we should trigger it - for SMS schedules no fallback is possible
//            if (commSchedule.getFallback() != null && !SmsHandler.isSmsProfile(commSchedule)) {
//                severe("The fallback schedule " + commSchedule.getFallback().getCommunicationProfile().getName() + " will be triggered.");
//                CommunicationSchedulerShadow shadow = commSchedule.getFallback().getShadow();
//                commSchedule.getFallback().update(shadow);
//                shadow.setNextCommunication(new Date());
//            }
//
//            journal.addAll(getMeterAmrLogging().getJournalEntries());
//            commSchedule.journal(journal);
//            commSchedule.logFailure(new Date(), "");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (BusinessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Log a failed event
//     *
//     * @param commSchedule
//     */
//    private void logConfigurationError(CommunicationScheduler commSchedule) {
//        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
//        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_CONFIGURATION));
//        journal.addAll(getMeterAmrLogging().getJournalEntries());
//        try {
//            commSchedule.journal(journal);
//            commSchedule.logFailure(new Date(), "");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (BusinessException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @return
     */
    private ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper(getRequestFactory(), getMeterAmrLogging());
        }
        return obisCodeMapper;
    }

    /**
     * @return
     */
    private MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    /**
     * @return
     */
    public RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            if (isOutboundSmsProfile) {
                requestFactory = new SmsRequestFactory(getLink(), getLogger(), getProtocolProperties(), getTimeZone(), getPhoneNumber(), getNetworkID());
            } else {
                requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties(), getTimeZone());
            }
        }
        return requestFactory;
    }

    public OutboundSmsHandler getOutboundSmsHandler() {
        if (outboundSmsHandler == null) {
            outboundSmsHandler = new OutboundSmsHandler(this);
        }
        return outboundSmsHandler;
    }

    /**
     * @return the meter's {@link TimeZone}
     */
    public TimeZone getTimeZone() {
//        if (getRtu() == null) {
            return TimeZone.getDefault();
//        }
//        return getRtu().getDeviceTimeZone();
    }

    public String getPhoneNumber() {
//        return getRtu().getPhoneNumber();
        return "Get the phoneNumber from a proper property value";
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        categories.add(getConnectivityCategory());
        categories.add(getMaintenanceCategory());
        categories.add(getSealConfigurationCategory());
        categories.add(getConfigurationCategory());
        categories.add(getKeyManagementCategory());
        categories.add(getTariffManagementCategory());
        return categories;
    }

    /**
     * @return the messages for the ConfigurationCategory
     */
    private MessageCategorySpec getConfigurationCategory() {
        MessageCategorySpec catConfiguration = new MessageCategorySpec("[01] Device configuration");
        catConfiguration.addMessageSpec(WritePDRMessage.getMessageSpec(false));
        catConfiguration.addMessageSpec(WriteMeterMasterDataMessage.getMessageSpec(false));
        catConfiguration.addMessageSpec(WriteConverterMasterDataMessage.getMessageSpec(false));
        catConfiguration.addMessageSpec(WriteGasParametersMessage.getMessageSpec(false));
        catConfiguration.addMessageSpec(ChangeDSTMessage.getMessageSpec(false));
        return catConfiguration;
    }

    /**
     * @return the messages for the ConnectivityCategory
     */
    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catConnectivity = new MessageCategorySpec("[02] " + RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        catConnectivity.addMessageSpec(addChangeGPRSSetup(RtuMessageKeyIdConstants.GPRSMODEMSETUP, RtuMessageConstant.GPRS_MODEM_SETUP, false));
        catConnectivity.addMessageSpec(addChangeSMSCSetup(RtuMessageKeyIdConstants.SMS_CHANGE_SMSC, RtuMessageConstant.SMS_CHANGE_SMSC, false));
        catConnectivity.addMessageSpec(addChangeDevicePhoneNumber(RtuMessageKeyIdConstants.CHANGE_DEVICE_PHONE_NUMBER, RtuMessageConstant.CHANGE_DEVICE_PHONE_NUMBER, false));
        return catConnectivity;
    }

    /**
     * @return the messages for the KeyManagementCategory
     */
    private MessageCategorySpec getKeyManagementCategory() {
        MessageCategorySpec catKeyManagement = new MessageCategorySpec("[03] Key management");
        catKeyManagement.addMessageSpec(ChangeTemporaryKeyMessage.getMessageSpec(false));
        catKeyManagement.addMessageSpec(ChangeExecutionKeyMessage.getMessageSpec(false));
        catKeyManagement.addMessageSpec(ActivateTemporaryKeyMessage.getMessageSpec(false));
        return catKeyManagement;
    }

    /**
     * @return the messages for the MaintenanceCategory
     */
    private MessageCategorySpec getSealConfigurationCategory() {
        MessageCategorySpec catMaintenance = new MessageCategorySpec("[04] Seals management");
        catMaintenance.addMessageSpec(ChangeSealStatusMessage.getMessageSpec(false));
        catMaintenance.addMessageSpec(TemporaryBreakSealMessage.getMessageSpec(false));
        return catMaintenance;
    }

    /**
     * @return the messages for the TariffManagementCategory
     */
    private MessageCategorySpec getTariffManagementCategory() {
        MessageCategorySpec catKeyManagement = new MessageCategorySpec("[05] Tariff management");
        catKeyManagement.addMessageSpec(TariffUploadPassiveMessage.getMessageSpec(false));
        catKeyManagement.addMessageSpec(TariffDisablePassiveMessage.getMessageSpec(false));
        return catKeyManagement;
    }

    /**
     * @return the messages for the MaintenanceCategory
     */
    private MessageCategorySpec getMaintenanceCategory() {
        MessageCategorySpec catMaintenance = new MessageCategorySpec("[99] Maintenance");
        catMaintenance.addMessageSpec(ReadPartialProfileDataMessage.getMessageSpec(false));
        catMaintenance.addMessageSpec(ForceSyncClockMessage.getMessageSpec(false));
        catMaintenance.addMessageSpec(WakeUpFrequency.getMessageSpec(false));
        return catMaintenance;
    }

    void warning(String message) {
        getMeterAmrLogging().logInfo(message);
        getLogger().warning(message);
    }

    public void severe(String message) {
        getMeterAmrLogging().logInfo(message);
        getLogger().severe(message);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        if (msgTag.getName().equals(TariffUploadPassiveMessage.MESSAGE_TAG)) {
            StringBuilder builder = new StringBuilder();

            builder.append("<");
            builder.append(msgTag.getName());

            int codeTableId = -1;
            for (Object maObject : msgTag.getAttributes()) {
                MessageAttribute ma = (MessageAttribute) maObject;
                String specName = ma.getSpec().getName();
                if (specName.equals(TariffUploadPassiveMessage.ATTR_cODE_TABLE_ID)) {
                    if (ma.getValue() == null || ma.getValue().length() == 0) {
                        continue;
                    } else {
                        codeTableId = Integer.valueOf(ma.getValue());
                        String base64 = CodeTableBase64Builder.getXmlStringFromCodeTable(codeTableId);
                        builder.append(" ").append(specName);
                        builder.append("=").append('"').append(base64).append('"');
                    }
                } else {
                    if (ma.getValue() == null || ma.getValue().length() == 0) {
                        continue;
                    }
                    builder.append(" ").append(specName);
                    builder.append("=").append('"').append(ma.getValue()).append('"');
                }
            }
            builder.append(">");
            addClosingTag(builder, msgTag.getName());
            return builder.toString();
        } else {
            return super.writeTag(msgTag);
        }

    }

    /**
     * This method is needed to describe the capabilities supported by the protocol,
     * and is used to create a fitting interface in EIServer.
     *
     * @return The {@link com.energyict.protocol.messaging.FirmwareUpdateMessagingConfig} containing all the capabillities of the protocol.
     */
    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        return new FirmwareUpdateMessagingConfig(false, true, true);
    }

    /**
     * Returns the message builder capable of generating and parsing messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing messages.
     */
    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return new FirmwareUpdateMessageBuilder();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
