package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.registers;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/06/2014 - 15:16
 */
public class AM540RegisterFactory extends DSMR40RegisterFactory {

    private AM540PLCRegisterMapper plcRegisterMapper;

    public AM540RegisterFactory(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo(register.getObisCode().toString());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> result = new ArrayList<RegisterValue>();
        List<Register> normalRegisters = new ArrayList<Register>();

        //First read out the G3 PLC registers, using the G3 PLC register mapper
        for (Register register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
                try {
                    RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                    //Now include the serial number
                    registerValue = new RegisterValue(register, registerValue.getQuantity(), registerValue.getEventTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getReadTime(), register.getRegisterSpecId(), registerValue.getText());
                    result.add(registerValue);
                } catch (NoSuchRegisterException e) {
                    protocol.getLogger().warning("Register with obiscode " + obisCode + " is not supported: " + e.getMessage());
                } catch (DataAccessResultException e) {
                    protocol.getLogger().warning("Error while reading out register with obiscode " + obisCode + ": " + e.getMessage());
                }
            } else {
                normalRegisters.add(register);
            }
        }

        //Now read out the "normal" DSMR registers, using the DSMR4.0 register factory
        result.addAll(super.readRegisters(normalRegisters));

        //Finally, return all the register values
        return result;
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(protocol.getDlmsSession());
        }
        return plcRegisterMapper;
    }
}
