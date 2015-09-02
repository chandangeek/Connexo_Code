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

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ReadRegisters extends AbstractRequest<List<OfflineRegister>, List<CollectedRegister>> {

    private boolean mustReceiveBilling = false;
    private boolean mustReceiveCurrent = false;
    private boolean mustReceiveInstant = false;

    public ReadRegisters(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    protected void doBefore() {
        mustReceiveBilling = shouldRequestBillingRegisters(getInput());
        mustReceiveCurrent = shouldRequestCurrentRegisters(getInput());
        mustReceiveInstant = shouldRequestInstantaneousRegisters(getInput());
    }

    public boolean shouldRequestCurrentRegisters(List<OfflineRegister> registers) {
        for (OfflineRegister register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (obisCode.getA() == 1 && obisCode.getB() == 0 && (obisCode.getC() == 1 || obisCode.getC() == 2) && obisCode.getD() == 8 && obisCode.getF() == 255) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if billing registers are defined on the device in EiServer.
     *
     * @param registers list of all registers defined on the device
     * @return true or false
     */
    public boolean shouldRequestBillingRegisters(List<OfflineRegister> registers) {
        for (OfflineRegister register : registers) {
            if (isBillingRegister(register)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBillingRegister(OfflineRegister register) {
        return register.getObisCode().getF() != 255;
    }

    public boolean shouldRequestInstantaneousRegisters(List<OfflineRegister> registers) {
        List<Integer> allowedCFields = new ArrayList<Integer>();
        allowedCFields.add(31);
        allowedCFields.add(51);
        allowedCFields.add(71);
        allowedCFields.add(32);
        allowedCFields.add(52);
        allowedCFields.add(72);
        allowedCFields.add(21);
        allowedCFields.add(41);
        allowedCFields.add(61);
        allowedCFields.add(23);
        allowedCFields.add(43);
        allowedCFields.add(63);
        allowedCFields.add(29);
        allowedCFields.add(49);
        allowedCFields.add(69);
        allowedCFields.add(85);
        allowedCFields.add(86);
        allowedCFields.add(87);

        for (OfflineRegister register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (allowedCFields.contains(obisCode.getC()) && obisCode.getA() == 1 && obisCode.getB() == 0 && obisCode.getD() == 7 && obisCode.getE() == 0 && obisCode.getF() == 255) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doRequest() {
        //Send necessary requests
        if (!receivedBillingRegisters() && mustReceiveBilling) {
            getAce4000().getObjectFactory().sendBDRequest();
        }
        if (!receivedCurrentRegisters() && mustReceiveCurrent) {
            getAce4000().getObjectFactory().sendCurrentRegisterRequest();
        }
        if (!receivedInstantRegisters() && mustReceiveInstant) {
            getAce4000().getObjectFactory().sendInstantVoltageAndCurrentRequest();
        }
    }

    private boolean receivedCurrentRegisters() {
        return isReceivedRequest(RequestType.CurrentRegisters);
    }

    private boolean receivedBillingRegisters() {
        return isReceivedRequest(RequestType.BillingRegisters);
    }

    private boolean receivedInstantRegisters() {
        return isReceivedRequest(RequestType.InstantRegisters);
    }

    @Override
    protected void parseResult() {
        //Check if all necessary registers are received
        boolean receivedAllNecessaryRegisters = false;
        if (mustReceiveBilling) {
            receivedAllNecessaryRegisters = receivedBillingRegisters();
        }
        if (mustReceiveCurrent) {
            receivedAllNecessaryRegisters = receivedCurrentRegisters();
        }
        if (mustReceiveInstant) {
            receivedAllNecessaryRegisters = receivedInstantRegisters();
        }

        //Retry if necessary, return results if all registers were received
        if (receivedAllNecessaryRegisters) {
            createResult("Requested register but the meter responded with NACK." + getReasonDescription());  //E.g. billing data not available
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