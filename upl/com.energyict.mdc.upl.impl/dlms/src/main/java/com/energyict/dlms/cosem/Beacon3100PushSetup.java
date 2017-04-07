package com.energyict.dlms.cosem;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.ProtocolLink;
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

    public AbstractDataType readIsPushEventEnabled() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.ALARM_SERVICE_ENABLED);
    }

    public AbstractDataType readDestinationAndMethod() throws IOException {
        return readDataType(Beacon3100PushSetupAttributes.SEND_DESTINATION_AND_METHOD);
    }

    public void writeSendDestinationAndMethod(int transportType, String destinationAddress, int messageType) throws IOException {
        Structure config = new Structure();
        config.addDataType(new TypeEnum(transportType));
        config.addDataType(new OctetString(destinationAddress.getBytes()));
        config.addDataType(new TypeEnum(messageType));
        write(Beacon3100PushSetupAttributes.SEND_DESTINATION_AND_METHOD, config.getBEREncodedByteArray());
    }

    public void enable(boolean enable) throws IOException {
        write(Beacon3100PushSetupAttributes.ALARM_SERVICE_ENABLED, new BooleanObject(enable));
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
