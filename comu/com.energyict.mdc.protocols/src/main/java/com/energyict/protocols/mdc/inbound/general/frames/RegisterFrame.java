package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocols.mdc.inbound.general.frames.parsing.RegisterInfo;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 25/06/12
 * Time: 11:08
 * Author: khe
 */
public class RegisterFrame extends AbstractInboundFrame {

    private final Clock clock;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final CollectedDataFactory collectedDataFactory;
    private CollectedRegisterList collectedRegisterList;

    @Override
    protected FrameType getType() {
        return FrameType.REGISTER;
    }

    public RegisterFrame(String frame, Clock clock, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory) {
        super(frame, issueService, identificationService);
        this.clock = clock;
        this.readingTypeUtilService = readingTypeUtilService;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public void doParse() {
        List<RegisterValue> registers = new ArrayList<>();
        this.parseParameters(registers);
        for (RegisterValue register : registers) {
            getCollectedRegisterList().addCollectedRegister(this.processRegister(register));
        }
    }

    private void parseParameters (List<RegisterValue> registers) {
        for (String parameter : this.getParameters()) {
            this.parseParameter(parameter, registers);
        }
    }

    private void parseParameter (String parameter, List<RegisterValue> registers) {
        String[] obisCodeAndValue = parameter.split("=");
        if (obisCodeAndValue.length == 2) {
            ObisCode obisCode;
            try {
                obisCode = ObisCode.fromString(obisCodeAndValue[0]);
            } catch (IllegalArgumentException e) {
                //Move on to the next parameter
                return;
            }
            RegisterInfo registerInfo = new RegisterInfo(obisCode, obisCodeAndValue[1], getInboundParameters().getReadTime());
            RegisterValue registerValue = registerInfo.parse();
            registers.add(registerValue);
        }
    }

    private CollectedRegister processRegister (RegisterValue register) {
        CollectedRegister deviceRegister;
        if (register.getObisCode().getF() != 255) {
            deviceRegister = this.collectedDataFactory.createBillingCollectedRegister(getRegisterIdentifier(register.getObisCode()),
                    this.readingTypeUtilService.getReadingTypeFrom(register.getObisCode(), register.getQuantity().getUnit()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(
                    this.clock.instant(),
                    null,
                    getInboundParameters().getReadTime().toInstant());
        } else if (register.getEventTime() != null) {
            deviceRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(register.getObisCode()),
                    this.readingTypeUtilService.getReadingTypeFrom(register.getObisCode(), register.getQuantity().getUnit()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(
                    this.clock.instant(),
                    null,
                    getInboundParameters().getReadTime().toInstant(),
                    register.getEventTime().toInstant());
        } else {
            deviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register.getObisCode()),
                    this.readingTypeUtilService.getReadingTypeFrom(register.getObisCode(), register.getQuantity().getUnit()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setReadTime(getInboundParameters().getReadTime().toInstant());
        }

        if (this.getDevice() == null) {
            deviceRegister.setFailureInformation(
                    ResultType.ConfigurationMisMatch,
                    this.getIssueService()
                            .newIssueCollector()
                            .addProblem(deviceRegister, "protocol.rtunotfound", getInboundParameters().getSerialNumber()));
        } else if (this.getDevice().getRegisterWithDeviceObisCode(register.getObisCode()) == null) {
            deviceRegister.setFailureInformation(
                    ResultType.ConfigurationMisMatch,
                    this.getIssueService()
                        .newIssueCollector()
                        .addProblem(deviceRegister, "protocol.registernotfound", register.getObisCode(), getInboundParameters().getSerialNumber()));
        }
        return deviceRegister;
    }

    private CollectedRegisterList getCollectedRegisterList(){
        if(this.collectedRegisterList == null){
            this.collectedRegisterList = this.collectedDataFactory.createCollectedRegisterList(getDeviceIdentifier());
            getCollectedDatas().add(this.collectedRegisterList);
        }
        return this.collectedRegisterList;
    }

    private RegisterIdentifier getRegisterIdentifier(ObisCode registerObisCode){
        return new RegisterDataIdentifierByObisCodeAndDevice(registerObisCode, registerObisCode, getDeviceIdentifier());
    }

}