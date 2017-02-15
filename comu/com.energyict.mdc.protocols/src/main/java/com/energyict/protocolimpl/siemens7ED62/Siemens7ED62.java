/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Siemens7ED62.java
 *
 * Created on 24 januari 2003, 14:43
 * <B>Description :</B><BR>
 * Class that implements the Siemens7ED62 SCTM protocol version of the meter.
 * <BR>
 */

package com.energyict.protocolimpl.siemens7ED62;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.Software7E1InputStream;
import com.energyict.protocolimpl.iec1107.Software7E1OutputStream;
import com.energyict.protocolimpl.sctm.base.GenericRegisters;
import com.google.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

// KV 06092005 WVEM
/**
 *
 * @author  Koen
 * @beginchanges
KV|07032005|changes for setTime and use of 8 character SCTM ID
KV|23032005|Changed header to be compatible with protocol version tool
KV|12012006|correct time set handling (add intervals)
KV|24012006|Avoid timeset too close to interval boundary
KV|16032006|Add ChannelMap to expose nr of channels
GN|03042008|Added the MSYNC
 * @endchanges
 */
public class Siemens7ED62 implements MeterProtocol, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Siemens 7ED62 SCTM";
    }

    private final PropertySpecService propertySpecService;
    // init
    private TimeZone timeZone;
    private Logger logger;
    private SiemensSCTM siemensSCTM;

    //validateProperties
    private String strID;
    private int iSCTMTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iProfileInterval;
    private int iEchoCancelling;
    private String strMeterClass;
    private String nodeId;
    private int forcedDelay;

    private static final String[]  meterReadingsC1 = {"1-1:1.8.1","1-1:1.8.2","1-1:1.8.3","1-1:1.8.4","1-1:5.8.1","1-1:5.8.2"};
    private static final String[]  meterReadingsC05 = {"181","182","183","184","581","582"};
    private static final String[]  meterReadingsCxx = {"8.1","8.2","8.3","8.4","8.1","8.2"}; // KV at KP 27032003

    private int nrOfChannels;
    private String[] meterReadings=null;

    SCTMDumpData dumpData=null;
    private boolean removePowerOutageIntervals;

    GenericRegisters genericRegisters; // KV 06092005 WVEM
	private int timeSetMethod;
	private long roundTripTime;
    private int DEBUG = 0;
    private boolean software7E1;

    @Inject
    public Siemens7ED62(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    /**
     * @throws IOException  */
    public void connect() throws IOException {
       try
       {
          siemensSCTM.connectMAC();
          siemensSCTM.flag(strID);
          siemensSCTM.sendInit();
       }
       catch(SiemensSCTMException e)
       {
          throw new IOException(e.getMessage());
       }
    }

    public void disconnect() {
       try
       {
          siemensSCTM.disconnectMAC();
       }
       catch(SiemensSCTMException e)
       {
          logger.severe("disconnect() error, "+e.getMessage());
       }
    }

    private SCTMDumpData getDumpData() throws IOException {
        if (dumpData == null)
            dumpData = new SCTMDumpData(siemensSCTM.getDumpData(),0);
        return dumpData;
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        if (channelId > meterReadings.length) throw new IOException("Siemens7ED62, getMeterReading, invalid channelId");
        return getDumpData().getRegister(meterReadings[channelId]);
    }
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return getDumpData().getRegister(name);
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return nrOfChannels;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(timeZone);
        calendarFrom.add(Calendar.YEAR,-10);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(timeZone);
        calendarFrom.setTime(lastReading);
        return doGetProfileData(calendarFrom,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        Calendar calendarFrom=ProtocolUtils.getCleanCalendar(timeZone);
        calendarFrom.setTime(from);
        Calendar calendarTo=ProtocolUtils.getCleanCalendar(timeZone);
        calendarTo.setTime(to);
        return doGetProfileData(calendarFrom,calendarTo,includeEvents);
    }

    private ProfileData doGetProfileData(Calendar calendarFrom, Calendar calendarTo, boolean includeEvents) throws IOException {
       try {
           ProfileData profileData=null;
           SCTMTimeData from = new SCTMTimeData(calendarFrom);
           SCTMTimeData to = new SCTMTimeData(calendarTo);

           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           baos.write(SiemensSCTM.PERIODICBUFFERS);
           baos.write(from.getBUFENQData());
           baos.write(to.getBUFENQData());
           byte[] data = siemensSCTM.sendRequest(SiemensSCTM.BUFENQ2, baos.toByteArray());
           SCTMProfileSingleBuffer7ED62 sctmp = new SCTMProfileSingleBuffer7ED62(data);
           profileData = sctmp.getProfileData(getProfileInterval(),timeZone, getNumberOfChannels(), -1, removePowerOutageIntervals);
           if (includeEvents) {
               GetEvents(calendarFrom,profileData);
               // Apply the events to the channel statusvalues
               profileData.applyEvents(getProfileInterval()/60);
           }
           return profileData;
       }
       catch(SiemensSCTMException e) {
          throw new IOException("Siemens7ED62, doGetProfileData, SiemensSCTMException, "+e.getMessage());
       }
    }

    private void GetEvents(Calendar calendar,ProfileData profileData) throws IOException {
       SCTMTimeData from = new SCTMTimeData(calendar);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       baos.write(SiemensSCTM.SPONTANEOUSBUFFERS);
       baos.write(from.getBUFENQData());
       List sctmEvents = doGetEvents(SiemensSCTM.BUFENQ1,baos.toByteArray());
       addToProfile(sctmEvents,profileData);
    }

    private void addToProfile(List sctmEvents,ProfileData profileData) {

        Iterator iterator = sctmEvents.iterator();
        while(iterator.hasNext()) {
           SCTMEvent sctmEvent = (SCTMEvent)iterator.next();

           switch(sctmEvent.getType()) {
               case 0xA1:
               case 0xA2:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType()));
                    break;

               case 0xC1:
               case 0xC2:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CONFIGURATIONCHANGE,sctmEvent.getType()));
                    break;

               case 0xA3:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERDOWN,sctmEvent.getType()));
                    profileData.addEvent(new MeterEvent(sctmEvent.getTo().getDate(timeZone),MeterEvent.POWERUP,sctmEvent.getType()));
                    break;

               case 0xD1:
               case 0xD2:
               case 0xD3:
               case 0xD4:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.SETCLOCK_BEFORE,sctmEvent.getType()));
                    profileData.addEvent(new MeterEvent(sctmEvent.getTo().getDate(timeZone),MeterEvent.SETCLOCK_AFTER,sctmEvent.getType()));
                    break;

               default:
                    profileData.addEvent(new MeterEvent(new Date(),MeterEvent.OTHER,sctmEvent.getType()));
                    break;

           } // switch(sctmEvent.type)
        }
    }

    private List doGetEvents(byte[] command, byte[] data) throws IOException {
        try {
           List sctmEvents = new ArrayList();
           byte[] received;

           while(true) {
               received = siemensSCTM.sendRequest(command, data);
               if (received == null) break;
               SCTMEvent sctmEvent = new SCTMEvent(received);
               sctmEvents.add(sctmEvent);
               command = SiemensSCTM.NEXT;
               data = SiemensSCTM.SPONTANEOUSBUFFERS;
           }

           return sctmEvents;
        }
        catch(SiemensSCTMException e) {
          throw new IOException("Siemens7ED62, doGetEvents, SiemensSCTMException, "+e.getMessage());
        }
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return iProfileInterval;
    }



    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return getDumpData().getRegister(name).getAmount().toString();
    }

    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    public void setTime() throws IOException {
           Calendar calendar=null;
           calendar = ProtocolUtils.getCalendar(timeZone);
           // KV 24012006
           int restMinutes = (getProfileInterval()/60) - (calendar.get(Calendar.MINUTE)%(getProfileInterval()/60));
           if (restMinutes > 1) {
               calendar.add(Calendar.MINUTE,1);
               calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);
               doSetTime(calendar);
           }
           else logger.warning("setTime(), time sync is too close to boundary, will try again next dialin session...");
    }

    private void doSetTime(Calendar calendar) throws IOException {
        try {
             SCTMTimeData timeData = new SCTMTimeData(calendar);
             Date systemDate = ProtocolUtils.getCalendar(getTimeZone()).getTime();
             roundTripTime = System.currentTimeMillis();
             Date meterDate = getTime();
             roundTripTime = System.currentTimeMillis() - roundTripTime;

             if (( getTimeSetMethod() == 0 ) || ((Math.abs(systemDate.getTime()-meterDate.getTime()) > 30000) ) ){
            	 siemensSCTM.sendRequest(siemensSCTM.SETTIME,timeData.getSETTIMEData());
				 waitForMinute(calendar);
				 siemensSCTM.sendRequest(siemensSCTM.SSYNC,null);
             }

             else{	// the MSYNC method -> not shown in statusBits

                 if (DEBUG == 1) logger.info("RoundTripTime: " + roundTripTime);

                 calendar.setTime(systemDate);
            	 logger.info("Difference = " + Math.abs(systemDate.getTime() - meterDate.getTime()) );
                 if ( meterDate.before(calendar.getTime()) ){
                	 if (DEBUG == 1) logger.info("WaitToAdd ...");
                	 waitForAddition(calendar, meterDate);
                 }
                 else{
                	 if (DEBUG == 1) logger.info("WaitToSub ...");
                	 waitForSubstraction(calendar, meterDate);
                 }

                 siemensSCTM.sendRequest(siemensSCTM.MSYNC, null);
                 if(DEBUG == 1)logger.info("MeterTime: " + getTime().toString());
                 if(DEBUG == 1)logger.info("SystemTime: " + Calendar.getInstance().getTime().toString());

             }
        }
        catch(SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, doSetTime, SiemensSCTMException, "+e.getMessage()) ;
        }
        catch(IOException e) {
            throw new IOException("Siemens7ED2, doSetTime, IOException, "+e.getMessage()) ;
        }
    } // private void doSetTime(Calendar calendar)

    private void waitForSubstraction(Calendar calendar, Date meterDate) throws NestedIOException {
    	Calendar meterCal = Calendar.getInstance(getTimeZone());
    	meterCal.setTime(meterDate);
    	int offSet = 28; //29-1 ; the meter doesn't show his milliseconds, can cause addition when we want substraction
    	int meterSeconds = meterCal.get(Calendar.SECOND);
    	long delay = -1;

    	if ( (meterCal.getTimeInMillis() - calendar.getTimeInMillis()) < 29000){
    		calendar.setTimeInMillis(System.currentTimeMillis());
		// this should set the meter to the system time
		delay = ( 59000 - calendar.get(Calendar.SECOND)*1000 + calendar.get(Calendar.MILLISECOND) ) - roundTripTime;
	}

    	else if (meterSeconds >= 29){
    		delay = ((59 - meterSeconds + offSet) * 1000) - roundTripTime;
    	}

    	else{
    		delay = ((offSet - meterCal.get(Calendar.SECOND)) * 1000) - roundTripTime;
    	}

    	if (DEBUG == 1) logger.info("SystemTime: " + calendar.getTime().toString() + " ** MeterTime: " + meterDate.toString() + " ** Delay: " + delay);

    	waitRoutine(delay);
	}

	private void waitForAddition(Calendar calendar, Date meterDate) throws NestedIOException {
    	Calendar meterCal = Calendar.getInstance(getTimeZone());
    	meterCal.setTime(meterDate);
    	int meterSeconds = meterCal.get(Calendar.SECOND);
    	long delay = -1;

    	if ( (calendar.getTimeInMillis() - meterCal.getTimeInMillis()) < 29000){
    		calendar.setTimeInMillis(System.currentTimeMillis());
    		// this should set the meter to the system time
			delay = ( 59000 - calendar.get(Calendar.SECOND)*1000 + calendar.get(Calendar.MILLISECOND) )  - roundTripTime;
    	}

    	else if (meterSeconds >= 29){
    		delay = ((59 - meterSeconds + 30)*1000);
    	}

    	else{
    		delay = ((30 - meterCal.get(Calendar.SECOND)) * 1000) - roundTripTime;
    	}

    	if (DEBUG == 1) logger.info("SystemTime: " + calendar.getTime().toString() + " ** MeterTime: " + meterDate.toString() + " ** Delay: " + delay);
    	waitRoutine(delay);
	}

	private void waitForMinute(Calendar calendar) throws IOException {
        int iDelay = ((59 - calendar.get(Calendar.SECOND))*1000)-iRoundtripCorrection;
        waitRoutine(iDelay);
    } // private void waitForMinute(Calendar calendar)

    private void waitRoutine(long delay) throws NestedIOException {
        while(delay>0) {
            try {
                if ((delay+roundTripTime) < 10000) {
                    Thread.sleep(delay);
                    break;
                }
                else {
                   Thread.sleep(10000);
                   long elapsedTime = System.currentTimeMillis();
                   siemensSCTM.sendInit();
                   elapsedTime = System.currentTimeMillis() - elapsedTime;
                   delay -= (10000+elapsedTime);
                   if (delay <= 0) break;
                }
            }
            catch(InterruptedException e) {
                throw new NestedIOException(e);
            }
            catch(SiemensSCTMException e) {
                throw new NestedIOException(e);
            }
        } // while(true)
	}

    public Date getTime() throws IOException {
        try {
            byte[] data = siemensSCTM.sendRequest(siemensSCTM.TABENQ3,siemensSCTM.DATETIME);
            long date = new SCTMTimeData(data).getDate(timeZone).getTime() - iRoundtripCorrection;
            return new Date(date);
        }
        catch(SiemensSCTMException e) {
            throw new IOException("Siemens7ED2, getTime, SiemensSCTMException, "+e.getMessage());
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException,UnsupportedException {
        throw new UnsupportedException();
    }

        @Override
    public String getVersion() {
        return getProtocolVersion();
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<String> result = new ArrayList<>();
        result.add("Timeout");
        result.add("Retries");
        result.add("EchoCancelling");
        result.add("MeterClass");
        result.add("RemovePowerOutageIntervals");
        result.add("LogBookReadCommand");
        result.add("ForcedDelay");
        result.add("ChannelMap");
        result.add("TimeSetMethod");
        result.add("Software7E1");
        return PropertySpecFactory.toPropertySpecs(result, this.propertySpecService);
    }

    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException
    {
        try {
            Iterator iterator= getRequiredProperties().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            iProfileInterval = Integer.parseInt(properties.getProperty(MeterProtocol.PROFILEINTERVAL,"900").trim()); // configured profile interval in seconds

            iSCTMTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","2").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling","0").trim());
            strMeterClass = properties.getProperty("MeterClass","1");
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            removePowerOutageIntervals = Integer.parseInt(properties.getProperty("RemovePowerOutageIntervals","0").trim())==1?true:false;
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay","100"));
            nrOfChannels = Integer.parseInt(properties.getProperty("ChannelMap", "6"));
            timeSetMethod = Integer.parseInt(properties.getProperty("TimeSetMethod","0").trim());
            software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
        }
        catch (NumberFormatException e) {
           throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }

    private void setMeterReadingRegisters() throws SiemensSCTMException {
        if (strMeterClass.compareTo("1") == 0) {
            meterReadings = meterReadingsC1;
        }
        else if (strMeterClass.compareTo("0.5") == 0) {
            meterReadings = meterReadingsC05;
        }
        else {
            meterReadings = meterReadingsC1;
            throw new SiemensSCTMException("Siemens7ED62, setMeterReadingRegisters, infotype MeterClass invalid value ("+strMeterClass+")");
        }
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try
        {
           siemensSCTM=new SiemensSCTM((software7E1 ? new Software7E1InputStream(inputStream) : inputStream),
                   (software7E1 ? new Software7E1OutputStream(outputStream) : outputStream),
                   iSCTMTimeoutProperty,iProtocolRetriesProperty,null,nodeId,iEchoCancelling,forcedDelay);
           genericRegisters = new GenericRegisters(siemensSCTM); // KV 06092005 WVEM
           setMeterReadingRegisters();

        }
        catch(SiemensSCTMException e)
        {
           logger.severe ("SiemensSCTM: init(...), "+e.getMessage());
        }
    }

    public void initializeDevice() throws IOException, UnsupportedException {
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
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

	public int getTimeSetMethod() {
		return timeSetMethod;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

    /**
     * Getter for property removePowerOutageIntervals.
     * @return Value of property removePowerOutageIntervals.
     */
    public boolean isRemovePowerOutageIntervals() {
        return removePowerOutageIntervals;
    }

    // KV 06092005 WVEM
    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
    *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (genericRegisters.isManufacturerSpecific(obisCode)) {
            return genericRegisters.readRegisterValue(obisCode);
        } else {
            String name;
            Date toTime = null;

        // *********************************************************************************
            // Billing counter
            if ((obisCode.toString().indexOf("1.0.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.1.0.1.0.255") != -1)) {
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(getDumpData().getBillingCounter()), Unit.getUndefined()));
            }

            // Billing point timestamp
            if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) && obisCode.getF() != 255) {
                Date billingDate = getBillingDate(obisCode);
                if (billingDate != null) {
                    return new RegisterValue(obisCode, billingDate);
                } else {
                    throw new NoSuchRegisterException("Could not parse the billing date for billing point " + obisCode.getF());
                }
            }

            if (strMeterClass.compareTo("0.5") == 0) {
                if (obisCode.getF() != 255) {
                    throw new NoSuchRegisterException("Readout of billing registers not yet implemented.");
                }
                name = convertObisCode2ShortCode(obisCode);
            } else {
                name = convertObisCode2Edis(obisCode);
            }

            Quantity q = getDumpData().getRegister(name);
            if (q == null) {
                throw new NoSuchRegisterException("Register with obiscode " + obisCode + " does not exist!");
            }

            if (obisCode.getF() != 255) {
                toTime = getBillingDate(obisCode);
            }

            return new RegisterValue(obisCode, getDumpData().getRegister(name), null, toTime == null ? new Date() : toTime);
        }
    }

    private Date getBillingDate(ObisCode obisCode) throws IOException {
        int length = getDumpData().getBillingCounterLength();
        String billingDate = "1-1:0.1.2" + "*" + ProtocolUtils.buildStringDecimal((dumpData.getBillingCounter() - obisCode.getF()), length);
        BigDecimal amount = getDumpData().getRegister(billingDate).getAmount();
        String dateTime = ProtocolUtils.buildStringDecimal(amount.intValue(), 8);

        try {
            Date date = ProtocolUtils.parseDateTimeWithTimeZone(dateTime, "MMddHHmm", timeZone);
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTime(date);
            cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            return cal.getTime();
        } catch (ParseException e) {
            logger.info("Could not parse the billing date for billing point " + obisCode.getF());
            return null;
        }
    }

   // KV 06092005 WVEM
   private String convertObisCode2Edis(ObisCode obisCode) throws IOException {
       String edis = obisCode.getA() + "-" + obisCode.getB() + ":" + obisCode.getC() + "." + obisCode.getD() + "." + obisCode.getE();
       if (obisCode.getF() != 255) {
           int reversedBillingPoint = getReversedBillingPoint(obisCode.getF());
           edis += "*" + ((reversedBillingPoint < 10) ? "0" + reversedBillingPoint : reversedBillingPoint);
       }
       return edis;
   }
   private String convertObisCode2ShortCode(ObisCode obisCode) {
      return Integer.toString(obisCode.getC()*100+obisCode.getD()*10+obisCode.getE());

   }

    private int getReversedBillingPoint(int point) throws IOException {
        int billingCounter = getDumpData().getBillingCounter();
        if ( (point + 1) > billingCounter ) {
            String info = "No values present for billing point " + point +".";
            logger.info(info);
            throw new NoSuchRegisterException(info);
        }

        return billingCounter - point;
    }
}
