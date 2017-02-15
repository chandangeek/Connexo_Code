/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.dlms.cosem.attributes.ZigbeeSETCControlAttributes;
import com.energyict.dlms.cosem.methods.ZigbeeSETCControlMethods;

import java.io.IOException;

public class ZigBeeSETCControl extends AbstractCosemObject {

    public static final ObisCode LN = ObisCode.fromString("0.0.35.2.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     */
    public ZigBeeSETCControl(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN.getLN()));
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.ZIGBEE_SETC_CONTROL.getClassId();
    }

    public BooleanObject getEnableDisableJoining() throws IOException {
        return new BooleanObject(getResponseData(ZigbeeSETCControlAttributes.ENABLE_DISABLE_JOINING), 0);
    }

    public void writeEnableDisableJoining(BooleanObject enableDisableJoining) throws IOException {
        write(ZigbeeSETCControlAttributes.ENABLE_DISABLE_JOINING, enableDisableJoining.getBEREncodedByteArray());
    }

    public void writeEnableDisableJoining(boolean enableDisableJoining) throws IOException {
        writeEnableDisableJoining(new BooleanObject(enableDisableJoining));
    }

    public Unsigned16 getJoinTimeout() throws IOException {
        return new Unsigned16(getResponseData(ZigbeeSETCControlAttributes.JOIN_TIMEOUT), 0);
    }

    public void writeJoinTimeout(Unsigned16 joinTimeout) throws IOException {
        write(ZigbeeSETCControlAttributes.JOIN_TIMEOUT, joinTimeout.getBEREncodedByteArray());
    }

    public byte[] registerDevice(RegisterZigbeeDeviceData zigbeeDeviceData) throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.REGISTER_DEVICE, zigbeeDeviceData);
    }

    public byte[] unRegisterDevice(ZigBeeIEEEAddress zigbeeDeviceData) throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.UNREGISTER_DEVICE, zigbeeDeviceData);
    }

    public byte[] unRegisterAllDevices() throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.UNREGISTER_ALL_DEVICES, new Unsigned8(0));
    }

}
