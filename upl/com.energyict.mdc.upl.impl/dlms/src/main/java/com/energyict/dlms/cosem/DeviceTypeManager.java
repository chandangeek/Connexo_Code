package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributes.DeviceTypeManagerAttributes;
import com.energyict.dlms.cosem.methods.DeviceTypeManagerMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 13:50
 */
public class DeviceTypeManager extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBISCODE = ObisCode.fromString("0.0.128.0.14.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public DeviceTypeManager(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBISCODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.DEVICE_TYPE_MANAGER.getClassId();
    }

    public Array readDeviceTypes() throws IOException {
        return readDataType(DeviceTypeManagerAttributes.DEVICE_TYPES, Array.class);
    }

    /**
     * Adds a device type to the set of known device types for this concentrator.
     */
    public void addDeviceType(Structure deviceType) throws IOException {
        methodInvoke(DeviceTypeManagerMethods.ADD_DEVICE_TYPE, deviceType);
    }

    /**
     * Removes a device type from the set of known device types for this concentrator.
     */
    public void removeDeviceType(long id) throws IOException {
        methodInvoke(DeviceTypeManagerMethods.REMOVE_DEVICE_TYPE, new Unsigned32(id));
    }

    /**
     * Updates the device type structure with the same ID as passed in the structure.
     */
    public void updateDeviceType(Structure deviceType) throws IOException {
        methodInvoke(DeviceTypeManagerMethods.UPDATE_DEVICE_TYPE, deviceType);
    }

    /**
     * Assigns a particular device type ID to a particular device (EUI64).
     */
    public void assignDeviceType(Structure meterDetails) throws IOException {
        methodInvoke(DeviceTypeManagerMethods.ASSIGN_DEVICE_TYPE, meterDetails);
    }
}