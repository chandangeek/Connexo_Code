package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.BatteryVoltageLowEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorArmDisconnectEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorArmLoadMonitorEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorArmModuleEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorArmOpticalEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorCloseButtonEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorCloseModuleEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorCloseOpticalEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorOpenAutoDisconnectEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorOpenLoadMonitorHighEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorOpenLoadMonitorLowEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorOpenModuleEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ContactorOpenOpticalEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.EndOfBillingEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.LongPowerFailEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.MagneticTamperEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.MainCoverEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.MeterErrorEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.OverVoltageEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.PowerFailEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ProgrammingEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.ReverserunEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.TerminalCoverEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.TransientEventLog;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.UnderVoltageEventLog;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/** @author  Koen */

abstract public class ABBA230RegisterData {

    final static int ABBA_STRING=0;
    final static int ABBA_DATE=1;
    final static int ABBA_NUMBER=2;
    final static int ABBA_LONG=3;
    final static int ABBA_BYTEARRAY=4;
    final static int ABBA_QUANTITY=5;
    final static int ABBA_INTEGER=6;
    final static int ABBA_64BITFIELD=7;
    final static int ABBA_BIGDECIMAL=8;
    final static int ABBA_HEX=9;
    final static int ABBA_HEX_LE=10;
    final static int ABBA_MD=11;
    final static int ABBA_CMD=12;
    final static int ABBA_HISTORICALVALUES=13;
    final static int ABBA_REGISTER=14;
    final static int ABBA_HISTORICALEVENTS=15;
    final static int ABBA_SYSTEMSTATUS=16;
    final static int ABBA_TARIFFSOURCES=17;
    final static int ABBA_HISTORICALDISPLAYSCALINGS=18;
    final static int ABBA_MDSOURCES=19;
    final static int ABBA_CUSTDEFREGCONFIG=20;
    final static int ABBA_INSTANTANEOUSVALUES=21;
    final static int ABBA_LOAD_PROFILE_INTEGRATION_PERIOD=22;
    final static int ABBA_LOAD_PROFILE_BY_DATE=23;
    final static int ABBA_LOAD_PROFILE_CONFIG=24;
    final static int ABBA_OVERVOLTAGEEVENTLOG=25;
    final static int ABBA_UNDERVOLTAGEEVENTLOG=26;
    final static int ABBA_PROGRAMMINGEVENTLOG=27;
    final static int ABBA_LONGPOWERFAILEVENTLOG=28;
    final static int ABBA_TERMINALCOVEREVENTLOG=29;
    final static int ABBA_MAINCOVEREVENTLOG=30;
    final static int ABBA_MAGNETICTAMPEREVENTLOG=31;
    final static int ABBA_REVERSERUNEVENTLOG=32;
    final static int ABBA_POWEREFAILEVENTLOG=33;
    final static int ABBA_TRANSIENTEVENTLOG=34;
    final static int ABBA_ENDOFBILLINGEVENTLOG=35;
    final static int ABBA_METERERROREVENTLOG=36;
    final static int ABBA_BATTERYVOLTAGELOWEVENTLOG=37;


    final static int ABBA_CONTACTOROPENOPTICALLOG=38;
    final static int ABBA_CONTACTOROPENMODULELOG=39;
    final static int ABBA_CONTACTORLOADMONITORLOWLOG=40;
    final static int ABBA_CONTACTOROPENLOADMONITORHIGHLOG=41;
    final static int ABBA_CONTACTOROPENAUTODISCONNECTLOG=42;
    final static int ABBA_CONTACTORARMOPTICALLOG=43;
    final static int ABBA_CONTACTORARMMODULELOG=44;
    final static int ABBA_CONTACTORARMLOADMONITORLOG=45;
    final static int ABBA_CONTACTORARMDISCONNECTLOG=46;
    final static int ABBA_CONTACTORCLOSEOPTICALLOG=47;
    final static int ABBA_CONTACTORCLOSEMODULELOG=48;
    final static int ABBA_CONTACTORCLOSEBUTTONLOG=49;

    final static int ABBA_INSTUMENTATION_PROFILE_INTEGRATION_PERIOD = 50;
    final static int ABBA_INSTRUMENTATION_PROFILE_BY_DATE = 51;
    final static int ABBA_INSTRUMENTATION_PROFILE_CONFIG = 52;

    abstract protected Unit getUnit();
    abstract protected int getType();
    abstract protected FlagIEC1107Connection getFlagIEC1107Connection();
    abstract protected ProtocolLink getProtocolLink();
    abstract protected ABBA230RegisterFactory getRegisterFactory();
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
                return buildHex((byte[])object);

            case ABBA_HEX_LE:
                return buildHexLE((Long) object);

            case ABBA_LOAD_PROFILE_INTEGRATION_PERIOD:
                return null;

            case ABBA_INSTUMENTATION_PROFILE_INTEGRATION_PERIOD:
                return null;

            case ABBA_LOAD_PROFILE_BY_DATE:
                return build((ProfileReadByDate)object);

            case ABBA_INSTRUMENTATION_PROFILE_BY_DATE:
                return build((ProfileReadByDate)object);

            default:
                throw new IOException("ABBA230RegisterData, parse , unknown type "+getType());
        }

    }

    private String buildHexLE(Long val) {
        long lVal = val.longValue();
        byte[] data = new byte[4];
        ProtocolUtils.val2HEXascii((int)lVal&0xFF,data,0);
        ProtocolUtils.val2HEXascii((int)(lVal>>8)&0xFF,data,2);

        return new String(data);
    }

    private String buildHex(byte[] val) {
        byte[] data = new byte[val.length*2];
        for (int i=0;i<val.length;i++) {
			ProtocolUtils.val2HEXascii((int)val[i]&0xFF,data,i*2);
		}
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

    private String build(ProfileReadByDate profileReadByDate) {

        byte [] ba = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        long shift = profileReadByDate.getFrom().getTime() / 1000;
        byte [] hex = ProtocolUtils.buildStringHex( shift, 8 ).getBytes();
        for( int i = 0; i < hex.length; i=i+2 ) {
            byte [] t = ProtocolUtils.getSubArray2(hex, 6-i, 2);
            System.arraycopy(t, 0, ba, i, 2);
        }

        shift = profileReadByDate.getTo().getTime() / 1000;
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
                    return new SystemStatus(data);

                case ABBA_TARIFFSOURCES:
                    return new TariffSources(data);

                case ABBA_MDSOURCES:
                    return new MDSources(data);

                case ABBA_CUSTDEFREGCONFIG:
                    return new CustDefRegConfig(data);

                case ABBA_LOAD_PROFILE_INTEGRATION_PERIOD:
                    return new Integer( getRegisterFactory().getDataType().integrationPeriod.parse(data[0]) );

                case ABBA_INSTUMENTATION_PROFILE_INTEGRATION_PERIOD:
                    return new Integer( getRegisterFactory().getDataType().integrationPeriod.parse(data[0]) );

                case ABBA_LOAD_PROFILE_BY_DATE: {
                    throw new ProtocolException("ABBA230RegisterData, parse, "
                            + "type can only be read " + getType());
                }

                case ABBA_INSTRUMENTATION_PROFILE_BY_DATE: {
                    throw new ProtocolException("ABBA230RegisterData, parse, "
                            + "type can only be read " + getType());
                }

                case ABBA_LOAD_PROFILE_CONFIG:
                    LoadProfileConfigRegister loadProfileConfigRegister = new LoadProfileConfigRegister();
                    loadProfileConfigRegister.loadConfig(getRegisterFactory(), data);
                    return loadProfileConfigRegister;

                case ABBA_INSTRUMENTATION_PROFILE_CONFIG:
                    InstrumentationProfileConfigRegister instrumentationProfileConfigRegister = new InstrumentationProfileConfigRegister();
                    instrumentationProfileConfigRegister.loadConfig(getRegisterFactory(), data);
                    return instrumentationProfileConfigRegister;

                case ABBA_OVERVOLTAGEEVENTLOG: {
                	OverVoltageEventLog o = new OverVoltageEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_UNDERVOLTAGEEVENTLOG: {
                	UnderVoltageEventLog o = new UnderVoltageEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_PROGRAMMINGEVENTLOG: {
                	ProgrammingEventLog o = new ProgrammingEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_LONGPOWERFAILEVENTLOG: {
                	LongPowerFailEventLog o = new LongPowerFailEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

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

                case ABBA_MAGNETICTAMPEREVENTLOG: {
                	MagneticTamperEventLog o = new MagneticTamperEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_REVERSERUNEVENTLOG: {
                	ReverserunEventLog o = new ReverserunEventLog(getProtocolLink().getTimeZone());
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

                case ABBA_CONTACTOROPENOPTICALLOG: {
                	ContactorOpenOpticalEventLog o = new ContactorOpenOpticalEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTOROPENMODULELOG: {
                	ContactorOpenModuleEventLog o = new ContactorOpenModuleEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORLOADMONITORLOWLOG: {
                	ContactorOpenLoadMonitorLowEventLog o = new ContactorOpenLoadMonitorLowEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTOROPENLOADMONITORHIGHLOG: {
                	ContactorOpenLoadMonitorHighEventLog o = new ContactorOpenLoadMonitorHighEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTOROPENAUTODISCONNECTLOG: {
                	ContactorOpenAutoDisconnectEventLog o = new ContactorOpenAutoDisconnectEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORARMOPTICALLOG: {
                	ContactorArmOpticalEventLog o = new ContactorArmOpticalEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_CONTACTORARMMODULELOG: {
                	ContactorArmModuleEventLog o = new ContactorArmModuleEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORARMLOADMONITORLOG: {
                	ContactorArmLoadMonitorEventLog o = new ContactorArmLoadMonitorEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORARMDISCONNECTLOG: {
                	ContactorArmDisconnectEventLog o = new ContactorArmDisconnectEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORCLOSEOPTICALLOG: {
                	ContactorCloseOpticalEventLog o = new ContactorCloseOpticalEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORCLOSEMODULELOG: {
                	ContactorCloseModuleEventLog o = new ContactorCloseModuleEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }
                case ABBA_CONTACTORCLOSEBUTTONLOG: {
                	ContactorCloseButtonEventLog o = new ContactorCloseButtonEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_METERERROREVENTLOG: {
                	MeterErrorEventLog o = new MeterErrorEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }

                case ABBA_BATTERYVOLTAGELOWEVENTLOG: {
                	BatteryVoltageLowEventLog o = new BatteryVoltageLowEventLog(getProtocolLink().getTimeZone());
                	o.parse(data);
                	return o;
                }


                default:
                    throw new ProtocolException("ABBA230RegisterData, parse , unknown type " + getType());
            }
        }
        catch(NumberFormatException e) {
            throw new ProtocolException("ABBA230RegisterData, parse error:" + e.getMessage());
        }
    }

    private Long parseLongHexLE(byte[] data) throws ProtocolException {
        return new Long(ProtocolUtils.getLongLE(data,getOffset(),getLength()));
    }
    private Long parseLongHex(byte[] data) throws ProtocolException{
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private BigDecimal parseBigDecimal(byte[] data) throws ProtocolException {
        if (getLength() > 8){
        	throw new ProtocolException("Elster A230RegisterData, parseBigDecimal, datalength should not exceed 8!");
        }
        try{
            BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
            return bd.movePointLeft(Math.abs(getUnit().getScale()));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Quantity parseQuantity(byte[] data) throws ProtocolException {
        if (getLength() > 8) {
			throw new ProtocolException("Elster A230RegisterData, parseQuantity, datalength should not exceed 8!");
		}
        try{
            BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
            return new Quantity(bd,getUnit());
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

    private Long parseBitfield(byte[] data) throws ProtocolException {
        if (getLength() > 8) {
			throw new ProtocolException("Elster A230RegisterData, parseBitfield, datalength should not exceed 8!");
		}
        return new Long(ProtocolUtils.getLong(data,getOffset(),getLength()));
    }

    private Long parseLong(byte[] data) throws ProtocolException {
        if (getLength() > 8) {
			throw new ProtocolException("Elster A230RegisterData, parseLong, datalength should not exceed 8!");
		}
        try{
            return new Long(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,getOffset(),getLength()))));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }

    }

    private Integer parseInteger(byte[] data) throws ProtocolException{
        if (getLength() > 4) {
			throw new ProtocolException("Elster A230RegisterData, parseInteger, datalength should not exceed 4!");
		}
        try{
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
