/*
 * Metcom.java
 *
 * Created on 8 april 2003, 16:37
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.Software7E1InputStream;
import com.energyict.protocolimpl.iec1107.Software7E1OutputStream;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;
import com.energyict.protocolimpl.siemens7ED62.SCTMRegister;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 *         <B>Changes :</B><BR>
 *         KV 08042003 Initial version.<BR>
 *         KV 17032004 Add HalfDuplex support
 *         KV 18032004 add ChannelMap
 *         KV 13122004 test for password == null
 *         GN 03042008 Added the MSYNC
 *         GN 17122008	Added timeSetMethod 3
 */
public abstract class Metcom extends PluggableMeterProtocol implements HalfDuplexEnabler {


    private static final int DEBUG = 0;
    private boolean TESTING = false;

    public abstract ProfileData getProfileData(boolean includeEvents) throws IOException;

    public abstract ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException;

    public abstract String getDefaultChannelMap();

    public abstract String buildDefaultChannelMap() throws IOException;

    public abstract int getNumberOfChannels() throws IOException;

    public abstract String getProtocolVersion();

    public abstract String getRegistersInfo(int extendedLogging) throws IOException;

    // init
    private TimeZone timeZone;
    private Logger logger;
    private SiemensSCTM siemensSCTM;

    //validateProperties
    private String strID;
    private String strPassword;
    private String nodeId;
    private int iSCTMTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private long roundTripTime;
    private int iProfileInterval;
    private int iEchoCancelling;
    private String strMeterClass;
    private int halfDuplex;
    private ChannelMap channelMap;
    private int extendedLogging;
    private byte[] logbookReadCommand;
    private HalfDuplexController halfDuplexController = null;
    private boolean removePowerOutageIntervals;
    private int forcedDelay;
    private int intervalStatusBehaviour;
    private int maxDelay = 30;
    private boolean software7E1 = false;

    //SCTMDumpData dumpData=null;
    List dumpDatas = null; // of type SCTMDumpData
    private int autoBillingPointNrOfDigits;
    protected int timeSetMethod;

    /**
     * Creates a new instance of Metcom3
     */
    public Metcom() {
    }

    public void connect() throws IOException {
        try {
            siemensSCTM.connectMAC();
            siemensSCTM.sendInit();
            if (strPassword.compareTo(nodeId) != 0) {
                sendPassword(strPassword.getBytes());
            }
        } catch (SiemensSCTMException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }

    }

    public void disconnect() {
        try {
            siemensSCTM.disconnectMAC();
        } catch (SiemensSCTMException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    protected SCTMDumpData getDumpData(int buffer) throws IOException {

        if (buffer < 0) {
            buffer = 0;
        }

        if (dumpDatas == null) {
            dumpDatas = new ArrayList();
        }

        SCTMDumpData dumpData;
        Iterator it = dumpDatas.iterator();
        while (it.hasNext()) {
            dumpData = (SCTMDumpData) it.next();
            if (dumpData.getBufferId() == buffer) {
                return dumpData;
            }
        }

        dumpData = new SCTMDumpData(getClearingData(buffer), buffer);
        dumpDatas.add(dumpData);
        return dumpData;
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public SiemensSCTM getSCTMConnection() {
        return siemensSCTM;
    }

    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    public String getRegister(String name) throws IOException {
        if ("GET_CLOCK_OBJECT".compareTo(name) == 0) {
            throw new NoSuchRegisterException();
        } else {
            return doGetRegister(name);
        }
    }

    private String doGetRegister(String name) throws IOException {
        try {
            byte[] data = siemensSCTM.sendRequest(SiemensSCTM.TABENQ1, name.getBytes());
            return new SCTMRegister(data).toString();
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, " + e.getMessage());
        }
    }

    protected BufferStructure getBufferStructure() throws IOException {
        return getBufferStructure(0);
    }

    protected BufferStructure getBufferStructure(int bufferId) throws IOException {
        try {
            return new BufferStructure(siemensSCTM.sendRequest(SiemensSCTM.TABENQ3, String.valueOf(20 + bufferId + 1).getBytes()));
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, " + e.getMessage());
        }
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    public void setTime() throws IOException {
        Calendar calendar = getTesting() ? JUnitTestCode.getCalendar() : ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MINUTE, 1);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        doSetTime(calendar);
    }

    private void doSetTime(Calendar calendar) throws IOException {
        try {

            // (OUTDATED) If timeSet is method 3, then first readout the Maximum set in seconds, otherwise timecalculation will be incorrect
            // 26/02/2013 - the maximum set should always be read out, cause the device is forcing it! (e.g.: MSYNC timesets > maximum set are silently ignored).
            maxDelay = 30;
            if (!getTesting() && getTimeSetMethod() != 0) {
                byte[] delayRequest = new byte[]{0x37, 0x30, 0x34, 0x30, 0x30};
                byte[] data = siemensSCTM.sendRequest(SiemensSCTM.TABENQ1, delayRequest);
                if (data != null) {
                    try {
                        maxDelay = Integer.parseInt(new String(data).trim());
                    } catch (NumberFormatException e) {
                        getLogger().info("Failed to read out the maximum allowed time set from register 704-00: could not parse value " + new String(data) +" - default value (30 seconds) will be used");
                    }
                }
            }

            SCTMTimeData timeData = new SCTMTimeData(calendar);
            Date systemDate = getTesting() ? JUnitTestCode.getCalendar().getTime() : ProtocolUtils.getCalendar(getTimeZone()).getTime();
            roundTripTime = System.currentTimeMillis();
            Date meterDate = getTesting() ? JUnitTestCode.getMeterTime() : getTime();
            roundTripTime = System.currentTimeMillis() - roundTripTime;

            long timeDifference = Math.abs(systemDate.getTime() - meterDate.getTime());
            if ((getTimeSetMethod() == 0) ||
                    ((getTimeSetMethod() == 1) && (timeDifference > 30000)) ||
                    ((getTimeSetMethod() == 2) && (timeDifference > (maxDelay * 1000)))) {
                getLogger().info("SSYNC timeset method applied.");
                if ((getTimeSetMethod() == 1) && (timeDifference > 30000)) {
                    getLogger().info("MSYNC method is not applied because the timedifference is larger than 30s (" + timeDifference / 1000 + ").");
                }
                if (getTesting()) {
                    JUnitTestCode.sendRequest(0);
                    JUnitTestCode.waitRoutine();
                } else {
                    siemensSCTM.sendRequest(SiemensSCTM.SETTIME, timeData.getSETTIMEData());
                    waitForMinute(calendar);
                    siemensSCTM.sendRequest(SiemensSCTM.SSYNC, null);
                }
            } else if ((getTimeSetMethod() == 1) || (getTimeSetMethod() == 2) || (getTimeSetMethod() == 3) || (getTimeSetMethod() == 4)) {    // the MSYNC method -> not shown in statusBits
                if ((getTimeSetMethod() == 3) && (timeDifference > (maxDelay * 1000))) {
                    getLogger().info("MSYNC method is not applied because the timedifference (" + timeDifference / 1000 + ") is larger than the configured maximum (" + maxDelay + ").");
                    return;
                }

                getLogger().info("MSYNC timeset method " + getTimeSetMethod() + " applied.");
                if (DEBUG == 1) {
                    System.out.println("RoundTripTime: " + roundTripTime);
                }

                calendar.setTime(systemDate);
                if (DEBUG == 1) {
                    System.out.println("Difference = " + timeDifference);
                }

                if (getTesting()) {
                    JUnitTestCode.sendRequest(1);
                    JUnitTestCode.waitRoutine();
                } else {
                    if (meterDate.before(calendar.getTime())) {
                        if (DEBUG == 1) {
                            System.out.println("WaitToAdd ...");
                        }
                        waitForAddition(calendar, meterDate);
                    } else {
                        if (DEBUG == 1) {
                            System.out.println("WaitToSub ...");
                        }
                        waitForSubstraction(calendar, meterDate);
                    }
                    siemensSCTM.sendRequest(SiemensSCTM.MSYNC, null);
                }

                if (DEBUG == 1) {
                    System.out.println("MeterTime: " + getTime().toString());
                }
                if (DEBUG == 1) {
                    System.out.println("SystemTime: " + Calendar.getInstance().getTime().toString());
                }
            }
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, doSetTime, SiemensSCTMException, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Siemens7ED2, doSetTime, IOException, " + e.getMessage());
        }
    }

    private void waitForSubstraction(Calendar calendar, Date meterDate) throws NestedIOException {
        Calendar meterCal = getTesting() ? JUnitTestCode.getCalendar() : Calendar.getInstance(getTimeZone());
        meterCal.setTime(meterDate);
        int offSet = 28; //29-1 ; the meter doesn't show his milliseconds, can cause addition when we want subtraction
        int meterSeconds = meterCal.get(Calendar.SECOND);
        long delay;

        if ((meterCal.getTimeInMillis() - calendar.getTimeInMillis()) < maxDelay * 1000) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            // this should set the meter to the system time
            delay = (59000 - calendar.get(Calendar.SECOND) * 1000 + calendar.get(Calendar.MILLISECOND)) - roundTripTime;
        }

//    	else if (meterSeconds >= 29){
//    		delay = ((59 - meterSeconds + offSet) * 1000) - roundTripTime;
//    	}
//
//    	else{
//    		delay = ((offSet - meterCal.get(Calendar.SECOND)) * 1000) - roundTripTime;
//    	}

        else {
            // we do minus 2000 to be sure we do not ADD!
            delay = (59000 - maxDelay * 1000 - 2000 + meterSeconds * 1000) - roundTripTime;
        }

        if (DEBUG == 1) {
            System.out.println("SystemTime: " + calendar.getTime().toString() + " ** MeterTime: " + meterDate.toString() + " ** Delay: " + delay);
        }

        waitRoutine(delay);
    }

    private void waitForAddition(Calendar calendar, Date meterDate) throws NestedIOException {
        Calendar meterCal = Calendar.getInstance(getTimeZone());
        meterCal.setTime(meterDate);
        int meterSeconds = meterCal.get(Calendar.SECOND);
        long delay;
        if (DEBUG == 1) {
            System.out.println(calendar.getTime() + " " + meterCal.getTime());
        }
        if (DEBUG == 1) {
            System.out.println("cal-meter: " + (calendar.getTimeInMillis() - meterCal.getTimeInMillis()));
        }
        if (DEBUG == 1) {
            System.out.println("MaxDelay: " + maxDelay * 1000);
        }
        if ((calendar.getTimeInMillis() - meterCal.getTimeInMillis()) < maxDelay * 1000) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            // this should set the meter to the system time
            delay = (59000 - calendar.get(Calendar.SECOND) * 1000 + calendar.get(Calendar.MILLISECOND)) - roundTripTime;
        }

//		else if (meterSeconds >= 29){
//			delay = ((59 - meterSeconds + 30)*1000);
//		}
//
//		else{
//			delay = ((30 - meterCal.get(Calendar.SECOND)) * 1000) - roundTripTime;
//		}

        else {
            delay = (59000 - maxDelay * 1000 - meterSeconds * 1000) - roundTripTime;
            if (delay < 0) {
                delay += 60;
            }
        }

        if (DEBUG == 1) {
            System.out.println("SystemTime: " + calendar.getTime().toString() + " ** MeterTime: " + meterDate.toString() + " ** Delay: " + delay);
        }
        waitRoutine(delay);
    }

    private void waitForMinute(Calendar calendar) throws IOException {
        int iDelay = ((59 - calendar.get(Calendar.SECOND)) * 1000) - iRoundtripCorrection;
        waitRoutine(iDelay);
    }

    private void waitRoutine(long delay) throws NestedIOException {
        int waiting = (maxDelay < 10000) ? maxDelay : 10000;
        while (delay > 0) {
            try {
                if (DEBUG == 1) {
                    System.out.println(new Date(System.currentTimeMillis()));
                }
                if ((delay + roundTripTime) < waiting) {
                    Thread.sleep(delay);
                    if (DEBUG == 1) {
                        System.out.println(new Date(System.currentTimeMillis()));
                    }
                    break;
                } else {
                    Thread.sleep(waiting);
                    if (DEBUG == 1) {
                        System.out.println(new Date(System.currentTimeMillis()));
                    }
                    long elapsedTime = System.currentTimeMillis();
                    if (getTesting()) {
                        JUnitTestCode.sendInit();
                    } else {
                        siemensSCTM.sendInit();
                    }
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    delay -= (waiting + elapsedTime);
                    if (delay <= 0) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (SiemensSCTMException e) {
                throw new NestedIOException(e);
            }
        } // while(true)
    }

    protected byte[] getClearingData(int buffer) throws IOException {
        try {
            byte[] data = siemensSCTM.sendRequest(SiemensSCTM.TABENQ3, new byte[]{'1', (byte) (0x31 + buffer)}); //siemensSCTM.CLEARINGDATA);

            if (DEBUG >= 1) {
                System.out.println("KV_DEBUG> " + new String(data));
            }

            return data;
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, " + e.getMessage());
        }
    }

    public void sendPassword(byte[] password) throws IOException {
        try {
            byte[] data = siemensSCTM.sendRequest(SiemensSCTM.PASSWORD, password);
            if (data != null) {
                String retVal = new String(data);
                if (retVal.compareTo("11111111") == 0) {
                    throw new IOException("Password verification failed. Password probably wrong!");
                } else if (retVal.compareTo("33333333") == 0) {
                    throw new IOException("Wrong password level for this type of access! Password probably wrong!");
                }
            } else {
                getLogger().log(Level.INFO, "Meter returned no data after Password");
            }
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, " + e.getMessage());
        }
    }

    public Date getTime() throws IOException {
        try {
            byte[] data = siemensSCTM.sendRequest(SiemensSCTM.TABENQ3, SiemensSCTM.DATETIME);

            String retVal = new String(data);
            if ((data.length == 8) && (retVal.compareTo("33333333") == 0)) {
                throw new IOException("Probably wrong password for Time&Date request! Password probably wrong!");
            }

            long date = new SCTMTimeData(data).getDate(getTimeZone()).getTime() - iRoundtripCorrection;
            return new Date(date);
        } catch (SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, " + e.getMessage());
        }
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            if (strPassword == null) {
                throw new MissingPropertyException("password key is missing!");
            }
            if ((strPassword.length() != 5) && (strPassword.length() != 8)) {
                throw new InvalidPropertyException("Password (SCTM ID) must have a length of 5 or 8!");
            }
            nodeId = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
            if (nodeId == null) {
                nodeId = strPassword;
            }


            iSCTMTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "2").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "900").trim()); // configured profile interval in seconds
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            strMeterClass = properties.getProperty("MeterClass", "20");
            halfDuplex = Integer.parseInt(properties.getProperty("HalfDuplex", "0").trim());
            removePowerOutageIntervals = Integer.parseInt(properties.getProperty("RemovePowerOutageIntervals", "0").trim()) == 1;
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay", "100"));
            setIntervalStatusBehaviour(Integer.parseInt(properties.getProperty("IntervalStatusBehaviour", "0")));

            if (properties.getProperty("ChannelMap") == null) {
                if (getDefaultChannelMap() == null) {
                    channelMap = null;
                } else {
                    channelMap = new ChannelMap(getDefaultChannelMap());
                }
            } else {
                channelMap = new ChannelMap(properties.getProperty("ChannelMap"));
            }

            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());

            if (properties.getProperty("LogBookReadCommand", "E4").compareTo("E6") == 0) {
                logbookReadCommand = SiemensSCTM.BUFENQ2; // E6
            } else {
                logbookReadCommand = SiemensSCTM.BUFENQ1; // E4
            }

            setAutoBillingPointNrOfDigits(Integer.parseInt(properties.getProperty("AutoBillingPointNrOfDigits", "1")));

            timeSetMethod = Integer.parseInt(properties.getProperty("TimeSetMethod", "0").trim());

            software7E1 = !"0".equalsIgnoreCase(properties.getProperty("Software7E1", "0"));

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    @Override
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        // lazy initializing
        dumpDatas = null;

        try {
            // KV 16022004 STA changed from 0x6X to 0x3X
            siemensSCTM = new SiemensSCTM(
                    isSoftware7E1() ? new Software7E1InputStream(inputStream) : inputStream,
                    isSoftware7E1() ? new Software7E1OutputStream(outputStream) : outputStream,
                    iSCTMTimeoutProperty,
                    iProtocolRetriesProperty,
                    strPassword,
                    nodeId,
                    iEchoCancelling,
                    halfDuplex != 0 ? halfDuplexController : null,
                    forcedDelay
            );
        } catch (SiemensSCTMException e) {
            logger.severe("SiemensSCTM: init(...), " + e.getMessage());
        }
    }

    public void initializeDevice() throws IOException {
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    // implement HalfDuplexEnabler
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        this.halfDuplexController.setDelay(halfDuplex);

        if (siemensSCTM != null) {
            siemensSCTM.setHalfDuplexController(halfDuplex != 0 ? this.halfDuplexController : null);
        }
    }

    /**
     * Getter for property channelMap.
     *
     * @return Value of property channelMap.
     */
    public com.energyict.protocolimpl.metcom.ChannelMap getChannelMap() throws IOException {
        if (channelMap == null) {
            channelMap = new ChannelMap(buildDefaultChannelMap());
        }

        return channelMap;
    }

    /**
     * Setter for property channelMap.
     *
     * @param channelMap New value of property channelMap.
     */
    public void setChannelMap(com.energyict.protocolimpl.metcom.ChannelMap channelMap) {
        this.channelMap = channelMap;
    }

    /**
     * Getter for property timeZone.
     *
     * @return Value of property timeZone.
     */
    public java.util.TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Getter for property strMeterClass.
     *
     * @return Value of property strMeterClass.
     */
    public java.lang.String getStrMeterClass() {
        return strMeterClass;
    }

    /**
     * Getter for property removePowerOutageIntervals.
     *
     * @return Value of property removePowerOutageIntervals.
     */
    public boolean isRemovePowerOutageIntervals() {
        return removePowerOutageIntervals;
    }

    /**
     * Getter for property logger.
     *
     * @return Value of property logger.
     */
    public java.util.logging.Logger getLogger() {
        return getTesting() ? JUnitTestCode.getLogger() : logger;
    }

    /**
     * Getter for property logbookReadCommand.
     *
     * @return Value of property logbookReadCommand.
     */
    public byte[] getLogbookReadCommand() {
        return this.logbookReadCommand;
    }

    public int getIntervalStatusBehaviour() {
        return intervalStatusBehaviour;
    }

    public void setIntervalStatusBehaviour(int intervalStatusBehaviour) {
        this.intervalStatusBehaviour = intervalStatusBehaviour;
    }

    public int getAutoBillingPointNrOfDigits() {
        return autoBillingPointNrOfDigits;
    }

    public void setAutoBillingPointNrOfDigits(int autoBillingPointNrOfDigits) {
        this.autoBillingPointNrOfDigits = autoBillingPointNrOfDigits;
    }

    public int getTimeSetMethod() {
        return timeSetMethod;
    }

    protected void setTesting(boolean b) {
        this.TESTING = b;
    }

    public boolean getTesting() {
        return TESTING;
    }

    public boolean isSoftware7E1() {
        return software7E1;
    }

}