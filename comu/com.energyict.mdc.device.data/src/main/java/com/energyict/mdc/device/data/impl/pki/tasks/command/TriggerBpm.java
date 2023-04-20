package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.pki.tasks.BpmProcessResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TriggerBpm implements Command {

    private final BpmProcessResolver bpmProcessResolver;
    private final BpmService bpmService;
    private final Optional<BpmProcessDefinition> bpmProcessDefinition;
    private final String keyRenewalBpmProcessDefinitionId;
    private final Logger logger;
    private int triggeredBpmProcessCounter = 0;

    public TriggerBpm(BpmService bpmService, BpmProcessResolver bpmProcessResolver, String bpmProcessId, Logger logger) {
        this.bpmService = bpmService;
        this.bpmProcessResolver = bpmProcessResolver;
        this.bpmProcessDefinition = bpmProcessResolver.resolve(bpmProcessId);
        this.keyRenewalBpmProcessDefinitionId = bpmProcessId;
        this.logger = logger;
    }

    @Override
    public void run(SecurityAccessor securityAccessor) throws CommandErrorException, CommandAbortException {
        if (bpmProcessDefinition.isPresent()) {
            if (this.bpmProcessResolver.canBeStarted(securityAccessor, keyRenewalBpmProcessDefinitionId)) {
                Map<String, Object> expectedParams = new HashMap<>();
                expectedParams.put("deviceId", securityAccessor.getDevice().getmRID());
                expectedParams.put("accessorType", securityAccessor.getSecurityAccessorType().getName());
                print(securityAccessor);
                bpmService.startProcess(bpmProcessDefinition.get(), expectedParams);
                logger.log(Level.INFO, "BPM process has been triggered on device " + securityAccessor.getDevice().getName()
                        + " mrid = " + securityAccessor.getDevice().getmRID() + " for " + securityAccessor.getSecurityAccessorType().getName());
                triggeredBpmProcessCounter++;
            } else {
                throw new CommandAbortException("Process already started");
            }
        } else {
            throw new CommandErrorException("No BPM process definition found. Could not start renew action for security accessor:" + securityAccessor);
        }

    }

    public int triggeredBpmProcessCounter() {
        return triggeredBpmProcessCounter;
    }

    private void print(SecurityAccessor securityAccessor) {
        StringBuilder sb = new StringBuilder();
        sb.append("Type=" + securityAccessor.getSecurityAccessorType().getName());
        sb.append(" Device=" + securityAccessor.getDevice().getName());
        sb.append('\n');
        logger.log(Level.INFO, sb.toString());
    }
}
