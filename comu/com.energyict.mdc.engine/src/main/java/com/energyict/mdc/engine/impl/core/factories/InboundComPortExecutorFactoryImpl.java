package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutor;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutorImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.issues.IssueService;

/**
 * Provides simple functionality to create an {@link InboundComPortExecutor}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 16:50
 */
public class InboundComPortExecutorFactoryImpl implements InboundComPortExecutorFactory{

    /**
     * Default no argument constructor
     */
    public InboundComPortExecutorFactoryImpl() {
    }

    @Override
    public InboundComPortExecutor create(InboundComPort inboundComPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        return new InboundComPortExecutorImpl(inboundComPort, comServerDAO, deviceCommandExecutor, issueService);
    }
}
