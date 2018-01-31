package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.Beacon3100PushSetupAttributes;
import com.energyict.dlms.cosem.methods.Beacon3100PushSetupMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Push setup IC
 * class id = 40, version = 0, logical name = 0-0:25.9.0.255 (0000190900FF)
 * The push setup COSEM IC allows for configuration of upstream push events.
 */
public class Beacon3100PushSetup extends AbstractCosemObject {

    private static final ObisCode OBIS_CODE_PUSH_SETUP = ObisCode.fromString("0.0.25.9.0.255");

    public Beacon3100PushSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId();
    }

    @Override
    public ObisCode getObisCode() {
        return getDefaultObisCode();
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE_PUSH_SETUP;
    }

    public AbstractDataType readDestinationAndMethod() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.SEND_DESTINATION_AND_METHOD);
    }

    public AbstractDataType readNumberOfRetries() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.NUMBER_OF_RETRIES);
    }

    public AbstractDataType readRepetitionDelay() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.REPETITION_DELAY);
    }

    public AbstractDataType readNotificationType() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.NOTIFICATION_TYPE);
    }

    public AbstractDataType readNotificationCiphering() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.NOTIFICATION_CIPHERING);
    }

    public AbstractDataType readAlarmServiceEnabled() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.ALARM_SERVICE_ENABLED);
    }

    public AbstractDataType readAlarmServiceEventCodes() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.ALARM_SERVICE_EVENT_CODES);
    }


    public void writeSendDestinationAndMethod(int transportType, String destinationAddress, int messageType) throws IOException {
        Structure config = new Structure();
        config.addDataType(new TypeEnum(transportType));
        config.addDataType(new OctetString(destinationAddress.getBytes()));
        config.addDataType(new TypeEnum(messageType));
        write(Beacon3100PushSetupAttributes.SEND_DESTINATION_AND_METHOD, config.getBEREncodedByteArray());
    }

    /**
     * Push setup IC
     * The push setup COSEM IC allows for configuration of upstream push events.
     * 0 disabled
     * 1 event_notification
     * 2 DLMS data notifications
     */
    public void enable(int notificationOption) throws IOException {
        write(Beacon3100PushSetupAttributes.NOTIFICATION_TYPE, new TypeEnum(notificationOption));
    }

    public boolean setSendTestNotificationMethod(String echoTestNotification) throws IOException {
        UTF8String requestData = UTF8String.fromString(echoTestNotification);

        byte[] resultBytes = methodInvoke(Beacon3100PushSetupMethods.SEND_TEST_NOTIFICATION_METHOD, requestData);
        AbstractDataType result = AXDRDecoder.decode(resultBytes);
        if (result==null){
            return false;
        }

        if (result.isBooleanObject()){
            return result.getBooleanObject().getState();
        }

        return false;
    }


    public void writeNotificationCiphering(int notificationCiphering) throws IOException {
        TypeEnum enumeration = new TypeEnum(notificationCiphering);
        write(Beacon3100PushSetupAttributes.NOTIFICATION_CIPHERING, enumeration.getBEREncodedByteArray());
    }

}
