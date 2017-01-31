/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.attributes.BeaconEventPushNotificationAttributes;

import java.io.IOException;

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