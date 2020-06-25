package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.bpm.BpmService;
import com.energyict.mdc.device.data.impl.pki.tasks.BpmProcessResolver;

import java.time.Instant;
import java.util.logging.Logger;

public class CommandExecutorFactory {

    public CommandExecutor renewal(CommandErrorHandler commandErrorHandler, BpmService bpmService, String bpmProcessId, Instant aTime, Logger logger) {
        SecAccFilter secAccFilter = new SecAccFilter();
        DeviceFilter deviceFilter = new DeviceFilter(aTime);
        DeviceSecurityAccessorFilter deviceSecurityAccessorFilter = new DeviceSecurityAccessorFilter();
        CheckSecuritySets checkSecuritySets = new CheckSecuritySets(logger);
        TriggerBpm triggerBpm = new TriggerBpm(bpmService, new BpmProcessResolver(bpmService, logger), bpmProcessId, logger);
        return new CommandExecutor(commandErrorHandler, secAccFilter, deviceFilter, deviceSecurityAccessorFilter, checkSecuritySets, triggerBpm);
    }

}
