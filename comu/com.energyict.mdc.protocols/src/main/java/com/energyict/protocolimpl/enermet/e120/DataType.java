package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.common.ApplicationException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Parser for composite datatypes in the application layer.  Only 3 types
 * are used:
 *
 * - Strings: zero terminated ascii strings
 * - Time: braindamaged time type of 7 bytes, but not millis
 * - RegisterValue: all meters readings, registers and profile data
 *
 * All the different kind of Type objects are held together by the DataType
 * class.
 *
 * @author fbo
 */
class DataType {

    private final TimeZone timeZone;

    private final StringType string;
    private final TimeType time;
    private final RegisterValueType registerValue;

    DataType(TimeZone timeZone){
        this.timeZone = timeZone;

        this.string = new StringType();
        this.time = new TimeType();
        this.registerValue = new RegisterValueType();
    }

    StringType getString( ){
        return string;
    }

    TimeType getTime(){
        return time;
    }

    RegisterValueType getRegisterValue(){
        return registerValue;
    }

    abstract class Type {
        public abstract Object parse(ByteArray input);

    }

    class StringType extends Type {
        public Object parse(ByteArray input) {
            return new String (input.sub(0,input.indexOf((byte)0x00)).getBytes());
        }
    }

    class TimeType extends Type {

        public Object parse(ByteArray input) {

            Calendar calendar=Calendar.getInstance(timeZone);
            calendar.clear();

            calendar.set(Calendar.YEAR, input.shortValue(0));
            calendar.set(Calendar.MONTH, input.byteValue(2) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, input.byteValue(3));
            calendar.set(Calendar.HOUR_OF_DAY, input.byteValue(4));
            calendar.set(Calendar.MINUTE, input.byteValue(5));
            calendar.set(Calendar.SECOND, input.byteValue(6));

            return calendar.getTime();

        }

        public ByteArray construct(Date time){

            ByteArray result = new ByteArray();

            Calendar c = Calendar.getInstance(timeZone);
            c.setTime(time);

            result.addShort( c.get(Calendar.YEAR) );
            result.addByte( c.get(Calendar.MONTH) + 1 );
            result.addByte( c.get(Calendar.DAY_OF_MONTH) );
            result.addByte( c.get(Calendar.HOUR_OF_DAY) );
            result.addByte( c.get(Calendar.MINUTE) );
            result.addByte( c.get(Calendar.SECOND) );

            return result;

        }

    }

    class RegisterValueType extends Type {

        public Object parse(ByteArray input) {

            int raw = input.intValue(0);
            Unit unit = UnitMap.get(input.byteValue(4));

            int state = input.byteValue(5);

            int decimals = state >> 5;
            int status = (state & 0x1E) >> 1;
            int reg_state = state & 0x01;

            BigDecimal bd = new BigDecimal(raw).movePointLeft(decimals);

            return
                new E120RegisterValue()
                    .setQuantity(new Quantity(bd, unit))
                    .setRawStatus(status)
                    .setRegisterStatus(RegisterStatus.getStatuses((byte)status))
                    .setTime((Date)time.parse( input.sub(6,7) ) )
                    .setInUse(reg_state > 0);

        }

    }

    class SecondsType extends Type {
        public Object parse(ByteArray input) {
            String msg = "SecondsType not supported.";
            throw new ApplicationException(msg);
        }
    }

    class AuthLevelType extends Type {
        public Object parse(ByteArray input) {
            String msg = "AuthType not supported.";
            throw new ApplicationException(msg);
        }
    }

    class OutputIndexType extends Type {
        public Object parse(ByteArray input) {
            String msg = "OutputIndexType not supported.";
            throw new ApplicationException(msg);
        }
    }

    class OutputStateType extends Type {
        public Object parse(ByteArray input) {
            String msg = "OutputStateType not supported.";
            throw new ApplicationException(msg);
        }
    }

    class RegisterIndexType extends Type {
        public Object parse(ByteArray input) {
            String msg = "RegisterIndexType not supported.";
            throw new ApplicationException(msg);
        }
    }

    class HistoricLevelType extends Type {
        public Object parse(ByteArray input) {
            String msg = "HistoricLevelType not supported.";
            throw new ApplicationException(msg);
        }
    }

}
