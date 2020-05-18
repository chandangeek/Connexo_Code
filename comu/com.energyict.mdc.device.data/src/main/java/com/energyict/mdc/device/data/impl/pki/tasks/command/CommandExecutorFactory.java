package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.bpm.BpmService;
import com.energyict.mdc.device.data.impl.pki.tasks.BpmProcessResolver;

import java.time.Instant;
import java.util.logging.Logger;

public class CommandExecutorFactory {

    public CommandExecutor renewal(CommandErrorHandler commandErrorHandler, BpmService bpmService, String bpmProcessId, Instant aTime, Logger logger) {
        SecAccFilter secAccFilter = new SecAccFilter(aTime, logger);
        CheckSecuritySets checkSecuritySets = new CheckSecuritySets(logger);
        TriggerBpm triggerBpm = new TriggerBpm(bpmService, new BpmProcessResolver(bpmService, logger), bpmProcessId, logger);
        return new CommandExecutor(commandErrorHandler, secAccFilter, checkSecuritySets, triggerBpm);
    }

}
