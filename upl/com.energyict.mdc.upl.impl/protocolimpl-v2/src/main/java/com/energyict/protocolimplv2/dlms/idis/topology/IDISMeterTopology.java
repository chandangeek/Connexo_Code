package com.energyict.protocolimplv2.dlms.idis.topology;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for building/managing of the device topology;<br/>
 * During this process all attached MBus devices will be discovered. Each MBus device is discovered
 * based on the MBusClient identification number (0.x.24.1.0.255 - attribute 6).
 *
 * @author khe
 * @since 19/12/2014 - 14:05
 */
public class IDISMeterTopology extends AbstractMeterTopology {

    private static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final int MAX_MBUS_CHANNELS = 4;
    private final AbstractDlmsProtocol protocol;
    List<DeviceMapping> deviceMapping = null;

    public IDISMeterTopology(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    protected int getMaxMBusChannels() {
        return MAX_MBUS_CHANNELS;
    }

    @Override
    public void searchForSlaveDevices() {
        deviceMapping = new ArrayList<>();
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        for (int i = 1; i <= getMaxMBusChannels(); i++) {
            try {
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);
                long serialNumberValue = protocol.getDlmsSession().getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10).getIdentificationNumber().getValue();
                if (serialNumberValue != 0) {
                    String serialNumber = String.valueOf(serialNumberValue);
                    deviceMapping.add(new DeviceMapping(serialNumber, i, false));
                }
            } catch (DataAccessResultException e) {
                // fetch next
            } catch (IOException e) {
                throw IOExceptionHandler.handle(e, protocol.getDlmsSession());
            }
        }
    }

    public List<DeviceMapping> getDeviceMapping() {
        if (deviceMapping == null) {
            searchForSlaveDevices();
        }
        return deviceMapping;
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int mBusChannelId = getPhysicalAddress(serialNumber);
        if (mBusChannelId == 0) {
            return obisCode;
        } else {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) mBusChannelId);
        }
    }

    @Override
    public int getPhysicalAddress(String serialNumber) {
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

    @Override
    public String getSerialNumber(ObisCode obisCode) {
        int bField = obisCode.getB();
        if (bField == 0 || bField == 128) {    // 128 is the notation of the CapturedObjects in mW for Electricity ...
            return this.protocol.getDlmsSession().getProperties().getSerialNumber();
        }

        for (DeviceMapping dm : getDeviceMapping()) {
            if (dm.getPhysicalAddress() == bField) {
                return dm.getSerialNumber();
            }
        }
        return "";
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(protocol.getOfflineDevice().getId()));
        for (DeviceMapping mapping : getDeviceMapping()) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(mapping.getSerialNumber()));
        }

        return deviceTopology;
    }
}