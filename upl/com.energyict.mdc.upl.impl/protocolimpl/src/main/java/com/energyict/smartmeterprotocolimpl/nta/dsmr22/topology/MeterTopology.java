package com.energyict.smartmeterprotocolimpl.nta.dsmr22.topology;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.SmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr22.composedobjects.ComposedMbusSerialNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 16:56:32
 */
public class MeterTopology implements MasterMeter {

    private static final ObisCode MbusClientObisCode = ObisCode.fromString("0.0.24.1.0.255");
    private static final int ObisCodeBFieldIndex = 1;

    public static final int MaxMbusDevices = 4;

    private final SmartNtaProtocol protocol;

    /**
     * The <CODE>ComposedCosemObject</CODE> for requesting all serialNumbers in 1 request
     */
    private ComposedCosemObject discoveryComposedCosemObject;

    /**
     * A List of localComposedCosemObjects containing the attributes to construct the serialNumber of the devices
     */
    private List<ComposedMbusSerialNumber> cMbusSerialNumbers = new ArrayList<ComposedMbusSerialNumber>();

    /**
     * A list of MbusMeter <CODE>DeviceMappings</CODE>
     */
    private List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();

    public MeterTopology(final SmartNtaProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Search for local slave devices so a general topology can be build up
     */
    public void searchForSlaveDevices() throws ConnectionException {
        this.discoveryComposedCosemObject = constructDiscoveryComposedCosemObject();
        discoverMbusDevices();
    }

    /**
     * Construct the discovery <CODE>ComposedCosemObject</CODE>. This will contain all the available MbusClient/SerialNumber-attributes for MbusMeters.
     * No data will be read in this method!
     *
     * @return the constructed composedObject for requesting MbusSerialNumber-data
     */
    protected ComposedCosemObject constructDiscoveryComposedCosemObject() {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
        for (int i = 1; i <= MaxMbusDevices; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, ObisCodeBFieldIndex, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                ComposedMbusSerialNumber cMbusSerial = new ComposedMbusSerialNumber(new DLMSAttribute(serialObisCode, MbusClientAttributes.MANUFACTURER_ID.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MbusClientAttributes.IDENTIFICATION_NUMBER.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MbusClientAttributes.VERSION.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MbusClientAttributes.DEVICE_TYPE.getAttributeNumber(), uo.getClassID()));
                dlmsAttributes.add(cMbusSerial.getManufacturerId());
                dlmsAttributes.add(cMbusSerial.getIdentificationNumber());
                dlmsAttributes.add(cMbusSerial.getVersion());
                dlmsAttributes.add(cMbusSerial.getDeviceType());
                cMbusSerialNumbers.add(cMbusSerial);
            }
        }
        return new ComposedCosemObject(this.protocol.getDlmsSession(), this.protocol.supportsBulkRequests(), dlmsAttributes);
    }

    private void discoverMbusDevices() throws ConnectionException {
        this.protocol.getLogger().log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        this.mbusMap = getMbusMapper();

        //TODO check if we can still check this !
//        // check if the current mbus slaves are still on the meter disappeared
//        checkForDisappearedMbusMeters(mbusMap);
//        // check if all the mbus devices are configured in EIServer
//        checkToUpdateMbusMeters(mbusMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        this.protocol.getLogger().log(Level.INFO, sb.toString());
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a List of <CODE>DeviceMappings</CODE>
     * @throws com.energyict.dialer.connection.ConnectionException
     *          if interframeTimeout has passed and maximum retries have been reached
     */
    protected List<DeviceMapping> getMbusMapper() throws ConnectionException {
        String mbusSerial;
        List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();
        for (int i = 1; i <= MaxMbusDevices; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, ObisCodeBFieldIndex, (byte) i);
            if (this.protocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                try {
                    Unsigned16 manufacturer = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i-1).getManufacturerId()).getUnsigned16();
                    Unsigned32 identification = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i-1).getIdentificationNumber()).getUnsigned32();
                    Unsigned8 version = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i-1).getVersion()).getUnsigned8();
                    Unsigned8 deviceType = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i-1).getDeviceType()).getUnsigned8();
                    mbusSerial = constructShortId(manufacturer, identification, version, deviceType);
                    if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase(""))) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                    }
                } catch (IOException e) {
                    if (e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1) {
                        throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
                    } // else, then the attributes are not available
                }
            }
        }
        return mbusMap;
    }

    /**
     * Construct the shortId from the four given fields
     *
     * @param manufacturer   - the manufacturer ID of the meter
     * @param identification - the identification number(serialnumber) of the meter
     * @param version        - the version of the device type
     * @param deviceType     - the device type
     * @return a string which is a concatenation of the manipulated given fields
     */
    protected String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType) {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append((char) (((manufacturer.getValue() & 0x7D00) / 32 / 32) + 64));
        strBuilder.append((char) (((manufacturer.getValue() & 0x03E0) / 32) + 64));
        strBuilder.append((char) ((manufacturer.getValue() & 0x001F) + 64));

        strBuilder.append(String.format((this.protocol.getProperties().getFixMbusHexShortId()) ? "%08d" : "%08x", identification.getValue()));    // 8 Hex digits with leading zeros
        strBuilder.append(String.format("%03d", version.getValue()));            // 3 Dec digits with leading zeros
        strBuilder.append(String.format("%02d", deviceType.getValue()));        // 2 Dec digits with leading zeros

        return strBuilder.toString();
    }

    public List<DeviceMapping> getMbusMeterMap() {
        return mbusMap;
    }

    /**
     * Search for the physicalAddress of the given SerialNumber
     *
     * @param serialNumber the given Serialnumber
     * @return the physicalAddress or -1 if the serialNumber was not found.
     */
    public int getPhysicalAddress(final String serialNumber) {

        if (serialNumber.equals(this.protocol.getSerialNumber())) {
            return this.protocol.getPhysicalAddress();
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getSerialNumber().equals(serialNumber)) {
                return dm.getPhysicalAddress();
            }
        }
        return -1;
    }

    /**
     * Search for the potential serialNumber of the given ObisCode. The B-field should indicate on which channel the meter is.
     *
     * @param obisCode the ObisCode
     * @return the serialNumber of the meter which corresponds with the B-field of the ObisCode
     */
    public String getSerialNumber(final ObisCode obisCode) {
        int bField = obisCode.getB();
        if (bField == this.protocol.getPhysicalAddress()) {
            return this.protocol.getSerialNumber();
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }

        return "";
    }
}
