package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {"osgi.command.scope=demo", "osgi.command.function=createDemoData"}, immediate = true)
public class DemoServiceImpl implements DemoService {

    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void createDemoData(){
        createComServer("deitvs015");
    }

    // Creates active ComServer with the name of the demo environment (deitvs015)
    private void createComServer(final String name){
        executeTransaction(new Transaction<OnlineComServer>(){
            @Override
            public OnlineComServer perform() {
                System.out.println("==> Creating ComServer...");
                OnlineComServer comServer = engineModelService.newOnlineComServerInstance();
                comServer.setName(name);
                comServer.setActive(true);
                comServer.setServerLogLevel(ComServer.LogLevel.INFO);
                comServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
                comServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.MINUTES));
                comServer.setSchedulingInterPollDelay(new TimeDuration(60, TimeDuration.SECONDS));
                comServer.setStoreTaskQueueSize(50);
                comServer.setNumberOfStoreTaskThreads(1);
                comServer.setStoreTaskThreadPriority(5);
                comServer.save();
                System.out.println("==> Success");
                return comServer;
            }
        });
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            return transactionService.execute(transaction);
        } catch (Exception ex) {
            System.out.println("==> Fail");
            ex.printStackTrace();
            return null;
        } finally {
            clearPrincipal();
        }
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        };
    }
}
