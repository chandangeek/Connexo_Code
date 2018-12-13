package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributeobjects.RegisterZigbeeDeviceData;
import com.energyict.dlms.cosem.attributeobjects.ZigBeeIEEEAddress;
import com.energyict.dlms.cosem.attributes.ZigbeeSETCControlAttributes;
import com.energyict.dlms.cosem.methods.ZigbeeSETCControlMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * SE T C = SEcurity Trust Center
 *
 * Copyrights EnergyICT
 * Date: 4/08/11
 * Time: 7:31
 */
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

    public byte[] backupHAN() throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.BACKUP_HAN, new Unsigned8(0));
    }

    public byte[] restoreHAN(Structure backUp) throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.RESTORE_HAN, backUp);
    }

    public byte[] removeMirror(Structure mirrorDataStructure) throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.REMOVE_MIRROR, mirrorDataStructure);
    }

    public byte[] updateLinkKey(OctetString ieeeAddress) throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.UPDATE_LINK_KEY, ieeeAddress);
    }

    public byte[] createHAN() throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.CREATE_HAN, new Unsigned8(0));
    }

    public byte[] removeHAN() throws IOException {
        return methodInvoke(ZigbeeSETCControlMethods.REMOVE_HAN, new Unsigned8(0));
    }

}
