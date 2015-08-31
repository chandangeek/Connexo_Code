package com.energyict.protocolimplv2.eict.webrtuz3.topology;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.DeviceMappingRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/04/2015 - 15:46
 */
public class WebRTUZ3MeterTopology extends AbstractMeterTopology {

    private static final ObisCode SERIALNR_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final DeviceMappingRange MBUS_DEVICES = new DeviceMappingRange(0x01, 0x20);
    private static final DeviceMappingRange EMETER_DEVICES = new DeviceMappingRange(0x21, 0x40);

    private final AbstractDlmsProtocol meterProtocol;

    /**
     * The <CODE>ComposedCosemObject</CODE> for requesting all serialNumbers in 1 request
     */
    private ComposedCosemObject discoveryComposedCosemObject;

    /**
     * A map of physicalAddresses and DLMSAttributes for the mbusSerialNumbers
     */
    private Map<Integer, DLMSAttribute> mbusSerialAttributes = new HashMap<Integer, DLMSAttribute>();

    /**
     * A map of physicalAddresses and DLMSAttributes for the emeterSerialNumbers
     */
    private Map<Integer, DLMSAttribute> emeterSerialAttributes = new HashMap<Integer, DLMSAttribute>();

    /**
     * A list of EMeter <CODE>DeviceMappings</CODE>
     */
    private List<DeviceMapping> eMeterMap = new ArrayList<DeviceMapping>();
    /**
     * A list of MbusMeter <CODE>DeviceMappings</CODE>
     */
    private List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();

    public WebRTUZ3MeterTopology(AbstractDlmsProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<DeviceMapping> geteMeterMap() {
        return eMeterMap;
    }

    public List<DeviceMapping> getMbusMap() {
        return mbusMap;
    }

    @Override
    public void searchForSlaveDevices() {
        constructDiscoveryComposedCosemObject();
        discoverEMeters();
        discoverMbusDevices();
    }

    /**
     * Construct the discovery <CODE>ComposedCosemObject</CODE>. This will contain all the available serialNumbers from Emeters and MbusMeters
     */
    private void constructDiscoveryComposedCosemObject() {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
        for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                mbusSerialAttributes.put(i, new DLMSAttribute(serialObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                dlmsAttributes.add(mbusSerialAttributes.get(i));
            }
        }
        for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                emeterSerialAttributes.put(i, new DLMSAttribute(serialObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                dlmsAttributes.add(emeterSerialAttributes.get(i));
            }
        }
        this.discoveryComposedCosemObject = new ComposedCosemObject(this.meterProtocol.getDlmsSession(), this.meterProtocol.getDlmsSessionProperties().isBulkRequest(), dlmsAttributes);
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     */
    protected void discoverMbusDevices() {
        mbusMap = new ArrayList<>();
        for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
            try {
                String mbusSerial;
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
                if (this.meterProtocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    OctetString serialOctetString = this.discoveryComposedCosemObject.getAttribute(this.mbusSerialAttributes.get(i)).getOctetString();
                    mbusSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase(""))) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                    }
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, meterProtocol.getDlmsSession())) {
                    continue;   //Go to the next meter
                }
            }
        }
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the e-meter.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     */
    protected void discoverEMeters() {
        eMeterMap = new ArrayList<>();
        for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
            try {
                String eMeterSerial;
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
                if (this.meterProtocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    OctetString serialOctetString = this.discoveryComposedCosemObject.getAttribute(this.emeterSerialAttributes.get(i)).getOctetString();
                    eMeterSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((eMeterSerial != null) && (!eMeterSerial.equalsIgnoreCase(""))) {
                        eMeterMap.add(new DeviceMapping(eMeterSerial, i));
                    }
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, meterProtocol.getDlmsSession())) {
                    continue;   //Go to the next meter
                }
            }
        }
    }

    @Override
    public String getSerialNumber(ObisCode obisCode) {
        int bField = obisCode.getB();
        if (bField == 0) {
            return this.meterProtocol.getDlmsSession().getProperties().getSerialNumber();
        }

        for (DeviceMapping deviceMapping : this.eMeterMap) {
            if (deviceMapping.getPhysicalAddress() == bField) {
                return deviceMapping.getSerialNumber();
            }
        }

        for (DeviceMapping deviceMapping : this.mbusMap) {
            if (deviceMapping.getPhysicalAddress() == bField) {
                return deviceMapping.getSerialNumber();
            }
        }

        return "";
    }

    @Override
    public int getPhysicalAddress(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            throw MdcManager.getComServerExceptionFactory().missingProperty("SerialNumber");
        }

        if (serialNumber.equals(meterProtocol.getDlmsSession().getProperties().getSerialNumber())) {
            return 0;   // the 'Master' has physicalAddress 0
        }

        for (DeviceMapping dm : this.eMeterMap) {
            if (dm.getSerialNumber().equals(serialNumber)) {
                return dm.getPhysicalAddress();
            }
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getSerialNumber().equals(serialNumber)) {
                return dm.getPhysicalAddress();
            }
        }

        throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("SerialNumber", serialNumber);
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int physicalAddress = getPhysicalAddress(serialNumber);
        if (physicalAddress == 0) {
            return obisCode;
        } else {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) physicalAddress);
        }
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(meterProtocol.getOfflineDevice().getId()));

        for (DeviceMapping mapping : eMeterMap) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(mapping.getSerialNumber()));
        }
        for (DeviceMapping mapping : mbusMap) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(mapping.getSerialNumber()));
        }

        return deviceTopology;
    }
}