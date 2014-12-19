package com.energyict.protocolimplv2.nta.dsmr50.registers;

import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/12/2014 - 14:29
 */
public class Dsmr50RegisterFactory extends Dsmr40RegisterFactory {

    private AM540PLCRegisterMapper plcRegisterMapper;

    public Dsmr50RegisterFactory(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        List<OfflineRegister> normalRegisters = new ArrayList<>();

        //First read out the G3 PLC registers, using the G3 PLC register mapper
        for (OfflineRegister register : allRegisters) {
            ObisCode obisCode = register.getObisCode();
            if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
                try {
                    RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
                        } else {
                            collectedRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage()));
                        }
                    }
                }
            } else {
                normalRegisters.add(register);
            }
        }

        //Now read out the "normal" DSMR registers, using the DSMR4.0 register factory
        collectedRegisters.addAll(super.readRegisters(normalRegisters));

        //Finally, return all the register values
        return collectedRegisters;
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(protocol.getDlmsSession());
        }
        return plcRegisterMapper;
    }


}