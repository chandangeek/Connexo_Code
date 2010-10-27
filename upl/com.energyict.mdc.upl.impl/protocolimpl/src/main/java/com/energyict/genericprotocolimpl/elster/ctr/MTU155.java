package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.elster.ctr.events.CTRMeterEvent;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConfigurationException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.profile.*;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.debug.DebugUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 24-sep-2010
 * Time: 11:43:45
 */
public class MTU155 extends AbstractGenericProtocol {

    private final Date now = new Date();
    private final StoreObject storeObject = new StoreObject();
    private final MTU155Properties properties = new MTU155Properties();
    private GprsRequestFactory requestFactory;
    private ObisCodeMapper obisCodeMapper;
    private Rtu rtu;
    private MeterAmrLogging meterAmrLogging;
    private MeterInfo meterInfo;

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }

    @Override
    public void initProperties() {
        properties.addProperties(getProperties());
    }

    @Override
    protected void doExecute() throws IOException, BusinessException, SQLException {

/*
        testMethod();
*/
        try {
            this.rtu = identifyAndGetRtu();
            log("Rtu with name '" + getRtu().getName() + "' connected successfully.");
            getProtocolProperties().addProperties(rtu.getProtocol().getProperties());
            getProtocolProperties().addProperties(rtu.getProperties());
            updateRequestFactory();
            readDevice();
            getStoreObject().doExecute();

        } catch (CTRException e) {
            e.printStackTrace();
            getLogger().severe(e.getMessage());
        } /*catch (BusinessException e) {
            e.printStackTrace();
            getLogger().severe(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe(e.getMessage());
        }*/
    }

    private void updateRequestFactory() {
        this.requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties());
    }

    private void testMethod() throws CTRException {

        getProtocolProperties().addProperty(MTU155Properties.KEYC, "32323232323232323232323232323232");
        getProtocolProperties().addProperty(MTU155Properties.TIMEOUT, "1000");
        getProtocolProperties().addProperty(MTU155Properties.RETRIES, "10");
        getProtocolProperties().addProperty(MTU155Properties.DEBUG, "0");
        getProtocolProperties().addProperty(MTU155Properties.ADDRESS, "0");
        getProtocolProperties().addProperty(MTU155Properties.SECURITY_LEVEL, "1");
        
        this.rtu = new DummyRtu(TimeZone.getTimeZone("GMT"));
        Calendar lastReading = ProtocolTools.createCalendar(2010, 10, 20, 0, 0, 0, 0, getTimeZone());
        for (int i = 1; i <= 4; i++) {
            ProfileData pd = new ProfileChannel(getRequestFactory(), new DummyChannel(i, 3600, lastReading, getRtu())).getProfileData();
            for (Object intervalData : pd.getIntervalDatas()) {
                if (intervalData instanceof IntervalData) {
                    System.out.println(intervalData);
                }
            }
            System.out.println();
        }
        for (int i = 5; i <= 12; i++) {
            ProfileData pd = new ProfileChannel(getRequestFactory(), new DummyChannel(i, 3600 * 24, lastReading, getRtu())).getProfileData();
            for (Object intervalData : pd.getIntervalDatas()) {
                if (intervalData instanceof IntervalData) {
                    System.out.println(intervalData);
                }
            }
            System.out.println();
        }
    }

    private void readDevice() {
        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();
        if (communicationSchedulers.size() == 0) {
            log("Rtu '" + getRtu().getName() + "' has no CommunicationSchedulers. Skipping.");
        } else {
            for (CommunicationScheduler cs : communicationSchedulers) {
                String csName = cs.displayString();
                meterAmrLogging = null;
                if (cs.getNextCommunication() == null) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
                } else if (cs.getNextCommunication().after(getNow())) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication not reached yet. Skipping.");
                } else {
                    log("CommunicationScheduler '" + csName + "' nextCommunication reached. Executing scheduler.");
                    try {
                        cs.startCommunication();
                        cs.startReadingNow();
                        executeCommunicationSchedule(cs);
                        logSuccess(cs);
                    } catch (CTRException e) {
                        getMeterAmrLogging().logInfo(e);
                        logFailure(cs);
                    } catch (SQLException e) {
                        getMeterAmrLogging().logInfo(e);
                        logFailure(cs);
                    } catch (IOException e) {
                        getMeterAmrLogging().logInfo(e);
                        logFailure(cs);
                    } catch (BusinessException e) {
                        getMeterAmrLogging().logInfo(e);
                        logFailure(cs);
                    }
                }
            }
        }
    }

    /**
     * @param communicationScheduler
     * @throws IOException
     */
    private void executeCommunicationSchedule(CommunicationScheduler communicationScheduler) throws IOException {
        CommunicationProfile communicationProfile = communicationScheduler.getCommunicationProfile();
        String csName = communicationScheduler.displayString();
        if (communicationProfile == null) {
            throw new CTRConfigurationException("CommunicationScheduler '" + csName + "' has no communication profile.");
        }

        // Check if the time is greater then allowed, if so then no data can be stored...
        // Don't do this when a forceClock is scheduled
        if (!communicationProfile.getForceClock() && !communicationProfile.getAdHoc()) {
            // TODO: implement method
        }

        // Read the clock & set if needed
        if (communicationProfile.getForceClock()) {
            Date meterTime = getMeterInfo().getTime();
            Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
            setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
            getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
            getMeterInfo().setTime(currentTime);
        } else {
            verifyAndWriteClock(communicationProfile);
        }

        // Read the events
        if (communicationProfile.getReadMeterEvents()) {
            getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getRtuSerialNumber());
            CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
            List<MeterEvent> meterEvents = meterEvent.getMeterEvents(getEventsFromDate());
            ProfileData profileData = new ProfileData();
            profileData.setMeterEvents(meterEvents);
            storeObject.add(getRtu(), profileData);
        }

        // Read the register values
        if (communicationProfile.getReadMeterReadings()) {
            getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + getRtuSerialNumber());
            storeObject.addAll(doReadRegisters(communicationProfile));
        }

        // Read the profiles
        if (communicationProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting profile data for meter with serialnumber: " + getRtuSerialNumber());
            readChannelData();
        }

        //Send the meter messages
        if (communicationProfile.getSendRtuMessage()) {
            getLogger().log(Level.INFO, "Sending messages to meter with serialnumber: " + getRtuSerialNumber());
            // TODO: implement method
        }

    }

    private Date getEventsFromDate() {
        return getRtu().getLastLogbook() == null ? ParseUtils.getClearLastMonthDate(getRtu().getDeviceTimeZone()) : getRtu().getLastLogbook();
    }

    private void readChannelData() {
        List<Channel> channelList = getRtu().getChannels();
        for (Channel channel : channelList) {
            try {
                ProfileChannel profile = new ProfileChannel(getRequestFactory(), channel, getMeterInfo().getTime());
                getLogger().info("Reading profile for channel [" + channel.getName() + "]");
                ProfileData pd = profile.getProfileData();
                storeObject.add(channel, pd);
            } catch (CTRException e) {
                getLogger().warning("Unable to read channelValues for channel [" + channel.getName() + "]" + e.getMessage());
            }
        }
    }

    /**
     * @param cp
     * @return
     */
    private Map<RtuRegister, RegisterValue> doReadRegisters(CommunicationProfile cp) {
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
                        RegisterValue registerValue = readRegister(obisCode);
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                            regValueMap.put(rtuRegister, registerValue);
                        }
                    } catch (NoSuchRegisterException e) {
                        log(Level.FINEST, e.getMessage());
                        getLogger().log(Level.INFO, "ObisCode " + obisCode + " is not supported by the meter.");
                    }
                }
            } catch (IOException e) {
                // TODO if the connection is out you should not try and read the others as well...
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Reading register with obisCode " + obisCode + " FAILED.");
                e.printStackTrace();
            }
        }
        return regValueMap;
    }

    /**
     * @param obisCode
     * @return
     * @throws NoSuchRegisterException
     * @throws CTRException
     */
    private RegisterValue readRegister(ObisCode obisCode) throws NoSuchRegisterException, CTRException {
        return getObisCodeMapper().readRegister(obisCode, null);
    }

    /**
     * @param communicationProfile
     * @throws IOException
     */
    protected void verifyAndWriteClock(CommunicationProfile communicationProfile) throws IOException {
        try {
            Date meterTime = getMeterInfo().getTime();
            Date now = Calendar.getInstance(getTimeZone()).getTime();

            setTimeDifference(Math.abs(now.getTime() - meterTime.getTime()));
            long diff = getTimeDifference() / 1000;

            log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "s.");
            if (communicationProfile.getWriteClock()) {
                if ((diff < communicationProfile.getMaximumClockDifference()) && (diff > communicationProfile.getMinimumClockDifference())) {
                    log(Level.INFO, "Metertime will be set to systemtime: " + now);
                    getMeterInfo().setTime(now);
                } else if (diff > communicationProfile.getMaximumClockDifference()) {
                    log(Level.INFO, "Metertime will not be set, timeDifference is to large.");
                }
            } else {
                log(Level.INFO, "WriteClock is disabled, metertime will not be set.");
            }

        } catch (IOException e) {
            log(Level.FINEST, e.getMessage());
            throw new IOException("Could not get or write the time." + e);
        }

    }

    private MeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new MeterInfo(getRequestFactory(), getLogger(), getTimeZone());
        }
        return meterInfo;
    }

    private String getRtuSerialNumber() {
        return getRtu().getSerialNumber();
    }

    private Rtu identifyAndGetRtu() throws CTRException {
        String pdr = readPdr();
        log("MTU155 with pdr='" + pdr + "' connected.");

        List<Rtu> rtus = CommonUtils.mw().getRtuFactory().findByDialHomeId(pdr);
        switch (rtus.size()) {
            case 0:
                throw new CTRConfigurationException("No rtu found in EiServer with callhomeId='" + pdr + "'");
            case 1:
                return rtus.get(0);
            default:
                throw new CTRConfigurationException("Found " + rtus.size() + " rtu's in EiServer with callhomeId='" + pdr + "', but only one allowed. Skipping communication until fixed.");
        }

    }

    /**
     * @return the pdr value as String
     * @throws CTRException
     */
    private String readPdr() throws CTRException {
        log("Requesting IDENTIFICATION structure from device");
        String pdr = getRequestFactory().readIdentificationStructure().getPdr().getValue();
        if (pdr == null) {
            throw new CTRException("Unable to detect meter. PDR value was 'null'!");
        }
        return pdr;
    }

    private MTU155Properties getProtocolProperties() {
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

    public Rtu getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

    public Date getNow() {
        return now;
    }

    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
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

    private void logFailure(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
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
     * @return
     */
    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper(getRequestFactory());
        }
        return obisCodeMapper;
    }

    /**
     * @return
     */
    public MeterAmrLogging getMeterAmrLogging() {
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
            requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties());
        }
        return requestFactory;
    }

    /**
     * @return the meter's {@link TimeZone}
     */
    public TimeZone getTimeZone() {
        if (getRtu() == null) {
            TimeZone tz = TimeZone.getDefault();
            getLogger().warning("Rtu not available! Using the default timeZone [" + tz.getID() + "] until rtu is available.");
            return tz;
        }
        return getRtu().getDeviceTimeZone();
    }

}
