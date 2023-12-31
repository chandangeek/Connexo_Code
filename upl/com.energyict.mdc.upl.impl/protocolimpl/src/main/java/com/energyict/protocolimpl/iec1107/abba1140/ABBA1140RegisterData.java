package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.EndOfBillingEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.InternalBatteryEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.MainCoverEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.MeterErrorEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.PhaseFailureEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.PowerFailEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.ReverserunEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.SecurityEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.TerminalCoverEventLog;
import com.energyict.protocolimpl.iec1107.abba1140.eventlogs.TransientEventLog;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/** @author  Koen */

abstract public class ABBA1140RegisterData {

    public final static int ABBA_STRING=0;
    public final static int ABBA_DATE=1;
    public final static int ABBA_NUMBER=2;
    public final static int ABBA_LONG=3;
    public final static int ABBA_BYTEARRAY=4;
    public final static int ABBA_QUANTITY=5;
    public final static int ABBA_INTEGER=6;
    public final static int ABBA_64BITFIELD=7;
    public final static int ABBA_BIGDECIMAL=8;
    public final static int ABBA_HEX=9;
    public final static int ABBA_HEX_LE=10;
    public final static int ABBA_MD=11;
    public final static int ABBA_CMD=12;
    public final static int ABBA_HISTORICALVALUES=13;
    public final static int ABBA_REGISTER=14;
    public final static int ABBA_HISTORICALEVENTS=15;
    public final static int ABBA_SYSTEMSTATUS=16;
    public final static int ABBA_TARIFFSOURCES=17;
    public final static int ABBA_HISTORICALDISPLAYSCALINGS=18;
    public final static int ABBA_MDSOURCES=19;
    public final static int ABBA_CUSTDEFREGCONFIG=20;
    public final static int ABBA_INSTANTANEOUSVALUES=21;
    public final static int ABBA_INTEGRATION_PERIOD=22;
    public final static int ABBA_LOAD_PROFILE_BY_DATE=23;
    public final static int ABBA_LOAD_PROFILE_CONFIG=24;

    public final static int ABBA_TERMINALCOVEREVENTLOG=29;
    public final static int ABBA_MAINCOVEREVENTLOG=30;
    public final static int ABBA_PHASEFAILUREEVENTLOG=31;
    public final static int ABBA_REVERSERUNEVENTLOG=32;
    public final static int ABBA_POWEREFAILEVENTLOG=33;
    public final static int ABBA_TRANSIENTEVENTLOG=34;
    public final static int ABBA_ENDOFBILLINGEVENTLOG=35;
    public final static int ABBA_METERERROREVENTLOG=36;
    public final static int ABBA_INTERNALBATTERYEVENTLOG = 37;
    public final static int ABBA_NET_CONSUMPTION = 38;
    public final static int ABBA_SECURITYLOG = 39;

    abstract protected Unit getUnit();
    abstract protected int getType();
    abstract protected FlagIEC1107Connection getFlagIEC1107Connection();
    abstract protected ProtocolLink getProtocolLink();
    abstract protected ABBA1140RegisterFactory getRegisterFactory();
    abstract protected int getOffset();
    abstract protected int getLength();


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

            case ABBA_INTEGRATION_PERIOD:
                return null;

            case ABBA_LOAD_PROFILE_BY_DATE:
                return build((LoadProfileReadByDate)object);

            default:
                throw new IOException("ABBA1140RegisterData, parse , unknown type "+getType());
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

    private String build(LoadProfileReadByDate loadProfileReadByDate) {

        byte [] ba = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        long shift = loadProfileReadByDate.getFrom().getTime() / 1000;
        byte [] hex = ProtocolUtils.buildStringHex( shift, 8 ).getBytes();
        for( int i = 0; i < hex.length; i=i+2 ) {
            byte [] t = ProtocolUtils.getSubArray2(hex, 6-i, 2);
            System.arraycopy(t, 0, ba, i, 2);
        }

        shift = loadProfileReadByDate.getTo().getTime() / 1000;
        hex = ProtocolUtils.buildStringHex( shift, 8 ).getBytes();
        for( int i = 0; i < hex.length; i=i+2 ) {
            byte [] t = ProtocolUtils.getSubArray2(hex, 6-i, 2);
            System.arraycopy(t, 0, ba, i+8, 2);
        }

        return new String(ba);

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
                    return new HistoricalRegister(data, getProtocolLink() );

                case ABBA_REGISTER:
                    return new MainRegister(parseQuantity(data));

                case ABBA_HISTORICALEVENTS:
                    return new HistoricalEventRegister(data, getProtocolLink().getTimeZone());

                case ABBA_SYSTEMSTATUS:
                    return new SystemStatus(data, getOffset(), getLength());

                case ABBA_TARIFFSOURCES:
                    return new TariffSources(data);

                case ABBA_MDSOURCES:
                    return new MDSources(data);

                case ABBA_CUSTDEFREGCONFIG:
                    return new CustDefRegConfig(data);

                case ABBA_INTEGRATION_PERIOD:
                    return new Integer( getRegisterFactory().getDataType().integrationPeriod.parse(data[0]) );

                case ABBA_LOAD_PROFILE_BY_DATE: {
                    String msg = "ABBA1140RegisterData, parse, "
                            + "type can only be read" + getType();
                    throw new IOException(msg);
                }

                case ABBA_LOAD_PROFILE_CONFIG:
                    return new LoadProfileConfigRegister(getRegisterFactory(), data);

                case ABBA_TERMINALCOVEREVENTLOG: {
                	TerminalCoverEventLog o = new TerminalCoverEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_MAINCOVEREVENTLOG: {
                	MainCoverEventLog o = new MainCoverEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_PHASEFAILUREEVENTLOG: {
                	PhaseFailureEventLog o = new PhaseFailureEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_REVERSERUNEVENTLOG: {
                	ReverserunEventLog o = new ReverserunEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_SECURITYLOG: {
                    SecurityEventLog o = new SecurityEventLog(getProtocolLink().getTimeZone());
                    o.parse(data);
                    return o;
                }

                case ABBA_POWEREFAILEVENTLOG: {
                	PowerFailEventLog o = new PowerFailEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_TRANSIENTEVENTLOG: {
                	TransientEventLog o = new TransientEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_ENDOFBILLINGEVENTLOG: {
                	EndOfBillingEventLog o = new EndOfBillingEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_METERERROREVENTLOG: {
                	MeterErrorEventLog o = new MeterErrorEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_INTERNALBATTERYEVENTLOG: {
                	InternalBatteryEventLog o = new InternalBatteryEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_NET_CONSUMPTION: {
                    return new MainRegister(parseNetConsumption(data));
                }

                default:
                    throw new ProtocolException("ABBA1140RegisterData, parse , unknown type " + getType());
            }
        }
        catch(NumberFormatException e) {
            throw new ProtocolException("ABBA1140RegisterData, parse error exception:" + e.getMessage());
        }
    }

    private Long parseLongHexLE(byte[] data) throws ProtocolException {
        return new Long(ProtocolUtils.getLongLE(data,getOffset(),getLength()));
    }
    private Long parseLongHex(byte[] data) throws IOException,NumberFormatException {
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private BigDecimal parseBigDecimal(byte[] data) throws ProtocolException {
        if (getLength() > 8) throw new ProtocolException("ABBA1140RegisterData, parseBigDecimal, datalength should not exceed 8!");
        try {
            BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data, getOffset(), getLength()))));
            return bd.movePointLeft(Math.abs(getUnit().getScale()));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Quantity parseQuantity(byte[] data) throws ProtocolException {
        if (getLength() > 8) throw new ProtocolException("ABBA1140RegisterData, parseQuantity, datalength should not exceed 8!");
        try {
           BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
           return new Quantity(bd,getUnit());
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Quantity parseNetConsumption(byte[] data) throws ProtocolException {
        if (getLength() > 8) throw new ProtocolException("ABBA1140RegisterData, parseQuantity, datalength should not exceed 8!");
        try {
            BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
            double d = bd.doubleValue();        //If necessary, convert to negative number
            if (d > 5 * (Math.pow(10, 15))) {
                d = Math.pow(10, 16) - d;
                d = -d;
            }
            return new Quantity(BigDecimal.valueOf(d),getUnit());
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Long parseBitfield(byte[] data) throws ProtocolException {
        if (getLength() > 8) throw new ProtocolException("ABBA1140RegisterData, parseBitfield, datalength should not exceed 8!");
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private Long parseLong(byte[] data) throws ProtocolException {
        if (getLength() > 8) throw new ProtocolException("ABBA1140RegisterData, parseLong, datalength should not exceed 8!");
        try {
            return new Long(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Integer parseInteger(byte[] data) throws ProtocolException {
        if (getLength() > 4) throw new ProtocolException("ABBA1140RegisterData, parseInteger, datalength should not exceed 4!");
        try {
            return new Integer(Integer.parseInt(Integer.toHexString(ProtocolUtils.getIntLE(data,getOffset(),getLength()))));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Date parseDate(byte[] data) throws ProtocolException {
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

}
