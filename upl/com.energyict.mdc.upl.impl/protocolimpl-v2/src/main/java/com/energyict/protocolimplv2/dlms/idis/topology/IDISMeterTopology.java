package com.energyict.protocolimplv2.dlms.idis.topology;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final CollectedDataFactory collectedDataFactory;

    public IDISMeterTopology(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
    }

    protected int getMaxMBusChannels() {
        return MAX_MBUS_CHANNELS;
    }

    @Override
    public void searchForSlaveDevices() {
        if(deviceMapping == null || (deviceMapping != null && deviceMapping.isEmpty())){
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
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries()+1);
                }
            }
        }
    }

    public List<DeviceMapping> getDeviceMapping() {
        if (deviceMapping == null) {
            deviceMapping = new ArrayList<>();
        }
        return deviceMapping;
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int mBusChannelId = getPhysicalAddress(serialNumber);
        if ((mBusChannelId  == 0 && !obisCode.anyChannel() && obisCode.getB() != 128)) { // then don't correct the obisCode
            return obisCode;
        }
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) mBusChannelId);
    }

    /**
     * Search for the next available physicalAddress
     *
     * @return the next available physicalAddress or -1 if none is available.
     */
    public int searchNextFreePhysicalAddress(){
        List<Integer> availablePhysicalAddresses = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        for (DeviceMapping dm : this.deviceMapping) {
            availablePhysicalAddresses.remove((Integer) dm.getPhysicalAddress());    // Remove the specified object from the list
        }
        return availablePhysicalAddresses.isEmpty() ? 0 : availablePhysicalAddresses.get(0);
    }

    @Override
    public int getPhysicalAddress(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            throw DeviceConfigurationException.missingProperty("SerialNumber");
        }
        if (serialNumber.equals(protocol.getDlmsSession().getProperties().getSerialNumber())) {
            return 0;
        }
        for (DeviceMapping mapping : getDeviceMapping()) {
            if (serialNumber.equals(mapping.getSerialNumber())) {
                return mapping.getPhysicalAddress();
            }
        }
        throw DeviceConfigurationException.unsupportedPropertyValue("SerialNumber", serialNumber);
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
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(protocol.getOfflineDevice().getId()));
        for (DeviceMapping mapping : getDeviceMapping()) {
            deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(mapping.getSerialNumber()));
        }

        return deviceTopology;
    }
}