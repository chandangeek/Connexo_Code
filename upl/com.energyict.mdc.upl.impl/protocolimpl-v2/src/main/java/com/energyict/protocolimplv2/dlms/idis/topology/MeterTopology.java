package com.energyict.protocolimplv2.dlms.idis.topology;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.AM500;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 14:05
 */
public class MeterTopology {

    private static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final int MAX_MBUS_CHANNELS = 4;
    private final AM500 protocol;

    public MeterTopology(AM500 protocol) {
        this.protocol = protocol;
    }

    public CollectedTopology discoverMBusDevices() {
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(protocol.getOfflineDevice().getId()));

        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        for (int i = 1; i <= MAX_MBUS_CHANNELS; i++) {
            try {
                obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);
                long serialNumberValue = protocol.getDlmsSession().getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10).getIdentificationNumber().getValue();
                if (serialNumberValue != 0) {
                    deviceTopology.addSlaveDevice(new DeviceIdentifierBySerialNumber(String.valueOf(serialNumberValue)));
                }
            } catch (DataAccessResultException e) {
                // fetch next
            } catch (IOException e) {
                throw IOExceptionHandler.handle(e, protocol.getDlmsSession());
            }
        }
        return deviceTopology;
    }
}
