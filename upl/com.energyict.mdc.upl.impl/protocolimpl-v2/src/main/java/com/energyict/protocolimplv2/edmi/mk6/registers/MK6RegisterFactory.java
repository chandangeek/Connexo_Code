/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.registers;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeMapper;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 3/03/2017 - 16:53
 */
public class MK6RegisterFactory implements DeviceRegisterSupport {

    private CommandLineProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private ObisCodeMapper obisCodeMapper;

    public MK6RegisterFactory(CommandLineProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
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
        return collectedDataFactory.createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode(), register.getDeviceIdentifier()));
    }

    protected CollectedRegister createCollectedRegister(OfflineRegister register, RegisterValue registerValue) {
        CollectedRegister deviceRegister = createDeviceRegister(register);
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    protected CollectedRegister createNotSupportedCollectedRegister(OfflineRegister register) {
        CollectedRegister failedRegister = createDeviceRegister(register);
        failedRegister.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(register, "registerXnotsupported", register.getObisCode()));
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