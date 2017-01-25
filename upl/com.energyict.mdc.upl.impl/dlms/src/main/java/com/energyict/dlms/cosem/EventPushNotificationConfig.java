package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.EventPushNotificationAttributes;
import com.energyict.dlms.cosem.methods.EventPushNotificationMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class EventPushNotificationConfig extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.25.9.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public EventPushNotificationConfig(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId();
    }

    public void writeSendDestinationAndMethod(int transportType, String destinationAddress, int messageType) throws IOException {
        Structure config = new Structure();
        config.addDataType(new TypeEnum(transportType));
        config.addDataType(new OctetString(destinationAddress.getBytes()));
        config.addDataType(new TypeEnum(messageType));
        write(EventPushNotificationAttributes.SEND_DESTINATION_AND_METHOD, config.getBEREncodedByteArray());
    }

    public void writePushObjectList(List<ObjectDefinition> objectDefinitionList) throws IOException {
        Array objectDefinitions = new Array();
        for (ObjectDefinition objectDefinition : objectDefinitionList) {
            Structure structure = new Structure();
            structure.addDataType(new Unsigned16(objectDefinition.getClassId()));
            structure.addDataType(OctetString.fromObisCode(objectDefinition.getObisCode()));
            structure.addDataType(new Integer8(objectDefinition.getAttributeIndex()));
            structure.addDataType(new Unsigned16(objectDefinition.getDataIndex()));
            objectDefinitions.addDataType(structure);
        }
        write(EventPushNotificationAttributes.PUSH_OBJECT_LIST, objectDefinitions.getBEREncodedByteArray());
    }

    public void writeNotificationCiphering(int notificationCiphering) throws IOException {
        TypeEnum enumeration = new TypeEnum(notificationCiphering);
        write(EventPushNotificationAttributes.NOTIFICATION_CYPHERING, enumeration.getBEREncodedByteArray());
    }

    public void setSendTestNotificationMethod(String echoTestNotification) throws IOException {
        methodInvoke(EventPushNotificationMethods.SEND_TEST_NOTIFICATION_METHOD, echoTestNotification.getBytes());
    }
}