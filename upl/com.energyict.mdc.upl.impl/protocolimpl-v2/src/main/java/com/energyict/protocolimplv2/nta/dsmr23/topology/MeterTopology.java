package com.energyict.protocolimplv2.nta.dsmr23.topology;

import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.MBusClientAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.topology.DeviceMapping;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMbusSerialNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 16:56:32
 */
public class MeterTopology extends AbstractMeterTopology {

    private static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode MbusClientObisCode = ObisCode.fromString("0.x.24.1.0.255");

    private static final int ObisCodeBFieldIndex = 1;
    public static final int MaxMbusDevices = 4;
    private static final String ignoreZombieMbusDevice = "@@@0000000000000";

    private final AbstractDlmsProtocol protocol;

    /**
     * The <CODE>ComposedCosemObject</CODE> for requesting all serialNumbers in 1 request
     */
    private ComposedCosemObject discoveryComposedCosemObject;

    /**
     * A List of localComposedCosemObjects containing the attributes to construct the serialNumber of the devices
     */
    private List<ComposedMbusSerialNumber> cMbusSerialNumbers = new ArrayList<>();

    /**
     * A list of MbusMeter <CODE>DeviceMappings</CODE>
     */
    private List<DeviceMapping> mbusMap = new ArrayList<>();
    private final CollectedDataFactory collectedDataFactory;

    public MeterTopology(final AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public void searchForSlaveDevices() {
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
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
        for (int i = 1; i <= MaxMbusDevices; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, ObisCodeBFieldIndex, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectListIgnoreBChannel(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);

            if (uo != null) {
                //uo.setObisCodeChannelB((byte) i);
                ComposedMbusSerialNumber cMbusSerial = new ComposedMbusSerialNumber(
                        new DLMSAttribute(serialObisCode, MBusClientAttributes.MANUFACTURER_ID.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MBusClientAttributes.IDENTIFICATION_NUMBER.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MBusClientAttributes.VERSION.getAttributeNumber(), uo.getClassID()),
                        new DLMSAttribute(serialObisCode, MBusClientAttributes.DEVICE_TYPE.getAttributeNumber(), uo.getClassID()));
                dlmsAttributes.add(cMbusSerial.getManufacturerId());
                dlmsAttributes.add(cMbusSerial.getIdentificationNumber());
                dlmsAttributes.add(cMbusSerial.getVersion());
                dlmsAttributes.add(cMbusSerial.getDeviceType());
                cMbusSerialNumbers.add(cMbusSerial);
            }
        }
        return new ComposedCosemObject(this.protocol.getDlmsSession(), this.protocol.getDlmsSession().getProperties().isBulkRequest(), dlmsAttributes);
    }

    private void discoverMbusDevices() {
        log("Starting discovery of MBusDevices");
        constructMbusMap();

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        log(sb.toString());
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a List of <CODE>DeviceMappings</CODE>
     */
    protected List<DeviceMapping> constructMbusMap() {
        String mbusSerial;
        if (mbusMap.isEmpty()) {
            mbusMap = new ArrayList<>();
            for (int i = 1; i <= MaxMbusDevices; i++) {
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, ObisCodeBFieldIndex, (byte) i);
                if (this.protocol.getDlmsSession().getMeterConfig().isObisCodeInObjectListIgnoreChannelB(serialObisCode)) {
                    try {
                        Unsigned16 manufacturer = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getManufacturerId()).getUnsigned16();
                        Unsigned8 version = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getVersion()).getUnsigned8();
                        Unsigned32 identification = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getIdentificationNumber()).getUnsigned32();
                        Unsigned8 deviceType = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getDeviceType()).getUnsigned8();
                        mbusSerial = constructShortId(manufacturer, identification, version, deviceType);
                        if ((mbusSerial != null) && (!"".equalsIgnoreCase(mbusSerial)) && !mbusSerial.equalsIgnoreCase(ignoreZombieMbusDevice)) {
                            mbusMap.add(new DeviceMapping(mbusSerial, i));
                        }
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                            //Move on to next
                        }
                    }
                }
            }
        }

        return mbusMap;
    }

    /**
     * Construct the shortId from the four given fields
     *
     * @param manufacturer - the manufacturer ID of the meter
     * @param identification - the identification number(serialnumber) of the meter
     * @param version - the version of the device type
     * @param deviceType - the device type
     * @return a string which is a concatenation of the manipulated given fields
     */
    protected String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType) {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append((char) (((manufacturer.getValue() & 0x7D00) / 32 / 32) + 64));
        strBuilder.append((char) (((manufacturer.getValue() & 0x03E0) / 32) + 64));
        strBuilder.append((char) ((manufacturer.getValue() & 0x001F) + 64));

        strBuilder.append(String.format(((this.protocol.getDlmsSession().getProperties()).getFixMbusHexShortId()) ? "%08d" : "%08x", identification.getValue()));    // 8 Hex digits with leading zeros
        strBuilder.append(String.format("%03d", version.getValue()));            // 3 Dec digits with leading zeros
        strBuilder.append(String.format("%02d", deviceType.getValue()));        // 2 Dec digits with leading zeros

        return strBuilder.toString();
    }

    public List<DeviceMapping> getMbusMeterMap() {
        return mbusMap;
    }

    @Override
    public int getPhysicalAddress(final String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            throw DeviceConfigurationException.missingProperty("SerialNumber");
        }

        if (serialNumber.equals(protocol.getDlmsSession().getProperties().getSerialNumber())) {
            return 0;   // the 'Master' has physicalAddress 0
        }

        if (this.mbusMap.isEmpty()) {
            throw DeviceConfigurationException.emptyMBusSet();
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getSerialNumber().equals(serialNumber)) {
                return dm.getPhysicalAddress();
            }
        }

        StringBuilder sb = new StringBuilder();
        // construct output for mbus
        for (DeviceMapping dm : this.mbusMap) {
            sb.append( dm.getSerialNumber() ).append(", ");
        }
        throw DeviceConfigurationException.mbusSerialNumberNotFound(serialNumber, sb.substring(0, sb.length() - 2));
    }

    @Override
    public String getSerialNumber(final ObisCode obisCode) {
        int bField = obisCode.getB();
        if (bField == 0 || bField == 128) {    // 128 is the notation of the CapturedObjects in mW for Electricity ...
            return this.protocol.getDlmsSession().getProperties().getSerialNumber();
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }
        return "";
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equals(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddress(serialNumber);
        }

        if ((address == 0 && obisCode.getB() != -1 && obisCode.getB() != 128)) { // then don't correct the obisCode
            return obisCode;
        }

        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) address);
        }
        return null;
    }

    /**
     * Returns the actual device topology (which should be the master device and a number of attached slave devices).
     * <p>
     * <b>Warning:</b> this method should only be called after the actual device topology is read out, or in other words
     * after method #searchForSlaveDevices() has been called!
     *
     * @return the current Topology
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(protocol.getOfflineDevice().getId()));
        for (DeviceMapping mapping : getMbusMeterMap()) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(mapping.getSerialNumber()));
        }

        return deviceTopology;
    }

    private void log(String message) {
        this.protocol.journal(message);
    }

    private void log(Level level, String message) {
        this.protocol.journal(level, message);
    }

    /**
     * Search for the next available physicalAddress
     *
     * @return the next available physicalAddress or -1 if none is available.
     */
    public int searchNextFreePhysicalAddress() {
        List<Integer> availablePhysicalAddresses = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        for (DeviceMapping dm : this.mbusMap) {
            availablePhysicalAddresses.remove((Integer) dm.getPhysicalAddress());    // Remove the specified object from the list
        }
        return availablePhysicalAddresses.isEmpty() ? 0 : availablePhysicalAddresses.get(0);
    }

}
