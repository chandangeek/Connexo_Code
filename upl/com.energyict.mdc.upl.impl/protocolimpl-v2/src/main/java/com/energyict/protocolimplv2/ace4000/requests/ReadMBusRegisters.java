package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ReadMBusRegisters extends AbstractRequest<List<OfflineRegister>, List<CollectedRegister>> {

    private boolean mustReceiveMBusBilling = false;
    private boolean mustReceiveMBusCurrent = false;

    public ReadMBusRegisters(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    protected void doBefore() {
        mustReceiveMBusBilling = shouldRequestBillingRegisters(getInput());
        mustReceiveMBusCurrent = shouldRequestCurrentRegisters(getInput());
    }

    private boolean shouldRequestBillingRegisters(List<OfflineRegister> registers) {
        if (registers.isEmpty()) {
            return true;        //If no input, request all registers
        }
        for (OfflineRegister register : registers) {
            if (register.getObisCode().getF() != 255) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldRequestCurrentRegisters(List<OfflineRegister> registers) {
        if (registers.isEmpty()) {
            return true;        //If no input, request all registers
        }
        for (OfflineRegister register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (obisCode.getF() == 255) {
                return true;
            }
        }
        return false;   //Only billing registers to be requested
    }

    @Override
    protected void doRequest() {
        //Send necessary requests
        if (!isReceivedRequest(RequestType.MBusBillingRegister) && mustReceiveMBusBilling) {
            getAce4000().getObjectFactory().sendMBusBillingDataRequest();
        }
        if (!isReceivedRequest(RequestType.MBusCurrentRegister) && mustReceiveMBusCurrent) {
            getAce4000().getObjectFactory().sendMBusCurrentRegistersRequest();
        }
    }

    @Override
    protected void parseResult() {
        //Check if all necessary registers are received
        boolean receivedAllNecessaryRegisters = false;
        if (mustReceiveMBusBilling) {
            receivedAllNecessaryRegisters = isReceivedRequest(RequestType.MBusBillingRegister);
        }
        if (mustReceiveMBusCurrent) {
            receivedAllNecessaryRegisters = isReceivedRequest(RequestType.MBusCurrentRegister);
        }

        //Retry if necessary, return results if all registers (or NACKs) were received
        if (receivedAllNecessaryRegisters) {
            createResult("Requested register but the meter responded with NACK." + getReasonDescription());
        }
    }

    @Override
    protected void handleException(ComServerExecutionException e) {
        createResult("Didn't receive register from meter");
    }

    private void createResult(String msg) {
        //Didn't receive all necessary registers, log properly and move on
        List<CollectedRegister> result = getAce4000().getCollectedRegisters();
        for (OfflineRegister rtuRegister : getInput()) {
            boolean receivedRegister = false;
            for (ObisCode registerObisCode : getAce4000().getReceivedRegisterObisCodeList()) {
                if (rtuRegister.getObisCode().equals(registerObisCode)) {
                    receivedRegister = true;
                    break;
                }
            }
            if (!receivedRegister) {
                CollectedRegister defaultDeviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(rtuRegister.getObisCode(), getAce4000().getDeviceIdentifier()));
                defaultDeviceRegister.setFailureInformation(ResultType.DataIncomplete, MdcManager.getIssueFactory().createWarning(rtuRegister.getObisCode(), msg, rtuRegister.getObisCode()));
                result.add(defaultDeviceRegister);
            }
        }
        setResult(result);
    }
}