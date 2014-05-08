package com.energyict.mdc.engine.impl.scheduling.factories;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.scheduling.InboundComPortExecutor;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;

/**
 * Copyrights EnergyICT
 * Date: 23/10/12
 * Time: 9:43
 */
public interface InboundComPortExecutorFactory {

    /**
     * Creates an InboundComPortExecutor based on the given parameters.
     *
     *
     * @param inboundComPort        the used InboundComPort
     * @param comServerDAO          the used ComServerDAO
     * @param deviceCommandExecutor the used DeviceCommandExecutor
     * @param issueService
     * @return the newly created InboundComPortExecutor
     */
    public InboundComPortExecutor create(InboundComPort inboundComPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService);

}
