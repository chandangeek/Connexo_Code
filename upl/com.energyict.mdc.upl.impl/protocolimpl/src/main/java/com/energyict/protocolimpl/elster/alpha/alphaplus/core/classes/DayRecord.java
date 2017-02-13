/*
 * DayRecord.java
 *
 * Created on 26 juli 2005, 11:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author koen
 */
public class DayRecord {
    
    private final int DEBUG=0;
    
    boolean dst;
    boolean holiday;
    List intervalDatas; // of type IntervalData
    
    
    
    
    /** Creates a new instance of DayRecord */
    public DayRecord(byte[] data, ClassFactory classFactory) throws IOException {
        parse(data,classFactory);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DayRecord: dst="+dst+", holiday="+holiday);
        Iterator it = intervalDatas.iterator();
        while(it.hasNext()) {
            IntervalData id = (IntervalData)it.next();
            strBuff.append(id.toString()+"\n");
        }
        return strBuff.toString();
    }
    
    private void parse(byte[] data, ClassFactory classFactory) throws IOException {
       intervalDatas = new ArrayList();

       
       int temp = ProtocolUtils.getInt(data, 3,1);
       int dayOfWeek = (temp&0x1C)>>2; // not used
       
       holiday = (temp&0x02)==0x02;
       dst = (temp&0x01)==0x01;

       // Profile data always follows standard time! See page 17-2 of the PowerPlus Alpha data dictionary
       // If DST is true, then clock was in DST when calendar is initialized for the  profileday. So,
       // use wintertimezone!
       // If DST was false in the summer, then use java dst corrected timezone because we know better!
       Calendar cal=null;
       if (dst==true) {
           cal = ClassParseUtils.getCalendar3(data, 0, ProtocolUtils.getWinterTimeZone(classFactory.getAlpha().getTimeZone()));
       }
       else {
           cal = ClassParseUtils.getCalendar3(data, 0, classFactory.getAlpha().getTimeZone());
       }
           
//System.out.println("KV_DEBUG> cal="+cal.getTime()+", dst="+dst+", holiday="+holiday);       
       
       // validate byte 6 of the day header which is the checksum of the complete day record
       if (!isChecksumValidated(data))
           throw new IOException("DayRecord, parse(), Wrong checksum in day record!");
       
       // parse day interval values
       boolean include=false;
       int eiStatus=0;
       int nrOfChannels = classFactory.getClass14LoadProfileConfiguration().getNrOfChannels();
       for (int interval=0;interval<classFactory.getClass14LoadProfileConfiguration().getIntervalsPerDay();interval++) {
           eiStatus=0;
           cal.add(Calendar.SECOND,classFactory.getClass14LoadProfileConfiguration().getLoadProfileInterval());
           
             
           IntervalData intervalData = new IntervalData(((Calendar)cal.clone()).getTime());
           for (int profileChannel=0;profileChannel<nrOfChannels;profileChannel++) {
              int rawValue = ProtocolUtils.getInt(data,6+2*(interval*nrOfChannels+profileChannel),2);
              int value = rawValue & 0x7FFF;
              boolean eventFlag = (rawValue & 0x8000) == 0x8000;
               if (eventFlag) {
                   eiStatus |= IntervalStateBits.OTHER;
               }

              // verify value
              if (value == 0x7FFF) {
                  include=false;
              }
              else if (value == 0x7FFE) {
                  include = true;
                  eiStatus |= IntervalStateBits.OVERFLOW;
              }
              else {
                  include = true;
              }
              
              if (include) {
                  
                  BigDecimal bd=BigDecimal.valueOf((long)value); // basic units (raw pulses)
                  
                  // Depending on ProtocolChannelMap, calculate engineering values (demand or energy)
                  if (classFactory.getAlpha().getProtocolChannelMap()!=null) {
                      if (classFactory.getAlpha().getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                          (classFactory.getAlpha().getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==1)) {
                          // demand values
                          if (classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel) == Class8FirmwareConfiguration.PHENOMENON_ACTIVE) {
                              BigDecimal multiplier = classFactory.getClass0ComputationalConfiguration().getUKE().multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getRLPSCAL()));
                              //multiplier = multiplier.multiply(BigDecimal.valueOf(1000));
                              multiplier = multiplier.multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getIntervalsPerHour()));
                              bd = BigDecimal.valueOf((long)value).multiply(multiplier);
                          }
                          else if (classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel) == Class8FirmwareConfiguration.PHENOMENON_REACTIVE) {
                              BigDecimal multiplier = classFactory.getClass7MeteringFunctionBlock().getXKE1().multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getRLPSCAL()));
                              //multiplier = multiplier.multiply(BigDecimal.valueOf(1000));
                              multiplier = multiplier.multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getIntervalsPerHour()));
                              bd = BigDecimal.valueOf((long)value).multiply(multiplier);
                          }
                          else throw new IOException("DayRecord, parse(), Invalid phenomenon "+classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel)+" for profile channel "+profileChannel);
                          
                      }   
                      else if (classFactory.getAlpha().getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                          (classFactory.getAlpha().getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==2)) {
                          // energy values
                          if (classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel) == Class8FirmwareConfiguration.PHENOMENON_ACTIVE) {
                              BigDecimal multiplier = classFactory.getClass0ComputationalConfiguration().getUKE().multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getRLPSCAL()));
                              //multiplier = multiplier.multiply(BigDecimal.valueOf(1000));
                              bd = BigDecimal.valueOf((long)value).multiply(multiplier);
                          }
                          else if (classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel) == Class8FirmwareConfiguration.PHENOMENON_REACTIVE) {
                              BigDecimal multiplier = classFactory.getClass7MeteringFunctionBlock().getXKE1().multiply(BigDecimal.valueOf((long)classFactory.getClass14LoadProfileConfiguration().getRLPSCAL()));
                              //multiplier = multiplier.multiply(BigDecimal.valueOf(1000));
                              bd = BigDecimal.valueOf((long)value).multiply(multiplier);
                          }
                          else throw new IOException("DayRecord, parse(), Invalid phenomenon "+classFactory.getClass14LoadProfileConfiguration().getPhenomenon(profileChannel)+" for profile channel "+profileChannel);
                      }
                  }
                  
                  
                  intervalData.addValue(bd, eiStatus, eiStatus);
              }
          } // for (int profileChannel=0;profileChannel<nrOfChannels;profileChannel++)
          
           if (include) 
               intervalDatas.add(intervalData);
           
       } // for (int interval=0;interval<classFactory.getClass14LoadProfileConfiguration().getIntervalsPerDay();interval++) 
       
    } // private void parse(byte[] data)

    private boolean isChecksumValidated(byte[] data) throws IOException {
       int rxChecksum = ProtocolUtils.getInt(data, 5,1);
       data[5] = 0; // reset checksum. If we don't, checksum result always 0
       int checksum = 0;
       for (int i=0;i<data.length;i++) {
           checksum += ((int)data[i]&0xFF);
       }    
       checksum= ((checksum&0xFF)^0xFF);
       
       if (DEBUG>=1) System.out.println("KV_DEBUG> DayRecord, isChecksumValidated, datalength=0x"+Integer.toHexString(data.length)+", checksum calculated=0x"+Integer.toHexString(checksum)+", checksum received=0x"+Integer.toHexString(rxChecksum)+", data="+ProtocolUtils.outputHexString(data));
       
       return checksum==rxChecksum;
    }
    
    public List getIntervalDatas() {
        return intervalDatas;
    }
    
} // public class DayRecord
