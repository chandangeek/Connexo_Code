/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
 * <p/>
 * <B>Description :</B><BR>
 * Class that implements the Siemens ZMD DLMS profile implementation
 * <BR>
 * <B>@beginchanges</B><BR>
 * KV|08042003|Initial version
 * KV|08102003|Set default of RequestTimeZone to 0
 * KV|10102003|generate OTHER MeterEvent when statusbit is not supported
 * KV|27102003|changed code for correct dst transition S->W
 * KV|20082004|Extended with obiscode mapping for register reading
 * KV|17032005|improved registerreading
 * KV|23032005|Changed header to be compatible with protocol version tool
 * KV|30032005|Improved registerreading, configuration data
 * KV|31032005|Handle DataContainerException
 * KV|15072005|applyEvents() done AFTER getting the logbook!
 * KV|10102006|extension to support cumulative values in load profile
 * KV|10102006|fix to support 64 bit values in load profile
 * @endchanges
 */

package com.energyict.protocolimpl.dlms;


import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.siemenszmd.LogBookReader;
import com.energyict.protocolimpl.dlms.siemenszmd.ObisCodeMapper;
import com.energyict.protocolimpl.dlms.siemenszmd.ZMDSecurityProvider;
import com.energyict.protocolimpl.dlms.siemenszmd.ZmdMessages;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

@Deprecated
/** Deprecated as of jan 2012 - please use the new SmartMeter protocol (com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD) instead. **/
public class DLMSZMD extends DLMSSN implements RegisterProtocol, DemandResetProtocol, MessageProtocol {

    private static final byte DEBUG = 0;
    // Interval List
    private static final byte IL_CAPUTURETIME = 0;
    private static final byte IL_EVENT = 12;
    private static final byte IL_DEMANDVALUE = 13;
    // Event codes as interpreted by MV90 for the Siemens ZMD meter
    private static final long EV_NORMAL_END_OF_INTERVAL = 0x00800000;
    private static final long EV_START_OF_INTERVAL = 0x00080000;
    private static final long EV_FATAL_ERROR = 0x00000001;
    private static final long EV_CORRUPTED_MEASUREMENT = 0x00000004;
    private static final long EV_SUMMER_WINTER = 0x00000008;
    private static final long EV_TIME_DATE_ADJUSTED = 0x00000020;
    private static final long EV_POWER_UP = 0x00000040;
    private static final long EV_POWER_DOWN = 0x00000080;
    private static final long EV_EVENT_LOG_CLEARED = 0x00002000;
    private static final long EV_LOAD_PROFILE_CLEARED = 0x00004000;
    private final MessageProtocol messageProtocol;
    int eventIdIndex;

    public DLMSZMD() {
        this.messageProtocol = new ZmdMessages(this);
    }

    protected String getDeviceID() {
        return "LGZ";
    }
    //private static final long EV_CAPTURED_EVENTS=       0x008860E5; // Add new events...

    //KV 27102003
    public Calendar initCalendarSW(boolean protocolDSTFlag, TimeZone timeZone) {
        Calendar calendar;
        if (protocolDSTFlag) {
            calendar = Calendar.getInstance(ProtocolUtils.getSummerTimeZone(timeZone));
        } else {
            calendar = Calendar.getInstance(ProtocolUtils.getWinterTimeZone(timeZone));
        }
        return calendar;
    }

    /** ProtocolVersion date */
    public String getProtocolVersion() {
        return "$Date$";
    }

    protected void getEventLog(ProfileData profileData, Calendar fromCalendar, Calendar toCalendar) throws IOException {
        LogBookReader logBookReader = new LogBookReader(this, getCosemObjectFactory());
        List<MeterEvent> meterEvents = logBookReader.getEventLog(fromCalendar, toCalendar);
        for (MeterEvent meterEvent : meterEvents) {
            profileData.addEvent(meterEvent);
        }
    }

    @Override
    protected SecurityProvider getSecurityProvider() {
        return new ZMDSecurityProvider(getProperties());
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     * the default value({@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    @Override
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(1573408L);
    }

    protected void buildProfileData(byte bNROfChannels, ProfileData profileData, ScalerUnit[] scalerunit, UniversalObject[] intervalList) throws IOException {
        byte bDOW;
        Calendar stdCalendar = null;
        Calendar dstCalendar = null;
        Calendar calendar = null;
        int i, t;
        IntervalData savedIntervalData = null;

        if (isRequestTimeZone()) {
            stdCalendar = ProtocolUtils.getCalendar(false, requestTimeZone());
            dstCalendar = ProtocolUtils.getCalendar(true, requestTimeZone());
        } else { // KV 27102003
            stdCalendar = initCalendarSW(false, getTimeZone());
            dstCalendar = initCalendarSW(true, getTimeZone());
        }

        if (DEBUG >= 1) {
            System.out.println("intervalList.length = " + intervalList.length);
        }

        for (i = 0; i < intervalList.length; i++) {

            // KV 27102003
            if (intervalList[i].getField(IL_CAPUTURETIME + 11) != 0xff) {
                if ((intervalList[i].getField(IL_CAPUTURETIME + 11) & 0x80) == 0x80) {
                    calendar = dstCalendar;
                } else {
                    calendar = stdCalendar;
                }
            } else {
                calendar = stdCalendar;
            }

            // Build Timestamp
            calendar.set(Calendar.YEAR, (int) ((intervalList[i].getField(IL_CAPUTURETIME) << 8) |
                    intervalList[i].getField(IL_CAPUTURETIME + 1)));
            calendar.set(Calendar.MONTH, (int) intervalList[i].getField(IL_CAPUTURETIME + 2) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, (int) intervalList[i].getField(IL_CAPUTURETIME + 3));
            calendar.set(Calendar.HOUR_OF_DAY, (int) intervalList[i].getField(IL_CAPUTURETIME + 5));
            calendar.set(Calendar.MINUTE, (int) intervalList[i].getField(IL_CAPUTURETIME + 6));
            calendar.set(Calendar.SECOND, (int) intervalList[i].getField(IL_CAPUTURETIME + 7));

            int iField = (int) intervalList[i].getField(IL_EVENT); // & (int)EV_CAPTURED_EVENTS; // KV 10102003, include all bits...
            iField &= (EV_NORMAL_END_OF_INTERVAL ^ 0xffffffff); // exclude EV_NORMAL_END_OF_INTERVAL bit
            iField &= (EV_SUMMER_WINTER ^ 0xffffffff); // exclude EV_SUMMER_WINTER bit // KV 10102003
            for (int bit = 0x1; bit != 0; bit <<= 1) {
                if ((iField & bit) != 0) {
                    profileData.addEvent(new MeterEvent(new Date(((Calendar) calendar.clone()).getTime().getTime()),
                            (int) mapLogCodes(bit),
                            (int) bit));
                }
            } // for (int bit=0x1;bit!=0;bit<<=1)

            // KV 12112002 following the Siemens integration handbook, only exclude profile entries where
            // status & EV_START_OF_INTERVAL is true
            // SVA Update: profile entries where status & EV_LOAD_PROFILE_CLEARED is true can also be left out.

            if (((intervalList[i].getField(IL_EVENT) & EV_START_OF_INTERVAL) == 0) && ((intervalList[i].getField(IL_EVENT) & EV_LOAD_PROFILE_CLEARED) == 0)) {

                // In case the EV_NORMAL_END_OF_INTERVAL bit is not set, calendar is possibly
                // not aligned to interval boundary caused by an event
                if ((intervalList[i].getField(IL_EVENT) & EV_NORMAL_END_OF_INTERVAL) == 0) {
                    // Following code does the aligning
                    int rest = (int) (calendar.getTime().getTime() / 1000) % getProfileInterval();
                    if (DEBUG >= 1) {
                        System.out.print(calendar.getTime() + " " + calendar.getTime().getTime() + ", timestamp adjusted with " + (getProfileInterval() - rest) + " sec.");
                    }
                    if (rest > 0) {
                        calendar.add(Calendar.SECOND, getProfileInterval() - rest);
                    }
                } else {
                    if (DEBUG >= 1) {
                        System.out.print(calendar.getTime() + " " + calendar.getTime().getTime() + ", statusbits = " + Integer.toHexString(iField));
                    }
                }

                // Fill profileData
                IntervalData intervalData = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()));

                for (t = 0; t < bNROfChannels; t++) {
                    Long val = new Long(intervalList[i].getField(IL_DEMANDVALUE + t));
                    intervalData.addValue(val);
                    if (DEBUG >= 1) {
                        System.out.print(", value = " + val.longValue());
                    }
                }

                if ((intervalList[i].getField(IL_EVENT) & EV_CORRUPTED_MEASUREMENT) != 0) {
                    intervalData.addStatus(IntervalData.CORRUPTED);
                }

                // In case the EV_NORMAL_END_OF_INTERVAL bit is not set, save the interval and add it to the next or save as separate!
                // Changed 19082015: all intervalData obj will be added 1-on-1 to the profileData - double intervals will be merged afterwards (see #mergeDuplicateIntervalsIncludingIntervalStatus below)
                if ((intervalList[i].getField(IL_EVENT) & EV_NORMAL_END_OF_INTERVAL) != 0) {
                            profileData.addInterval(intervalData);
                    } else {
                    roundUp2nearestInterval(intervalData);
                        profileData.addInterval(intervalData);
                    }
            } // if ((intervalList[i].getField(IL_EVENT) & EV_START_OF_INTERVAL) == 0)

            if (DEBUG >= 1) {
                System.out.println();
            }

        } // for (i=0;i<intervalList.length;i++) {

        // In case of double intervals (which can occur if a time shift has been done) then merge them together
        profileData.setIntervalDatas(mergeDuplicateIntervalsIncludingIntervalStatus(profileData.getIntervalDatas()));
    } // ProfileData buildProfileData(...)

    /**
     * Merge the duplicate intervals from the given list of IntervalData elements.
     * When merging multiple IntervalData elements, the Eis/protocol statuses are merged as well. The merging
     * process will take into account whether the channel contains a cumulative value or simple consumption.
     *
     * @param intervals the list of IntervalData elements which should be checked for doubles
     * @return the merged list of IntervalData elements (which now should no longer contain duplicate intervals)
     */
    private List<IntervalData> mergeDuplicateIntervalsIncludingIntervalStatus(List<IntervalData> intervals) throws IOException {
        List<IntervalData> mergedIntervals = new ArrayList<IntervalData>();
        for (IntervalData id2compare : intervals) {
            boolean alreadyProcessed = false;
            for (IntervalData merged : mergedIntervals) {
                if (merged.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                    alreadyProcessed = true;
                    break;
                }
            }

            if (!alreadyProcessed) {
                List<IntervalData> toAdd = new ArrayList<IntervalData>();
                for (IntervalData id : intervals) {
                    if (id.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                        toAdd.add(id);
                    }
                }
                IntervalData md = new IntervalData(id2compare.getEndTime());

                for (IntervalData intervalData : toAdd) {
                    if (md.getIntervalValues().isEmpty()) {
                        md.setIntervalValues(intervalData.getIntervalValues());
                    } else {
                        md.setIntervalValues(addIntervalData(md, intervalData).getIntervalValues());
                    }
                    md.addEiStatus(intervalData.getEiStatus());
                    md.addProtocolStatus(intervalData.getProtocolStatus());
                }
                mergedIntervals.add(md);
            }
        }
        return mergedIntervals;
    }

    // KV 15122003
    private void roundDown2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int) (intervalData.getEndTime().getTime() / 1000) % getProfileInterval();
        if (rest > 0) {
            intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime() / 1000) - rest) * 1000);
        }
    }

    // KV 15122003
    private void roundUp2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int) (intervalData.getEndTime().getTime() / 1000) % getProfileInterval();
        if (rest > 0) {
            intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime() / 1000) + (getProfileInterval() - rest)) * 1000);
        }
    }

    // KV 15122003
    private int getNrOfIntervals(IntervalData intervalData) throws IOException {
        return (int) (intervalData.getEndTime().getTime() / 1000) / getProfileInterval();
    }

    // KV 15122003 changed
    private IntervalData addIntervalData(IntervalData cumulatedIntervalData, IntervalData currentIntervalData) throws IOException {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int i;
        long current;
        for (i = 0; i < currentCount; i++) {
            if (getMeterConfig().getChannelObject(i).isCapturedObjectCumulative()) {
                current = ((Number) currentIntervalData.get(i)).longValue();
            } else {
                current = ((Number) currentIntervalData.get(i)).longValue() + ((Number) cumulatedIntervalData.get(i)).longValue();
            }
            intervalData.addValue(new Long(current));
        }
        return intervalData;
    }

    private long mapLogCodes(long lLogCode) {
        switch ((int) lLogCode) {
            case (int) EV_FATAL_ERROR:
                return (MeterEvent.FATAL_ERROR);
            case (int) EV_CORRUPTED_MEASUREMENT:
                return (MeterEvent.OTHER);
            case (int) EV_TIME_DATE_ADJUSTED:
                return (MeterEvent.SETCLOCK);
            case (int) EV_POWER_UP:
                return (MeterEvent.POWERUP);
            case (int) EV_POWER_DOWN:
                return (MeterEvent.POWERDOWN);
            case (int) EV_EVENT_LOG_CLEARED:
                return (MeterEvent.OTHER);
            case (int) EV_LOAD_PROFILE_CLEARED:
                return (MeterEvent.CLEAR_DATA);
            default:
                return (MeterEvent.OTHER);
        } // switch(lLogCode)
    } // private void mapLogCodes(long lLogCode)

    /**
     * Return the serialNumber of the device.
     *
     * @return the serialNumber of the device.
     * @throws java.io.IOException if an error occurs during the read
     */
    @Override
    public String getSerialNumber() throws IOException {
        /** The serial number is present in a reserved object: COSEM Logical device name object
         * In order to facilitate access using SN referencing, this object has a reserved short name by DLMS/COSEM convention: 0xFD00.
         * See topic 'Reserved base_names for special COSEM objects' in the DLMS Blue Book.
         **/
        String retrievedSerial = getCosemObjectFactory().getGenericRead(0xFD00, DLMSUtils.attrLN2SN(2)).getString();
        if (retrievedSerial.toLowerCase().startsWith("lgz")) {
            return retrievedSerial.substring(3);
        } else {
            return retrievedSerial;
        }
    }

    @Override
    protected void validateSerialNumber() throws IOException {
        if ((getConfiguredSerialNumber() == null) || ("".compareTo(getConfiguredSerialNumber()) == 0)) {
            return;
        }

        String configuredSerial = getConfiguredSerialNumber().toLowerCase().startsWith("lgz") ? getConfiguredSerialNumber().substring(3) : getConfiguredSerialNumber();
        String meterSerial = getSerialNumber();

        if ((meterSerial != null) && (meterSerial.compareTo(configuredSerial) == 0)) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + meterSerial + ", configured sn=" + configuredSerial);
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((strID != null) && (strID.length() > 16)) {
                throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            }

            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            iHDLCTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iDelayAfterFailProperty = Integer.parseInt(properties.getProperty("DelayAfterfail", "3000").trim());
            iRequestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0").trim());
            iRequestClockObject = Integer.parseInt(properties.getProperty("RequestClockObject", "0").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iClientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "32").trim());
            iServerUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "1").trim());
            iServerLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "0").trim());
            eventIdIndex = Integer.parseInt(properties.getProperty("EventIdIndex", "-1").trim()); // ZMD=1, ZMQ=2

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DLMS ZMD, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            ObisCodeMapper ocm = new ObisCodeMapper(getCosemObjectFactory(), getMeterConfig(), this);
            return ocm.getRegisterValue(obisCode);
        } catch (NestedIOException e) {
            if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                throw e;    // In case of a connection exception (of which we cannot recover), do throw the error.
            }
            String msg = "Problems while reading register " + obisCode.toString() + ": " + e.getMessage();
            getLogger().log(Level.WARNING, msg);
            throw new NoSuchRegisterException(msg);
        } catch (Exception e) {
            String msg = "Problems while reading register " + obisCode.toString() + ": " + e.getMessage();
            getLogger().log(Level.WARNING, msg);
            throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
        }
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /**
     * Execute a billing reset on the device. After receiving the 'Demand Reset'
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws java.io.IOException
     */
    public void resetDemand() throws IOException {
        GenericInvoke gi = new GenericInvoke(this, new ObjectReference(getMeterConfig().getObject(new DLMSObis(ObisCode.fromString("0.0.240.1.0.255").getLN(), (short) 10100, (short) 0)).getBaseName()), 6);
        gi.invoke(new Integer8(0).getBEREncodedByteArray());
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }
}
