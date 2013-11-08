package com.energyict.protocolsmdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedRegisterList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.protocolsmdc.protocol.inbound.general.frames.parsing.RegisterInfo;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumberPlaceHolder;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;

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
        List<RegisterValue> registers = new ArrayList<RegisterValue>();
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
            deviceRegister = MdcManager.getCollectedDataFactory().createBillingCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(new Date(), null, getInboundParameters().getReadTime());
        } else if (register.getEventTime() != null) {
            deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(new Date(), null, getInboundParameters().getReadTime(), register.getEventTime());
        } else {
            deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setReadTime(getInboundParameters().getReadTime());
        }

        if (this.getDevice() == null) {
            deviceRegister.setFailureInformation(ResultType.ConfigurationMisMatch, MdcManager.getIssueCollector().addProblem(deviceRegister, "protocol.rtunotfound", getInboundParameters().getSerialNumber()));
        } else if (this.getDevice().getRegister(register.getObisCode()) == null) {
            deviceRegister.setFailureInformation(ResultType.ConfigurationMisMatch, MdcManager.getIssueCollector().addProblem(deviceRegister, "protocol.registernotfound", register.getObisCode(), getInboundParameters().getSerialNumber()));
        }
        return deviceRegister;
    }

    private CollectedRegisterList getCollectedRegisterList(){
        if(this.collectedRegisterList == null){
            this.collectedRegisterList = MdcManager.getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifier());
            getCollectedDatas().add(this.collectedRegisterList);
        }
        return this.collectedRegisterList;
    }

    private RegisterIdentifier getRegisterIdentifier(ObisCode registerObisCode){
        return new RegisterDataIdentifierByObisCodeAndDevice(registerObisCode, getDeviceIdentifier());
    }

    private DeviceIdentifierBySerialNumberPlaceHolder getDeviceIdentifier() {
        return new DeviceIdentifierBySerialNumberPlaceHolder(serialNumberPlaceHolder);
    }

}