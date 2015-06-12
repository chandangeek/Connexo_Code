package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 13:11
 */
public class G3DeviceInfo {

    private static final ObisCode LOGICAL_DEVICE_NAME_OBIS = ObisCode.fromString("0.0.42.0.0.255");
    private static final ObisCode FIRMWARE_VERSIONS_OBIS = ObisCode.fromString("1.0.0.2.0.255");

    private final CosemObjectFactory cosemObjectFactory;

    private String serial = null;
    private String firmware = null;

    public G3DeviceInfo(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    /**
     * Read the serial number from the device and cache it
     */
    public final String getSerialNumber() throws IOException {
        if (serial == null) {
            final Data data = cosemObjectFactory.getData(LOGICAL_DEVICE_NAME_OBIS);
            final OctetString logicalDeviceName = data.getValueAttr(OctetString.class);
            byte[] rawSerial = logicalDeviceName.getOctetStr();
            SerialNumber serialNumber = SerialNumber.fromBytes(rawSerial); //Parsing error throws IOException too
            this.serial = serialNumber.getEuridisADS();
        }
        return serial;
    }

    /**
     * Read the firmware versions from the device and cache them
     */
    public final String getFirmwareVersions() throws IOException {
        if (firmware == null) {
            final Data data = cosemObjectFactory.getData(FIRMWARE_VERSIONS_OBIS);
            Array fwEntries = data.getValueAttr(Array.class);
            final StringBuilder sb = new StringBuilder();
            String fwVersionEntry;
            for (int i = 0; i < fwEntries.nrOfDataTypes(); i++) {
                fwVersionEntry = FirmwareVersion.fromStructure(fwEntries.getDataType(i, Structure.class)).getDisplayString();
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append('[').append(fwVersionEntry).append(']');
            }
            this.firmware = sb.toString();
        }
        return firmware;
    }
}