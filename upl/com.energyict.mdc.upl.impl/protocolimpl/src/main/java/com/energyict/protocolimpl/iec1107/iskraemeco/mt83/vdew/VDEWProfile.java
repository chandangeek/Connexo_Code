/*
 * VDEWProfile.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
abstract public class VDEWProfile { 
    
    private static final int DEBUG=0;
    
    private ProtocolLink protocolLink=null;
    private AbstractVDEWRegistry abstractVDEWRegistry=null;
    private VDEWProfileHeader vdewProfileHeader=null;
    private MeterExceptionInfo meterExceptionInfo=null; // KV 17022004
    private boolean keepStatus;
    
    /** Creates a new instance of VDEWProfile */
    public VDEWProfile(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink,AbstractVDEWRegistry abstractVDEWRegistry) {
        this(meterExceptionInfo, protocolLink, abstractVDEWRegistry, true);
    }
    public VDEWProfile(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink,AbstractVDEWRegistry abstractVDEWRegistry, boolean keepStatus) {
        this.protocolLink = protocolLink;
        this.meterExceptionInfo=meterExceptionInfo;
        this.abstractVDEWRegistry = abstractVDEWRegistry;
        this.keepStatus = keepStatus;
    }
    
    /******************************************************
     ******************** PUBLIC METHODS ******************
     ******************************************************/
    public VDEWProfileHeader getProfileHeader() throws IOException {
        if (vdewProfileHeader == null) 
            vdewProfileHeader = new VDEWProfileHeader(getProtocolLink().getFlagIEC1107Connection());
        return vdewProfileHeader;
    }
    
    public void doLogMeterDataCollection(ProfileData profileData) {
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
   }
   
    
    /*******************************************************
     ****************** PROTECTED METHODS ******************
     *******************************************************/
    protected byte[] vdewReadR6(byte[] data) throws IOException {
        try {
            protocolLink.getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ6,data);
            byte[] rawprofile = protocolLink.getFlagIEC1107Connection().receiveRawData();
            abstractVDEWRegistry.validateData(rawprofile);
            return rawprofile;
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new ProtocolConnectionException("VDEWProfile, readRawProfile, FlagIEC1107ConnectionException, "+e.getMessage(), e.getReason());
        }
    }
    

    protected byte[] readRawData(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        return readRawData(fromCalendar,toCalendar,0);
    } // protected byte[] readRawData(Calendar fromCalendar, Calendar toCalendar, profileId) 
    
    protected byte[] readRawData(Calendar fromCalendar, Calendar toCalendar, int profileId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(getProtocolLink().getTimeZone().inDaylightTime(fromCalendar.getTime()) ? '1' : '0');
        byteArrayOutputStream.write(((fromCalendar.get(Calendar.YEAR)%100)/10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(Calendar.YEAR)%100)%10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(Calendar.MONTH)+1)/10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(Calendar.MONTH)+1)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.DAY_OF_MONTH)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.DAY_OF_MONTH)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.HOUR_OF_DAY)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.HOUR_OF_DAY)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.MINUTE)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(Calendar.MINUTE)%10)+0x30);
        byteArrayOutputStream.write((int)';');
        byteArrayOutputStream.write(getProtocolLink().getTimeZone().inDaylightTime(toCalendar.getTime()) ? '1' : '0');
        byteArrayOutputStream.write(((toCalendar.get(Calendar.YEAR)%100)/10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(Calendar.YEAR)%100)%10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(Calendar.MONTH)+1)/10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(Calendar.MONTH)+1)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.DAY_OF_MONTH)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.DAY_OF_MONTH)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.HOUR_OF_DAY)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.HOUR_OF_DAY)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.MINUTE)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(Calendar.MINUTE)%10)+0x30);
         
        return doReadRawProfile(new String(byteArrayOutputStream.toByteArray()), profileId);
    } // protected byte[] readRawData(Calendar fromCalendar, Calendar toCalendar, profileId) 
   
    protected ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar, int profileId) throws IOException {
        byte[] responseData=null;
        ProfileData profileData=new ProfileData();
        try {
            responseData = readRawData(fromCalendar,toCalendar,profileId);
            if (DEBUG >= 1) {
               System.out.println("length = "+responseData.length);
               System.out.println(new String(responseData));
            }
            profileData = buildProfileData(responseData);
        }
        catch(VDEWException e) {
           //absorb
            getProtocolLink().getLogger().warning("VDEWException, ERROR received when requesting profile data, probably no profile data available");
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new ProtocolConnectionException("doGetProfileData> "+e.getMessage(), e.getReason());
        }
        catch(IOException e) {
           throw new IOException("doGetProfileData> "+e.getMessage());
        }

        if (DEBUG >= 2) ProtocolUtils.printResponseData(responseData);

        return profileData;

    } // protected ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException
    
    protected List doGetLogBook(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        byte[] responseData=null;
        List meterEvents=new ArrayList();
        try {
            responseData = readRawData(fromCalendar,toCalendar,98);
            if (DEBUG >= 1) {
               System.out.println("length = "+responseData.length);
               System.out.println(new String(responseData));
            }
            meterEvents = buildMeterEvents(responseData);
        }
        catch(VDEWException e) {
            getProtocolLink().getLogger().warning("VDEWException, ERROR received when requesting logbook data, probably no logbook data available");
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new ProtocolConnectionException("doGetLogBook> "+e.getMessage(), e.getReason());
        }
        catch(IOException e) {
           throw new IOException("doGetLogBook> "+e.getMessage());
        }

        if (DEBUG >= 2) ProtocolUtils.printResponseData(responseData);

        return meterEvents;

    } // protected List doGetLogBook(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException

    /*******************************************************
     ******************** PRIVATE METHODS ******************
     *******************************************************/
    
//    private byte[] doReadRawProfile(String data) throws IOException {
//        return doReadRawProfile(data,0);
//    }
    
    private byte[] doReadRawProfile(String data,int profileid) throws IOException {
        String cmd = "P."+ProtocolUtils.buildStringDecimal(profileid, 2)+"("+data+";8)";
        return vdewReadR6(cmd.getBytes());
    } // private byte[] doReadRawProfile()    
    
    private int getLogical0BasedChannelId(String[] edisCodes,int fysical0BasedChannelId) throws IOException {
        for (int i=0;i<edisCodes.length;i++) {
            if (getFysical0BasedChannelId(edisCodes[i]) == (fysical0BasedChannelId+1))
                return i;
        }
        throw new IOException("VDEWProfile, getLogical0BasedChannelId(), 0-based fysical channel "+fysical0BasedChannelId+" does not exist in profileheader!");
    } 
    
    private int getFysical0BasedChannelId(String edisCode) {
        return Integer.parseInt(edisCode.substring(edisCode.indexOf("-")+1,edisCode.indexOf(":")))-1;    
    }
    
    protected ProfileData buildProfileData(byte[] responseData) throws IOException {
        ProfileData profileData;
        Calendar calendar=null;
        byte bStatus=0;
        byte bNROfValues=0;
        int profileInterval=0;
        DataParser dp = new DataParser(getProtocolLink().getTimeZone());
        Unit[] units=null;
        boolean buildChannelInfos=false;
        int t;
        int eiCode=0;
        IntervalData intervalDataSave=null;
        boolean partialInterval=false;
        String[] edisCodes=null;
        
        // We suppose that the profile contains nr of channels!!
        try {
            VDEWTimeStamp vts = new VDEWTimeStamp(getProtocolLink().getTimeZone());
            profileData = new ProfileData();        

            int i=0;
            while(true) {
                
                if (responseData[i] == 'P') {
                   i+=4; // skip P.01 
                   i=gotoNextOpenBracket(responseData,i);
                   
                   if (dp.parseBetweenBrackets(responseData,i).compareTo("ERROR") == 0)
                       throw new IOException("No entries in object list.");
                   
                   vts.parse(dp.parseBetweenBrackets(responseData,i));
                   calendar = vts.getCalendar();
                   
                   i=gotoNextOpenBracket(responseData,i+1);
                   bStatus =  parseIntervalStatus(responseData, i);
                   
                   eiCode = 0;
                   for (t=0;t<8;t++) {
                      if ((bStatus & (byte)(0x01<<t)) != 0) {
                           eiCode |= mapStatus2IntervalStateBits(bStatus&(byte)(0x01<<t) &0xFF);
                      }
                   }

                   // KV 02112005
                   if (((bStatus&SEASONAL_SWITCHOVER)==SEASONAL_SWITCHOVER) && 
                         (vts.getMode()==VDEWTimeStamp.MODE_SUMMERTIME) && 
                         (!getProtocolLink().getTimeZone().inDaylightTime(calendar.getTime())) &&  
                         ((bStatus&DEVICE_CLOCK_SET_INCORRECT)==DEVICE_CLOCK_SET_INCORRECT)) {
                       calendar.add(Calendar.MILLISECOND,-1*getProtocolLink().getTimeZone().getDSTSavings());
                   }
                   
                   if (!ParseUtils.isOnIntervalBoundary(calendar,getProtocolLink().getProfileInterval())) {
                       partialInterval=true;
                       // roundup to the first interval boundary
                       ParseUtils.roundUp2nearestInterval(calendar,getProtocolLink().getProfileInterval());
                   }
                   else {
                       partialInterval=false;
                   }
                   
                   i=gotoNextOpenBracket(responseData,i+1);
                   profileInterval = Integer.parseInt(dp.parseBetweenBrackets(responseData,i));
                   if ((profileInterval*60) != getProtocolLink().getProfileInterval())
                      throw new IOException("buildProfileData() error, mismatch between configured profileinterval ("+getProtocolLink().getProfileInterval()+") and meter profileinterval ("+(profileInterval*60)+")!");
                       
                   i=gotoNextOpenBracket(responseData,i+1);
                   // KV 06092005 K&P
                   //bNROfValues = ProtocolUtils.bcd2nibble(responseData,i+1);
                   bNROfValues = (byte)Integer.parseInt(dp.parseBetweenBrackets(responseData,i));
                   
                   units = new Unit[bNROfValues];
                   if (bNROfValues > getProtocolLink().getNumberOfChannels()) 
                      throw new IOException("buildProfileData() error, mismatch between configured nrOfChannels ("+getProtocolLink().getNumberOfChannels()+") and meter profile nrOfChannels ("+bNROfValues+")!");
                   
                   // get the units
                   edisCodes = new String[bNROfValues];
                   for (t=0;t<bNROfValues;t++) {// skip all obis codes
                      i=gotoNextOpenBracket(responseData,i+1);
                      edisCodes[t] = dp.parseBetweenBrackets(responseData,i);
                      i=gotoNextOpenBracket(responseData,i+1);
                      units[t] = Unit.get(dp.parseBetweenBrackets(responseData,i));
                   }
                   
                   // KV 06092005 K&P changes
                   if (!buildChannelInfos) {
                       int id=0;
                       for (t=0;t<bNROfValues;t++) {
                          ChannelInfo chi=null;
                          if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
                              int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
                              if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0) != -1) {
                                   chi = new ChannelInfo(id,getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0), "channel_"+id,units[t]); 
                                   id++;
                              }
                              if (!getProtocolLink().isRequestHeader()) {
                                 if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).isCumul()) 
                                     chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getWrapAroundValue());
                              }
                          }
                          else {
                              chi = new ChannelInfo(t,"channel_"+t,units[t]);
                              if (!getProtocolLink().isRequestHeader()) {
                                 if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).isCumul()) 
                                     chi.setCumulativeWrapValue(getProtocolLink().getProtocolChannelMap().getProtocolChannel(t).getWrapAroundValue());
                              }
                          }
                          
                          
                          // KV 06092005 K&P changes
                          if (chi != null)
                              profileData.addChannel(chi);  
                       }
                       buildChannelInfos = true;
                   }
                   
                   i= gotoNextCR(responseData,i+1);
                }
                else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i+=1; // skip 
                }
                else {
                  // Fill profileData     
                  IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()),eiCode,bStatus);
                    
                  if (!keepStatus) eiCode=0;
                  
                  for (t=0;t<bNROfValues;t++) { // skip all obis codes
                      i=gotoNextOpenBracket(responseData,i);
                      BigDecimal bd = new BigDecimal(dp.parseBetweenBrackets(responseData,i));
                      //long lVal = bd.longValue();
                      // KV 06092005 K&P changes
                      if (getProtocolLink().getProtocolChannelMap().isMappedChannels()) {
                          int fysical0BasedChannelId = getFysical0BasedChannelId(edisCodes[t]);
                          if (getProtocolLink().getProtocolChannelMap().getProtocolChannel(fysical0BasedChannelId).getIntValue(0) != -1)
                              intervalData.addValue(bd); //new Long(lVal));
                      }
                      else {
                          intervalData.addValue(bd); //new Long(lVal));
                      }
                      
                      i++;
                   }
                   
                   if (partialInterval) {
                      
                      if (intervalDataSave != null) {
                          if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
                             if (DEBUG >= 1) System.out.println("KV_DEBUG> partialInterval, add partialInterval to currentInterval");
                             intervalData = addIntervalData(intervalDataSave,intervalData); // add intervals together to avoid double interval values...
                          }
                          else {
                             if (DEBUG >= 1) System.out.println("KV_DEBUG> partialInterval, save partialInterval to profiledata and assign currentInterval to partialInterval");
                             profileData.addInterval(intervalDataSave); // save the partiel interval. Timestamp has been adjusted to the next intervalboundary 
                             intervalDataSave=intervalData;
                          }
                      }
                      else {
                          if (DEBUG >= 1) System.out.println("KV_DEBUG> partialInterval, assign currentInterval to partialInterval");
                          intervalDataSave=intervalData;
                      }
                   }
                   else {
                      // If there was a patrialinterval within interval x and the next interval has the same timestamp as interval x,
                      // then we must add them together! 
                      // If the next interval's timestamps != timestamp of interval x, save the partial interval as separate entry for interval x.
                      if (intervalDataSave != null) {
                          if (intervalData.getEndTime().getTime() == intervalDataSave.getEndTime().getTime()) {
                             if (DEBUG >= 1) System.out.println("KV_DEBUG> add partialInterval to currentInterval");
                             intervalData = addIntervalData(intervalDataSave,intervalData); // add intervals together to avoid double interval values...
                          }
                          else {
                             if (DEBUG >= 1) System.out.println("KV_DEBUG> save partialInterval to profiledata");
                             profileData.addInterval(intervalDataSave); // save the partiel interval. Timestamp has been adjusted to the next intervalboundary 
                          }
                          intervalDataSave = null;
                      }
                      
                      if (DEBUG >= 1) System.out.println("KV_DEBUG> save currentInterval to profiledata");
                      // save the current interval
                      profileData.addInterval(intervalData);
                   }
                  
                   calendar.add(Calendar.MINUTE,profileInterval);
                   
                   i= gotoNextCR(responseData,i+1);
                   
                }

                if (i>=responseData.length) {
                    break;
                }

            } // while(true)
        }
        catch(IOException e) {
           throw new IOException("buildProfileData> "+e.getMessage());
        }

        return profileData;
        
    } // private ProfileData buildProfileData(byte[] responseData) throws IOException
   
    
    private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        
        int i;
        for (i=0;i<currentCount;i++) {
            BigDecimal val1 = (BigDecimal)currentIntervalData.get(i);
            BigDecimal val2 = (BigDecimal)cumulatedIntervalData.get(i);
            intervalData.addValue(val1.add(val2));
        }
        return intervalData;
    }    
    
    protected List buildMeterEvents(byte[] responseData) throws IOException {
        
        List meterEvents = new ArrayList();
        int t;
        Calendar calendar=null;
        DataParser dp = new DataParser(getProtocolLink().getTimeZone());
        
        try {
            VDEWTimeStamp vts = new VDEWTimeStamp(getProtocolLink().getTimeZone());
            int i=0;
            while(true) {
                if (responseData[i] == 'P') {
                   i+=4; // skip P.01
                   i=gotoNextOpenBracket(responseData,i);
                   
                   // geen entries in logbook
                   if (dp.parseBetweenBrackets(responseData,i).compareTo("ERROR") == 0)
                       return meterEvents;
                       
                   
                   // P.98 (ZSTs13)(Status)()(nrofentries)(KZ1)()..(KZz)()(Element1)..(Elementz)
                   //         0        1    2      3(eg 2)  4   5    6   7    8           9  
                   vts.parse(dp.parseBetweenBrackets(responseData,i,0));
                   calendar = (Calendar)vts.getCalendar().clone();
                   
                   long status = Long.parseLong(dp.parseBetweenBrackets(responseData,i,1),16);
                   
                   // KV 02112005
                   if (((status&SEASONAL_SWITCHOVER)==SEASONAL_SWITCHOVER) && 
                         (vts.getMode()==VDEWTimeStamp.MODE_SUMMERTIME) && 
                         (!getProtocolLink().getTimeZone().inDaylightTime(calendar.getTime())) &&  
                         ((status&DEVICE_CLOCK_SET_INCORRECT)==DEVICE_CLOCK_SET_INCORRECT)) {
                       calendar.add(Calendar.MILLISECOND,-1*getProtocolLink().getTimeZone().getDSTSavings());
                   }
                   
                   
                   // See A1500 dproduct description on page 45 for the explenation of the 16 statusbits.
                   // Use status to parse the meterevents. Lower statusbyte of meterevents is the same as
                   // the intervalstatus byte. So, therefor, we omit the reading of the logbook
                   
                   for (t=0;t<16;t++) {
                      String msg = null;
                      long logBit = (status & (long)(0x0001<<t)); 
                      if (logBit != 0) {
                           if ((logBit == DEVICE_CLOCK_SET_INCORRECT) || (logBit == SEASONAL_SWITCHOVER)) {
                              String datePart = dp.parseBetweenBrackets(responseData,i,8);
                              String timePart = dp.parseBetweenBrackets(responseData,i,9);
                              vts.parse(datePart,timePart);
                              msg = vts.getCalendar().getTime().toString();  
                           }
                           meterEvents.add(getMeterEvent(new Date(calendar.getTime().getTime()),(int)logBit,msg));
                      }
                   }
                   
                   
                   i= gotoNextCR(responseData,i+1);
                }
                else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i+=1; // skip 
                }
                else {
                   i= gotoNextCR(responseData,i+1);
                }

                if (i>=responseData.length) {
                    break;
                }

            } // while(true)
        }
        catch(IOException e) {
           throw new IOException("buildProfileData> "+e.getMessage());
        }

        return meterEvents;
        
    } // private List buildMeterEvents(byte[] responseData)    

    
    private int gotoNextOpenBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '(') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }
    private int gotoNextClosedBracket(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == ')') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }
    
    private int gotoNextCR(byte[] responseData,int i) {
        while(true) {
            if (responseData[i] == '\r') break;
            i++;
            if (i>=responseData.length) break;
        }
        return i;
    }

    /** 
     * Parse/Extract status flag from the interval data.   
     * 
     * The main purpose of this method is to be overridden.  When a meter 
     * deviates from the standard, specific behaviour can be added in the 
     * subclasses. 
     * 
     * @param ba        byte array containing meter response/raw data
     * @param startIdx  start position of interval status flag 
     * @return          status flag as a byte      
     * 
     * @throws IOException
     */
    protected byte parseIntervalStatus(byte [] ba, int startIdx) throws IOException {
        
        return ProtocolUtils.hex2byte(ba,startIdx+1);
        
    }
    
    /*
     * VDEW status flags
     *
     */
    
    // appears only in the logbook
    protected static final int CLEAR_LOADPROFILE = 0x4000;
    protected static final int CLEAR_LOGBOOK = 0x2000;
    protected static final int END_OF_ERROR = 0x0400;
    protected static final int BEGIN_OF_ERROR = 0x0200;    
    protected static final int VARIABLE_SET = 0x0100;    
    
    // appears in the logbook and the intervalstatus
    protected static final int POWER_FAILURE = 0x0080;
    protected static final int POWER_RECOVERY = 0x0040;
    protected static final int DEVICE_CLOCK_SET_INCORRECT = 0x0020;  // Changed KV 12062003
    protected static final int DEVICE_RESET = 0x0010;
    protected static final int SEASONAL_SWITCHOVER = 0x0008;
    protected static final int DISTURBED_MEASURE = 0x0004;
    protected static final int RUNNING_RESERVE_EXHAUSTED = 0x0002;
    protected static final int FATAL_DEVICE_ERROR = 0x0001;
    
    private long mapLogCodes(long lLogCode) {
        switch((int)lLogCode) {
            case CLEAR_LOADPROFILE: return(MeterEvent.CLEAR_DATA);
            case CLEAR_LOGBOOK: return(MeterEvent.CLEAR_DATA);
            case END_OF_ERROR: return(MeterEvent.METER_ALARM);
            case BEGIN_OF_ERROR: return(MeterEvent.METER_ALARM);
            case VARIABLE_SET: return(MeterEvent.CONFIGURATIONCHANGE);
            case DEVICE_CLOCK_SET_INCORRECT: return(MeterEvent.SETCLOCK);
            case SEASONAL_SWITCHOVER: return(MeterEvent.OTHER);
            case FATAL_DEVICE_ERROR: return(MeterEvent.FATAL_ERROR);
            case DISTURBED_MEASURE: return(MeterEvent.OTHER);
            case POWER_FAILURE: return(MeterEvent.POWERDOWN);
            case POWER_RECOVERY: return(MeterEvent.POWERUP);
            case DEVICE_RESET: return(MeterEvent.CLEAR_DATA);
            case RUNNING_RESERVE_EXHAUSTED: return(MeterEvent.OTHER);
            default: return(MeterEvent.OTHER);
            
        } // switch(lLogCode)
        
    } // private void mapLogCodes(long lLogCode)    
    
    private int mapStatus2IntervalStateBits(int status) {
        switch(status) {
            case CLEAR_LOADPROFILE: return(IntervalStateBits.OTHER);
            case CLEAR_LOGBOOK: return(IntervalStateBits.OTHER);
            case END_OF_ERROR: return(IntervalStateBits.OTHER);
            case BEGIN_OF_ERROR: return(IntervalStateBits.OTHER);
            case VARIABLE_SET: return(IntervalStateBits.CONFIGURATIONCHANGE);
            case DEVICE_CLOCK_SET_INCORRECT: return(IntervalStateBits.SHORTLONG);
            case SEASONAL_SWITCHOVER: return(IntervalStateBits.SHORTLONG);
            case FATAL_DEVICE_ERROR: return(IntervalStateBits.OTHER);
            case DISTURBED_MEASURE: return(IntervalStateBits.CORRUPTED);
            case POWER_FAILURE: return(IntervalStateBits.POWERDOWN);
            case POWER_RECOVERY: return(IntervalStateBits.POWERUP);
            case DEVICE_RESET: return(IntervalStateBits.OTHER);
            case RUNNING_RESERVE_EXHAUSTED: return(IntervalStateBits.OTHER);
            default: return(IntervalStateBits.OTHER);
            
        } // switch(status)
        
    } // private void mapStatus2IntervalStateBits(int status)  
    
    
    private MeterEvent getMeterEvent(Date date, long logcode, String msg) {
        
        switch((int)logcode) {
            case CLEAR_LOADPROFILE: return(new MeterEvent(date,MeterEvent.CLEAR_DATA,(int)logcode,"Erase load profile"));
            case CLEAR_LOGBOOK: return(new MeterEvent(date,MeterEvent.CLEAR_DATA,(int)logcode,"Erase logbook"));
            case END_OF_ERROR: return(new MeterEvent(date,MeterEvent.METER_ALARM,(int)logcode,"End of impermissible operating condition"));
            case BEGIN_OF_ERROR: return(new MeterEvent(date,MeterEvent.METER_ALARM,(int)logcode,"Begin of impermissible operating condition"));
            case VARIABLE_SET: return(new MeterEvent(date,MeterEvent.CONFIGURATIONCHANGE,(int)logcode,"Variable set"));
            case DEVICE_CLOCK_SET_INCORRECT: return(new MeterEvent(date,MeterEvent.SETCLOCK,(int)logcode,"Device clock has been set, "+msg));
            case SEASONAL_SWITCHOVER: return(new MeterEvent(date,MeterEvent.OTHER,(int)logcode,msg));
            case FATAL_DEVICE_ERROR: return(new MeterEvent(date,MeterEvent.FATAL_ERROR,(int)logcode));
            case DISTURBED_MEASURE: return(new MeterEvent(date,MeterEvent.OTHER,(int)logcode));
            case POWER_FAILURE: return(new MeterEvent(date,MeterEvent.POWERDOWN,(int)logcode));
            case POWER_RECOVERY: return(new MeterEvent(date,MeterEvent.POWERUP,(int)logcode));
            case DEVICE_RESET: return(new MeterEvent(date,MeterEvent.CLEAR_DATA,(int)logcode));
            case RUNNING_RESERVE_EXHAUSTED: return(new MeterEvent(date,MeterEvent.OTHER,(int)logcode));
            default: return(new MeterEvent(date,MeterEvent.OTHER,(int)logcode));
            
        } // switch(lLogCode)
        
    } // private MeterEvent getMeterEvent(Date date, long logcode)
    
    
    /***************************************************************
     ******************** GETTER & SETTER METHODS ******************
     ***************************************************************/
    protected AbstractVDEWRegistry getAbstractVDEWRegistry() {
        return abstractVDEWRegistry;   
    }
    
    protected ProtocolLink getProtocolLink() {
        return protocolLink;   
    }
    
   /** Getter for property meterExceptionInfo.
    * @return Value of property meterExceptionInfo.
    *
    */
   public MeterExceptionInfo getMeterExceptionInfo() {
       return meterExceptionInfo;
   }    
} // VDEWProfile
