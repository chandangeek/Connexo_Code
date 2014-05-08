package com.energyict.mdc.engine.impl.scheduling.factories;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.scheduling.MultiThreadedScheduledComPort;
import com.energyict.mdc.engine.impl.scheduling.ScheduledComPort;
import com.energyict.mdc.engine.impl.scheduling.SingleThreadedScheduledComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.issues.IssueService;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implmementation for the {@link ScheduledComPortFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:24)
 */
public class ScheduledComPortFactoryImpl implements ScheduledComPortFactory {

    private ComServerDAO comServerDAO;
    private DeviceCommandExecutor deviceCommandExecutor;
    private ThreadFactory threadFactory;

    public ScheduledComPortFactoryImpl (ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        this(comServerDAO, deviceCommandExecutor, Executors.defaultThreadFactory());
    }

    public ScheduledComPortFactoryImpl (ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory) {
        super();
        this.comServerDAO = comServerDAO;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.threadFactory = threadFactory;
    }

    @Override
    public ScheduledComPort newFor(OutboundComPort comPort, IssueService issueService) {
        if (comPort.isActive()) {
            switch (comPort.getNumberOfSimultaneousConnections()) {
                case 0: {
                    return null;
                }
                case 1: {
                    return new SingleThreadedScheduledComPort(comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, issueService);
                }
                default: {
                    return new MultiThreadedScheduledComPort(comPort, this.comServerDAO, this.deviceCommandExecutor, this.threadFactory, issueService);
                }
            }
        }
        else {
            return null;
        }
    }

}