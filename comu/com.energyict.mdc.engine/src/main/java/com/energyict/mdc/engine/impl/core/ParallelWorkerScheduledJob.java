package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.util.concurrent.CountDownLatch;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/08/2016 - 16:43
 */
public class ParallelWorkerScheduledJob extends ScheduledComTaskExecutionGroup implements Runnable {

    private final ParallelRootScheduledJob parallelRootScheduledJob;
    private final CountDownLatch start;

    private Boolean connectionEstablished = null;

    public ParallelWorkerScheduledJob(ParallelRootScheduledJob parallelRootScheduledJob, CountDownLatch start) {
        super(((OutboundComPort) parallelRootScheduledJob.getComPort()), parallelRootScheduledJob.getComServerDAO(), parallelRootScheduledJob.getDeviceCommandExecutor(), parallelRootScheduledJob.getConnectionTask(), parallelRootScheduledJob.getServiceProvider());
        this.parallelRootScheduledJob = parallelRootScheduledJob;
        this.start = start;
    }

    @Override
    public void run() {
        this.execute();
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("ComPort schedule worker for " + getComPort().getName());

        /*
        1/ Connect to the device
        2/ loop and execute until you don't get anything anymore
        3/ store stuff if required
        * */

        try {
            this.start.await(); // wait until the parallelRoot has finished populating the queue

            GroupedDeviceCommand groupedDeviceCommand = parallelRootScheduledJob.next();
            while (groupedDeviceCommand != null) {
                boolean success = false;
                try {
                    checkForConnection();
                    groupedDeviceCommand.updateComChannelForComCommands(getExecutionContext().getComPortRelatedComChannel());
                    groupedDeviceCommand.performAfterConnectionSetup(getExecutionContext());
                    success = true;
                } finally {
                    if (success) {
                        groupedDeviceCommand = parallelRootScheduledJob.next();
                        if (groupedDeviceCommand != null) {
                            parallelRootScheduledJob.completedASingleJob(); // do a countdown
                        } // else the countdown will happen the executionContext is delivered
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                this.completeConnection();
            } finally {
                this.closeConnection();
                parallelRootScheduledJob.complete(this);
            }
        }
    }

    private boolean checkForConnection() {
        if (connectionEstablished == null) {
            createExecutionContext(false);
            connectionEstablished = this.establishConnectionFor(this.getComPort());
        }
        return connectionEstablished;
    }
}