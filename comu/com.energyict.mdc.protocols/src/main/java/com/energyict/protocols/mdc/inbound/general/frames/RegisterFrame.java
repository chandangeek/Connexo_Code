package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumberPlaceHolder;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;
import com.energyict.protocols.mdc.inbound.general.frames.parsing.RegisterInfo;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 25/06/12
 * Time: 11:08
 * Author: khe
 */
public class RegisterFrame extends AbstractInboundFrame {

    private CollectedRegisterList collectedRegisterList;

    @Override
    protected FrameType getType() {
        return FrameType.REGISTER;
    }

    public RegisterFrame(String frame, SerialNumberPlaceHolder serialNumberPlaceHolder) {
        super(frame, serialNumberPlaceHolder);
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
            deviceRegister = this.getCollectedDataFactory().createBillingCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(new Date(), null, getInboundParameters().getReadTime());
        } else if (register.getEventTime() != null) {
            deviceRegister = this.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(new Date(), null, getInboundParameters().getReadTime(), register.getEventTime());
        } else {
            deviceRegister = this.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setReadTime(getInboundParameters().getReadTime());
        }

        if (this.getDevice() == null) {
            deviceRegister.setFailureInformation(ResultType.ConfigurationMisMatch, Bus.getIssueService()
                    .newIssueCollector()
                    .addProblem(deviceRegister, "protocol.rtunotfound", getInboundParameters().getSerialNumber()));
        } else if (this.getDevice().getRegisterWithDeviceObisCode(register.getObisCode()) == null) {
            deviceRegister.setFailureInformation(ResultType.ConfigurationMisMatch, Bus.getIssueService()
                    .newIssueCollector()
                    .addProblem(deviceRegister, "protocol.registernotfound", register.getObisCode(), getInboundParameters().getSerialNumber()));
        }
        return deviceRegister;
    }

    private CollectedRegisterList getCollectedRegisterList(){
        if(this.collectedRegisterList == null){
            this.collectedRegisterList = this.getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifier());
            getCollectedDatas().add(this.collectedRegisterList);
        }
        return this.collectedRegisterList;
    }

    private RegisterIdentifier getRegisterIdentifier(ObisCode registerObisCode){
        return new RegisterDataIdentifierByObisCodeAndDevice(registerObisCode, registerObisCode, getDeviceIdentifier());
    }

    private DeviceIdentifierBySerialNumberPlaceHolder getDeviceIdentifier() {
        return new DeviceIdentifierBySerialNumberPlaceHolder(serialNumberPlaceHolder);
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}