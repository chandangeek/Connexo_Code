package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.model.*;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {"osgi.command.scope=demo", "osgi.command.function=createDemoData"}, immediate = true)
public class DemoServiceImpl implements DemoService {

    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile MasterDataService masterDataService;
    private volatile MeteringService meteringService;

    public void createDemoData() {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                OnlineComServer comServer = createComServer("deitvs015");
                OutboundComPort outboundTCPPort = createOutboundTcpComPort("DefaultActiveOutboundTCPPort", comServer);
                createOutboundTcpComPortPool("DefaultActiveComPortPool", outboundTCPPort);
                InboundComPortPool inboundServletComPortPool = createInboundServletComPortPool("DefaultInboundServletComPortPool");
                createInboundServletPort("DefaultActiveInboundServletPort", 4444, comServer, inboundServletComPortPool);
                createRegisterTypes();
            }
        });
    }

    // Creates active ComServer with the name of the demo environment (deitvs015)
    private OnlineComServer createComServer(final String name) {

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

    // Creates active outbound TCP port with 5 simultaneous connections
    private OutboundComPort createOutboundTcpComPort(final String name, final ComServer comServer) {

        System.out.println("==> Creating Outbound TCP Port...");
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(name, 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        comPort.save();
        return comPort;

    }

    private OutboundComPortPool createOutboundTcpComPortPool(final String name, final OutboundComPort... comPorts) {

        System.out.println("==> Creating Outbound TCP Port Pool...");
        OutboundComPortPool outboundComPortPool = engineModelService.newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName(name);
        outboundComPortPool.setTaskExecutionTimeout(new TimeDuration(0, TimeDuration.SECONDS));
        if (comPorts != null) {
            for (OutboundComPort comPort : comPorts) {
                outboundComPortPool.addOutboundComPort(comPort);
            }
        }
        outboundComPortPool.save();
        return outboundComPortPool;

    }

    private InboundComPortPool createInboundServletComPortPool(final String name) {

        System.out.println("==> Creating Inbound Servlet Port Pool...");
        InboundDeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(DlmsSerialNumberDiscover.class.getName()).get(0);
        InboundComPortPool inboundComPortPool = engineModelService.newInboundComPortPool();
        inboundComPortPool.setActive(true);
        inboundComPortPool.setComPortType(ComPortType.SERVLET);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(protocolPluggableClass);
        inboundComPortPool.setName(name);
        inboundComPortPool.save();
        return inboundComPortPool;

    }

    private ServletBasedInboundComPort createInboundServletPort(final String name, final int portNumber, final ComServer comServer, final InboundComPortPool comPortPool) {

        System.out.println("==> Creating Inbound Servlet Port...");
        ServletBasedInboundComPort.ServletBasedInboundComPortBuilder comPortBuilder = comServer.newServletBasedInboundComPort(name, "context", 10, portNumber);
        comPortBuilder.active(true).comPortPool(comPortPool).keyStoreSpecsFilePath("");
        ServletBasedInboundComPort comPort = comPortBuilder.add();
        comPort.save();
        return comPort;

    }

    private void createRegisterTypes(){

        System.out.println("==> Creating Register Types...");
        createRegisterType("Active Energy Import Tariff 1 (kWh)", "1.0.1.8.1.255", "kWh","0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0", 0);
        createRegisterType("Active Energy Import Tariff 1 (Wh)", "1.0.1.8.1.255", "Wh",  "0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0", 0);
        createRegisterType("Active Energy Import Tariff 2 (kWh)", "1.0.1.8.2.255", "kWh","0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0", 0);
        createRegisterType("Active Energy Import Tariff 2 (Wh)", "1.0.1.8.2.255", "Wh",  "0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0", 0);
        createRegisterType("Active Energy Export Tariff 1 (kWh)", "1.0.1.8.1.255", "kWh","0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0", 0);
        createRegisterType("Active Energy Export Tariff 1 (Wh)", "1.0.1.8.1.255", "Wh",  "0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0", 0);
        createRegisterType("Active Energy Export Tariff 2 (kWh)", "1.0.1.8.2.255", "kWh","0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0", 0);
        createRegisterType("Active Energy Export Tariff 2 (Wh)", "1.0.1.8.2.255", "Wh",  "0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0", 0);

    }

    private RegisterType createRegisterType(String name, String obisCode, String unit, String readingType, int timeOfUse){

        Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
        if (!readingTypeOptional.isPresent()){
            System.out.println("==> No such reading type"); // how to create?
        }
        RegisterType registerType = masterDataService.newRegisterType(name, ObisCode.fromString(obisCode), Unit.get(unit), readingTypeOptional.get(), timeOfUse);
        registerType.save();
        return registerType;

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

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            T result = transactionService.execute(transaction);
            System.out.println("==> Success");
            return result;
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
