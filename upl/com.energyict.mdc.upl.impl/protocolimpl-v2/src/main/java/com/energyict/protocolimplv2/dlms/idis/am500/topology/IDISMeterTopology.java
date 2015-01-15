package com.energyict.protocolimplv2.dlms.idis.am500.topology;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 14:05
 */
public class IDISMeterTopology {

    private static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final int MAX_MBUS_CHANNELS = 4;
    private final AM500 protocol;
    List<DeviceMapping> deviceMapping = null;

    public IDISMeterTopology(AM500 protocol) {
        this.protocol = protocol;
    }

    public CollectedTopology discoverMBusDevices() {
        deviceMapping = new ArrayList<>();
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(protocol.getOfflineDevice().getId()));

        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        for (int i = 1; i <= MAX_MBUS_CHANNELS; i++) {
            try {
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);
                long serialNumberValue = protocol.getDlmsSession().getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10).getIdentificationNumber().getValue();
                if (serialNumberValue != 0) {
                    String serialNumber = String.valueOf(serialNumberValue);
                    deviceMapping.add(new DeviceMapping(serialNumber, i, false));
                    deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(serialNumber));
                }
            } catch (DataAccessResultException e) {
                // fetch next
            } catch (IOException e) {
                throw IOExceptionHandler.handle(e, protocol.getDlmsSession());
            }
        }
        return deviceTopology;
    }

    public List<DeviceMapping> getDeviceMapping() {
        if (deviceMapping == null) {
            discoverMBusDevices();
        }
        return deviceMapping;
    }

    /**
     * If the SerialNumber is empty, throw missingProperty exception
     * If the SerialNumber matches with the e-meter's, return the original obiscode
     * If the SerialNumber matches with one of the slave meters, return the corrected obiscode. This means that the B-field is replaced with the MBus channel ID.
     * If the SerialNumber does not match one of the e-meter / slave meters, throw UnsupportedPropertyValueException
     */
    public ObisCode getCorrectedObisCode(ObisCode originalObisCode, String serialNumber) {
        int mBusChannelId = getMBusChannelId(serialNumber);
        if (mBusChannelId == 0) {
            return originalObisCode;
        } else {
            return ProtocolTools.setObisCodeField(originalObisCode, 1, (byte) mBusChannelId);
        }
    }

    public int getMBusChannelId(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            throw MdcManager.getComServerExceptionFactory().missingProperty("SerialNumber");
        }
        if (serialNumber.equals(protocol.getSerialNumber())) {
            return 0;
        }
        for (DeviceMapping mapping : getDeviceMapping()) {
            if (serialNumber.equals(mapping.getSerialNumber())) {
                return mapping.getPhysicalAddress();
            }
        }
        throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("SerialNumber", serialNumber);
    }
}