package com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMbusSerialNumber;

import java.io.IOException;
import java.sql.SQLException;
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
    private static String ignoreZombieMbusDevice = "@@@0000000000000";

    private final AbstractSmartNtaProtocol protocol;

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

    private Device rtu;

    public MeterTopology(final AbstractSmartNtaProtocol protocol) {
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
        return new ComposedCosemObject(this.protocol.getDlmsSession(), this.protocol.supportsBulkRequests(), dlmsAttributes);
    }

    private void discoverMbusDevices() throws ConnectionException {
        log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        this.mbusMap = getMbusMapper();

        if (this.mbusMap.size() > 0) {
            try {
                // check if all the mbus devices are configured in EIServer
                checkToUpdateMbusMeters(mbusMap);
            } finally {
                ProtocolTools.closeConnection();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        log(Level.INFO, sb.toString());
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
                    Unsigned16 manufacturer = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getManufacturerId()).getUnsigned16();
                    Unsigned8 version = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getVersion()).getUnsigned8();
                    Unsigned32 identification = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getIdentificationNumber()).getUnsigned32();
                    Unsigned8 deviceType = this.discoveryComposedCosemObject.getAttribute(this.cMbusSerialNumbers.get(i - 1).getDeviceType()).getUnsigned8();
                    mbusSerial = constructShortId(manufacturer, identification, version, deviceType);
                    if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase("")) && !mbusSerial.equalsIgnoreCase(ignoreZombieMbusDevice)) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                    }
                } catch (IOException e) {
                    if (e.getMessage().contains("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error")) {
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

        strBuilder.append(String.format((((Dsmr23Properties) this.protocol.getProperties()).getFixMbusHexShortId()) ? "%08d" : "%08x", identification.getValue()));    // 8 Hex digits with leading zeros
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
        if (bField == this.protocol.getPhysicalAddress() || bField == 128) {    // 128 is the notation of the CapturedObjects in mW for Electricity ...
            return this.protocol.getSerialNumber();
        }

        for (DeviceMapping dm : this.mbusMap) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }
        return "";
    }

    /**
     * Check the ghostMbusDevices and create the mbusDevices.
     * Also check if the zombie MBus device is in the list (@@@0000000000000), this should be ignored as wel.
     *
     * @param mbusDeviceMap a List of Mbus DeviceMappings
     */
    private void checkToUpdateMbusMeters(List<DeviceMapping> mbusDeviceMap) {

        for (DeviceMapping deviceMapping : mbusDeviceMap) {
            if (!ignoreZombieMbusDevice.equals(deviceMapping.getSerialNumber())) {
                try {
                    findOrCreateMbusDevice(deviceMapping.getSerialNumber());
                } catch (SQLException e) {
                    log(Level.SEVERE, "Could not create MbusDevice - " + e.getMessage());
                } catch (BusinessException e) {
                    log(Level.SEVERE, "Could not create MbusDevice - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Find or create the MbusDevice based on his serialNumber
     *
     * @param serialNumber the serialnumber of the MbusDevice
     * @return the requested Mbus Device
     * @throws SQLException      if a database error occurs
     * @throws BusinessException when a business related error occurs
     */
    private Device findOrCreateMbusDevice(String serialNumber) throws SQLException, BusinessException {
        List<Device> mbusList = ProtocolTools.mw().getDeviceFactory().findBySerialNumber(serialNumber);
        if (mbusList.size() == 1) {
            Device mbusRtu = mbusList.get(0);
            // Check if gateway has changed, and update if it has
            if ((mbusRtu.getGateway() == null) || (mbusRtu.getGateway().getId() != getRtuFromDatabaseBySerialNumber().getId())) {
                mbusRtu.updateGateway(getRtuFromDatabaseBySerialNumber());
            }
            return mbusRtu;
        } else if (mbusList.size() > 1) {
            log(Level.SEVERE, "Multiple meters where found with serial: " + serialNumber + ". Meter will not be handled.");
            return null;
        }

        DeviceType rtuType = getRtuType();
        if (rtuType == null) {
            return null;
        } else {
            return createMeter(rtuType, serialNumber);
        }
    }

    /**
     * Create a new Device based on the given DeviceType and SerialNumber
     *
     * @param rtuType      the DeviceType to create a new mete from
     * @param serialNumber the name/serialnumber to give to the new Device
     * @return the new Device
     * @throws SQLException      when a database exception occurs
     * @throws BusinessException when a business related error occurs
     */
    private Device createMeter(DeviceType rtuType, String serialNumber) throws SQLException, BusinessException {
        DeviceShadow shadow = rtuType.getConfigurations().get(0).newDeviceShadow();

        shadow.setName(serialNumber);
        shadow.setSerialNumber(serialNumber);

        String folderExtName = (String) this.protocol.getProperties().getProtocolProperties().get("FolderExtName");
        if (folderExtName != null) {
            Folder result = ProtocolTools.mw().getFolderFactory().findByExternalName(folderExtName);
            if (result != null) {
                shadow.setFolderId(result.getId());
            } else {
                log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
            }
        } else {
            log(Level.INFO, "New meter will be placed in prototype folder.");
        }

        shadow.setGatewayId(getRtuFromDatabaseBySerialNumber().getId());
        return ProtocolTools.mw().getDeviceFactory().create(shadow);
    }

    /**
     * Create an DeviceType based on the custom property DeviceType
     *
     * @return the requested DeviceType
     */
    private DeviceType getRtuType() {
        String type = (String) this.protocol.getProperties().getProtocolProperties().get("DeviceType");
        if (Utils.isNull(type)) {
            log(Level.WARNING, "No automatic meter creation: no property DeviceType defined.");
            return null;
        } else {
            DeviceType rtuType = ProtocolTools.mw().getDeviceTypeFactory().find(type);
            if (rtuType == null) {
                log(Level.INFO, "No rtutype defined with name '" + type + "'");
                return null;
            } else if (rtuType.getPrototypeDevice() == null) {
                log(Level.INFO, "Rtutype '" + type + "' has not prototype rtu");
                return null;
            }
            return rtuType;
        }
    }

    /**
     * Get the Device from the Database based on his SerialNumber
     *
     * @return the Device
     */
    private Device getRtuFromDatabaseBySerialNumber() {
        if (rtu == null) {
            String serial = this.protocol.getSerialNumber();
            this.rtu = ProtocolTools.mw().getDeviceFactory().findBySerialNumber(serial).get(0);
        }
        return rtu;
    }

    private final void log(Level level, String message) {
        this.protocol.getLogger().log(level, message);
    }
}
