package com.energyict.protocolimplv2.edmi.mk10.registers;

import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;

import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeMapper;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 28/02/2017 - 15:21
 */
public class MK10RegisterFactory implements DeviceRegisterSupport {

    private CommandLineProtocol protocol;
    private ObisCodeMapper obisCodeMapper;

    public MK10RegisterFactory(CommandLineProtocol protocol) {
        this.protocol = protocol;

    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(registers.size());
        for (OfflineRegister register : registers) {
            try {
                RegisterValue registerValue = getObisCodeMapper().getRegisterValue(register.getObisCode());
                collectedRegisters.add(createCollectedRegister(register, registerValue));
            } catch (NoSuchRegisterException e) {
                collectedRegisters.add(createNotSupportedCollectedRegister(register));
            } catch (CommunicationException e) {
                if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                    collectedRegisters.add(createNotSupportedCollectedRegister(register));
                } else {
                    throw e; // Rethrow the original communication exception
                }
            }
        }
        return collectedRegisters;
    }

    protected CollectedRegister createDeviceRegister(OfflineRegister register) {
        return MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode()));
    }

    protected CollectedRegister createCollectedRegister(OfflineRegister register, RegisterValue registerValue) {
        CollectedRegister deviceRegister = createDeviceRegister(register);
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    protected CollectedRegister createNotSupportedCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register, "registerXnotsupported", register.getObisCode()));
        return failedRegister;
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(getProtocol());
        }
        return obisCodeMapper;
    }
}