package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.actarisace4000.ACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author gna
 */
public class ObjectFactory {

    private ACE4000 ace4000;
    private Acknowledge acknowledge = null;
    private ConfigurationAcknowledge configurationAcknowledge = null;
    private FirmwareVersion firmwareVersion = null;
    private AutoPushConfig autoPushConfig = null;
    private FullMeterConfig fullMeterConfig = null;
    private Announcement announcement = null;
    private CurrentReadings currentReadings = null;
    private LoadProfile loadProfile = null;
    private MBusBillingData mBusBillingData = null;
    private MBusCurrentReadings mbCurrReadings = null;
    private ForceTime forceTime = null;
    private SyncTime syncTime = null;
    private DateTime readTime = null;
    private TimeConfig configTime = null;
    private BillingConfig billingConfig = null;
    private MaxDemandRegister maximumDemandRegisters = null;
    private InstantVoltAndCurrent instantVoltAndCurrent = null;
    private BillingData billingData = null;

    private int trackingID = -1;
    private boolean sendAck = false;      //Indicates whether or not the parsed message must be ACK'ed.
    private EventData eventData = null;
    private List<MeterEvent> meterEvents;

    private boolean receivedEvents = false;
    private boolean receivedCurrentRegisters = false;
    private boolean receivedBillingRegisters = false;
    private boolean receivedMBusCurrentRegisters = false;
    private boolean receivedMBusBillingRegisters = false;
    private boolean receivedMaxDemandRegisters = false;
    private boolean receivedInstantRegisters = false;
    private boolean receivedLoadProfile = false;
    private boolean clockWasSet = false;

    public ObjectFactory(ACE4000 ace4000) {
        this.ace4000 = ace4000;
    }

    public boolean isReceivedEvents() {
        return receivedEvents;     //Indicates whether or not an extra request for events is necessary
    }

    public boolean isReceivedBillingRegisters() {
        return receivedBillingRegisters;
    }

    public boolean isClockWasSet() {
        return clockWasSet;
    }

    public void setClockWasSet(boolean clockWasSet) {
        this.clockWasSet = clockWasSet;
    }

    public boolean isReceivedCurrentRegisters() {
        return receivedCurrentRegisters;
    }

    public boolean isReceivedInstantRegisters() {
        return receivedInstantRegisters;
    }

    public boolean isReceivedMaxDemandRegisters() {
        return receivedMaxDemandRegisters;
    }

    public boolean isReceivedLoadProfile() {
        return receivedLoadProfile;
    }

    public boolean isReceivedMBusBillingRegisters() {
        return receivedMBusBillingRegisters;
    }

    public boolean isReceivedMBusCurrentRegisters() {
        return receivedMBusCurrentRegisters;
    }

    public FullMeterConfig getFullMeterConfig() {
        if (fullMeterConfig == null) {
            fullMeterConfig = new FullMeterConfig(this);
        }
        return fullMeterConfig;
    }

    public Announcement getAnnouncement() {
        if (announcement == null) {
            announcement = new Announcement(this);
        }
        return announcement;
    }

    public CurrentReadings getCurrentReadings() {
        if (currentReadings == null) {
            currentReadings = new CurrentReadings(this);
        }
        return currentReadings;
    }

    public MBusCurrentReadings getMBCurrentReadings() {
        if (mbCurrReadings == null) {
            mbCurrReadings = new MBusCurrentReadings(this);
        }
        return mbCurrReadings;
    }

    public LoadProfile getLoadProfile() {
        if (loadProfile == null) {
            loadProfile = new LoadProfile(this);
        }
        return loadProfile;
    }

    public MBusBillingData getMBusBillingData() {
        if (mBusBillingData == null) {
            mBusBillingData = new MBusBillingData(this);
        }
        return mBusBillingData;
    }

    public MaxDemandRegister getMaximumDemandRegisters() {
        if (maximumDemandRegisters == null) {
            maximumDemandRegisters = new MaxDemandRegister(this);
        }
        return maximumDemandRegisters;
    }

    public EventData getEventData() {
        if (eventData == null) {
            eventData = new EventData(this);
        }
        return eventData;
    }

    public InstantVoltAndCurrent getInstantVoltAndCurrent() {
        if (instantVoltAndCurrent == null) {
            instantVoltAndCurrent = new InstantVoltAndCurrent(this);
        }
        return instantVoltAndCurrent;
    }

    public BillingData getBillingData() {
        if (billingData == null) {
            billingData = new BillingData(this);
        }
        return billingData;
    }

    public BillingConfig getBillingConfig() {
        if (billingConfig == null) {
            billingConfig = new BillingConfig(this);
        }
        return billingConfig;
    }

    public ForceTime getForceTime() {
        if (forceTime == null) {
            forceTime = new ForceTime(this);
        }
        return forceTime;
    }

    public DateTime getReadTime() {
        if (readTime == null) {
            readTime = new DateTime(this);
        }
        return readTime;
    }

    public TimeConfig getConfigTime() {
        if (configTime == null) {
            configTime = new TimeConfig(this);
        }
        return configTime;
    }

    public SyncTime getSyncTime() {
        if (syncTime == null) {
            syncTime = new SyncTime(this);
        }
        return syncTime;
    }

    public Acknowledge getAcknowledge() {
        if (acknowledge == null) {
            acknowledge = new Acknowledge(this);
        }
        return acknowledge;
    }

    public FirmwareVersion getFirmwareVersion() {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(this);
        }
        return firmwareVersion;
    }

    public ConfigurationAcknowledge getConfigurationAcknowledge() {
        if (configurationAcknowledge == null) {
            configurationAcknowledge = new ConfigurationAcknowledge(this);
        }
        return configurationAcknowledge;
    }

    public AutoPushConfig getAutoPushConfig() {
        if (autoPushConfig == null) {
            autoPushConfig = new AutoPushConfig(this);
        }
        return autoPushConfig;
    }

    private String getRetryDescription() {
        return getAce4000().getRetry() == -1 ? "" : " [Retry " + (getAce4000().getRetry() + 1) + "]";
    }

    /**
     * Send a request for full meter configuration
     *
     * @throws IOException when the communication fails
     */
    public void sendFullMeterConfigRequest() throws IOException {
        log(Level.INFO, "Sending meter configuration request" + getRetryDescription());
        getFullMeterConfig().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getFullMeterConfig().request();
    }

    /**
     * Send xml with the meters autopush config - Startime, Stoptime pushwindow ...
     *
     * @param enabled                - enabled daily push
     * @param start                  - startTime in minutes after midnight
     * @param stop                   - stopTime in minutes after midnight
     * @param random                 - true/false if push can start randomly in pushwindow
     * @param retryWindowPercentage: the percentage of the window that is reserved for retries
     * @throws IOException when the communication fails
     */
    public void setAutoPushConfig(int enabled, int start, int stop, boolean random, int retryWindowPercentage) throws IOException {
        log(Level.INFO, "Sending request to change the auto push configuration" + getRetryDescription());
        getAutoPushConfig().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getAutoPushConfig().setEnableState(enabled);
        getAutoPushConfig().setOpen(start);
        getAutoPushConfig().setClose(stop);
        getAutoPushConfig().setRandom(random);
        getAutoPushConfig().setRetryWindowPercentage(retryWindowPercentage);

        getAutoPushConfig().request();
    }

    /**
     * Request all the loadprofile data
     *
     * @throws IOException when the communication fails
     */
    public void sendLoadProfileRequest() throws IOException {
        log(Level.INFO, "Sending profile data request (all data)" + getRetryDescription());
        getLoadProfile().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getLoadProfile().request();
    }

    /**
     * Request the loadprofile data from a certain point in time
     *
     * @param from equals the the point in time
     * @throws IOException when the communication fails
     */
    public void sendLoadProfileRequest(Date from) throws IOException {
        log(Level.INFO, "Sending profile data request, from date = " + from.toString() + getRetryDescription());
        getLoadProfile().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getLoadProfile().setFrom(from);
        getLoadProfile().request();
    }

    /**
     * Request all the MBus billing data
     *
     * @throws IOException when the communication fails
     */
    public void sendMBusBillingDataRequest() throws IOException {
        log(Level.INFO, "Sending MBus billing data request (all data)" + getRetryDescription());
        getMBusBillingData().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getMBusBillingData().request();
    }

    /**
     * Request the instant voltage and current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendInstantVoltageAndCurrentRequest() throws IOException {
        log(Level.INFO, "Sending request for instantaneous voltage and current registers" + getRetryDescription());
        getInstantVoltAndCurrent().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getInstantVoltAndCurrent().request();
    }

    /**
     * Request the MBus billing data from a certain point in time
     *
     * @param from equals the point in time
     * @throws IOException when the communication fails
     */
    public void sendMBusBillingDataRequest(Date from) throws IOException {
        log(Level.INFO, "Sending MBus billing data request, from date = " + from.toString() + getRetryDescription());
        getMBusBillingData().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());     //TODO should we put the slave SN here?? //TODO
        getMBusBillingData().setFrom(from);
        getMBusBillingData().request();
    }

    /**
     * Request the MBus current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendMBusCurrentRegistersRequest() throws IOException {
        log(Level.INFO, "Sending MBus current registers request" + getRetryDescription());
        getMBCurrentReadings().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());     //TODO should we put the slave SN here?? //TODO
        getMBCurrentReadings().request();
    }

    /**
     * Request all billing data from the E-meter
     *
     * @throws IOException when the communication fails
     */
    public void sendBDRequest() throws IOException {
        log(Level.INFO, "Sending billing data (all) request" + getRetryDescription());
        getBillingData().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getBillingData().request();
    }

    /**
     * Request the billing data from the E-meter from a certain point in time
     *
     * @param from equals the point in time
     * @throws IOException when the communication fails
     */
    public void sendBDRequest(Date from) throws IOException {
        log(Level.INFO, "Sending billing data request" + getRetryDescription());
        getBillingData().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getBillingData().setFrom(from);
        getBillingData().request();
    }

    /**
     * Request the current registers
     *
     * @throws IOException when the communication fails
     */
    public void sendCurrentRegisterRequest() throws IOException {
        log(Level.INFO, "Sending current registers request" + getRetryDescription());
        getCurrentReadings().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getCurrentReadings().request();
    }

    /**
     * Send the E-meters billingdata  configuration
     *
     * @param enabled   - billingdata is enabled/disabled
     * @param intervals - interval in seconds between two records
     * @param numbOfInt - number of records to store
     * @throws IOException when the communication fails
     */
    public void sendBDConfig(int enabled, int intervals, int numbOfInt) throws IOException {
        log(Level.INFO, "Sending billing data configuration (for e-meter) request" + getRetryDescription());
        getBillingConfig().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getBillingConfig().setEnabled(enabled);
        getBillingConfig().setInterval(intervals);
        getBillingConfig().setNumOfRecs(numbOfInt);
        getBillingConfig().request();
    }

    /**
     * Force the meter time to the system time
     *
     * @throws IOException when the communication fails
     */
    public void sendForceTime() throws IOException {
        log(Level.INFO, "Sending force time request, keeping the DST and meter time zone in mind" + getRetryDescription());
        getForceTime().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getForceTime().request();
    }

    /**
     * Send a request to read the events
     *
     * @throws IOException when the communication fails
     */
    public void sendEventRequest() throws IOException {
        log(Level.INFO, "Sending request to read the events" + getRetryDescription());
        getEventData().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getEventData().request();
    }

    /**
     * Sync the meter time to the system time
     *
     * @throws IOException when the communication fails
     */
    public void sendSyncTime() throws IOException {
        log(Level.INFO, "Sending time sync request" + getRetryDescription());
        getSyncTime().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getSyncTime().request();
    }

    /**
     * Send the time sync configuration
     *
     * @param diff  - maximum allowed time difference for timesync to take place (in seconds)
     * @param trip  - maximum SNTP trip time in seconds
     * @param retry - maximum number of clock sync retries allowed
     * @throws IOException when the communication fails
     */
    public void sendTimeConfig(int diff, int trip, int retry) throws IOException {
        log(Level.INFO, "Sending time configuration request" + getRetryDescription());
        getConfigTime().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getConfigTime().setDiff(diff);
        getConfigTime().setTrip(trip);
        getConfigTime().setRetry(retry);
        getConfigTime().request();
    }

    /**
     * Send an acknowledgment with for a certain message with a given tracking number
     *
     * @param tracker equals the tracking number
     * @throws IOException when the communication fails
     */
    public void sendAcknowledge(int tracker) throws IOException {
        log(Level.INFO, "Sending acknowledge with tracking ID [" + tracker + "]");
        getAcknowledge().setTrackingID(tracker);
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getAcknowledge().prepareXML();
        getAcknowledge().request();
    }

    public void sendConfigurationAcknowledge(int tracker) throws IOException {
        log(Level.INFO, "Sending configuration acknowledge with tracking ID [" + tracker + "]");
        getAcknowledge().setTrackingID(tracker);
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getConfigurationAcknowledge().prepareXML();
        getConfigurationAcknowledge().request();
    }

    /**
     * Request the firmware versions of the meter
     *
     * @throws IOException when the communication fails
     */
    public void sendFirmwareRequest() throws IOException {
        log(Level.INFO, "Sending firmware version request" + getRetryDescription());
        getFirmwareVersion().setTrackingID(getIncreasedTrackingID());
        getAce4000().setNecessarySerialNumber(getAce4000().getMasterSerialNumber());
        getFirmwareVersion().prepareXML();
        getFirmwareVersion().request();
    }

    public ACE4000 getAce4000() {
        return ace4000;
    }

    /**
     * Parse the received XML to the corresponding object
     *
     * @param xml - the received MeterXML string
     * @throws ParserConfigurationException
     * @throws SAXException                 when the xml parsing fails
     * @throws IOException                  when the communication fails
     * @throws BusinessException
     */
    public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException, BusinessException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        System.out.println(xml);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Element topElement = document.getDocumentElement();
            parseElements(topElement);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new ParserConfigurationException("Failed to make a new builder from the documentBuilderFactory" + e.getMessage() + "(Received xml: " + xml + ")");
        } catch (SAXException e) {
            e.printStackTrace();
            throw new SAXException("Could not parse the received xmlString." + e.getMessage() + "(Received xml: " + xml + ")");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (DOMException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage() + "(Received xml: " + xml + ")");
        }
    }

    private void parseElements(Element element) throws IOException, SQLException, BusinessException {

        String nodeName = element.getNodeName();

        try {
            if (nodeName.equalsIgnoreCase(XMLTags.MPUSH)) {
                NodeList nodes = element.getElementsByTagName(XMLTags.METERDATA);
                Element md = (Element) nodes.item(0);

                if (md.getNodeName().equalsIgnoreCase(XMLTags.METERDATA)) {
                    NodeList mdNodeList = md.getChildNodes();

                    for (int i = 0; i < mdNodeList.getLength(); i++) {
                        Element mdElement = (Element) mdNodeList.item(i);
                        if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.SERIALNUMBER)) {
                            getAce4000().setPushedSerialNumber(mdElement.getTextContent());
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.TRACKER)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));    // add the radius because we receive hex
                            sendAck = true;               //Every received trackingId must be ACK'ed
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ACKNOWLEDGE)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));
                            log(Level.INFO, "Received an acknowledge with tracking ID [" + getTrackingID() + "]");
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGACK)) {
                            setTrackingID(Integer.parseInt(mdElement.getTextContent(), 16));
                            log(Level.INFO, "Received a configuration acknowledge with tracking ID [" + getTrackingID() + "]");
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPR)) {
                            log(Level.INFO, "Received a loadProfile element.");
                            getLoadProfile().parse(mdElement);
                            receivedLoadProfile = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPRABS)) {
                            log(Level.INFO, "Received a loadProfile element.");
                            getLoadProfile().parse(mdElement);
                            receivedLoadProfile = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSBILLINGDATA)) {
                            log(Level.INFO, "Received MBus billing data.");
                            getMBusBillingData().parse(mdElement);
                            receivedMBusBillingRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.EVENT)) {
                            log(Level.INFO, "Received events");
                            getEventData().parse(mdElement);
                            receivedEvents = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.INSTVC)) {
                            log(Level.INFO, "Received instantaneous registers");
                            getInstantVoltAndCurrent().parse(mdElement);
                            receivedInstantRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.ANNOUNCE)) {
                            log(Level.INFO, "Received a device announcement.");
                            getAnnouncement().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.REJECT)) {
                            int reason = Integer.parseInt(mdElement.getTextContent(), 16);
                            log(Level.WARNING, "Meter rejected a message. Reason: " + reason);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CURREADING)) {
                            log(Level.INFO, "Received current readings from meter.");
                            getCurrentReadings().parse(mdElement);
                            receivedCurrentRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MBUSCREADING)) {
                            log(Level.INFO, "Received current readings from MBus meter.");
                            getMBCurrentReadings().parse(mdElement);
                            receivedMBusCurrentRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.RESFIRMWARE)) {
                            log(Level.INFO, "Received firmware versions.");
                            getFirmwareVersion().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.BILLDATA)) {
                            log(Level.INFO, "Received billing data from meter.");
                            getBillingData().parse(mdElement);
                            receivedBillingRegisters = true;
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.CONFIGURATION)) {
                            log(Level.INFO, "Received configuration from meter.");
                            getFullMeterConfig().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.METERTIME)) {
                            log(Level.INFO, "Received timing parameters.");
                            getReadTime().parse(mdElement);
                        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.MAXDEMAND)) {
                            log(Level.INFO, "Received maximum demand registers.");
                            getMaximumDemandRegisters().parse(mdElement);
                            receivedMaxDemandRegisters = true;
                        }
                    }
                } else {
                    throw new ApplicationException("Unknown tag found in xml response: " + nodes.item(0).getNodeName());
                }
            } else {
                throw new ApplicationException("Unknown tag found in xml response: " + element.getNodeName());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        } catch (DOMException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } catch (BusinessException e) {
            e.printStackTrace();
            throw e;
        }
        if (sendAck) {
            sendAcknowledge(getTrackingID());
            sendAck = false;
        }
    }

    protected void log(Level level, String msg) {
        getAce4000().getLogger().log(level, msg);
    }

    protected int getTrackingID() {
        return trackingID;
    }

    protected int getIncreasedTrackingID() {
        trackingID++;
        if (trackingID > 4096) {
            trackingID = 0;
        }
        return trackingID;
    }

    protected void setTrackingID(int trackingID) {
        this.trackingID = trackingID;
    }

    public Date getCurrentMeterTime() {
        return getCurrentMeterTime(getAce4000().isDst(), new Date());
    }

    public Date getMeterTime(Date date) {
        return getCurrentMeterTime(getAce4000().isDst(), date);
    }

    /**
     * Convert a given date to a time stamp in the meter's time zone
     *
     * @param dst  whether or not to add dst offset
     * @param date the given date
     * @return the resulting date
     */
    public Date getCurrentMeterTime(boolean dst, Date date) {
        TimeZone timeZone = getAce4000().getDeviceTimeZone();
        Calendar now = Calendar.getInstance(timeZone);
        now.setTime(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        cal.setLenient(true);
        cal.add(Calendar.HOUR_OF_DAY, dst ? -1 : 0);   //Convert to base time: cut off the DST offset
        return cal.getTime();
    }

    public Date convertMeterDateToSystemDate(long seconds) {
        return convertMeterDateToSystemDate(new Date(seconds * 1000));
    }

    /**
     * Convert a timestamp received from the meter to a system time stamp
     *
     * @param date the given timestamp
     * @return the converted date in the system's time zone
     */
    public Date convertMeterDateToSystemDate(Date date) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTime(date);

        Calendar meter = Calendar.getInstance(getAce4000().getDeviceTimeZone());

        meter.set(Calendar.YEAR, utc.get(Calendar.YEAR));
        meter.set(Calendar.MONTH, utc.get(Calendar.MONTH));
        meter.set(Calendar.DATE, utc.get(Calendar.DATE));
        meter.set(Calendar.HOUR_OF_DAY, utc.get(Calendar.HOUR_OF_DAY));
        meter.set(Calendar.MINUTE, utc.get(Calendar.MINUTE));
        meter.set(Calendar.SECOND, utc.get(Calendar.SECOND));
        meter.set(Calendar.MILLISECOND, utc.get(Calendar.MILLISECOND));
        meter.setLenient(true);
        meter.add(Calendar.HOUR_OF_DAY, getAce4000().isDst() ? 1 : 0);    //Convert from base time to actual time: add DST offset
        return meter.getTime();
    }

    public List<MeterEvent> getMeterEvents() {
        if (meterEvents == null) {
            meterEvents = new ArrayList<MeterEvent>();
        }
        return meterEvents;
    }

    /**
     * Returns all master registers: current readings, billing registers, maximum demand registers and instantaneous registers.
     *
     * @return master registers
     */
    public MeterReadingData getAllMasterRegisters() {
        MeterReadingData mrd;
        mrd = getCurrentReadings().getMrd();

        for (RegisterValue registerValue : getBillingData().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        for (RegisterValue registerValue : getMaximumDemandRegisters().getMdr().getRegisterValues()) {
            mrd.add(registerValue);
        }
        for (RegisterValue registerValue : getInstantVoltAndCurrent().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        return mrd;
    }

    /**
     * Returns all slave registers: current readings and billing registers
     *
     * @return slave registers
     */
    public MeterReadingData getAllMBusRegisters() {            //Fetch all current and billing registers
        MeterReadingData mrd;
        mrd = getMBCurrentReadings().getMrd();

        for (RegisterValue registerValue : getMBusBillingData().getMrd().getRegisterValues()) {
            mrd.add(registerValue);
        }
        return mrd;
    }

    /**
     * Checks if the registers were received.
     * If not, request the missing registers.
     *
     * @param fromDate for querying the billing registers
     * @return if the registers have been received or not
     * @throws IOException communication error
     */
    public boolean requestAllMasterRegisters(Date fromDate) throws IOException {
        boolean received = true;
        if (!isReceivedBillingRegisters()) {
            sendBDRequest(fromDate);
            received = false;
        }
        if (!isReceivedCurrentRegisters()) {
            sendCurrentRegisterRequest();
            received = false;
        }
        if (!isReceivedInstantRegisters()) {
            sendInstantVoltageAndCurrentRequest();
            received = false;
        }
        return received;
    }

    /**
     * Checks if slave registers were received.
     * If not, request the missing registers.
     *
     * @param fromDate for querying the billing registers
     * @return if the registers have been received or not
     * @throws IOException communication error
     */
    public boolean requestAllSlaveRegisters(Date fromDate) throws IOException {
        boolean received = true;
        if (!isReceivedMBusBillingRegisters()) {
            sendMBusBillingDataRequest(fromDate);
            received = false;
        }
        if (!isReceivedMBusCurrentRegisters()) {
            sendMBusCurrentRegistersRequest();
            received = false;
        }
        return received;
    }
}