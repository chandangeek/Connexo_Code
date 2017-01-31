/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Captain hook lost his hand to a crocodile and hears the ticking all the time.
 * This handler will log a tic and a tac on a service call for a device, a ratio 1 per second, during 5 minutes.
 * Attempt to implement PAUSED state
 */
@Component(name = "com.energyict.mdc.servicecall.example.captainhook.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=CaptainHookHandler")
public class CaptainHookHandler implements ServiceCallHandler {

    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServiceCallService serviceCallService;

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipleService;

    public CaptainHookHandler() {
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setThreadPrincipleService(ThreadPrincipalService threadPrincipleService) {
        this.threadPrincipleService = threadPrincipleService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating captain hook handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case ONGOING:
                if (DefaultState.PENDING.equals(oldState)) {
                    // this check will prevent the timer to be created multiple times when transitioning back to ONGOING
                    // from PAUSED, but in case of a restart while in state PAUSED, the timer would never be created when
                    // transitioning to ONGOING. Solution to this problem: use a ScheduledTask: it is persistent and will be restarted!
                    ticTac(serviceCall);
                }
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState));
                break;
        }
    }

    protected void ticTac(ServiceCall serviceCall) {
        TicTacTask task = new TicTacTask(serviceCall);
        Timer timer = new Timer("Service call " + serviceCall.getId());
        timer.schedule(task, new Date(), 1000);
    }

    class TicTacTask extends TimerTask {

        private final long serviceCallId;
        private final AtomicInteger count = new AtomicInteger(360);
        private boolean tic = true;

        TicTacTask(ServiceCall serviceCall) {
            this.serviceCallId = serviceCall.getId();
        }

        @Override
        public void run() {
            threadPrincipleService.set(() -> "Console");
            try (TransactionContext context = transactionService.getContext()) {
                ServiceCall serviceCall = serviceCallService.getServiceCall(serviceCallId)
                        .get(); // we need a fresh copy to read the latest state
                if (DefaultState.ONGOING.equals(serviceCall.getState())) {
                    Device device = (Device) serviceCall.getTargetObject().get();
                    serviceCall.log(LogLevel.FINE, "Device " + device.getId() + ": " + (tic ? "TIC" : "TAC"));
                    tic = !tic;
                    if (count.addAndGet(-1) <= 0) {
                        this.cancel();
                        serviceCall.log(LogLevel.WARNING, "I'm all done with clocks");
                        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    }
                } else if (DefaultState.CANCELLED.equals(serviceCall.getState())) {
                    serviceCall.log(LogLevel.WARNING, "Looks like I got cancelled");
                    this.cancel();
                }
                context.commit();
            }
        }
    }
}
