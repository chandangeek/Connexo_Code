package com.energyict.protocolimpl.enermet.e120;

import com.energyict.protocolimpl.enermet.e120.DataType.RegisterValueType;

import java.util.LinkedHashMap;
import java.util.Map;

class MessageType {

    private static Map all = new LinkedHashMap();

    /* Login session */
    public static MessageType AUTHENTICATION =
        create( 0x0000, "Authentication", new DefaultParser());

    /* Device serial number */
    public static MessageType DEVICE_ID =
        create( 0x0005, "Device ID", new DeviceIdParser());

    /* System time used in series and logs. No DST changes. */
    public static MessageType GET_TIME =
        create(0x0007, "Get time", new GetTimeParser());

    /* Time set with limits */
    public static MessageType SET_TIME_NORMAL =
        create(0x0008, "Set time, normal absolute", new DefaultParser());

    /* Time set w/o limits */
    public static MessageType SET_TIME_MASTER =
        create(0x000A, "Set time, master absolute", new DefaultParser());

    /* Get register value and status info */
    public static MessageType REGISTER_VALUE =
        create(0x0014, "Register value", new RegisterParser() );

    /* Values from a period of time */
    public static MessageType SERIES_ON_TIME =
        create(0x000017, "Multiple series based on time", new SeriesParser());

    /* Values starting from given time */
    public static MessageType SERIES_ON_COUNT =
        create(0x0018, "Multiple series based on count", new SeriesParser());

    /* High priority dynamic relay control Tariff control messages */
    public static MessageType AD_HOC_RELAY_READ =
        create(0x0019, "Ad-hoc relay control read", new DefaultParser());

    /* Tarif control messages */
    public static MessageType AD_HOC_RELAY_WRITE =
        create(0x001A, "Ad-hoc relay control write", new DefaultParser());

    /* Dynamic control of relays Tariff control messages */
    public static MessageType DYNAMIC_RELAY_READ =
        create(0x001B, "Dynamic relay control read", new DefaultParser());

    /* Tariff control messages */
    public static MessageType DYNAMIC_RELAY_WRITE =
        create(0x001C, "Dynamic relay control write", new DefaultParser());

    /* */
    public static MessageType TIME_ZONE_READ =
        create(0x0205, "Authentication", new DefaultParser());

    /* */
    public static MessageType TIME_ZONE_WRITE =
        create(0x0206, "Authentication", new DefaultParser());

    /* Tariff control messages */
    public static MessageType WEEK_CLOCK_SEASON_READ =
        create(0x0207, "Week clock season configuration read", new DefaultParser());

    /* Tariff control messages */
    public static MessageType WEEK_CLOCK_SEASON_WRITE =
        create(0x0208, "Week clock season configuration write", new DefaultParser());

    /* Tariff control messages */
    public static MessageType WEEK_CLOCK_READ =
        create(0x0209, "Week clock configuration read (new)", new DefaultParser());

    /* Tariff control messages */
    public static MessageType WEEK_CLOCK_WRITE =
        create(0x0020A, "Week clock configuration write (new)", new DefaultParser());

    private int id;
    private String name;
    private Parser parser;

    private static MessageType create(int id, String description, Parser parser){
        MessageType mi = new MessageType(id, description, parser);
        all.put(""+id, mi);
        return mi;
    }

    private MessageType(int id, String name, Parser parser) {
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    Response parse(E120 e120, ByteArray ba){
        return parser.parse(e120, ba);
    }

    public short shortValue(){
        return (short)id;
    }

    public static MessageType get(int id){
        return (MessageType)all.get(""+id);
    }

    public String toString( ){
        String s = "0x" + Integer.toHexString(id);
        return "MessageId [" + s + ", " + name + "]";
    }

    static class DefaultParser implements Parser {
        /** return */
        public Response parse(E120 e120, ByteArray byteArray) {

            NackCode nack = NackCode.get(byteArray.byteValue(2));
            return new DefaultResponse(nack);

        }
    }

    static class DeviceIdParser implements Parser {

        public Response parse(E120 e120, ByteArray byteArray) {

            NackCode nack = NackCode.get(byteArray.byteValue(2));
            DefaultResponse response = new DefaultResponse(nack);

            if( !nack.isOk() ) return response;

            ByteArray vba = byteArray.sub(3);
            Object v = (Object)e120.getDataType().getString().parse(vba);

            response.setValue(v);
            return response;

        }

    }

    static class GetTimeParser implements Parser {

        public Response parse(E120 e120, ByteArray byteArray) {

            NackCode nack = NackCode.get(byteArray.byteValue(2));
            DefaultResponse response = new DefaultResponse(nack);

            if( !nack.isOk() ) return response;

            ByteArray vba = byteArray.sub(3);
            Object v = (Object)e120.getDataType().getTime().parse(vba);

            response.setValue(v);
            return response;

        }

    }

    static class RegisterParser implements Parser {

        public Response parse(E120 e120, ByteArray byteArray) {

            NackCode nack = NackCode.get(byteArray.byteValue(2));
            DefaultResponse response = new DefaultResponse(nack);

            if( !nack.isOk() ) return response;

            ByteArray vba = byteArray.sub(5);
            Object v = (Object)e120.getDataType().getRegisterValue().parse(vba);
            response.setValue(v);

            return response;

        }

    }

    static class SeriesParser implements Parser {

        public Response parse(E120 e120, ByteArray byteArray) {

            NackCode nack = NackCode.get(byteArray.byteValue(2));
            SeriesResponse response = new SeriesResponse(nack);

            if( !nack.isOk() ) return response;

            response.setRegisterIndex(byteArray.byteValue(3));
            int valueCount = byteArray.shortValue(4);

            RegisterValueType rParser = e120.getDataType().getRegisterValue();

            for(int i = 0; i < valueCount; i++ ) {
                ByteArray register = byteArray.sub(6+(i*13), 13);
                E120RegisterValue registerValue = null;
                registerValue = (E120RegisterValue)rParser.parse(register);

                if( !registerValue.isIllegal() )
                    response.addValue(registerValue);
                else
                    System.out.println( "not good " + registerValue );
            }

            return response;

        }

    }

}
