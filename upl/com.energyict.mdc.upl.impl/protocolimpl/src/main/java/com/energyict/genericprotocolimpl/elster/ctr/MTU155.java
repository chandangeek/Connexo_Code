package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.*;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.elster.ctr.discover.InstallationDateDiscover;
import com.energyict.genericprotocolimpl.elster.ctr.discover.MTU155Discover;
import com.energyict.genericprotocolimpl.elster.ctr.events.CTRMeterEvent;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.profile.ProfileChannel;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.tariff.CodeTableBase64Builder;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.RtuDiscoveredEvent;
import com.energyict.protocolimpl.debug.DebugUtils;
import com.energyict.protocolimpl.messages.*;
import com.energyict.protocolimpl.utils.MeterEventUtils;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 12-may-2011
 * Time: 13:16:45
 */
public class MTU155 extends AbstractGenericProtocol {

    private final StoreObject storeObject = new StoreObject();
    private final MTU155Properties properties = new MTU155Properties();
    private GprsRequestFactory requestFactory;
    private ObisCodeMapper obisCodeMapper;
    private Rtu rtu;
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

        try {
            getProtocolProperties().addProperties(getPropertiesFromProtocolClass());
            log("Incomming TCP connection from: " + getRequestFactory().getIPAddress());
            updateRequestFactory();

            logMeterInfo();

            this.rtu = identifyAndGetRtu();
            log("Rtu with name '" + getRtu().getName() + "' connected successfully.");
            getProtocolProperties().addProperties(rtu.getProtocol().getProperties());
            getProtocolProperties().addProperties(rtu.getProperties());
            updateRequestFactory();
            checkSerialNumbers();
            readDevice();
        } catch (CTRException e) {
            severe(e.getMessage());
        } finally {
            try {
                disconnect();
            } catch (Exception e) {
                severe("Error closing connection: " + e.getMessage());
            }
        }

        try {
            getStoreObject().doExecute();
            if (discoveredEvent != null) {
                log("Sending new RtuDiscoveredEvent for rtu [" + getRtu() + "]");
                MeteringWarehouse.getCurrent().signalEvent(discoveredEvent);
            }
        } catch (BusinessException e) {
            e.printStackTrace();
            severe(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            severe(e.getMessage());
        }

    }

    private void logMeterInfo() {
        IdentificationResponseStructure structure = getRequestFactory().getIdentificationStructure();
        CTRAbstractValue<String> pdrObject = structure != null ? structure.getPdr() : null;
        String pdr = pdrObject != null ? pdrObject.getValue() : null;

        MeterInfo meterInfo = getRequestFactory().getMeterInfo();

        log("MTU155 with pdr='" + pdr + "'");
        log("Serial number of the MTU155='" + meterInfo.getMTUSerialNumber() + "'");
        log("Serial number of the converter='"+ meterInfo.getConverterSerialNumber() +"'");
        log("Serial number of the gas meter='" + structure.getMeterSerialNumber() +"'");
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
            List<CommunicationScheduler> communicationSchedulers = getRtu() != null ? getRtu().getCommunicationSchedulers() : new ArrayList<CommunicationScheduler>();
            for (CommunicationScheduler cs : communicationSchedulers) {
                logConfigurationError(cs);
            }
            throw e;
        }
    }

    private void validateMeterSerialNumber() throws CTRConfigurationException {
        String meterSerial = getRequestFactory().getIdentificationStructure().getMeterSerialNumber();
        String rtuSerial = getMeterSerialNumberFromRtu();
        if ((meterSerial == null) || ("".equals(meterSerial))) {
            severe("Unable to check the serial number of the device! mtuSerial was 'null'");
        } else if ((rtuSerial == null) || ("".equals(rtuSerial))) {
            severe("Unable to check the serial number of the device! Rtu serialnumer in EiServer was empty. Meter serial was [" + meterSerial + "]");
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
                return protocol.getProperties();
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
        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();
        boolean connectionOk = true;
        for (CommunicationScheduler cs : communicationSchedulers) {
            String csName = cs.displayString();
            if (!SmsHandler.isSmsProfile(cs)) {
                meterAmrLogging = null;
                if (cs.getNextCommunication() == null) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
                } else if (cs.getNextCommunication().after(getNow())) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication not reached yet. Skipping.");
                } else {
                    storeStartTime();
                    log("CommunicationScheduler '" + csName + "' nextCommunication reached. Executing scheduler.");
                    try {
                        if (connectionOk) {
                            cs.startCommunication();
                            cs.startReadingNow();
                            executeCommunicationSchedule(cs);
                            logSuccess(cs);
                        } else {
                            throw new CTRConnectionException("CTR connection to device down.");
                        }
                    } catch (CTRConnectionException e) {
                        connectionOk = false;
                        severe(e.getMessage());
                        logFailure(cs);
                    } catch (CTRException e) {
                        severe(e.getMessage());
                        logFailure(cs);
                    } catch (SQLException e) {
                        severe(e.getMessage());
                        logFailure(cs);
                    } catch (IOException e) {
                        severe(e.getMessage());
                        logFailure(cs);
                    } catch (BusinessException e) {
                        severe(e.getMessage());
                        logFailure(cs);
                    }
                }
            } else {
                log("CommunicationScheduler '" + csName + "' is only ment for SMS. Skipping.");
            }
        }
    }

    private void storeStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Executes the communication schedule. Can set time, read time, read event records, read profile data or read register data.
     *
     * @param communicationScheduler: the device's communication schedule
     * @throws IOException
     */
    private void executeCommunicationSchedule(CommunicationScheduler communicationScheduler) throws CTRConfigurationException, CTRConnectionException {
        CommunicationProfile communicationProfile = communicationScheduler.getCommunicationProfile();
        String csName = communicationScheduler.displayString();
        if (communicationProfile == null) {
            throw new CTRConfigurationException("CommunicationScheduler '" + csName + "' has no communication profile.");
        }

        //Send the meter messages
        if (communicationProfile.getSendRtuMessage()) {
            getLogger().log(Level.INFO, "Sending messages to meter with serial number: " + getMeterSerialNumberFromRtu());
            sendMeterMessages();
        }

        // Check if the time is greater then allowed, if so then no data can be stored...
        // Don't do this when a forceClock is scheduled
        if (!communicationProfile.getForceClock() && !communicationProfile.getAdHoc()) {
            // TODO: implement method
        }

        // Read the clock & set if needed
        if (communicationProfile.getForceClock()) {
            try {
                Date meterTime = getRequestFactory().getMeterInfo().getTime();
                Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
                setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
                severe("Forced to set meterClock to systemTime: " + currentTime);
                getRequestFactory().getMeterInfo().setTime(currentTime);
            } catch (CTRConnectionException e) {
                throw e;
            } catch (CTRException e) {
                severe(e.getMessage());
            }
        } else {
            try {
                verifyAndWriteClock(communicationProfile);
            } catch (CTRConnectionException e) {
                throw e;
            } catch (CTRException e) {
                severe(e.getMessage());
            }
        }

        // Read the events
        if (communicationProfile.getReadMeterEvents()) {
            try {
                getLogger().log(Level.INFO, "Getting events for meter with serial number: " + getMeterSerialNumberFromRtu());
                CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
                List<MeterEvent> meterEvents = meterEvent.getMeterEvents(getRtu().getLastLogbook());
                ProfileData profileData = new ProfileData();
                profileData.setMeterEvents(meterEvents);
                storeObject.add(getRtu(), profileData);
                validateAndGetInstallationDate(meterEvents);
            } catch (CTRConnectionException e) {
                throw e;
            } catch (CTRDiscoverException e) {
                severe(e.getMessage());
            } catch (CTRException e) {
                severe("Unable to read events: " + e.getMessage());
            }
        }

        // Read the register values
        if (communicationProfile.getReadMeterReadings()) {
            getLogger().log(Level.INFO, "Getting registers for meter with serial number: " + getMeterSerialNumberFromRtu());
            storeObject.addAll(doReadRegisters(communicationProfile));
        }

        // Read the profiles
        if (communicationProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting profile data for meter with serial number: " + getMeterSerialNumberFromRtu());
            readChannelData();
        }

    }

    private void validateAndGetInstallationDate(List<MeterEvent> meterEvents) throws CTRDiscoverException {
        ObisCode obis = ObisCode.fromString(ObisCodeMapper.OBIS_INSTALL_DATE);
        RtuRegister register = getRtu().getRegister(obis);
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
        Iterator<RtuMessage> it = getRtu().getPendingMessages().iterator();
        RtuMessage rm = null;
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

    /**
     * Read registers from the meter
     *
     * @param cp
     * @return
     */
    private Map<RtuRegister, RegisterValue> doReadRegisters(CommunicationProfile cp) throws CTRConnectionException {
        HashMap<RtuRegister, RegisterValue> regValueMap = new HashMap<RtuRegister, RegisterValue>();
        Iterator<RtuRegister> rtuRegisterIterator = getRtu().getRegisters().iterator();
        List groups = cp.getRtuRegisterGroups();
        while (rtuRegisterIterator.hasNext()) {
            ObisCode obisCode = null;
            try {
                RtuRegister rtuRegister = rtuRegisterIterator.next();
                if (CommonUtils.isInRegisterGroup(groups, rtuRegister)) {
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();
                    try {
                        RegisterValue registerValue = getObisCodeMapper().readRegister(obisCode);
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                            regValueMap.put(rtuRegister, registerValue);
                        }
                    } catch (NoSuchRegisterException e) {
                        log(Level.FINEST, e.getMessage());
                        getMeterAmrLogging().logRegisterFailure(e, obisCode);
                        getLogger().log(Level.INFO, "ObisCode " + obisCode + " is not supported by the meter.");
                    }
                }
            } catch (CTRConnectionException e) {
                throw e;
            } catch (CTRException e) {
                // TODO if the connection is out you should not try and read the others as well...
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Reading register with obisCode " + obisCode + " FAILED.");
            }
        }
        return regValueMap;
    }

    /**
     * Write the meter clock
     *
     * @param communicationProfile
     * @throws IOException
     */
    private void verifyAndWriteClock(CommunicationProfile communicationProfile) throws CTRException {
        Date meterTime = getRequestFactory().getMeterInfo().getTime();
        Date now = Calendar.getInstance(getTimeZone()).getTime();

        setTimeDifference(Math.abs(now.getTime() - meterTime.getTime()));
        long diff = getTimeDifference() / 1000;

        log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
        if (communicationProfile.getWriteClock()) {
            if ((diff < communicationProfile.getMaximumClockDifference()) && (diff > communicationProfile.getMinimumClockDifference())) {
                severe("Metertime will be set to systemtime: " + now);
                getRequestFactory().getMeterInfo().setTime(now);
            } else if (diff > communicationProfile.getMaximumClockDifference()) {
                severe("Metertime will not be set, timeDifference is too large.");
            }
        } else {
            log("WriteClock is disabled, metertime will not be set.");
        }

    }

    /**
     * Get the serial from the rtu in EiServer. If Rtu == null, return null as serial number
     *
     * @return
     */
    private String getMeterSerialNumberFromRtu() {
        if ((getRtu() != null) && (getRtu().getSerialNumber() != null)) {
            return getRtu().getSerialNumber().trim();
        } else {
            return null;
        }
    }

    /**
     * Get the serial from the converter in EiServer. If Rtu == null, return null as serial number
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
    private Rtu identifyAndGetRtu() throws CTRException {
        String pdr = readPdr();
        log("MTU155 with pdr='" + pdr + "' connected.");

        List<Rtu> rtus = CommonUtils.mw().getRtuFactory().findByDialHomeId(pdr);
        switch (rtus.size()) {
            case 0:
                if (getProtocolProperties().isDisableDSTForKnockingDevices()) {
                    getDiscover().disableDSTForKnockingDevice(pdr);
                }
                if (getProtocolProperties().isFastDeployment()) {
                    log("Rtu not found in EIServer. Starting fast discover.");
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
        mtu155.execute(null, dialer, Logger.getLogger(MTU155.class.getName()));

    }

    private Rtu getRtu() {
        return rtu;
    }

    private StoreObject getStoreObject() {
        return storeObject;
    }

    private Date getNow() {
        return new Date();
    }

    /**
     * Log a successful event
     *
     * @param commSchedule
     */
    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.journal(journal);
            commSchedule.logSuccess(new Date());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

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

    /**
     * Log a failed event
     *
     * @param commSchedule
     */
    private void logFailure(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.START, "" + getNow().getTime()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_PROTOCOLERROR));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.journal(journal);
            commSchedule.logFailure(new Date(), "");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log a failed event
     *
     * @param commSchedule
     */
    private void logConfigurationError(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getConnectTime()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_CONFIGURATION_ERROR));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.journal(journal);
            commSchedule.logFailure(new Date(), "");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

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
    public GprsRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties(), getTimeZone());
        }
        return requestFactory;
    }

    /**
     * @return the meter's {@link TimeZone}
     */
    private TimeZone getTimeZone() {
        if (getRtu() == null) {
            return TimeZone.getDefault();
        }
        return getRtu().getDeviceTimeZone();
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
}
