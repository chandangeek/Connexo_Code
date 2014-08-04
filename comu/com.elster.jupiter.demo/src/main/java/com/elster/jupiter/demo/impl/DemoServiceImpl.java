package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.*;
import com.energyict.mdc.protocol.api.ComPortType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {"osgi.command.scope=demo", "osgi.command.function=createDemoData"}, immediate = true)
public class DemoServiceImpl implements DemoService {

    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void createDemoData(){
        OnlineComServer comServer = createComServer("deitvs015");
        OutboundComPort outboundTCPPort = createOutpoundTcpComPort("DefaultActiveOutboundTCPPort", comServer);
        OutboundComPortPool comPortPool = createOutboundTcpComPortPool("DefaultActiveComPortPool", outboundTCPPort);
        
    }

    // Creates active ComServer with the name of the demo environment (deitvs015)
    private OnlineComServer createComServer(final String name){
        return executeTransaction(new Transaction<OnlineComServer>(){
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
                return comServer;
            }
        });
    }

    // Creates active outbound TCP port with 5 simultaneous connections
    private OutboundComPort createOutpoundTcpComPort(final String name, final ComServer comServer){
        return executeTransaction(new Transaction<OutboundComPort>(){
            @Override
            public OutboundComPort perform() {
                System.out.println("==> Creating Outbound TCP Port...");
                OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(name, 5);
                outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
                return outboundComPortBuilder.add();
            }
        });
    }

    private OutboundComPortPool createOutboundTcpComPortPool(final String name, final OutboundComPort... comPorts){
        return executeTransaction(new Transaction<OutboundComPortPool>(){
            @Override
            public OutboundComPortPool perform() {
                System.out.println("==> Creating Outbound TCP Port Pool...");
                OutboundComPortPool outboundComPortPool = engineModelService.newOutboundComPortPool();
                outboundComPortPool.setActive(true);
                outboundComPortPool.setComPortType(ComPortType.TCP);
                outboundComPortPool.setName(name);
                if (comPorts != null) {
                    for (OutboundComPort comPort : comPorts) {
                        outboundComPortPool.addOutboundComPort(comPort);
                    }
                }
                outboundComPortPool.save();
                return outboundComPortPool;
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
            System.out.println("==> Success");
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
