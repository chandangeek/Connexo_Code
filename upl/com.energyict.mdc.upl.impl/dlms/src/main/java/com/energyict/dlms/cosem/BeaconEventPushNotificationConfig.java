package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.BeaconEventPushNotificationAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class BeaconEventPushNotificationConfig extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.12.255");

    /**
     * Creates a new instance of BeaconEventPushNotificationConfig
     */
    public BeaconEventPushNotificationConfig(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.EVENT_NOTIFICATION.getClassId();
    }

    public AbstractDataType readIsPushEventEnabled() throws IOException {
        return readDataType(BeaconEventPushNotificationAttributes.IS_ENABLED);
    }

    public AbstractDataType readDestinationAndMethod() throws IOException {
        return readDataType(BeaconEventPushNotificationAttributes.SEND_DESTINATION_AND_METHOD);
    }

    public void writeSendDestinationAndMethod(int transportType, String destinationAddress, int messageType) throws IOException {
        Structure config = new Structure();
        config.addDataType(new TypeEnum(transportType));
        config.addDataType(new OctetString(destinationAddress.getBytes()));
        config.addDataType(new TypeEnum(messageType));
        write(BeaconEventPushNotificationAttributes.SEND_DESTINATION_AND_METHOD, config.getBEREncodedByteArray());
    }

    public void enable(boolean enable) throws IOException {
        write(BeaconEventPushNotificationAttributes.IS_ENABLED, new BooleanObject(enable));
    }
}