/*
 * ABBA1700Profile.java
 *
 * Created on 28 april 2003, 18:23
 *
 * Changes:
 * KV 12012004 bugfix crossday
 *             changed powerfail behaviour
 * KV 16022004 extend ABB1700_REGISTERCONFIG with external input channels
 * KV 25112004 extend channelinfo build to use correct unit from customerdefined registers
 *             created getUnitInfo() method to get the Unit info about the registers
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 * Changes:
 * KV 11062004 Add intervalstatus flags
 */
public class ABBA1700Profile {

    private static final int DEBUG=0;
    ProtocolLink protocolLink=null;
    ABBA1700RegisterFactory abba1700RegisterFactory=null;
    private static final int MAX_NR_OF_CHANNELS=14;
    long profileconfig=-1; // lazy initializing
    //private static final Unit[] ABB1700_REGISTERCONFIG = {Unit.get(BaseUnit.WATT,-3),Unit.get(BaseUnit.WATT,-3),Unit.get(BaseUnit.VOLTAMPEREREACTIVE,-3),Unit.get(BaseUnit.VOLTAMPEREREACTIVE,-3),Unit.get(BaseUnit.VOLTAMPEREREACTIVE,-3),Unit.get(BaseUnit.VOLTAMPEREREACTIVE,-3),Unit.get(BaseUnit.VOLTAMPERE,-3),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0),Unit.get(BaseUnit.COUNT,0)};

    /** Creates a new instance of ABBA1700Profile */
    public ABBA1700Profile(ProtocolLink protocolLink,ABBA1700RegisterFactory abba1700RegisterFactory) {
        this.protocolLink = protocolLink;
        this.abba1700RegisterFactory = abba1700RegisterFactory;
    }

    public ProfileData getProfileData() throws IOException {
        getABBA1700RegisterFactory().setRegister("LoadProfileSet",new Long(0xFFFF));
        return doGetProfileData();
    }

    public ProfileData getProfileData(Date from, Date to) throws IOException {
        if (to.getTime() < from.getTime()) throw new IOException("ABBA1700Profile, getProfileData, error ("+from+") > ("+to+")");
        long offset = to.getTime() - from.getTime();
        final long ONEDAY=24*60*60*1000;

        //long nrOfDaysToRetrieve = ((offset % ONEDAY) == 0) ? offset/ONEDAY : (offset/ONEDAY)+1;

        // KV 12012004 bugfix crossday
        long tostd = to.getTime() + (long)getTimeZone().getOffset(to.getTime());
        long fromstd = from.getTime() + (long)getTimeZone().getOffset(from.getTime());
        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;

        getABBA1700RegisterFactory().setRegister("LoadProfileSet",new Long(nrOfDaysToRetrieve));
        return doGetProfileData();
    }

    public ProfileData doGetProfileData() throws IOException {
        byte[] data;
        if (protocolLink.isIEC1107Compatible()) {
            long nrOfBlocks = ((Long)getABBA1700RegisterFactory().getRegister("LoadProfile64Blocks")).longValue()+1; // KV_DEBUG 1 more block...
            data = getABBA1700RegisterFactory().getRegisterRawData("LoadProfile", (int)nrOfBlocks*64);
        }
        else {
            long nrOfBlocks = ((Long)getABBA1700RegisterFactory().getRegister("LoadProfile256Blocks")).longValue();
            data = getABBA1700RegisterFactory().getRegisterRawDataStream("LoadProfile",(int)nrOfBlocks);
        }

       // KV_DEBUG
//        if (DEBUG>=1) {
//          System.out.println("KV_DEBUG> Write data to file!!!!!!!!!!");
//          FileOutputStream fos = new FileOutputStream(new File("rawdata_"+String.valueOf((new Date()).getTime())+".bin"));
//          fos.write(data);
//          fos.close();
//       }

        ProfileData profileData = parse(new ByteArrayInputStream(data),protocolLink.getNumberOfChannels());
        if (DEBUG>=1) doLogMeterDataCollection(profileData);

        return profileData;
    }

    private ABBA1700RegisterFactory getABBA1700RegisterFactory() {
        return abba1700RegisterFactory;
    }

    protected long getChannelMask() throws IOException {
        if (profileconfig == -1)
            profileconfig = ((Long)getABBA1700RegisterFactory().getRegister("LoadProfileConfiguration")).longValue();
        long channelMask = ((profileconfig&0x7F00L)>>1)|(profileconfig&0x7FL);
        return channelMask;
    }

    protected int getChannelIndex(int channelID) throws IOException {
        if (channelID >= protocolLink.getNumberOfChannels()) throw new IOException("ABBA1700, getChannelIndex, channelID error");
        long channelMask = getChannelMask();
        int channelIndex,channelCount=0;
        for (channelIndex=0; channelIndex<MAX_NR_OF_CHANNELS ; channelIndex++)
            if ((channelMask & (0x1L<<channelIndex)) != 0) {
                if (channelCount == channelID) break;
                channelCount++;
            }
        return channelIndex;
    }

    private Calendar incCalendar(Calendar calendar, int integrationTime) {
        calendar.add(Calendar.SECOND,integrationTime);
        return calendar;
    }

    private TimeZone getTimeZone() {
        TimeZone tz = null;
        if (protocolLink == null)
            tz = TimeZone.getTimeZone("GMT+1");
        else
            tz = protocolLink.getTimeZone();
        return tz;
    }

    private Calendar setCalendar(int integrationTime, boolean dst, long shift) {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone(),shift);
        calendar.set(Calendar.MINUTE,(calendar.get(Calendar.MINUTE)/(integrationTime/60))*(integrationTime/60));
        calendar.set(Calendar.SECOND,0);
        return calendar;
    }

    public Unit getUnitInfo(int channelId) throws IOException {
        String registerName = null;
        Unit unit = null;
        if (channelId == 0) {
            registerName = "CummMainImport";
        }
        else if (channelId == 1) {
            registerName = "CummMainExport";
        }
        else if (channelId == 2) {
            registerName = "CummMainQ1";
        }
        else if (channelId == 3) {
            registerName = "CummMainQ2";
        }
        else if (channelId == 4) {
            registerName = "CummMainQ3";
        }
        else if (channelId == 5) {
            registerName = "CummMainQ4";
        }
        else if (channelId == 6) {
            registerName = "CummMainVA";
        }
        else if ((channelId >= 7) && (channelId <= 9))  {
            CustDefRegConfig cdrc = (CustDefRegConfig)abba1700RegisterFactory.getRegister("CustDefRegConfig");
            unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(channelId-7),true);
        }
        else if (channelId == 10) {
            registerName = "ExternalInput1";
        }
        else if (channelId == 11) {
            registerName = "ExternalInput2";
        }
        else if (channelId == 12) {
            registerName = "ExternalInput3";
        }
        else if (channelId == 13) {
            registerName = "ExternalInput4";
        }
        if (unit == null) {
            unit = abba1700RegisterFactory.getABBA1700Register(registerName).getUnit();
        }

        return unit.getFlowUnit();

    } // public Unit getUnitInfo(int channelId)

    private static final int GOT_MARKER=0;
    private static final int GOT_DATA=1;

    public ProfileData parse(ByteArrayInputStream bai, int nrOfChannels) throws IOException {

       ProfileData profileData = new ProfileData();
       ABBA1700ProfileEntry profileEntry=null,previousProfileEntry=null; // KV 12012004
       ABBA1700ProfileEntry profileEntryResult=null;
       int integrationTime=0;
       boolean dst=false;

       Calendar calendar = null;

       // precondition fullfilled ?
       profileEntry = ABBA1700ProfileEntry.getInstance(bai,nrOfChannels);
       if ((profileEntry.getType()) != ABBA1700ProfileEntry.NEWDAY)
          throw new IOException("ABBA16700ProfileParser, parse, profile data should start with 'E4', new day marker!");

       // initialize
       integrationTime = profileEntry.getIntegrationPeriod();
       dst = profileEntry.isDST();
       calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
       profileEntryResult = null;

       for (int i=0;i<nrOfChannels;i++) {
           if (protocolLink == null) {
               profileData.addChannel(new ChannelInfo(i,
                                                      "ELSTERA1700_channel_"+i,
                                                      Unit.get("")));
           }
           else {
               Unit unit;
               unit = getUnitInfo(getChannelIndex(i));
               profileData.addChannel(new ChannelInfo(i,
                                                      "ELSTERA1700_channel_"+i,
                                                      unit));
           }
       }

       int state = GOT_MARKER; // entry state

       bai.reset();
       // parse datastream & build profiledata
       while(bai.available() > 0) {
           profileEntry = ABBA1700ProfileEntry.getInstance(bai,nrOfChannels);

           if (DEBUG>=1) System.out.println(profileEntry.toString(getTimeZone(),dst));

           // The opposite check is not possible since the meter local tim  dst behaviour can be set NOT to follow DST while
           // the profile data has it's dst flag set.
           if ((getTimeZone().useDaylightTime()) && !dst)
               throw new IOException("ABBA1700Profile, parse, configured timezone expects profiledata to follow DST, correct first!");

           addEventToProfile(profileData,profileEntry,dst,getTimeZone());

           switch(state) {
               case GOT_MARKER: {
                   if (profileEntry.isExternalData()) {
                       calendar = saveInProfile(profileData,profileEntry,calendar,integrationTime);
                   }
                   else if (profileEntry.isMarker()) {
                       switch (profileEntry.getType()) {
                           case ABBA1700ProfileEntry.NEWDAY:
                               if (DEBUG>=1) {
                                   System.out.println();
                                   System.out.println("**************************************************************************************");
                               }
                               nrOfChannels = profileEntry.getNumberOfChannels();
                               integrationTime = profileEntry.getIntegrationPeriod();
                               dst = profileEntry.isDST();
                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               break;

                           case ABBA1700ProfileEntry.POWERDOWN:
                               break;

                           case ABBA1700ProfileEntry.ENDOFDATA:
                               break;

                           case ABBA1700ProfileEntry.DAYLIGHTSAVING:
                               break;

                           case ABBA1700ProfileEntry.POWERUP:
                           case ABBA1700ProfileEntry.FORCEDENDOFDEMAND:
                           case ABBA1700ProfileEntry.TIMECHANGE:
                           case ABBA1700ProfileEntry.LOADPROFILECLEARED:
                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               break;

                           case ABBA1700ProfileEntry.CONFIGURATIONCHANGE: // 14012008
                               nrOfChannels = profileEntry.getNumberOfChannels();
                               integrationTime = profileEntry.getIntegrationPeriod();
                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               break;

                       } // switch (profileEntry.getType())
                   }
                   else if (!profileEntry.isMarker()) {
                       // KV 12012004 changed powerfail behaviour
                       if (previousProfileEntry.getType() == ABBA1700ProfileEntry.POWERDOWN) {

                           calendar = saveInProfile(profileData,profileEntry,calendar,integrationTime);
                           profileEntryResult = null;
                       }
                       else {
                           if (profileEntryResult == null) {
                               profileEntryResult = ABBA1700ProfileEntry.getCleanInstance(nrOfChannels);
                           }
                           profileEntryResult.add(profileEntry);

                           state = GOT_DATA;
                       }
                   }

               } break; // GOT_MARKER

               case GOT_DATA: {
                   if (profileEntry.isExternalData()) {
                       //absorb
                   }
                   else if (profileEntry.isMarker()) {
                       switch (profileEntry.getType()) {
                           case ABBA1700ProfileEntry.NEWDAY:
                               if (DEBUG>=1) {
                                   System.out.println();
                                   System.out.println("**************************************************************************************");
                               }
                               nrOfChannels = profileEntry.getNumberOfChannels();
                               integrationTime = profileEntry.getIntegrationPeriod();
                               dst = profileEntry.isDST();
                               calendar = saveInProfile(profileData,profileEntryResult,calendar,integrationTime);
                               profileEntryResult = null;

                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               state = GOT_MARKER;
                               break;

                           case ABBA1700ProfileEntry.POWERDOWN:
                           case ABBA1700ProfileEntry.ENDOFDATA:
                               calendar = saveInProfile(profileData,profileEntryResult,calendar,integrationTime);
                               profileEntryResult = null;
                               state = GOT_MARKER;
                               break;

                           case ABBA1700ProfileEntry.DAYLIGHTSAVING:
                               break;

                           case ABBA1700ProfileEntry.POWERUP:
                           case ABBA1700ProfileEntry.FORCEDENDOFDEMAND:
                           case ABBA1700ProfileEntry.TIMECHANGE:
                           case ABBA1700ProfileEntry.LOADPROFILECLEARED:
                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               state = GOT_MARKER;
                               break;

                           case ABBA1700ProfileEntry.CONFIGURATIONCHANGE: // 14012008
                               nrOfChannels = profileEntry.getNumberOfChannels();
                               integrationTime = profileEntry.getIntegrationPeriod();
                               calendar = setCalendar(integrationTime,dst,profileEntry.getTime());
                               state = GOT_MARKER;
                               break;


                       } // switch (profileEntry.getType())
                   }
                   else if (!profileEntry.isMarker()) {
                       calendar = saveInProfile(profileData,profileEntryResult,calendar,integrationTime);
                       profileEntryResult = profileEntry;
                   }
               } break; // GOT_DATA

           } //  switch(state)



           if (profileEntry.getType() == ABBA1700ProfileEntry.ENDOFDATA) break;

           previousProfileEntry = profileEntry; // KV 12012004

       } // while(bai.available() > 0)

       profileData.applyEvents(integrationTime/60);
       if (DEBUG>=2)doLogMeterDataCollection(profileData);

       // NOT DONE!
       // validate(profileData);

       return profileData;

    } // public ProfileData parse(byte[] data)

    // KV 12012004
    // check for double values and add them together...
    private void validate(ProfileData profileData) {
        profileData.sort();
        IntervalData currentIntervalData=null,previousIntervalData=null;
        Iterator intervalDataIterator = profileData.getIntervalDatas().iterator();
        while(intervalDataIterator.hasNext()) {
            currentIntervalData = (IntervalData)intervalDataIterator.next();
            if (previousIntervalData != null) {
                if (currentIntervalData.getEndTime().getTime() == previousIntervalData.getEndTime().getTime()) {
                    // add current to previous and remove current...
                    addIntervalValues(currentIntervalData,previousIntervalData);
                    intervalDataIterator.remove();
                }
            }
            previousIntervalData = currentIntervalData;
        }
    }

    // KV 12012004
    // add current to previous and update previous
    private void addIntervalValues(IntervalData currentIntervalData, IntervalData previousIntervalData) {
        IntervalData tempIntervalData = new IntervalData(previousIntervalData.getEndTime());
        for (int i = 0 ; i < previousIntervalData.getIntervalValues().size() ; i++) {
            int current = currentIntervalData.get(i).intValue() + previousIntervalData.get(i).intValue();
            tempIntervalData.addValue(new Integer(current));
        }
        previousIntervalData.setIntervalValues(tempIntervalData.getIntervalValues());
    }

    private void addEventToProfile(ProfileData profileData, ABBA1700ProfileEntry profileEntry, boolean dst, TimeZone timeZone) {
       if (profileEntry.isMarker()) {
           switch (profileEntry.getType()) {
               case ABBA1700ProfileEntry.NEWDAY:
               case ABBA1700ProfileEntry.CONFIGURATIONCHANGE:
               case ABBA1700ProfileEntry.ENDOFDATA:
                   break;

               case ABBA1700ProfileEntry.POWERDOWN:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.POWERDOWN,
                            ABBA1700ProfileEntry.POWERDOWN));
                   break;

               case ABBA1700ProfileEntry.POWERUP:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.POWERUP,
                            ABBA1700ProfileEntry.POWERUP));
                    break;
               case ABBA1700ProfileEntry.DAYLIGHTSAVING:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.SETCLOCK,
                            ABBA1700ProfileEntry.DAYLIGHTSAVING));
                    break;
               case ABBA1700ProfileEntry.FORCEDENDOFDEMAND:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.OTHER,
                            ABBA1700ProfileEntry.FORCEDENDOFDEMAND));
                    break;
               case ABBA1700ProfileEntry.TIMECHANGE:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.SETCLOCK,
                            ABBA1700ProfileEntry.TIMECHANGE));
                    break;
               case ABBA1700ProfileEntry.LOADPROFILECLEARED:
                    profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                            MeterEvent.CLEAR_DATA,
                            ABBA1700ProfileEntry.LOADPROFILECLEARED));
                    break;
               default:profileData.addEvent(new MeterEvent(ProtocolUtils.getCalendar(timeZone,profileEntry.getTime()).getTime(),
                       MeterEvent.OTHER, profileEntry.getType()));
           } // switch (profileEntry.getType())
       }
    }

    private Calendar saveInProfile(ProfileData profileData, ABBA1700ProfileEntry profileEntryResult, Calendar calendar, int integrationTime) throws IOException {
       for (int index = 0 ; index < profileEntryResult.getNrOfIntervals() ; index++) {
          IntervalData result=null;
          // if external data and first value of external data, add it to the last intervaldata entry
          if ((profileEntryResult.isExternalData()) && (index==0)) {
             if (profileData.getIntervalDatas().size() == 0) { // first event is a newday marker followed by external data
                 // set calendar backwards for the nr of intervals and skip the first interval...
                 //System.out.println(calendar.getTime());
                 calendar = incCalendar(calendar,-1*integrationTime*(profileEntryResult.getNrOfIntervals()-1));
                 //System.out.println(calendar.getTime());

             }
             else {
                 IntervalData lastEntry = profileData.getIntervalData(profileData.getIntervalDatas().size()-1);
                 calendar.setTime(lastEntry.getEndTime()); // restore calendar to last interval
                 IntervalData entry2add = getIntervalData(profileData,  profileEntryResult.getValues(index), profileEntryResult.getStatus(), calendar);
                 result = addIntervalData(lastEntry,entry2add);
                 try {
                     profileData.getIntervalDatas().remove(profileData.getIntervalDatas().size() - 1);
                 } catch (IndexOutOfBoundsException e) {
                     // Absorb exception
                 }
             }
          }
          else {
             calendar = incCalendar(calendar,integrationTime);
             result = getIntervalData(profileData,  profileEntryResult.getValues(index), profileEntryResult.getStatus(), calendar);
          }
          if (result!=null) {
              if (result.getValueCount() == profileData.getNumberOfChannels())
                  profileData.addInterval(result);
          }
       }
       return calendar;
    }

    private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData) {
        IntervalData intervalData = null;
        if (cumulatedIntervalData.getIntervalValues().size() == currentIntervalData.getIntervalValues().size()) {
            intervalData = new IntervalData(currentIntervalData.getEndTime());
            int currentCount = currentIntervalData.getValueCount();
            for (int i = 0; i < currentCount; i++) {
                intervalData.addValue(getValue(currentIntervalData, i) + getValue(cumulatedIntervalData, i));
            }
        }
        return intervalData;
    }

    long getValue(IntervalData data, int index) {
        return data.get(index).longValue();
    }

    // ***********************
    // standard COP5 build
    private static final int METER_TRANSIENT_RESET=0x01;
    private static final int TIME_SYNC=0x02;
    private static final int DATA_CHANGE=0x04;
    private static final int BATTERY_FAIL=0x08;
    private static final int REVERSE_RUN=0x20;
    private static final int PHASE_FAILURE=0x40;


    private IntervalData getIntervalData(ProfileData profileData, long[] values, int status, Calendar calendar) throws IOException {
        // Add interval data...
        IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()));

        // KV 11062004
        if ((status & METER_TRANSIENT_RESET) == METER_TRANSIENT_RESET) {
            intervalData.addEiStatus(IntervalStateBits.OTHER);
        }
        if ((status & TIME_SYNC) == TIME_SYNC) {
            intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
        }
        if ((status & DATA_CHANGE) == DATA_CHANGE) {
            intervalData.addEiStatus(IntervalStateBits.CONFIGURATIONCHANGE);
        }
        if ((status & BATTERY_FAIL) == BATTERY_FAIL) {
            intervalData.addEiStatus(IntervalStateBits.BATTERY_LOW);
            profileData.addEvent(new MeterEvent(new Date(calendar.getTime().getTime()), MeterEvent.BATTERY_VOLTAGE_LOW, BATTERY_FAIL));
        }
        if ((status & REVERSE_RUN) == REVERSE_RUN) {
            intervalData.addEiStatus(IntervalStateBits.REVERSERUN);
        }
        if ((status & PHASE_FAILURE) == PHASE_FAILURE) {
            intervalData.addEiStatus(IntervalStateBits.PHASEFAILURE);
        }

        // KV 20072005
        if (values != null) {
            for (int t=0;t<values.length;t++)
                 intervalData.addValue(new Long(values[t]));
        }
        else intervalData.addValue(new Long(0));

        return intervalData;
    }

   private void doLogMeterDataCollection(ProfileData profileData)
   {
       if (profileData == null) return;
       int i,iNROfChannels=profileData.getNumberOfChannels();
       int t,iNROfIntervals=profileData.getNumberOfIntervals();
       int z,iNROfEvents=profileData.getNumberOfEvents();
       System.out.println("Channels: "+iNROfChannels);
       System.out.println("Intervals par channel: "+iNROfIntervals);
       for (t=0;t<iNROfIntervals;t++) {
           System.out.println(" Interval "+t+" endtime = "+profileData.getIntervalData(t).getEndTime());
           for (i=0;i<iNROfChannels;i++) {
               System.out.println("Channel "+i+" Interval "+t+" = "+profileData.getIntervalData(t).get(i)+", status = "+profileData.getIntervalData(t).getEiStatus(i)+" "+profileData.getChannel(i).getUnit());
           }
       }
       System.out.println("Events in profiledata: "+iNROfEvents);
       for (z=0;z<iNROfEvents;z++) {
           System.out.println("Event "+z+" = "+profileData.getEvent(z).getEiCode()+", "+profileData.getEvent(z).getProtocolCode()+" at "+profileData.getEvent(z).getTime());
       }

   } // private void doLogMeterDataCollection(ProfileData profileData)  throws ProtocolReaderException

   static public void main(String[] args) {
       try {
           ABBA1700Profile p = new ABBA1700Profile(null,null);
           File file = new File("rawdata_1200326721707.bin");
           byte[] data = new byte[(int)file.length()];
           FileInputStream fis = new FileInputStream(file);
           fis.read(data);
           System.out.println(p.parse(new ByteArrayInputStream(data),2));
       }
       catch(Exception e) {
           e.printStackTrace();
       }
   }

}
