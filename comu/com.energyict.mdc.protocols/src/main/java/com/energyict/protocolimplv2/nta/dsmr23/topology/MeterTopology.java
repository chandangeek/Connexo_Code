/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr23.topology;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMbusSerialNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MeterTopology extends AbstractMeterTopology {

    private static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode MbusClientObisCode = ObisCode.fromString("0.0.24.1.0.255");
    private static final int OBIS_CODE_B_FIELD_INDEX = 1;
    public static final int MAX_MBUS_DEVICES = 4;
    private static final String IGNORE_ZOMBIE_MBUS_DEVICE = "@@@0000000000000";
    private static final int ObisCodeBFieldIndex = 1;
    public static final int MaxMbusDevices = 4;
    private static String ignoreZombieMbusDevice = "@@@0000000000000";

    private final AbstractDlmsProtocol protocol;
    private final IssueService issueService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;

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

    /**
     * The device topology containing a DeviceIdentifier for the master and each of the attached slaves.
     */
    private CollectedTopology deviceTopology;

    public MeterTopology(final AbstractDlmsProtocol protocol, IssueService issueService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory) {
        this.protocol = protocol;
        this.issueService = issueService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
    }

    /**
     * Search for local slave devices so a general topology can be build up
     */
    public void searchForSlaveDevices() {
        this.discoveryComposedCosemObject = constructDiscoveryComposedCosemObject();
        this.deviceTopology = discoverMbusDevices();
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        return null;
    }

    @Override
    public int searchNextFreePhysicalAddress() {
        return 0;
    }

    /**
     * Construct the discovery <CODE>ComposedCosemObject</CODE>. This will contain all the available MbusClient/SerialNumber-attributes for MbusMeters.
     * No data will be read in this method!
     *
     * @return the constructed composedObject for requesting MbusSerialNumber-data
     */
    private ComposedCosemObject constructDiscoveryComposedCosemObject() {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
        for (int i = 1; i <= MAX_MBUS_DEVICES; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, OBIS_CODE_B_FIELD_INDEX, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                ComposedMbusSerialNumber cMbusSerial = new ComposedMbusSerialNumber(
                        new DLMSAttribute(serialObisCode, MbusClientAttributes.MANUFACTURER_ID.getAttributeNumber(), uo.getClassID()),
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
        return new ComposedCosemObject(this.protocol.getDlmsSession(), this.protocol.getDlmsSession().getProperties().isBulkRequest(), dlmsAttributes);
    }

    private CollectedTopology discoverMbusDevices() {
        log(Level.FINE, "Starting discovery of MBusDevices");
        CollectedTopology collectedTopology = constructDeviceTopology();

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        log(Level.FINE, sb.toString());
        return collectedTopology;
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a List of <CODE>DeviceMappings</CODE>
     */
    private CollectedTopology constructDeviceTopology() {
        String mbusSerial;
        mbusMap = new ArrayList<>();
        deviceTopology = this.collectedDataFactory.createCollectedTopology(protocol.getOfflineDevice().getDeviceIdentifier());
        for (int i = 1; i <= MAX_MBUS_DEVICES; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientObisCode, OBIS_CODE_B_FIELD_INDEX, (byte) i);
            if (this.protocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                try {
                    Unsigned16 manufacturer = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getManufacturerId()).getUnsigned16();
                    Unsigned8 version = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getVersion()).getUnsigned8();
                    Unsigned32 identification = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getIdentificationNumber()).getUnsigned32();
                    Unsigned8 deviceType = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getDeviceType()).getUnsigned8();
                    mbusSerial = constructShortId(manufacturer, identification, version, deviceType);
                    if ((mbusSerial != null) && (!"".equalsIgnoreCase(mbusSerial)) && !mbusSerial.equalsIgnoreCase(IGNORE_ZOMBIE_MBUS_DEVICE)) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                        deviceTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierBySerialNumber(mbusSerial));
                    }
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        //Move on to next
                    }
                }
            }
        }
        return deviceTopology;
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
    private String constructShortId(Unsigned16 manufacturer, Unsigned32 identification, Unsigned8 version, Unsigned8 deviceType) {
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

    /**
     * Search for the physicalAddress of the given SerialNumber
     *
     * @param serialNumber the given Serialnumber
     * @return the physicalAddress or -1 if the serialNumber was not found.
     */
    public int getPhysicalAddress(final String serialNumber) {

        if (serialNumber.equals(protocol.getDlmsSession().getProperties().getSerialNumber())) {
            return 0;   // the 'Master' has physicalAddress 0
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

    public CollectedTopology getCollectedDeviceTopology() {
        if (deviceTopology == null) {
            searchForSlaveDevices();
        }
        return deviceTopology;
    }

    private void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }
}
