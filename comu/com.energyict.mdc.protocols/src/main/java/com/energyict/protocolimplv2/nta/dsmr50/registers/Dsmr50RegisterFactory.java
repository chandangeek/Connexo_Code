package com.energyict.protocolimplv2.nta.dsmr50.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
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

    public Dsmr50RegisterFactory(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, boolean supportsBulkRequests, CollectedDataFactory collectedDataFactory) {
        super(protocol, issueService, readingTypeUtilService, supportsBulkRequests, collectedDataFactory);
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
                    CollectedRegister deviceRegister = getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register),
                            register.getReadingType());
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime().toInstant(), registerValue.getFromTime().toInstant(), registerValue.getToTime().toInstant(), registerValue.getEventTime().toInstant());
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