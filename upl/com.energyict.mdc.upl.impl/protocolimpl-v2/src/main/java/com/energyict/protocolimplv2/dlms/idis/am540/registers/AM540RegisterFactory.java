package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;

import java.io.IOException;

/**
 * RegisterFactory created for the AM540 protocol <br/>
 * Note: extends from AM130RegisterFactory - which enables readout of all common elements -
 * this class adds readout of the various PLC objects.
 *
 * @author sva
 * @since 11/08/2015 - 15:46
 */
public class AM540RegisterFactory extends AM130RegisterFactory {

    private AM540PLCRegisterMapper plcRegisterMapper;

    public AM540RegisterFactory(AM540 am540) {
        super(am540);
    }

    @Override
    protected CollectedRegister readRegister(OfflineRegister offlineRegister) {
        ObisCode obisCode = offlineRegister.getObisCode();

        if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
            try {
                RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
                deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                return deviceRegister;
            } catch (IOException e) {
                return handleIOException(offlineRegister, e);
            }
        } else {
            return super.readRegister(offlineRegister);
        }
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(getMeterProtocol().getDlmsSession());
        }
        return plcRegisterMapper;
    }
}