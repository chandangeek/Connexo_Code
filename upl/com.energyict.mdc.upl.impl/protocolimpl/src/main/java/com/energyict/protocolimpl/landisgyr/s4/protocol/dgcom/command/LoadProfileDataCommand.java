/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.util.Equality;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 *
 * @author Koen
 */
public class LoadProfileDataCommand extends AbstractCommand {
    
    private final int DEBUG=0;
    private int memorySize;
    private List intervalDatas;
    private List channelInfos;
    private List meterEvents;
    
    /** Creates a new instance of TemplateCommand */
    public LoadProfileDataCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileDataCommand:\n");
        strBuff.append("   intervalDatas="+getIntervalDatas()+"\n");
        strBuff.append("   channelInfos="+getChannelInfos()+"\n");
        strBuff.append("   memorySize="+getMemorySize()+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() throws IOException {
        //if (DEBUG>=1) System.out.println(getCommandFactory().getLoadProfileLimit());
        if (DEBUG>=1) System.out.println(getCommandFactory().getTOUAndLoadProfileOptions());

        // 2 extra 1K blocks to be sure that we have a full day... Problem is with the DATE_STAMP
        // If this is not OK, we can always build in a mechanism that counts the DATA entries before the first DATE_STAMP and then
        // at first DATE_STAMP roll back with correct interval decrements...
        int memorySizeInKbytes=(getMemorySize()/1024)+2;

        
        byte[] data=null;
        /*
        Load Profile Memory Available
        Normal Memory Extended Memory
        DX *6.00k/6.75k 30.0k
        RXS3 25.4k 121.4k
        RXS4 32k 122k
        */
        
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
           if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00) { // RXS4
               if (getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory() && (memorySizeInKbytes > 122))
                   memorySizeInKbytes = 122;
               else if ((!getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory()) && (memorySizeInKbytes > 32)) 
                   memorySizeInKbytes = 32;
           }
           else if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()<3.00) { // RXS3
               if (getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory() && (memorySizeInKbytes > 122))
                   memorySizeInKbytes = 122;
               else if ((!getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory()) && (memorySizeInKbytes > 26)) 
                   memorySizeInKbytes = 26;
           }
            data = new byte[]{(byte)0x03,(byte)(memorySizeInKbytes&0xFF),(byte)((memorySizeInKbytes>>8)&0xFF),0,0,0,0,0,0};
        }
        if (getCommandFactory().getFirmwareVersionCommand().isDX()) {
            
            if (getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory() && (memorySizeInKbytes > 30))
                memorySizeInKbytes = 30;
            else if ((!getCommandFactory().getTOUAndLoadProfileOptions().is128KMemory()) && (memorySizeInKbytes > 7)) 
                memorySizeInKbytes = 7;
            
            data = new byte[]{(byte)0x03,(byte)(memorySizeInKbytes&0xFF),0,0,0,0,0,0,0};
        }
        
        setSize(memorySizeInKbytes*1024);
        
        if (DEBUG>=2) System.out.println("KV_DEBUG> memorySizeInKbytes = 0x"+Integer.toHexString(memorySizeInKbytes)+", size = "+getSize()+" bytes"); 
                
        return data;
    }
    
    // events
    private final int DATE_STAMP=0;
    private final int TIME_STAMP=1;
    private final int DATA=2;
    
    // states
    private final int STATE_DATE_STAMP=0;
    private final int STATE_VALUE=1;
    private final int STATE_VALUE_PARTIAL=2;
    
    protected void parse(byte[] data) throws IOException {
        intervalDatas = collect(data);
        validate(intervalDatas);
        
        if (DEBUG>=1) {
            for (int i=0;i<intervalDatas.size();i++) {
                System.out.println(intervalDatas.get(i));
            }
        }
        
        setChannelInfos(new ArrayList());
        for (int channel=0; channel<getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels(); channel++) {
           ChannelInfo channelInfo = new ChannelInfo(channel,"L&G S4 channel "+channel,getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileChannelUnit(channel));    
           if (getCommandFactory().getLoadProfileMetricSelectionRXCommand().isEnergy(channel))
               channelInfo.setMultiplier(getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileChannelMultiplier(channel));
           getChannelInfos().add(channelInfo);        
        } // for (int channel=1; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)        
        
    }
    
    protected void validate(List intervalDatas) throws IOException {
        IntervalData intervalData,intervalData2add;
        
        if (intervalDatas.size() ==1) {
            intervalData = (IntervalData)intervalDatas.get(0);
            if (intervalData.getIntervalValues().size() != getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels()) {
                if (DEBUG>=2) System.out.println("KV_DEBUG> Trash the only interval, it does not contain all channels!");
                intervalDatas.remove(0);
            }
        }
        
        for (int i=0;i<(intervalDatas.size()-1);i++) {
            intervalData2add = (IntervalData)intervalDatas.get(i);
            intervalData = (IntervalData)intervalDatas.get(i+1);
            if (intervalData.getIntervalValues().size() != getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels()) {
                if (DEBUG>=2) System.out.println("KV_DEBUG> Trash interval ("+(i+1)+"), it does not contain all channels!");
                intervalDatas.remove(i+1);
                continue;
            }
            if (Equality.equalityHoldsFor(intervalData.getEndTime()).and(intervalData2add.getEndTime())) {
                ParseUtils.addIntervalValues(intervalData, intervalData2add);
               intervalDatas.remove(i);
            }
        } // for (int i=0;i<(intervalDatas.size()-1);i++)
    }

//    private Calendar searchInitialCalendar(byte[] data) throws IOException {
//        int offset=0;
//        int length = data.length;
//        int interval=0;
//        Calendar cal=null;
//        int channelIndex=0;
//        boolean dateStamp=false;
//        boolean timeStamp=false;
//        
//        while (offset<length) {
//            int value = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
//            int event = DATA;
//            if ((value&0x8000)==0x8000) {
//                if ((value&0x4000)==0x4000)
//                    event = TIME_STAMP;
//                else
//                    event = DATE_STAMP;
//            }
//            else {
//                if (!checkParity(value)) {
//                    if (DEBUG>=1) System.out.println("KV_DEBUG> Corrupted value, bad parity");
//                    // KV_TO_DO set corrupted intervalstate bit!
//                }
//                value &= 0x3FFF; // mask out bit 14 & 15
//            }
//            
//            if (event == DATE_STAMP) {
//                if (DEBUG>=2) System.out.println("DATE_STAMP");                
//                
//                // KV 07082007
//                // if calendar is older then previous, remove already collected intervals
//                Calendar temp = getDateStamp(value);
//                if ((cal != null) && (temp.getTime().before(cal.getTime()))) {
//                    if (DEBUG>=2) System.out.println("KV_DEBUG> Trash all received intervals until now...");   
//                }
//                cal = temp;
//                        
//                if (dateStamp)
//                    timeStamp=false;
//                if (!timeStamp)
//                    dateStamp=true;
//                
//            } // if (event == DATE_STAMP)
//            
//            if (event == TIME_STAMP) {
//                if (DEBUG>=2) System.out.println("TIME_STAMP");                  
//                getTimeStamp(cal,value);
//                if ((!dateStamp) && (!timeStamp)) {
//                    timeStamp = true;
//                }
//                else if (timeStamp) {
//                    dateStamp=false;
//                    timeStamp=false;
//                }
//                ParseUtils.roundDown2nearestInterval(cal, getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getProfileInterval());
//                
//            } // if (event == TIME_STAMP)
//            
//            interval++;
//            
//        } //  while (offset<length)
//    }
    
    protected List collect(byte[] data) throws IOException {
        int offset=0;
        int length = data.length;
        int interval=0;
        Calendar cal=null;
        int channelIndex=0;
        int intervalStateBits = 0;
        List intervalDatas = new ArrayList();
        boolean dateStamp=false;
        boolean timeStamp=false;
        IntervalData intervalData=null;
        
        
        
        while (offset<length) {
            int value = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
            int event = DATA;
            if ((value&0x8000)==0x8000) {
                if ((value&0x4000)==0x4000)
                    event = TIME_STAMP;
                else
                    event = DATE_STAMP;
            }
            else {
                if (!checkParity(value)) {
                    if (DEBUG>=1) System.out.println("KV_DEBUG> Corrupted value, bad parity");
                    // KV_TO_DO set corrupted intervalstate bit!
                }
                value &= 0x3FFF; // mask out bit 14 & 15
            }
            
            if (DEBUG>=2) System.out.println("KV_DEBUG> interval "+interval+", 0x"+Integer.toHexString(value));

            
            
            if (event == DATE_STAMP) {
                if (DEBUG>=2) System.out.println("DATE_STAMP");                
                
                // KV 07082007
                // if calendar is older then previous, remove already collected intervals
                Calendar temp = getDateStamp(value);
                cal = temp;
                        
                if (dateStamp)
                    timeStamp=false;
                if (!timeStamp)
                    dateStamp=true;
                
            } // if (event == DATE_STAMP)
            
            if (event == TIME_STAMP) {
                if (DEBUG>=2) System.out.println("TIME_STAMP");
                getTimeStamp(cal,value);

                if ((!dateStamp) && (!timeStamp)) {
                    timeStamp = true;
                    intervalStateBits |= IntervalStateBits.POWERDOWN;
                    getMeterEvents().add(new MeterEvent(cal.getTime(),MeterEvent.POWERDOWN));
                }
                else if ((dateStamp) && (!timeStamp)) {
                    
                    if (!((intervalStateBits&IntervalStateBits.SHORTLONG) == IntervalStateBits.SHORTLONG)) {
                        intervalStateBits |= IntervalStateBits.SHORTLONG;
                        getMeterEvents().add(new MeterEvent(cal.getTime(),MeterEvent.SETCLOCK_BEFORE));
                        
                    }
                    else {
                        getMeterEvents().add(new MeterEvent(cal.getTime(),MeterEvent.SETCLOCK_AFTER));
    
                    }
                }
                else if (timeStamp) {
                    intervalStateBits |= IntervalStateBits.POWERUP;
                    dateStamp=false;
                    timeStamp=false;
                    getMeterEvents().add(new MeterEvent(cal.getTime(),MeterEvent.POWERUP));
                }
                
                ParseUtils.roundDown2nearestInterval(cal, getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getProfileInterval());
                if (DEBUG>=1) System.out.println("roundup to "+cal.getTime());
                
            } // if (event == TIME_STAMP)
            
            if (event == DATA) {
                if (DEBUG>=2) System.out.println("DATA");                 
                if (cal!=null) {
                   
                    if (channelIndex == 0) {
                        // add 15 minutes
                        cal.add(Calendar.SECOND, getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getProfileInterval());
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),intervalStateBits);
                        intervalDatas.add(intervalData);
                    }
                    if (DEBUG>=1) System.out.println("KV_DEBUG> "+cal.getTime()+" --> "+value+", unit="+getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileChannelUnit(channelIndex)+", multiplier="+getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileChannelMultiplier(channelIndex)+" statebits = 0x"+Integer.toHexString(intervalStateBits));
    
                    intervalData.addValue(BigDecimal.valueOf(value));
                    if (channelIndex++ >= (getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels()-1))
                        channelIndex=0;

                    // reset flags
                    dateStamp=false;
                    //timestamp=false;
                    intervalStateBits=0;
                    
                }
                
            } // if (event == DATA)
            
            interval++;
        } //  while (offset<length)
        intervalDatas = ProtocolTools.mergeDuplicateIntervals(intervalDatas);
        return intervalDatas;
    } // protected void parse(byte[] data) throws IOException
    
    
    private boolean checkParity(int val) throws IOException {
        int count=0;
        for (int i=0x0001;i!=0x8000;i<<=1) {
            if ((val&i)==i) count++;
        }
        return (count%2)==0;
    }
    
    private Calendar getDateStamp(int value) throws IOException {
        Calendar cal = ProtocolUtils.getCalendar(getCommandFactory().getS4().getTimeZone());
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex((byte)(value&0xFF)));
        cal.set(Calendar.MONTH,(ProtocolUtils.BCD2hex((byte)((value>>8)&0x1F)))-1);
        
        // set timepart to 0
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal;
    }
    
    private void getTimeStamp(Calendar cal, int value) throws IOException {
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        int nrOfSeconds = (value&0x3FFF)*6; // 6 second increments
        cal.add(Calendar.SECOND, nrOfSeconds);
        if (DEBUG>=1) System.out.println("add "+nrOfSeconds+" seconds, cal="+cal.getTime());
        
    }
    
    public int getMemorySize() {
        return memorySize;
    }
    
    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    public List getIntervalDatas() {
        return intervalDatas;
    }

    private void setIntervalDatas(List intervalDatas) {
        this.intervalDatas = intervalDatas;
    }

    public List getChannelInfos() {
        return channelInfos;
    }

    private void setChannelInfos(List channelInfos) {
        this.channelInfos = channelInfos;
    }

    public List getMeterEvents() {
        if (meterEvents == null){
            meterEvents = new ArrayList(0);
        }
        return meterEvents;
    }

    private void setMeterEvents(List meterEvents) {
        this.meterEvents = meterEvents;
    }
}
