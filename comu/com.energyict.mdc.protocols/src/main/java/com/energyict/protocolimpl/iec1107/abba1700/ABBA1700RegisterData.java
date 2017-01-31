/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ABBA1700RegisterData.java
 *
 * Created on 25 april 2003, 9:15
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PhaseFailureCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PhaseFailureCounter2;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PowerDownCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.PowerDownCounter2;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ProgrammingCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ReverseRunCounter;
import com.energyict.protocolimpl.iec1107.abba1700.counters.ReverseRunCounter2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * @author  Koen
 */
abstract public class ABBA1700RegisterData {

    static final int ABBA_STRING=0;
    static final int ABBA_DATE=1;
    static final int ABBA_NUMBER=2;
    static final int ABBA_LONG=3;
    static final int ABBA_BYTEARRAY=4;
    static final int ABBA_QUANTITY=5;
    static final int ABBA_INTEGER=6;
    static final int ABBA_64BITFIELD=7;
    static final int ABBA_BIGDECIMAL=8;
    static final int ABBA_HEX=9;
    static final int ABBA_HEX_LE=10;
    static final int ABBA_MD=11;
    static final int ABBA_CMD=12;
    static final int ABBA_HISTORICALVALUES=13;
    static final int ABBA_REGISTER=14;
    static final int ABBA_HISTORICALEVENTS=15;
    static final int ABBA_SYSTEMSTATUS=16;
    static final int ABBA_TARIFFSOURCES=17;
    static final int ABBA_HISTORICALDISPLAYSCALINGS=18;
    static final int ABBA_MDSOURCES=19;
    static final int ABBA_CUSTDEFREGCONFIG=20;
    static final int ABBA_INSTANTANEOUSVALUES=21;
    static final int ABBA_PROGRAMMING_COUNTER =22;
    static final int ABBA_PHASE_FAILURE_COUNTER =23;
    static final int ABBA_POWER_DOWN_COUNTER =24;
    static final int ABBA_REVERSE_RUN_COUNTER =25;
    static final int ABBA_BATTERY_STATUS = 26;
    static final int ABBA_PHASE_FAILURE_COUNTER2 = 27;
    static final int ABBA_POWER_DOWN_COUNTER2 = 28;
    static final int ABBA_REVERSE_RUN_COUNTER2 = 29;


    abstract protected Unit getUnit();

    abstract protected int getType();

    abstract protected FlagIEC1107Connection getFlagIEC1107Connection();

    abstract protected ProtocolLink getProtocolLink();

    abstract protected int getOffset();

    abstract protected int getLength();

    abstract protected ABBA1700MeterType getMeterType();


    protected String buildData(Object object) throws IOException {
       switch(getType()) {
           case ABBA_STRING:
               return (String)object;

           case ABBA_DATE:
               return buildDate((Date)object);

           case ABBA_NUMBER:
               return null;

           case ABBA_LONG:
               return null;

           case ABBA_INTEGER:
               return null;

           case ABBA_64BITFIELD:
               return null;

           case ABBA_BYTEARRAY:
               return null;

           case ABBA_QUANTITY:
               return null;

           case ABBA_BIGDECIMAL:
               return null;

           case ABBA_HEX:
               return null;

           case ABBA_HEX_LE:
               return buildHexLE((Long)object);

           default:
               throw new IOException("ABBA1700RegisterData, parse , unknown type "+getType());
       }
    }

    private String buildHexLE(Long val) {
        long lVal = val.longValue();
        byte[] data = new byte[4];
        ProtocolUtils.val2HEXascii((int)lVal&0xFF,data,0);
        ProtocolUtils.val2HEXascii((int)(lVal>>8)&0xFF,data,2);

        return new String(data);
    }

    private String buildDate(Date date) {
        Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
        calendar.clear();
        calendar.setTime(date);
        byte[] data = new byte[14];

        ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,0);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,2);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,4);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.DAY_OF_MONTH),data,6);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,8);
        ProtocolUtils.val2BCDascii(0,data,10);
        ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,12);

        return new String(data);
    }

    protected Object parse(byte[] data) throws IOException {
       try {
           switch(getType()) {
               case ABBA_STRING:
                   return new String(data);

               case ABBA_DATE:
                   return parseDate(data);

               case ABBA_NUMBER:
                   return null;

               case ABBA_LONG:
                   return parseLong(data);

               case ABBA_INTEGER:
                   return parseInteger(data);

               case ABBA_64BITFIELD:
                   return parseBitfield(data);

               case ABBA_BYTEARRAY:
                   return data;

               case ABBA_QUANTITY:
                   return parseQuantity(data);

               case ABBA_BIGDECIMAL:
                   return parseBigDecimal(data);

               case ABBA_HEX:
                   return parseLongHex(data);

               case ABBA_HEX_LE:
                   return parseLongHexLE(data);

               case ABBA_MD:
                   return new MaximumDemand(ProtocolUtils.getSubArray2(data,getOffset(),getLength()), getProtocolLink().getTimeZone());

               case ABBA_CMD:
                   return new CumulativeMaximumDemand(ProtocolUtils.getSubArray2(data,getOffset(),getLength()));

               case ABBA_HISTORICALVALUES:
                   return new HistoricalValues(data, getProtocolLink().getTimeZone(),getMeterType());

               case ABBA_REGISTER:
                   return new MainRegister(parseQuantity(data));

               case ABBA_HISTORICALEVENTS:
                   return new HistoricalEvents(data, getProtocolLink().getTimeZone());

               case ABBA_SYSTEMSTATUS:
                   return new SystemStatus(data);

               case ABBA_TARIFFSOURCES:
                   return new TariffSources(data,getMeterType());

               case ABBA_HISTORICALDISPLAYSCALINGS:
                   return new HistoricalDisplayScalings(data,getMeterType());

               case ABBA_MDSOURCES:
                   return new MDSources(data);

               case ABBA_INSTANTANEOUSVALUES:
                   return new InstantaneousValue(data);

               case ABBA_CUSTDEFREGCONFIG:
                   return new CustDefRegConfig(data);

               case ABBA_PROGRAMMING_COUNTER:
                   ProgrammingCounter pc = new ProgrammingCounter(getProtocolLink());
                   pc.parse(data);
                   return pc;

               case ABBA_PHASE_FAILURE_COUNTER:
                   PhaseFailureCounter pfc = new PhaseFailureCounter(getProtocolLink());
                   pfc.parse(data);
                   return pfc;

                case ABBA_PHASE_FAILURE_COUNTER2:
                    PhaseFailureCounter2 pfc2 = new PhaseFailureCounter2(getProtocolLink());
                    pfc2.parse(data);
                    return pfc2;

               case ABBA_POWER_DOWN_COUNTER:
                   PowerDownCounter pdc = new PowerDownCounter(getProtocolLink());
                   pdc.parse(data);
                   return pdc;

                case ABBA_POWER_DOWN_COUNTER2:
                    PowerDownCounter2 pdc2 = new PowerDownCounter2(getProtocolLink());
                    pdc2.parse(data);
                    return pdc2;

               case ABBA_REVERSE_RUN_COUNTER:
                   ReverseRunCounter rrc = new ReverseRunCounter(getProtocolLink());
                   rrc.parse(data);
                   return rrc;

                case ABBA_REVERSE_RUN_COUNTER2:
                    ReverseRunCounter2 rrc2 = new ReverseRunCounter2(getProtocolLink());
                    rrc2.parse(data);
                    return rrc2;

               case ABBA_BATTERY_STATUS:
                   BatterySupportStatus bss = new BatterySupportStatus(getProtocolLink(), data);
                   return bss;
               default:
                   throw new IOException("ABBA1700RegisterData, parse , unknown type "+getType());
           }
       }
       catch(NumberFormatException e) {
           throw new IOException("ABBA1700RegisterData, parse error");
       }
    }

    private Long parseLongHexLE(byte[] data) throws IOException,NumberFormatException {
        return new Long(ProtocolUtils.getLongLE(data,getOffset(),getLength()));
    }

    private Long parseLongHex(byte[] data) throws IOException,NumberFormatException {
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private BigDecimal parseBigDecimal(byte[] data) throws IOException,NumberFormatException {
        if (getLength() > 8) {
            throw new IOException("ABBA1700RegisterData, parseBigDecimal, datalength should not exceed 8!");
        }
        BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
        return bd.movePointLeft(Math.abs(getUnit().getScale()));
    }

    private Quantity parseQuantity(byte[] data) throws IOException,NumberFormatException {
        if (getLength() > 8) {
            throw new IOException("ABBA1700RegisterData, parseQuantity, datalength should not exceed 8!");
        }
        BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
        return new Quantity(bd,getUnit());
    }

    private Long parseBitfield(byte[] data) throws IOException {
        if (getLength() > 8) {
            throw new IOException("ABBA1700RegisterData, parseBitfield, datalength should not exceed 8!");
        }
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private Long parseLong(byte[] data) throws IOException,NumberFormatException {
        if (getLength() > 8) {
            throw new IOException("ABBA1700RegisterData, parseLong, datalength should not exceed 8!");
        }
        return new Long(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
    }

    private Integer parseInteger(byte[] data) throws IOException,NumberFormatException {
        if (getLength() > 4) {
            throw new IOException("ABBA1700RegisterData, parseInteger, datalength should not exceed 4!");
        }
        return new Integer(Integer.parseInt(Integer.toHexString(ProtocolUtils.getIntLE(data,getOffset(),getLength()))));
    }

    protected Date parseDate(byte[] data) throws IOException {
       Calendar calendar = ProtocolUtils.getCalendar(getProtocolLink().getTimeZone());
       calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[0]));
       calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[1]));
       calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[2]));
       calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex((byte)((int)data[3]&0x3F)));
       calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex((byte)((int)data[4]&0x1F))-1);
       int y = ProtocolUtils.BCD2hex(data[6]);
       calendar.set(Calendar.YEAR,y == 99 ? 1999 : y+2000);
       return calendar.getTime();
    }

    /**
     * Creates a new instance of ABBA1700RegisterData
     */
    public ABBA1700RegisterData() {
    }

}
