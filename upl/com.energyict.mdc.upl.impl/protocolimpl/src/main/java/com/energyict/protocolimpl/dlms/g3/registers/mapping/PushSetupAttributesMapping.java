package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.Beacon3100PushSetup;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Mapping Push setup IC
 class id = 40, version = 0, logical name = 0-0:25.9.0.255 (0000190900FF)
 The push setup COSEM IC allows for configuration of upstream push events.
 */
public class PushSetupAttributesMapping extends RegisterMapping {


    public PushSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return Beacon3100PushSetup.getDefaultObisCode().equalsIgnoreBillingField(obisCode);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final Beacon3100PushSetup pushSetup = getCosemObjectFactory().getBeacon3100PushSetup();
        return parse(obisCode, readAttribute(obisCode, pushSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, Beacon3100PushSetup pushSetup) throws IOException {

        switch (obisCode.getF()) {

            // logical_name
            case 1:
                return OctetString.fromObisCode(Beacon3100PushSetup.getDefaultObisCode());

            // send_destination_and_method
            case 3:
                return pushSetup.readDestinationAndMethod();

            //number_of_retries
            case 6:
                return pushSetup.readNumberOfRetries();

            //repetition_delay
            case 7:
                return pushSetup.readRepetitionDelay();

            //notification_type
            case 254:
                return pushSetup.readNotificationType();

            //notification_ciphering
            case 253:
                return pushSetup.readNotificationCiphering();

            //alarm_service_enabled
            case 252:
                return pushSetup.readAlarmServiceEnabled();

            //alarm_service_event_codes
            case 251:
                return pushSetup.readAlarmServiceEventCodes();

            default:
                throw new NoSuchRegisterException("BeaconEventPushNotificationConfig attribute [" + obisCode.getF() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        try {
            switch (obisCode.getF()) {

                // Logical name
                case 1:
                    return new RegisterValue(obisCode, Beacon3100PushSetup.getDefaultObisCode().toString());

                // send_destination_and_method
                case 3:
                    return parseDestinationAndMethod(obisCode, abstractDataType);

                //number_of_retries
                case 6:
                    int retries = abstractDataType.getUnsigned8().getValue();
                    return new RegisterValue(obisCode, new Quantity(retries, Unit.getUndefined()));

                //repetition_delay
                case 7:
                    int delay = abstractDataType.getUnsigned16().getValue();
                    return new RegisterValue(obisCode, new Quantity(delay, Unit.getUndefined()));

                //notification_type
                case 254:
                    return new RegisterValue(obisCode, NotificationType.findName(abstractDataType.getTypeEnum().getValue()));

                //notification_ciphering
                case 253:
                    return new RegisterValue(obisCode, NotificationCiphering.findName(abstractDataType.getTypeEnum().getValue()));

                //alarm_service_enabled
                case 252:
                    boolean isEnabled = abstractDataType.getBooleanObject().getState();
                    return new RegisterValue(obisCode, new Quantity(isEnabled?1:0, Unit.getUndefined()));

                //alarm_service_event_codes
                case 251:
                    return parseAlarmServiceEventCodes(obisCode, abstractDataType);

                default:
                    throw new NoSuchRegisterException("BeaconPushSetup attribute [" + obisCode.getB() + "] not supported!");

            }
        } catch (JSONException e){
            return null;
        }
    }

    private RegisterValue parseAlarmServiceEventCodes(ObisCode obisCode, AbstractDataType abstractDataType) throws JSONException {
        Array array = abstractDataType.getArray();

        JSONArray jsonArray = new JSONArray();
        for (AbstractDataType item : array.getAllDataTypes()){
            //An event code is a concatenation of the DLMS code and device-code (which results in an 32-bit unsigned integer).

            long fullEventCode = item.getUnsigned32().longValue();
            long dlmsCode = fullEventCode >> 16;
            long deviceCode = fullEventCode & 0xFFFF;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dlmsCode", dlmsCode);
            jsonObject.put("deviceCode", deviceCode);
            jsonArray.put(jsonObject);
        }
        return new RegisterValue(obisCode, jsonArray.toString());
    }

    private enum TransportServiceType{
        TCP(0),
        UDP(1),
        FTP(2),
        SMTP(3),
        SMS(4),
        HDLC(5),
        M_Bus(6),
        ZigBee(7);

        private int id = 0;
        TransportServiceType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static String findName(int id){
            for (TransportServiceType type : values()){
                if (type.getId() == id){
                    return type.toString();
                }
            }
            return "Reserved:"+id;
        }
    }

    private enum MessageType{
        AXDR(0),
        XML(1);

        private int id = 0;
        MessageType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static String findName(int id){
            for (MessageType type : values()){
                if (type.getId() == id){
                    return type.toString();
                }
            }
            return "Reserved:"+id;
        }
    }
    private RegisterValue parseDestinationAndMethod(ObisCode obisCode, AbstractDataType abstractDataType) throws JSONException {
        Structure structure = abstractDataType.getStructure();
        TypeEnum transportService = structure.getDataType(0).getTypeEnum();
        OctetString destination = structure.getDataType(1).getOctetString();
        TypeEnum message = structure.getDataType(2).getTypeEnum();

        JSONObject json = new JSONObject();
        json.put("transportService", TransportServiceType.findName(transportService.getValue()));
        json.put("destination", destination.stringValue());
        json.put("message", MessageType.findName(message.getValue()));

        return new RegisterValue(obisCode, json.toString());
    }

    private enum NotificationType{
        DISABLED(0),
        EVENT_NOTIFICATION(1),
        DATA_NOTIFICATION(2);

        private int id = 0;
        NotificationType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static String findName(int id){
            for (NotificationType type : values()){
                if (type.getId() == id){
                    return type.toString();
                }
            }
            return "Unknown:"+id;
        }
    }

    private enum NotificationCiphering{
        NONE(0),
        GLOBAL_CIPHERING(1),
        GLOBAL_CIPHERING_WITH_SIGNATURE(2);

        private int id = 0;
        NotificationCiphering(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static String findName(int id){
            for (NotificationCiphering type : values()){
                if (type.getId() == id){
                    return type.toString();
                }
            }
            return "Unknown:"+id;
        }
    }
}
