package com.energyict.genericprotocolimpl.ace4000;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cpo.*;
import com.energyict.genericprotocolimpl.ace4000.objects.ObjectFactory;
import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import javax.naming.ConfigurationException;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author gna & khe
 */
public class ACE4000 extends AbstractGenericProtocol {

    private static final String START = "<MPush>";
    private static final int MAX_TIMEOUT = 40000;
    private InputStream inputStream;
    private OutputStream outputStream = null;
    private Device masterMeter;
    private HashMap<String, Device> mBusMeters = new HashMap<String, Device>();
    private ACE4000Properties properties = new ACE4000Properties();
    private ObjectFactory objectFactory;

    private String pushedSerialNumber;
    private String masterSerialNumber = "";
    private StringBuilder errorString = null;
    private ACE4000Messages ace4000Messages;

    private int oneTimer = 0;
    private long connectTime = 0;
    private List schedulers = new ArrayList();
    private boolean success = false;
    private int retry = -1;

    public ACE4000() {
    }

    public ACE4000Messages getACE4000Messages() {
        if (ace4000Messages == null) {
            ace4000Messages = new ACE4000Messages(this);
        }
        return ace4000Messages;
    }

    public List getMessageCategories() {
        return getACE4000Messages().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getACE4000Messages().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getACE4000Messages().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getACE4000Messages().writeValue(value);
    }

    /**
     * Reads pushed XML messages and parses them.
     * Extra requests are sent if more data is needed according to the communication schedule.
     * Takes the timeout and the number of allowed retries into account.
     */
    public void doExecute() throws BusinessException, SQLException, IOException {

        try {
            masterMeter = null;
//            if (super.getCommunicationScheduler() == null) {    // we got a message from the COMMSERVER UDP Listener
//
//                log("** A new UDP session is started **");
//                setConnectTime(System.currentTimeMillis());
//
//                this.inputStream = getLink().getInputStream();
//                this.outputStream = getLink().getOutputStream();
//                setObjectFactory(new ObjectFactory(this));
//
//                // keep reading until you get no data for [timeout] period
//                long interMessageTimeout = System.currentTimeMillis() + getTimeOut();
//
//                while (true) {    // this loop controls the responses that we get from our own requests
//                    while (true) {    // this loop controls the UDP packets pushed from the meter
//                        int kar;
//                        StringBuilder msg = new StringBuilder();
//                        if (inputStream.available() > 0) {
//                            while (inputStream.available() > 0) {
//                                kar = inputStream.read();
//                                msg.append((char) kar);
//                            }
//                            interMessageTimeout = System.currentTimeMillis() + getTimeOut();
//                        } else {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                                throw new InterruptedException(e + "(Interrupted while waiting for next message.)");
//                            }
//                        }
//                        String xml = msg.toString();
//                        if (!"".equals(xml)) {
//                            String[] messages = xml.split(START);              //Split concatenated messages
//                            for (String message : messages) {
//                                if (!"".equals(message)) {
//                                    getObjectFactory().parseXML(START + message);
//                                }
//                            }
//                        }
//                        if (System.currentTimeMillis() - interMessageTimeout > 0) {
//                            break; // we can leave the loop cause we did not receive a message within the defined timeout interval
//                        }
//                    }    // end of UDP listen loop - check if we want to do something
//
//                    interMessageTimeout = doRequestsOnce(interMessageTimeout);    //Do extra requests if the schedule needs extra data, keep the session alive
//
//                    if (System.currentTimeMillis() - interMessageTimeout > 0) {
//                        oneTimer = 0;       //retry
//                        interMessageTimeout = doRequestsOnce(interMessageTimeout);
//                        if (System.currentTimeMillis() - interMessageTimeout > 0) {
//                            break; // we can leave the loop cause we did not receive a message within the defined interval
//                        }
//                    }
//                }
//                storeData();
//                success = true;
//            }

        } catch (Exception e) {
            getErrorString().append(e.toString());
            success = false;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());

        } finally {
            try {
                if (success) {
                    addSuccessLogging();
                } else {
                    log("An error occurred and was logged in the AMR journal.");
                    log(getErrorString().toString());
                    addFailureLogging(getErrorString());
                }
            } finally {
                log("** Closing the UDP session **");
                // Close the connection after an SQL exception, connection will startup again if requested
                Environment.getDefault().closeConnection();
            }
        }
    }

    public void doExecuteSms(String msg) throws BusinessException, SQLException {
        try {
            masterMeter = null;
            log("** A new SMS message was received **");
            setObjectFactory(new ObjectFactory(this));
            getObjectFactory().setRequestsAllowed(false);
            getObjectFactory().parseXML(msg);
            storeData();
            success = true;

        } catch (Exception e) {
            if (e instanceof SQLException) {
                Environment.getDefault().closeConnection();
            }
            getErrorString().append(e.toString());
            success = false;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());

        } finally {
            if (success) {
                addSuccessLogging();
            } else {
                log("An error occurred while parsing an SMS and was logged in the AMR journal.");
                log(getErrorString().toString());
                addFailureLogging(getErrorString());
            }
        }
    }

    private int getTimeOut() {
        int timeout = getProtocolProperties().getTimeout();
        return timeout > MAX_TIMEOUT ? MAX_TIMEOUT : timeout;
    }

    /**
     * Checks the communication schedule. Does extra requests if relevant data was not auto pushed yet.
     *
     * @param interSessionTimeOut update the time out moment if new requests are sent
     * @return the incremented time out moment, if new requests were sent
     * @throws IOException            communication error
     * @throws ConfigurationException meter serial number is not found (or not unique) in EiServer database
     * @throws com.energyict.cbo.BusinessException
     *                                when a database transaction fails
     * @throws java.sql.SQLException  when data storage fails
     */
    private long doRequestsOnce(long interSessionTimeOut) throws IOException, ConfigurationException, BusinessException, SQLException {
        return interSessionTimeOut;
//        if (oneTimer == 0 && (schedulers.size() != 0)) {
//            boolean newRequest = false;
//            getObjectFactory().resetTrackingIDs();
//            for (CommunicationScheduler scheduler : schedulers) {
//
//                Date fromDate = scheduler.getRtu().getLastReading();
//                List<OldDeviceMessage> messages = scheduler.getRtu().getOldPendingMessages();
//                if (getACE4000Messages().shouldRetry(messages) && scheduler.getCommunicationProfile().getSendRtuMessage()) {
//                    if (retry < getProtocolProperties().getRetries()) {
//                        for (OldDeviceMessage message : messages) {
//                            getACE4000Messages().doMessage(message);
//                            newRequest = getACE4000Messages().isNewRequest();
//                        }
//                    } else {
//                        getACE4000Messages().logTimeoutMessages(messages, retry);
//                    }
//                }
//                if (scheduler.getCommunicationProfile().getReadDemandValues()) {
//                    if (getObjectFactory().shouldRetryLoadProfile()) {
//                        if (retry < getProtocolProperties().getRetries()) {
//                            if (fromDate == null) {
//                                getObjectFactory().sendLoadProfileRequest();
//                            } else {
//                                getObjectFactory().sendLoadProfileRequest(fromDate);
//                            }
//                            newRequest = true;
//                        } else {
//                            log(Level.SEVERE, "Sent request to read load profile " + (retry + 1) + " times, meter didn't reply");
//                        }
//                    }
//                }
//                if (scheduler.getCommunicationProfile().getReadMeterEvents()) {
//                    if (getObjectFactory().shouldRetryEvents()) {
//                        if (retry < getProtocolProperties().getRetries()) {
//                            getObjectFactory().sendEventRequest();
//                            newRequest = true;
//                        } else {
//                            log(Level.SEVERE, "Sent request to read events " + (retry + 1) + " times, meter didn't reply");
//                        }
//                    }
//                }
//                if (scheduler.getCommunicationProfile().getReadMeterReadings()) {
//                    if (retry < getProtocolProperties().getRetries()) {
//                        if (!getObjectFactory().requestAllMasterRegisters(fromDate, getMasterMeter().getRegisters())) {
//                            newRequest = true;
//                        }
//                    } else {
//                        log(Level.SEVERE, "Sent request to read registers " + (retry + 1) + " times, meter didn't reply");
//                    }
//                    if (!getMBusMetersMap().isEmpty()) {
//                        if (retry < getProtocolProperties().getRetries()) {
//                            if (!getObjectFactory().requestAllSlaveRegisters(fromDate, getMBusMetersMap())) {
//                                newRequest = true;
//                            }
//                        } else {
//                            log(Level.SEVERE, "Sent request to read registers " + (retry + 1) + " times, meter didn't reply");
//                        }
//                    }
//                }
//
//                if (scheduler.getCommunicationProfile().getForceClock() || scheduler.getCommunicationProfile().getWriteClock()) {
//                    if (!getObjectFactory().isClockWasSet()) {
//                        if (retry < getProtocolProperties().getRetries()) {
//                            getObjectFactory().sendForceTime();
//                            newRequest = true;
//                        } else {
//                            log(Level.SEVERE, "Sent request to read events " + (retry + 1) + " times, meter didn't reply");
//                        }
//                    }
//                }

//			getObjectFactory().setAutoPushConfig(1, 655, 665, false, 10);
//			getObjectFactory().sendBDConfig(1, 1, 15);
//			getObjectFactory().sendFullMeterConfigRequest();
//			getObjectFactory().sendTimeConfig(4800, 120, 3);
//            }
//            oneTimer++;
//            if (newRequest) {
//                retry++;
//            }
//            return newRequest ? System.currentTimeMillis() + getTimeOut() : interSessionTimeOut;
//        }
//        return interSessionTimeOut;
    }

    private void storeData() throws SQLException, BusinessException, ConfigurationException {
//        boolean storedEvents = false;
//        boolean storedRegisters = false;
//        boolean storedProfileDate = false;
//
//        for (CommunicationScheduler scheduler : schedulers) {
//            if (scheduler.getCommunicationProfile().getSendRtuMessage()) {
//                setMessageResults(true);
//            }
//
//            if (!storedProfileDate && (scheduler.getCommunicationProfile().getReadDemandValues() || getACE4000Messages().isProfileDataRequested())) {
//                ProfileData profileData = getObjectFactory().getLoadProfile().getProfileData();
//                int size = profileData.getIntervalDatas().size();
//                if (size > 0) {
//                    log("Storing profile data, received " + size + " intervals");
//                    profileData.sort();
//                    if (getACE4000Messages().isProfileDataRequested()) {
//                        ProfileData result = new ProfileData();
//                        result.setChannelInfos(profileData.getChannelInfos());
//                        for (Object intervalObject : profileData.getIntervalDatas()) {
//                            IntervalData intervalData = (IntervalData) intervalObject;
//                            Date endTime = intervalData.getEndTime();
//                            Date toDate = getObjectFactory().getLoadProfile().getToDate();
//                            Date fromDate = getObjectFactory().getLoadProfile().getFrom();
//                            if ((endTime.before(toDate) && endTime.after(fromDate)) || endTime.equals(toDate) || endTime.equals(fromDate)) {
//                                result.addInterval(intervalData);
//                            }
//                        }
//                        profileData = result;
//                    }
//                    getMasterMeter().store(profileData);
//                }
//                storedProfileDate = true;
//            }
//
//            if (!storedEvents && (scheduler.getCommunicationProfile().getReadMeterEvents() || getACE4000Messages().isEventsRequested())) {
//                storeEvents();
//                storedEvents = true;
//            }
//
//            if (!storedRegisters && (scheduler.getCommunicationProfile().getReadMeterReadings())) {
//                MeterReadingData allMasterRegisters = getObjectFactory().getAllMasterRegisters();
//                storeMasterRegisters(allMasterRegisters);
//                for (String serialNumber : mBusMeters.keySet()) {
//                    MeterReadingData slaveRegisters = getObjectFactory().getAllMBusRegisters(serialNumber);
//                    storeSlaveRegisters(slaveRegisters, mBusMeters.get(serialNumber));
//                }
//                storedRegisters = true;
//            }
//        }
//
//        if ((getObjectFactory().getLoadProfile().getProfileData().getIntervalDatas().size() > 0) && !storedProfileDate) {
//            log("Received profile data, but none are stored because of the communication profile settings");
//        }
//        if ((getObjectFactory().getMeterEvents().size() > 0) && (!storedEvents)) {
//            log("Received " + getObjectFactory().getMeterEvents().size() + " event(s), but none are stored because of the communication profile settings");
//        }
//        if (!storedRegisters && ((getObjectFactory().getAllMasterRegisters().getRegisterValues().size() > 0) || getObjectFactory().getAllMBusRegisters().getRegisterValues().size() > 0)) {
//            log("Received register data, but none are stored because of the communication profile settings");
//        }
    }

    public void setMessageResults(boolean end) throws BusinessException, SQLException {
//        if (getObjectFactory().isRequestsAllowed()) {
//            for (CommunicationScheduler scheduler : getCommSchedulers()) {
//                getACE4000Messages().setMessageResult(end, scheduler.getRtu().getOldPendingMessages());
//            }
//        }
    }

    private void storeEvents() throws SQLException, BusinessException {
        List<MeterEvent> meterEvents = getObjectFactory().getMeterEvents();
        if (meterEvents.size() > 0) {
            ProfileData profileData = new ProfileData();
            List<MeterEvent> result = new ArrayList<MeterEvent>();
            for (MeterEvent meterEvent : meterEvents) {
                boolean add = false;
                if (meterEvent.getEiCode() == MeterEvent.POWERDOWN) {
                    if (!meterEvent.getMessage().contains("Duration")) {
                        for (MeterEvent event : meterEvents) {
                            if ((event.getMessage().contains("Duration")) && (event.getTime().equals(meterEvent.getTime()) && (event.getEiCode() == MeterEvent.POWERDOWN))) {
                                meterEvent = new MeterEvent(meterEvent.getTime(), meterEvent.getEiCode(), meterEvent.getProtocolCode(), meterEvent.getMessage() + ", " + event.getMessage());
                                add = true;
                                break;
                            }
                        }
                    }
                } else {
                    add = true;
                }

                Date lastLogbook = getMasterMeter().getLastLogbook();
                if (add && (meterEvent.getTime().after(lastLogbook == null ? new Date(0) : lastLogbook))) {
                    result.add(meterEvent);
                }
            }


            log("Received " + meterEvents.size() + " events, storing " + result.size() + " new events");
            profileData.setMeterEvents(result);
            getMasterMeter().store(profileData);
        }
    }

    private void storeMasterRegisters(MeterReadingData mrd) throws SQLException, BusinessException {
        if (mrd.getRegisterValues().size() > 0) {

            //Don't store register values if the register is not defined on the RTU
            MeterReadingData result = new MeterReadingData();
            for (RegisterValue registerValue : mrd.getRegisterValues()) {
                for (Register rtuRegister : getMasterMeter().getRegisters()) {
                    if (rtuRegister.getRegisterSpec().getObisCode().equals(registerValue.getObisCode())) {
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        result.add(registerValue);
                        break;
                    }
                }
            }

            log("Received " + mrd.getRegisterValues().size() + " registers for master meter, storing data for " + result.getRegisterValues().size() + " configured registers");
            getMasterMeter().store(result);
        }
    }

    private void storeSlaveRegisters(MeterReadingData mrd, Device slave) throws SQLException, BusinessException {
        if (mrd.getRegisterValues().size() > 0) {

            //Don't store register values if the register is not defined on the RTU
            MeterReadingData result = new MeterReadingData();
            if (slave != null) {
                for (RegisterValue registerValue : mrd.getRegisterValues()) {
                    for (com.energyict.mdw.amr.Register rtuRegister : slave.getRegisters()) {
                        if (rtuRegister.getRegisterSpec().getObisCode().equals(registerValue.getObisCode())) {
                            registerValue.setRtuRegisterId(rtuRegister.getId());
                            result.add(registerValue);
                            break;
                        }
                    }
                }
                log("Received " + mrd.getRegisterValues().size() + " registers for slave meter [" + slave.getSerialNumber() + "], storing data for " + result.getRegisterValues().size() + " configured registers");
                slave.store(result);
            }
        }
    }

    public boolean isDst() {
        return getDeviceTimeZone().inDaylightTime(getObjectFactory().getCurrentMeterTime(false, new Date()));
    }

    public TimeZone getDeviceTimeZone() {
//        if (getCommSchedulers().size() == 0 || getCommSchedulers().get(0).getRtu() == null || TimeZone.getDefault() == null) {
//            TimeZone timeZone = TimeZone.getDefault();
//            log(Level.WARNING, "No device time zone found, using system time zone: " + timeZone.getDisplayName());
//            return timeZone;
//        }
        return TimeZone.getDefault();
    }

    private void setConnectTime(long currentTimeMillis) {
        this.connectTime = currentTimeMillis;
    }

    public long getConnectTime() {
        return connectTime;
    }

    private void addFailureLogging(StringBuilder eString) throws SQLException, BusinessException {
        if (getMasterMeter() != null) {
//            for (CommunicationScheduler cs : getMasterMeter().getCommunicationSchedulers()) {
//                if (!cs.getActive()) {
//                    cs.startCommunication();
//                    AMRJournalManager amrjm = new AMRJournalManager(getMasterMeter(), cs);
//                    amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, eString.toString()));
//                    amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - getConnectTime()) / 1000));
//                    amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
//                    amrjm.updateRetrials();
//                    break;
//                }
//            }
        } else {
            log(Level.WARNING, "Failed to enter an AMR journal entry, meter doesn't exist in database");
        }
    }

    private void addSuccessLogging() throws SQLException, BusinessException {
        if (getMasterMeter() != null) {
//            for (CommunicationScheduler cs : getMasterMeter().getCommunicationSchedulers()) {
//                if (!cs.getActive()) {
//                    cs.startCommunication();
//                    AMRJournalManager amrjm = new AMRJournalManager(getMasterMeter(), cs);
//                    amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - getConnectTime()) / 1000));
//                    amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CC_OK));
//                    amrjm.updateLastCommunication();
//                    break;
//                }
//            }
        } else {
            log(Level.WARNING, "Failed to enter an AMR journal entry, meter doesn't exist in database");
        }
    }

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }

    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

//    @Override
//    public List<PropertySpec> getRequiredProperties() {
//        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
//    }
//
//    @Override
//    public List<PropertySpec> getOptionalProperties() {
//        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
//    }

    public void initProperties(Properties properties) {
        this.properties.addProperties(properties);
    }

    public ACE4000Properties getProtocolProperties() {
        return properties;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Important method to determine the meters topology
     * If we get a serialNumber from the UDPListener, fill in all the serialNumber we can find in the database and
     * fill in the necessary Device's as well
     *
     * @param pushedSerialNumber the received serial number
     * @throws BusinessException when the database doesn't contain the serial number
     */
    public void setPushedSerialNumber(String pushedSerialNumber) throws BusinessException {
        this.pushedSerialNumber = pushedSerialNumber;

        try {
            if (masterMeter == null) {    // Find the concerning RTU in the database, load the properties

                if (isMasterMeter(getPushedSerialNumber())) {
                    initProperties(getMasterMeter().getProtocolProperties().toStringProperties());
                    findAllSlaveMeters();
                } else if (isSlaveMeter(getPushedSerialNumber())) {
                    initProperties(getMBusMetersMap().get(getPushedSerialNumber()).getProtocolProperties().toStringProperties());
                    findMasterMeter();
                    findAllSlaveMeters();
                } else {
                    throw new ApplicationException("Meter with serial number " + pushedSerialNumber + " is not found in database");
                }

                log("Received data from meter with serial number " + getMasterSerialNumber());

            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }

    public boolean isMasterMeter(String serialNumber) throws ConfigurationException {

        // find by CallHomeID, unique in database
        List meterList = mw().getDeviceFactory().findByDialHomeId("ACE4000" + serialNumber);

        if (meterList.size() == 1) {    // we found him, take him down boys ....
            setMasterMeter((Device) meterList.get(0));
            setMasterSerialNumber(serialNumber);
            return true;
        } else if (meterList.size() > 1) {
            getLogger().severe("Multiple meters where found with serial: " + serialNumber);
            throw new ConfigurationException("Multiple meters where found with serial: " + serialNumber);
        }
        return false;
    }

    /**
     * Check if the received serial number is a slave device.
     * Also add the device to the map of slaves.
     *
     * @param serialNumber the received serial number
     * @return boolean
     * @throws ConfigurationException Multiple meters where found with serial
     */
    public boolean isSlaveMeter(String serialNumber) throws ConfigurationException {

        // find by CallHomeID, unique in database
        List meterList = mw().getDeviceFactory().findByDialHomeId("ACE4000MB" + serialNumber);

        if (meterList.size() == 1) {    // we found him, take him down boys ....
            getMBusMetersMap().put(serialNumber, (Device) meterList.get(0));
            return true;
        } else if (meterList.size() > 1) {
            getLogger().severe("Multiple meters where found with serial: " + serialNumber);
            throw new ConfigurationException("Multiple meters where found with serial: " + serialNumber);
        }
        return false;
    }

    /**
     * Returns the serialNumber of the E-meter
     * Needs to be set before the message is sent!
     *
     * @return a serialNumber
     */
    public String getMasterSerialNumber() {
        return masterSerialNumber;
    }

    /**
     * The serial number received in a pushed XML message.
     * This identifies the meter that sent the message, it can be looked up in the EiServer database.
     *
     * @return the serial number
     */
    public String getPushedSerialNumber() {
        return pushedSerialNumber;
    }

    /**
     * Sets the masters serialNumber
     *
     * @param masterSerialNumber the serial number of the master meter
     */
    public void setMasterSerialNumber(String masterSerialNumber) {
        this.masterSerialNumber = masterSerialNumber;
    }

    /**
     * Contains a map of the serial numbers with their Device's.
     * No database overkill if we keep on asking the Device
     *
     * @return the MBusMeters hashMap
     */
    public HashMap<String, Device> getMBusMetersMap() {
        if (mBusMeters == null) {
            mBusMeters = new HashMap<String, Device>();
        }
        return mBusMeters;
    }

    public int getMeterProfileInterval() {
        if (masterMeter != null && !masterMeter.getChannels().isEmpty()) {
            return masterMeter.getChannels().get(0).getIntervalInSeconds();
        } else {
            return -1;
        }
    }

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     *
     * @return the current metering warehouse
     */
    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
    }

    public Device getMasterMeter() {
        return masterMeter;
    }

//    public List<CommunicationScheduler> getCommSchedulers() {
//        return schedulers;
//    }

    public void setMasterMeter(Device meter) {
        this.masterMeter = meter;
        setCommunicationScheduler();
    }

    private void setCommunicationScheduler() {
//        for (CommunicationScheduler cs : getMasterMeter().getCommunicationSchedulers()) {
//            if (!cs.getActive() && cs.getDialerFactory().getDialerClassName().equalsIgnoreCase("")) {
//                if (!schedulers.contains(cs)) {
//                    schedulers.add(cs);
//                }
//            }
//        }
    }

    private void findMasterMeter() {
        if (!getMBusMetersMap().isEmpty()) {
            Device mbusSlave = getMBusMetersMap().get(getPushedSerialNumber());

            if (mbusSlave.getGateway() != null) {
                setMasterMeter(mbusSlave.getGateway());
                setMasterSerialNumber(getMasterMeter().getSerialNumber());
            } else {
                getLogger().severe("MBus slave meter has no gateway configured so no master meter was found!");
            }
        } else {
            getLogger().severe("Master meter can NOT be found because no slaves are detected.");
        }
    }

    private void findAllSlaveMeters() {
        if (masterMeter != null) {
            List<Device> slaves = masterMeter.getDownstreamDevices();
            if (slaves.size() > 0) {
                for (Device slave : slaves) {
                    if (!getMBusMetersMap().containsKey(slave.getSerialNumber())) {
                        getMBusMetersMap().put(slave.getSerialNumber(), slave);
                    }
                }
            } else {
                log("No slave meters were found on meter " + getMasterSerialNumber());
            }
        } else {
            getLogger().severe("No slaves can be found because the master meter is NULL.");
        }
    }

    public StringBuilder getErrorString() {
        if (errorString == null) {
            errorString = new StringBuilder();
        }
        return errorString;
    }

    public int getRetry() {
        return retry;
    }
}