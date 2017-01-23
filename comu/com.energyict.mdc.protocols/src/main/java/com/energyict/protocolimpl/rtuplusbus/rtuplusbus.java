package com.energyict.protocolimpl.rtuplusbus;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/*
 *  Changes:
 *  KV 19022004 use of RtuPlusSettings
 *              protocolretries = 5 (default)
 *  KV 15032004 removed read of profileinterval from protocol
 */

/**
 * @author fbo
 * @beginchanges KV  |19022004| use of RtuPlusSettings protocolretries = 5 (default)
 * KV  |15032004| removed read of profileinterval from protocol
 * FBL |14042005| Fix for the missing values:
 * ||
 * || There was a small bug: when a timeout occurs the protocol does not
 * || register the new interval.  And probably such a timeout occurs more often
 * || in a multi-drop setup.  This resulted in missing intervals.
 * ||
 * ||
 * || Extra checking:
 * ||
 * || 1) The sender id of the message is used for matching request and response.
 * ||
 * || 2) Before an interval is added, it has to be checked if it is,
 * || actually the next interval.  It can be that the pointer in the meter
 * || did not move over, and the previous interval is returned.  If the
 * || date/time is the same as in the previous interval, it is not added.
 * || That will not conflict with dst transitions, but can conflict with
 * || time sets. Take note!
 * ||
 * || 3) When an exception occurs during communication the protocol now waits
 * || for the meter to stop sending (for 10 seconds).  This to avoid the
 * || protocol to go out of sync.
 * ||
 * || 4) The reading of registers now works differently for profile data
 * || and none profile data.  The exception and retry handling has to be
 * || different for the NEXT/SAME mechanism to work.
 * ||
 * || and a small bug fix for for displaying name of meter.
 * ||
 * || NodeId is no longer used, now use NodeAddress.
 * KV|17062005|Extend for the HalfDuplex mechanism
 * FBO|27062005|Fix error handling: bug for very old meters
 * FBO|27062005|Fix endless profile fetch loop
 * FBO|27062005|Fix bug in error handling (waitForSilence needs to pause before polling )
 * FBO|28022007|Added extra checks before setting time.  The time may not be set
 * over interval boundaries or within 20 s of an interval boundary.  Normally
 * this is checked by ProtocolReaderBase, but not in older (6.9.x) versions of
 * ComServer.
 * @endchanges
 */


public class rtuplusbus extends PluggableMeterProtocol implements HalfDuplexEnabler {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU RtuPlusBus";
    }

    private static final int DEBUG = 0;

    private RtuPlusBusFrames RtuPlusBusFrame;
    private static final byte CMD_READ_CLOCK = 'J';
    private static final byte CMD_FORCED_WRITE_CLOCK = 'd';   // Force the clock with this command!
    private static final byte CMD_LOGON = 'V';
    private static final byte CMD_LOGOFF = 'U'; // 0x55
    private static final byte CMD_LISTOFCHANNELS = 'b';
    private static final byte CMD_READ_FIRMWAREVERSION = 'i';
    private static final byte CMD_READ_RTU_PARAMETERS = 'K'; // 0x4B
    private static final byte CMD_READ_LAST_RECORD = 'S'; // 83 0x53
    public static final byte CMD_READ_NEXT_RECORD = 'c'; // 99 0x63
    public static final byte CMD_READ_SAME_RECORD = 'e'; // 101 0x65
    private static final byte CMD_READ_LOGBOOK = 'L';


    Logger logger = null;
    TimeZone timeZone = null;
    boolean connected = false; // to secure the disconnect. When not connected, don't invoke disconnect!

    // The number of Channels can vary from 0 to 32 according to the configuration
    int iListOfChannels[] = null;   // This list contains the Channel numbers
    int iRoundtripCorrection = 0;
    int iMaximumNumberOfRecords = 5500;

    RtuPlusSettings rtuPlusSettings = new RtuPlusSettings();
    int profileInterval;

    int halfDuplex;

    // Time difference in ms between system time and rtu time
    private long rtuTimeDelta[];

    @Inject
    public rtuplusbus(PropertySpecService propertySpecService) {
        super(propertySpecService);
        RtuPlusBusFrame = new RtuPlusBusFrames();
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        RtuPlusBusFrame.setInputStream(inputStream);
        RtuPlusBusFrame.setOutputStream(outputStream);
        RtuPlusBusFrame.setLogger(logger);
    }

    //
    //  P  R  O  P  E  R  T  I  E  S     AND     K  E  Y  S
    //


    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        long llPassword;
        int liNodeID;
        int liProtocolTimeoutProperty;
        int liProtocolRetriesProperty;
        int liDelayAfterFailProperty;
        int liRtuPlusBusProtocolVersion;

        Iterator iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (properties.getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }

        try {
            halfDuplex = Integer.parseInt(properties.getProperty("HalfDuplex", "0").trim());
            if (halfDuplex > 0) {
                RtuPlusBusFrame.setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay", "150").trim()));
            } else {
                RtuPlusBusFrame.setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay", "0").trim()));
            }


            // Node ID or Address
            liNodeID = Integer.parseInt(properties.getProperty(MeterProtocol.NODEID));
            if (liNodeID >= 255 || liNodeID < 3) {
                throw new MissingPropertyException("NodeID for the RtuPlusBus Protocol must be >= 3 and <= 255.  Value is now: " + liNodeID);
            }
            // The Password is an unsigned 32 bits integer
            llPassword = Long.parseLong(properties.getProperty(MeterProtocol.PASSWORD));
            if (llPassword <= 0 || llPassword > 0x7FFFFFFF) {
                throw new MissingPropertyException("Password must be a positive number between 0 and " + 0x7FFFFFFF);
            }
        } catch (NumberFormatException e) {
            throw new MissingPropertyException("Password and/or Node Address might be wrong or empty!, " + e.toString());
        }

        // Other Communication settings
        // KV 03062003 changed
        liProtocolTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "4500").trim()); // was 3000
        liProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim()); // was 2

        liDelayAfterFailProperty = Integer.parseInt(properties.getProperty("DelayAfterFail", "3000").trim());
        liRtuPlusBusProtocolVersion = Integer.parseInt(properties.getProperty("RtuPlusBusProtocolVersion", "2").trim());
        iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "350").trim());
        iMaximumNumberOfRecords = Integer.parseInt(properties.getProperty("MaximumNumberOfRecords", "5500").trim());
        profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "900").trim());

        // Set all Properties now..
        RtuPlusBusFrame.setProtocolProperties(liProtocolTimeoutProperty, liProtocolRetriesProperty, liDelayAfterFailProperty, liRtuPlusBusProtocolVersion);
        RtuPlusBusFrame.setPassword(llPassword);
        RtuPlusBusFrame.setNodeID(liNodeID);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "DelayAfterFail",
                    "RtuPlusBusProtocolVersion",
                    "MaximumNumberOfRecords",
                    "HalfDuplex",
                    "ForcedDelay");
    }


    //
    //  C  O  N  N  E  C  T     A N D    D  I  S  C  O  N  N  E  C  T
    //
    public void connect() throws IOException {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        }

        try {
            doRtuLogon();
            connected = true;
        } catch (RtuPlusBusException e) {
            throw new NestedIOException(e, "RtuPlusBus" + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connected) {
                RtuPlusBusFrame.doWrite(CMD_LOGOFF);
                RtuPlusBusFrame.doAbort();
            }
        } catch (RtuPlusBusException e) {
            logger.warning(e.getMessage());
        }
    }


    public void doRtuLogon() throws RtuPlusBusException {
        int[] liReceivedData;
        long lTimeOfRTU = 0;

        // Get the RTU-Time (attention: do not call getTime().getTime() as the references are different 1/1/1980 versus 1/1/1970!!
        liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_READ_CLOCK);
        if (liReceivedData != null) {
            lTimeOfRTU = (liReceivedData[3] & 0xFF) << 24;
            lTimeOfRTU += (liReceivedData[2] & 0xFF) << 16;
            lTimeOfRTU += (liReceivedData[1] & 0xFF) << 8;
            lTimeOfRTU += (liReceivedData[0] & 0xFF);
        }

        // Time well received and ..
        if (lTimeOfRTU > 0) {
            liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_LOGON, RtuPlusBusFrame.doScramblePassword(lTimeOfRTU));
            if (liReceivedData != null) {
                if (liReceivedData[0] != 0) { // Logged on!
                    // Eventually also check if name of RTU is equal to ...
                    String params = RtuPlusBusFrame.doWriteAndReadS(CMD_READ_RTU_PARAMETERS);
                    // parse the settings
                    rtuPlusSettings.parse(params.getBytes());
                    if (DEBUG >= 1) {
                        System.out.println(rtuPlusSettings.toString());
                    }

                    if (rtuPlusSettings.getName() != null) {
                        logger.info("RTU name: " + rtuPlusSettings.getName());
                    } else {
                        throw new RtuPlusBusException("Could not logon, received RTU settings invalid!");
                    }

                    return;
                }
            }
            throw new RtuPlusBusException("Could not logon.");
        }
    }


    //
    //  T  I  M  E
    //


    public void setTime() throws IOException {

        Date systemTime = new Date();
        Date meterTime = calculatedRtuTime();

        if (isCrossBoundary(systemTime, meterTime, getProfileInterval())) {

            String msg =
                    "time difference too close to (within 20 sec) or crosses the " +
                            "intervalboundary, will try again next communication session ";

            logger.severe(msg);

            return;
        }

        long llTimeInSecsSince1Jan1980;
        long llTimeNowIs;
        int liWriteData[];
        Date lDate = new Date();
        liWriteData = new int[4];

        // First find time in Seconds since 1/1/1980
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(1980, Calendar.JANUARY, 1, 0, 0, 0);
        llTimeNowIs = lDate.getTime();
        llTimeInSecsSince1Jan1980 = (llTimeNowIs - calendar.getTimeInMillis()) / 1000;

        // Write the Time to the RTU
        liWriteData[3] = (int) ((llTimeInSecsSince1Jan1980 >> 24) & 0xFF);
        liWriteData[2] = (int) ((llTimeInSecsSince1Jan1980 >> 16) & 0xFF);
        liWriteData[1] = (int) ((llTimeInSecsSince1Jan1980 >> 8) & 0xFF);
        liWriteData[0] = (int) (llTimeInSecsSince1Jan1980 & 0xFF);

        try {
            RtuPlusBusFrame.doWriteAndReadI(CMD_FORCED_WRITE_CLOCK, liWriteData);
        } catch (RtuPlusBusException e) {
            logger.severe("Could not Set the Time! / " + e.getMessage());
            String msg = "RtuPlusBus, SetTime(): " + e.getMessage();
            throw new NestedIOException(e, msg);
        }

    }

    public Date getTime() throws IOException {
        long lTimeOfRTU = 0;
        int[] liReceivedData;

        try { // First get the RTU Time ..
            liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_READ_CLOCK);
            if (liReceivedData != null) {

                lTimeOfRTU = (liReceivedData[3] & 0xFF) << 24;
                lTimeOfRTU += (liReceivedData[2] & 0xFF) << 16;
                lTimeOfRTU += (liReceivedData[1] & 0xFF) << 8;
                lTimeOfRTU += (liReceivedData[0] & 0xFF);

                // Time of RTU succesfully received ..
                // Calculate the difference with the Host time ..
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.clear();
                calendar.set(1980, Calendar.JANUARY, 1, 0, 0, 0);
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (lTimeOfRTU * 1000));

                Date rslt = new Date(calendar.getTime().getTime());

                rtuTimeDelta = new long[]{System.currentTimeMillis() - rslt.getTime()};

                return rslt;

            } else {
                throw new IOException("Did not receive any data");
            }

        } catch (RtuPlusBusException e) {
            logger.severe("Could not Read the Time!");
            throw new NestedIOException(e);
        }

    }

    /**
     * Calculate the rtu time using the rtuTimeDelta.
     * <p/>
     * If the rtu time has not been fetched before (=> rtuTimeDelta == null)
     * then fetch the date.
     * <p/>
     * If the rtu time has been fetched before, then calculate the current
     * time in the rtu using the time delta.
     *
     * @return date of the rtu
     */
    private Date calculatedRtuTime() throws IOException {
        if (rtuTimeDelta == null) {
            return getTime();
        }
        return new Date(System.currentTimeMillis() - rtuTimeDelta[0]);
    }

    /**
     * This check is run before a setTime():
     * <p/>
     * meterTime and systemTime must lay in same interval
     * meterTime and systemTime must be 20s before interval boundary
     * <p/>
     * Since older versions of ProtocolReader did not implement this, and
     * rtuplusbus needs to work with these older versions.
     * (Same method as in ProtocolReaderBase. copy/pasted)
     */
    private boolean isCrossBoundary(
            Date systemTime, Date meterTime, int iProfileInterval) {

        final int safetyMargin = 20;

        if (((meterTime.getTime() / 1000) / iProfileInterval) ==
                ((systemTime.getTime() / 1000) / iProfileInterval)) {
            if ((((meterTime.getTime() / 1000) % iProfileInterval) > safetyMargin) &&
                    (((meterTime.getTime() / 1000) % iProfileInterval) < (iProfileInterval - safetyMargin)) &&
                    (((systemTime.getTime() / 1000) % iProfileInterval) > safetyMargin) &&
                    (((systemTime.getTime() / 1000) % iProfileInterval) < (iProfileInterval - safetyMargin))) {
                return false;
            }
        }

        return true;

    }

    //
    // R  E  G  I  S  T  E  R  S
    //

    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }


    //
    //  M E T E R R E A D I N G S
    //

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }


    //
    //  D E M A N D   V A L U E S
    //


    public int getNumberOfChannels() throws IOException {
        int i, j;
        int liNbrOfChannels = 0;
        int liReceivedData[];
        //liReceivedData = new int[256];     // Received data from RTU
        //iListOfChannels = new int[32];     // List of Channels .. call


        if (DEBUG >= 1) {
            System.out.println("CMD_LISTOFCHANNELS");
        }

        // CMD_LISTOFCHANNELS
        try {
            liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_LISTOFCHANNELS);
            if (liReceivedData != null) { // Frame contains DateTime (4bytes) followed by Status and then
                // 32 integers of 2 bytes containing a non zero value if that channel will
                // return a profile
                for (i = 0; i < 32; i++) {
                    if ((liReceivedData[(4 + 1 + 2 * i)] * 256 + liReceivedData[(4 + 1 + 2 * i + 1)]) > 0) {
                        liNbrOfChannels++;
                    }
                }

                // Instantiate the list for the exact number of Channels..
                // Now we know how much channels we have
                j = 0;
                iListOfChannels = new int[liNbrOfChannels];
                for (i = 0; i < 32; i++) {
                    if ((liReceivedData[(4 + 1 + 2 * i)] * 256 + liReceivedData[(4 + 1 + 2 * i + 1)]) > 0) {
                        iListOfChannels[j++] = i;
                    }
                }
            }

            if (liNbrOfChannels < 1) { // No Profiles availlable
                logger.severe("No Profiles available (no Channels configured for logging).");
                throw new UnsupportedException("No Profiles available");
            }
        } catch (RtuPlusBusException e) {
            throw new NestedIOException(e, "Reading list of available channels. " + e.getMessage());
        }

        return liNbrOfChannels;
    }

    public int getProfileInterval() throws IOException { // Read it from the RTU Structure!!
//        if (rtuPlusSettings.getProfileInterval() != -1)
//           return rtuPlusSettings.getProfileInterval();
//        else
//           return 900;
        return profileInterval;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException { // Full Read
        return (getProfileData(new Date(0), includeEvents));
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException();
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        // Partial Read
        int i;
        int liTariffCode;
        int liNumberOfIntervalsRetrieved;
        int[] liReceivedData;
        long lTimeOfRecord, calTimeOfRecord;
        long calTimeOfLastReadingRecord = lastReading.getTime() / 1000;
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();

        Number[] channelValues;

        // Check the number of Channels and fill list with available channels
        getNumberOfChannels();
        channelValues = new Number[iListOfChannels.length];
        ProfileData profileData = new ProfileData();
        //logger.info( "The following Channels provide Profiles:" );
        for (i = 0; i < iListOfChannels.length; i++) {  //logger.info( "CHN" + iListOfChannels[i] );
            profileData.addChannel(new ChannelInfo(i, iListOfChannels[i], ("RtuPlus.CHN" + iListOfChannels[i]), Unit.get(BaseUnit.COUNT)));
        }

        if (DEBUG >= 1) {
            System.out.println("CMD_READ_LAST_RECORD");
        }
        // channelValues[ iListOfChannels.length ] = Long.parseLong();
        // Get the Interavals ..
        //logger.info( "Reading last Record" );
        try {
            liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_READ_LAST_RECORD);
        } catch (RtuPlusBusException e) {
            throw new IOException("Reading Last Record: " + e.getMessage());
        }

        calTimeOfRecord = calTimeOfLastReadingRecord;              // Stop if LastRecord found..
        liNumberOfIntervalsRetrieved = iMaximumNumberOfRecords;    // Stop after this number of records.. don't turn around for ever.


        while ((calTimeOfRecord >= calTimeOfLastReadingRecord) && (liNumberOfIntervalsRetrieved > 0)) {

            // Extract and Convert the Time for that Record to Calendar..
            // In first positition (at 0) we have the Status Code
            // Positions 1,2,3,4 are the bytes of the Serial Time ..
            lTimeOfRecord = (liReceivedData[4] & 0xFF) << 24;
            lTimeOfRecord += (liReceivedData[3] & 0xFF) << 16;
            lTimeOfRecord += (liReceivedData[2] & 0xFF) << 8;
            lTimeOfRecord += (liReceivedData[1] & 0xFF);
            calendar.set(1980, Calendar.JANUARY, 1, 0, 0, 0);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (lTimeOfRecord * 1000));
            calTimeOfRecord = calendar.getTime().getTime() / 1000;
            if (DEBUG >= 1) {
                System.out.println("calTimeOfRecord " + calendar.getTime());
            }
            // Extract the Tariff Code
            liTariffCode = liReceivedData[0] & 0xFF;

            // Extraxt all Channel Values ..
            for (i = 0; i < iListOfChannels.length; i++) {
                //channelValues[ i ] = new Long( (liReceivedData[ (4 + 1 + 2 * i) ] * 256 + liReceivedData[ (4 + 1 + 2 * i + 1) ] ) );
                // KV 17072003 bugfix LE ipv BE
                channelValues[i] = new Long((liReceivedData[(4 + 1 + 2 * i)] + liReceivedData[(4 + 1 + 2 * i + 1)] * 256));
            }

            IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()), 0, 0, liTariffCode);
            intervalData.addValues(channelValues);
            profileData.addInterval(intervalData);

            // Read next Record ..
            //logger.info( "Reading next Record" );
            try {
                if (DEBUG >= 1) {
                    System.out.println("CMD_READ_NEXT_RECORD");
                }
                liNumberOfIntervalsRetrieved--;
                liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_READ_NEXT_RECORD);
            } catch (RtuPlusBusException e) {
                if (DEBUG >= 1) {
                    System.out.println("CMD_READ_NEXT_RECORD FAILED " + e + e.getReason());
                }
                throw new NestedIOException(e);
            }

        }


        // Always include Event Buffers.
        // Read the Logbook
        try {
            logger.info("Reading the Logbook");
            liReceivedData = RtuPlusBusFrame.doWriteAndReadI(CMD_READ_LOGBOOK);
            RtuPlusBusLogbook lLogbook;
            lLogbook = new RtuPlusBusLogbook(this.timeZone);
            lLogbook.parseLogbook(liReceivedData, profileData);
            // KV 11062003
            profileData.applyEvents(getProfileInterval() / 60);
        } catch (RtuPlusBusException e) {
            logger.severe("Error while Reading the Logbook: " + e.getMessage());
            throw new NestedIOException(e, "RtuPlusBus, Reading the Logbook: " + e.getMessage());
        }


        // Apply the events to the channel statusvalues
        // profileData.applyEvents(bInterval);

        return profileData;
    }

    //
    //  M  I  S  C  E  L  E  A  N  E  O  U  S
    //

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    public void stop() {
        if (RtuPlusBusFrame != null) {
            RtuPlusBusFrame.doAbort();
        }
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    public void release() throws IOException {
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        if (halfDuplex > 0) {
            halfDuplexController.setDelay(halfDuplex);
            RtuPlusBusFrame.setHalfDuplexController(halfDuplexController);
        }
    }

    public int getIRoundtripCorrection() {
        return iRoundtripCorrection;
    }

    public static String cmdToString(byte cmd) {
        switch (cmd) {
            case 'J':
                return "CMD_READ_CLOCK";
            case 'd':
                return "CMD_FORCED_WRITE_CLOCK";
            case 'V':
                return "CMD_LOGON";
            case 'U':
                return "CMD_LOGOFF";
            case 'b':
                return "CMD_LISTOFCHANNELS";
            case 'i':
                return "CMD_READ_FIRMWAREVERSION";
            case 'K':
                return "CMD_READ_RTU_PARAMETERS";
            case 'S':
                return "CMD_READ_LAST_RECORD";
            case 'c':
                return "CMD_READ_NEXT_RECORD";
            case 'e':
                return "CMD_READ_SAME_RECORD";
            case 'L':
                return "CMD_READ_LOGBOOK";
            default:
                return "unknown";
        }
    }
}  // End of RtuPlusBus class

