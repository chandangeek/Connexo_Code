package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedRegisterList;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.inbound.general.frames.parsing.RegisterInfo;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

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

    public RegisterFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        super(frame, callHomeIdPlaceHolder);
    }

    @Override
    public void doParse() {
        List<RegisterValue> registers = new ArrayList<RegisterValue>();
        this.parseParameters(registers);
        for (RegisterValue register : registers) {
            getCollectedRegisterList().addCollectedRegister(this.processRegister(register));
        }
    }

    private void parseParameters(List<RegisterValue> registers) {
        for (String parameter : this.getParameters()) {
            this.parseParameter(parameter, registers);
        }
    }

    private void parseParameter(String parameter, List<RegisterValue> registers) {
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

    private CollectedRegister processRegister(RegisterValue register) {
        CollectedRegister deviceRegister;
        Date readTime = getInboundParameters().getReadTime();
        readTime = readTime == null ? new Date() : readTime;
        if (register.getObisCode().getF() != 255) {
            deviceRegister = MdcManager.getCollectedDataFactory().createBillingCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(readTime, null, readTime);
        } else if (register.getEventTime() != null) {
            deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setCollectedTimeStamps(readTime, null, readTime, register.getEventTime());
        } else {
            deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register.getObisCode()));
            deviceRegister.setCollectedData(register.getQuantity(), register.getText());
            deviceRegister.setReadTime(readTime);
        }

        return deviceRegister;
    }

    private CollectedRegisterList getCollectedRegisterList() {
        if (this.collectedRegisterList == null) {
            this.collectedRegisterList = MdcManager.getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifierByDialHomeIdPlaceHolder());
            getCollectedDatas().add(this.collectedRegisterList);
        }
        return this.collectedRegisterList;
    }

    private RegisterIdentifier getRegisterIdentifier(ObisCode registerObisCode) {
        return new RegisterDataIdentifierByObisCodeAndDevice(registerObisCode, getDeviceIdentifierByDialHomeIdPlaceHolder());
    }

}