/*
 * ProfileDataCompressed.java
 *
 * Created on 18 mei 2005, 17:18
 */

package com.energyict.protocolimpl.emon.ez7.core.command;  

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.BigDecimal;
import com.energyict.cbo.NestedIOException;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.cbo.*;
import com.energyict.dialer.connection.ConnectionException;

/**
 *
 * @author  Koen
 */
public class ProfileDataCompressed extends AbstractCommand {
    
    private static final int DEBUG=0;
    private static final String COMMAND="RQ2";
    private static final int NR_OF_CHANNELS=8;
    
    int dayBlockNr;
    ProfileStatus profileStatus=null;
    List intervalDatas=null;
    
    
    /** Creates a new instance of ProfileDataCompressed */
    public ProfileDataCompressed(EZ7CommandFactory ez7CommandFactory,int dayBlockNr) {
        super(ez7CommandFactory);
        this.dayBlockNr=dayBlockNr;
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        if (getProfileStatus().getCurrentBlockStart()!=null) {
            strBuff.append("ProfileDataCompressed:\n");
            strBuff.append(getProfileStatus()+"\n");
            Iterator it = getIntervalDatas().iterator();
            while(it.hasNext()) {
                IntervalData intervalData = (IntervalData)it.next();
                strBuff.append(intervalData.toString()+"\n");
            }
            return strBuff.toString();
        }
        else {
            return null;
        }
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        //nrOfBlocks=ez7CommandFactory.getProfileStatus().getNrOfDayBlocks();
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND,ProtocolUtils.buildStringHex(dayBlockNr+1,2).toUpperCase());
        parse(data);
    }
    
    private void parse(byte[] data) throws ConnectionException, IOException {
        
        if (DEBUG>=2) System.out.println("KV_DEBUG> "+new String(data));
        
        profileStatus = new ProfileStatus(ez7CommandFactory);
        profileStatus.parse(data);
        if (profileStatus.getCurrentBlockStart() != null) {
            
            if (DEBUG>=1) System.out.println("KV_DEBUG> "+profileStatus);
            
            String strData = new String(data);
            int index=0;
            for (int i=0;i<3;i++)
                index = strData.indexOf("\r\n",index)+"\r\n".length();
            
            byte[] intervalData = ProtocolUtils.getSubArray(data,index);
            intervalDatas=new ArrayList();
            
            int interval=0;
            int length=0;
            
            ByteArrayInputStream bais = new ByteArrayInputStream(intervalData);
            LittleEndianInputStream leis = new LittleEndianInputStream(bais);
            try {
                // init calendar to the currentdayblock timestamp found in the profile status header
                Calendar cal = ProtocolUtils.getCleanCalendar(ez7CommandFactory.getEz7().getTimeZone());
                cal.setTime(profileStatus.getCurrentBlockStart());
                
                // To check for the DST switch...
                boolean inDaylightTime = ez7CommandFactory.getEz7().getTimeZone().inDaylightTime(cal.getTime());
                int intervalsPerHour=3600/ez7CommandFactory.getEz7().getProfileInterval(); 
                int overlapHourIntervals=intervalsPerHour;        
                boolean include=true;
                boolean dstSwitch=false;
                IntervalData[] overlapIntervals = new IntervalData[overlapHourIntervals];
                int overlapIntervalIndex=0;
                
                while(true) {
                    // safety...
                    // blocks returned maximum 96 or 288 intervals depending on the integrationtime!
                    // if, for some reason, larger blocks returned, throw an exception
                    if (((profileStatus.getProfileInterval()==900) && (interval>96)) ||
                    ((profileStatus.getProfileInterval()==1800) && (interval>288)))
                        throw new IOException("ProfileDataCompressed, parse(), error profile storage format. Too many intervals for "+profileStatus.getProfileInterval()+" sec integration time!");
                    
                    // calculate interval close timestamp
                    if (include) {
                       cal.add(Calendar.SECOND,profileStatus.getProfileInterval());
                    }
                    List intervalValues = new ArrayList();
                    
                    for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
                        int value;
                        if (profileStatus.getProfileResolution() == 1)
                            value = (int)leis.readByte()&0xFF;
                        else if (profileStatus.getProfileResolution() == 2)
                            value = (int)leis.readShort()&0xFFFF;
                        else throw new IOException("ProfileDataCompressed, parse(), error profile storage format "+profileStatus.getProfileResolution()+" bytes!");
                        value = convert(value);
                        if (DEBUG>=1) {
                            if (channel==0)
                                System.out.print("interval="+(interval)+", ");
                            System.out.print("0x"+Integer.toHexString(value)+" ");
                        }
                        if (ez7CommandFactory.getHookUp().isChannelEnabled(channel)) {  
                            BigDecimal bd = ez7CommandFactory.getMeterInformation().calculateValue(channel, value);
                            bd = bd.multiply(getEz7CommandFactory().getEz7().getAdjustChannelMultiplier());
                            intervalValues.add(new IntervalValue(bd,0,0));
                        }
                    } // for (int channel=0;channel<NR_OF_CHANNELS;channel++)
                    
                    
                    
                    
                    int status = (int)leis.readByte()&0xFF;
                    if (DEBUG>=1) System.out.println("0x"+Integer.toHexString(status)+" ");
                    leis.readShort(); // skip crlf
                    
//ez7CommandFactory.getEz7().getLogger().severe("KV_DEBUG> "+cal.getTime()+", "+ez7CommandFactory.getEz7().getTimeZone().inDaylightTime(cal.getTime()));
                    
                    // Summer -> Winter transition, add extra hour
                    // in case of DST, use same interval for the overlapping hour. This info i got from Kantol Khek, the IMon logger overwrites the overlapping hour...
                    if (ez7CommandFactory.getImonInformation().isUseDST() && 
                        (inDaylightTime == true) && 
                        (ez7CommandFactory.getEz7().getTimeZone().inDaylightTime(cal.getTime()) == false)) {
                       if (!dstSwitch) {
                           for (int i=0;i<intervalsPerHour;i++)
                               intervalDatas.add(overlapIntervals[i]);
                           cal.add(Calendar.SECOND,3600); // add one hour
                       }
                       dstSwitch=true;
//ez7CommandFactory.getEz7().getLogger().severe("KV_DEBUG> summer->winter change day");  
                    } // summer -W winter transition

                    // Winter -> Summer Set hour to missing
                    if (ez7CommandFactory.getImonInformation().isUseDST() && 
                        (inDaylightTime == false) && 
                        (ez7CommandFactory.getEz7().getTimeZone().inDaylightTime(cal.getTime()) == true)) {
                        if (dstSwitch && (overlapHourIntervals-- > 0)) {
                            include = false; // was false
//ez7CommandFactory.getEz7().getLogger().severe("KV_DEBUG> winter->summer change day, exclude interval");                               
                        }
                        else {
//ez7CommandFactory.getEz7().getLogger().severe("KV_DEBUG> winter->summer change day");                               
                            include = true;
                        }
                        dstSwitch = true;
                    } // winter -> summer transition
                    
                    if (include) {
                        //IntervalData id = new IntervalData(((Calendar)cal.clone()).getTime(),((status>0) && (status<255))?IntervalStateBits.SHORTLONG:0,status,0,intervalValues);
                        IntervalData id = new IntervalData(((Calendar)cal.clone()).getTime(),((status>0) && (status<255))?IntervalStateBits.POWERDOWN|IntervalStateBits.POWERUP:0,status,0,intervalValues);
                        intervalDatas.add(id);
                        // save last hour during summertime...
                        if (ez7CommandFactory.getEz7().getTimeZone().inDaylightTime(cal.getTime()) == true) {
                            overlapIntervals[overlapIntervalIndex] = id;
                            if (overlapIntervalIndex++>=(intervalsPerHour-1))
                                overlapIntervalIndex=0;
                        }
                    }
                    interval++;
                } // while(true)
            }
            catch(EOFException e) {
                // end of file reached!
            }
        } // if (profileStatus.getCurrentBlockStart() != null)
    } // private void parse(byte[] data) throws ConnectionException, IOException
    
    private int convert(int value) {
        int lower = value % 0x100;
        int upper = value / 0x100;
        lower--;
        upper--;
        return lower + 0xFF*upper;
    }
    
    /**
     * Getter for property profileStatus.
     * @return Value of property profileStatus.
     */
    public com.energyict.protocolimpl.emon.ez7.core.command.ProfileStatus getProfileStatus() {
        return profileStatus;
    }
    
    /**
     * Getter for property intervalDatas.
     * @return Value of property intervalDatas.
     */
    public java.util.List getIntervalDatas() {
        return intervalDatas;
    }
    
    /**
     * Setter for property intervalDatas.
     * @param intervalDatas New value of property intervalDatas.
     */
    public void setIntervalDatas(java.util.List intervalDatas) {
        this.intervalDatas = intervalDatas;
    }
    
//    static public void main(String[] args) {
//        double val = 345.56789;
//        int channelval=0;
//        double multiplier = Math.pow((double)10, (double)(channelval));
//        double valRounded = (double)Math.round(val*multiplier)/multiplier;
//        System.out.println(val);
//        System.out.println(valRounded);
//    } 
    
}
