package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.topology;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 7/06/13 - 11:19
 */
public class XemexMeterTopology extends MeterTopology {

    protected static final ObisCode MbusClientDeviceID2ObisCode = ObisCode.fromString("0.0.96.2.0.255");

    /**
     * A List of localComposedCosemObjects containing the attributes to construct the serialNumber of the devices
     */
    private List<DLMSAttribute> cMbusDLMSAttributes = new ArrayList<>();

    public XemexMeterTopology(AbstractSmartNtaProtocol protocol, TopologyService topologyService) {
        super(protocol, topologyService);
    }

    @Override
    public void searchForSlaveDevices() throws ConnectionException {
        setDiscoveryComposedCosemObject(constructDiscoveryComposedCosemObject());
        discoverMbusDevices();
    }

    @Override
    protected ComposedCosemObject constructDiscoveryComposedCosemObject() {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
        for (int i = 1; i <= MAX_MBUS_DEVICES; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MbusClientDeviceID2ObisCode, MeterTopology.OBIS_CODE_B_FIELD_INDEX, (byte) i);
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(getProtocol().getDlmsSession().getMeterConfig().getInstantiatedObjectList(), serialObisCode);
            if (uo != null) {
                DLMSAttribute attribute = new DLMSAttribute(serialObisCode, DataAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                dlmsAttributes.add(attribute);
                cMbusDLMSAttributes.add(attribute);
            }
        }
        return new ComposedCosemObject(getProtocol().getDlmsSession(), getProtocol().supportsBulkRequests(), dlmsAttributes);
    }

    @Override
    protected void discoverMbusDevices() throws ConnectionException {
        log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        this.mbusMap = getMbusMapper();

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        log(Level.INFO, sb.toString());
    }

    @Override
    protected List<DeviceMapping> getMbusMapper() throws ConnectionException {
        String mbusSerial;
        List<DeviceMapping> mbusMap = new ArrayList<>();
        for (int i = 1; i <= MAX_MBUS_DEVICES; i++) {
            ObisCode serialObisCode = ProtocolTools.setObisCodeField(MeterTopology.MBUS_CLIENT_OBIS_CODE, MeterTopology.OBIS_CODE_B_FIELD_INDEX, (byte) i);
            if (getProtocol().getDlmsSession().getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                try {
                    mbusSerial = getDiscoveryComposedCosemObject().getAttribute(this.cMbusDLMSAttributes.get(i - 1)).getOctetString().stringValue();
                    if ((mbusSerial != null) && (!"".equalsIgnoreCase(mbusSerial)) && !mbusSerial.equalsIgnoreCase(IGNORE_ZOMBIE_MBUS_DEVICE)) {
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
}
