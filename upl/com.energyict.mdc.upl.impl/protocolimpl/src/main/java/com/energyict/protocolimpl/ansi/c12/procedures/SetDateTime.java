/*
 * SetDateTime.java
 *
 * Created on 24 oktober 2005, 11:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SetDateTime extends AbstractProcedure {
    
    
    Date responseDateTimeBefore;
    Date responseDateTimeAfter;
    
    
    /** Creates a new instance of SetDateTime */
    public SetDateTime(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(10));
    }

    protected void parse(byte[] data) throws IOException {
        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int timeFormat = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        if (getProcedureFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)  {
            responseDateTimeBefore = C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(data, 0, timeFormat, getProcedureFactory().getC12ProtocolLink().getTimeZone(),dataOrder);
            responseDateTimeAfter = C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(data, C12ParseUtils.getLTimeSize(timeFormat), timeFormat, getProcedureFactory().getC12ProtocolLink().getTimeZone(),dataOrder);
        }
        else {
            responseDateTimeBefore = C12ParseUtils.getDateFromLTime(data, 0, timeFormat, getProcedureFactory().getC12ProtocolLink().getTimeZone(),dataOrder);
            responseDateTimeAfter = C12ParseUtils.getDateFromLTime(data, C12ParseUtils.getLTimeSize(timeFormat), timeFormat, getProcedureFactory().getC12ProtocolLink().getTimeZone(),dataOrder);
        }
        
    }
    
    // KV_TO_DO see C12.19 page 52 dst & timezone handling?
    protected void prepare() throws IOException {
        //edMode = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getEndDeviceModeAndStatusTable().getEdMode();
        TimeZone timeZone = getProcedureFactory().getC12ProtocolLink().getTimeZone();
        //byte[] data = new 
        DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
        int setMaskBitfield = 0x03; // set time = true & date=true
        int timeFormat = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        byte[] timeDate = null;
        switch(timeFormat) {
            case 0:
                throw new IOException("SetDateTime, prepare, , timeformat != 0 expected! Cannot continue!");
            case 1: {
                Calendar cal = Calendar.getInstance(timeZone);
                cal.add(Calendar.MILLISECOND,getProcedureFactory().getC12ProtocolLink().getInfoTypeRoundtripCorrection());
                timeDate = new byte[6];
                timeDate[0] = ProtocolUtils.hex2BCD(0);
                timeDate[1] = ProtocolUtils.hex2BCD(0);
                timeDate[2] = ProtocolUtils.hex2BCD(0);
                timeDate[3] = ProtocolUtils.hex2BCD(cal.get(Calendar.HOUR_OF_DAY));
                timeDate[4] = ProtocolUtils.hex2BCD(cal.get(Calendar.MINUTE));
                timeDate[5] = ProtocolUtils.hex2BCD(cal.get(Calendar.SECOND));
            } break;
            case 2: {
                Calendar cal = Calendar.getInstance(timeZone);
                cal.add(Calendar.MILLISECOND,getProcedureFactory().getC12ProtocolLink().getInfoTypeRoundtripCorrection());
                timeDate = new byte[6];
                timeDate[0] = (byte)(cal.get(Calendar.YEAR)-2000);
                timeDate[1] = (byte)(cal.get(Calendar.MONTH)+1);
                timeDate[2] = (byte)cal.get(Calendar.DAY_OF_MONTH);
                timeDate[3] = (byte)cal.get(Calendar.HOUR_OF_DAY);
                timeDate[4] = (byte)cal.get(Calendar.MINUTE);
                timeDate[5] = (byte)cal.get(Calendar.SECOND);
            } break;
                
            case 3: {
                timeDate = new byte[6];
                Calendar calGMT=null;
                calGMT = Calendar.getInstance(TimeZone.getTimeZone("GMT")); 
                calGMT.add(Calendar.MILLISECOND,getProcedureFactory().getC12ProtocolLink().getInfoTypeRoundtripCorrection());

                // Due to a spec non-conformity in the Sentinel meter for the UDATE (GMT minutes from 1970)
                if (getProcedureFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0) {
                    if (timeZone.inDaylightTime(calGMT.getTime()))
                        calGMT.add(Calendar.MILLISECOND, +(timeZone.getRawOffset()-3600000));
                    else
                        calGMT.add(Calendar.MILLISECOND, +timeZone.getRawOffset());                 
                }
                
                
                long minutes = (calGMT.getTime().getTime()/1000)/60;
                int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
                if (dataOrder == 0) { // least significant first
                    timeDate[0] = (byte)(minutes);
                    timeDate[1] = (byte)(minutes>>8);
                    timeDate[2] = (byte)(minutes>>16);
                    timeDate[3] = (byte)(minutes>>24);
                    timeDate[4] = (byte)(byte)calGMT.get(Calendar.SECOND);
                }
                else if (dataOrder == 1) { // most significant first
                    timeDate[0] = (byte)(minutes>>24);
                    timeDate[1] = (byte)(minutes>>16);
                    timeDate[2] = (byte)(minutes>>8);
                    timeDate[3] = (byte)(minutes);
                    timeDate[4] = (byte)(byte)calGMT.get(Calendar.SECOND);
                }
                else throw new IOException("SetTimeDate, prepare(), invalid dataOrder "+dataOrder);
            } break;
        } // switch(timeFormat)
        
        byte[] data = new byte[1+timeDate.length+1];
        data[0] = (byte)setMaskBitfield;
        System.arraycopy(timeDate,0,data,1,timeDate.length);
        data[data.length-1] = (byte)getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getTimeDataQualBitfield();
        setProcedureData(data);
    } // protected void prepare() throws IOException
} // public class SetDateTime extends AbstractProcedure
