package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.util.Collections;
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
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public ReadMBusRegisters(ACE4000Outbound ace4000, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(ace4000);
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        multiFramedAnswer = true;
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
        boolean receivedAllNecessaryRegisters = true;
        if (mustReceiveMBusBilling) {
            receivedAllNecessaryRegisters &= isReceivedRequest(RequestType.MBusBillingRegister);
        }
        if (mustReceiveMBusCurrent) {
            receivedAllNecessaryRegisters &= isReceivedRequest(RequestType.MBusCurrentRegister);
        }

        //Retry if necessary, return results if all registers (or NACKs) were received
        if (receivedAllNecessaryRegisters) {
            createResult("Requested register but the meter responded with NACK. " + getReasonDescription());
        }
    }

    @Override
    protected void handleException(RuntimeException e) {
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
                if (isSupported(rtuRegister)) {
                    CollectedRegister defaultDeviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(rtuRegister.getObisCode(), getAce4000().getDeviceIdentifier()));
                    defaultDeviceRegister.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(rtuRegister.getObisCode(), "registerXissue", rtuRegister.getObisCode(), msg));
                    result.add(defaultDeviceRegister);
                } else {
                    CollectedRegister defaultDeviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(rtuRegister.getObisCode(), getAce4000().getDeviceIdentifier()));
                    defaultDeviceRegister.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(rtuRegister.getObisCode(), "registerXnotsupported", rtuRegister.getObisCode()));
                    result.add(defaultDeviceRegister);
                }
            }
        }
        setResult(result);
    }

    /**
     * Indicate if the requested register is supported by the device or not
     */
    private boolean isSupported(OfflineRegister offlineRegister) {
        List<OfflineRegister> offlineRegisters = Collections.singletonList(offlineRegister);
        return shouldRequestBillingRegisters(offlineRegisters) || shouldRequestCurrentRegisters(offlineRegisters);
    }
}