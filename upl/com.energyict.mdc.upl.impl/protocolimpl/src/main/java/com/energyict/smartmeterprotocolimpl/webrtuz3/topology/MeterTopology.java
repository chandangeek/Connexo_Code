package com.energyict.smartmeterprotocolimpl.webrtuz3.topology;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.smartmeterprotocolimpl.webrtuz3.topology.DeviceMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * Provides functionality about the Master and Slave topology.
 * <pre>
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 9:50:25
 * </pre>
 */
public class MeterTopology {

    private static final int ObisCodeBFieldIndex = 1;

    /**
     * Device channel mappings
     */
    public static final DeviceMappingRange MBUS_DEVICES = new DeviceMappingRange(0x01, 0x20);
    public static final DeviceMappingRange EMETER_DEVICES = new DeviceMappingRange(0x21, 0x40);

    /**
     * The device obisCodes
     */
    public static final ObisCode SERIALNR_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");

    private final WebRTUZ3 meterProtocol;

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
    List<DeviceMapping> eMeterMap = new ArrayList<DeviceMapping>();
    /**
     * A list of MbusMeter <CODE>DeviceMappings</CODE>
     */
    List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();


    public MeterTopology(WebRTUZ3 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Discover all the Slave meters (Emeters and MbusMeters)
     *
     * @throws ConnectionException if the connection caused an error
     */
    public void discoverSlaveDevices() throws ConnectionException {
        constructDiscoveryComposedCosemObject();
        discoverEMeters();
        discoverMbusDevices();
    }

    /**
     * Construct the discovery <CODE>ComposedCosemObject</CODE>. This will contain all the available serialNumbers from Emeters and MbusMeters
     */
    protected void constructDiscoveryComposedCosemObject() {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
        for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, ObisCodeBFieldIndex, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                mbusSerialAttributes.put(i, new DLMSAttribute(serialObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                dlmsAttributes.add(mbusSerialAttributes.get(i));
            }
        }
        for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, ObisCodeBFieldIndex, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                emeterSerialAttributes.put(i, new DLMSAttribute(serialObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                dlmsAttributes.add(emeterSerialAttributes.get(i));
            }
        }
        this.discoveryComposedCosemObject = new ComposedCosemObject(this.meterProtocol.getDlmsSession(), this.meterProtocol.supportsBulkRequests(), dlmsAttributes);
    }

    /**
     * Discover Mbus devices
     */
    public void discoverMbusDevices() throws ConnectionException {
        this.meterProtocol.getLogger().log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        this.mbusMap = getMbusMapper();

        //TODO check if we can still check this !
//        // check if the current mbus slaves are still on the meter disappeared
//        checkForDisappearedMbusMeters(mbusMap);
//        // check if all the mbus devices are configured in EIServer
//        checkToUpdateMbusMeters(mbusMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (int i = 0; i < this.mbusMap.size(); i++) {
            DeviceMapping deviceMapping = this.mbusMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        this.meterProtocol.getLogger().log(Level.INFO, sb.toString());

    }

    /**
     * Discover EMeters devices
     */
    public void discoverEMeters() throws ConnectionException {
        this.meterProtocol.getLogger().log(Level.FINE, "Starting discovery of eMeters");
        // get an MbusDeviceMap
        this.eMeterMap = getEmeterMapper();

        //TODO check if we can still check this !
//        // check if the current mbus slaves are still on the meter disappeared
//        checkForDisappearedEMeters(eMeterMap);
//        // check if all the mbus devices are configured in EIServer
//        checkToUpdateEMeters(eMeterMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(this.eMeterMap.size()).append(" eMeter devices: ").append("\r\n");
        for (int i = 0; i < this.eMeterMap.size(); i++) {
            DeviceMapping deviceMapping = this.eMeterMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        this.meterProtocol.getLogger().log(Level.INFO, sb.toString());

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
        for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
            mbusSerial = "";
            try {
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, ObisCodeBFieldIndex, (byte) i);
                if (this.meterProtocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    OctetString serialOctetString = this.discoveryComposedCosemObject.getAttribute(this.mbusSerialAttributes.get(i)).getOctetString();
                    mbusSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase(""))) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1) {
                    throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
                }
                //log(Level.FINE, "Could not retrieve the mbus serialNumber for channel " + i + ": " + e.getMessage());
            }
        }
        return mbusMap;
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the e-meter.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a List of <CODE>DeviceMappings</CODE>
     * @throws ConnectionException if interframeTimeout has passed and maximum retries have been reached
     */
    protected List<DeviceMapping> getEmeterMapper() throws ConnectionException {
        String eMeterSerial;
        List<DeviceMapping> eMeterMap = new ArrayList<DeviceMapping>();
        for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
            eMeterSerial = "";
            try {
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, ObisCodeBFieldIndex, (byte) i);
                if (this.meterProtocol.getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    OctetString serialOctetString = this.discoveryComposedCosemObject.getAttribute(this.emeterSerialAttributes.get(i)).getOctetString();
                    eMeterSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((eMeterSerial != null) && (!eMeterSerial.equalsIgnoreCase(""))) {
                        eMeterMap.add(new DeviceMapping(eMeterSerial, i));
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1) {
                    throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
                }
                //log(Level.FINE, "Could not retrieve the eMeter serialNumber for channel " + i + ": " + e.getMessage());
            }
        }
        return eMeterMap;
    }

    /**
     * Search for the physicalAddress of the given SerialNumber
     *
     * @param serialNumber the given Serialnumber
     * @return the physicalAddress or -1 if the serialNumber was not found.
     */
    public int getPhysicalAddress(String serialNumber) {

        if (serialNumber.equals(this.meterProtocol.getSerialNumber())) {
            return this.meterProtocol.getPhysicalAddress();
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
        return -1;
    }

    public List<DeviceMapping> geteMeterMap() {
        return eMeterMap;
    }

    public List<DeviceMapping> getMbusMap() {
        return mbusMap;
    }

    /**
     * Search for the potential serialNumber of the given ObisCode. The B-field should indicate on which channel the meter is.
     *
     * @param obisCode the ObisCode
     * @return the serialNumber of the meter which corresponds with the B-field of the ObisCode
     */
    public String getSerialNumber(ObisCode obisCode) {
        int bField = obisCode.getB();
        if (bField == this.meterProtocol.getPhysicalAddress()) {
            return this.meterProtocol.getSerialNumber();
        }

        for (DeviceMapping dm : this.eMeterMap) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }

        return "";
    }

    /**
     * Protected setter for the {@link #mbusMap}
     *
     * @param deviceMappings the new list of DeviceMappings to set
     */
    public void setMbusDeviceMappings(List<DeviceMapping> deviceMappings) {
        this.mbusMap = deviceMappings;
    }

    /**
     * Protected setter for the {@link #eMeterMap}
     *
     * @param deviceMappings the new list of DeviceMappings to set
     */
    public void setEmeterDeviceMappings(List<DeviceMapping> deviceMappings) {
        this.eMeterMap = deviceMappings;
    }

    /**
     * Getter for the {@link #discoveryComposedCosemObject}
     *
     * @return the requested object
     */
    protected ComposedCosemObject getDiscoveryComposedCosemObject() {
        return discoveryComposedCosemObject;
    }
}
