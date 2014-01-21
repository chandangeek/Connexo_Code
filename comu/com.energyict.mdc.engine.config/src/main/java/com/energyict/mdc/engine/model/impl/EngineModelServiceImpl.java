package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.engine.model.impl.ComPortImpl.OUTBOUND_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.OFFLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.ONLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.REMOTE_COMSERVER_DISCRIMINATOR;

@Component(name = "com.energyict.mdc.engine.model", service = {EngineModelService.class, InstallService.class} )
public class EngineModelServiceImpl implements EngineModelService, InstallService, OrmClient {

    private volatile DataModel dataModel;

    @Inject
    public EngineModelServiceImpl(OrmService ormService) {
        this.setOrmService(ormService);
        activate();
        if (!dataModel.isInstalled()) {
        	install();
        }
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("CEM", "ComServer Engine Model");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EngineModelService.class).toInstance(EngineModelServiceImpl.this);
                bind(ServletBasedInboundComPort.class).to(ServletBasedInboundComPortImpl.class);
                bind(ModemBasedInboundComPort.class).to(ModemBasedInboundComPortImpl.class);
                bind(TCPBasedInboundComPort.class).to(TCPBasedInboundComPortImpl.class);
                bind(UDPBasedInboundComPort.class).to(UDPBasedInboundComPortImpl.class);
                bind(OutboundComPort.class).to(OutboundComPortImpl.class);
                bind(ComPortPoolMember.class).to(ComPortPoolMemberImpl.class);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }


    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public ComServer findComServer(String name) {
        return getComServerDataMapper().getUnique("name", name).orNull();
    }

    @Override
    public ComServer findComServer(long id) {
        return getComServerDataMapper().getUnique("id", id).orNull();
    }

    @Override
    public List<ComServer> findAllComServers() {
        List<ComServer> comServers = new ArrayList<>();
        comServers.addAll(getComServerDataMapper().find());
        return comServers;
    }

    @Override
    public ComServer findComServerBySystemName() {
        return this.findComServer(HostName.getCurrent());
    }

    @Override
    public List<OnlineComServer> findAllOnlineComServers() {
        return convertComServerListToOnlineComServers(getComServerDataMapper().find("class", ONLINE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public List<RemoteComServer> findAllRemoteComServers() {
        return convertComServerListToRemoteComServers(getComServerDataMapper().find("class", REMOTE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public List<RemoteComServer> findRemoteComServersForOnlineComServer(OnlineComServer onlineComServer) {
        return convertComServerListToRemoteComServers(getComServerDataMapper().find("class", REMOTE_COMSERVER_DISCRIMINATOR, "onlineComServer", onlineComServer));    }

    @Override
    public List<OfflineComServer> findAllOfflineComServers() {
        return convertComServerListToOfflineComServers(getComServerDataMapper().find("class", OFFLINE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public int getOfflineServerCount() {
        return getComServerDataMapper().find("class", OFFLINE_COMSERVER_DISCRIMINATOR).size();
    }

    @Override
    public List<RemoteComServer> findRemoteComServersWithOnlineComServer(OnlineComServer onlineComServer) {
        return convertComServerListToRemoteComServers(getComServerDataMapper().find("onlineComServer", onlineComServer));
    }

    @Override
    public OnlineComServer newOnlineComServerInstance() {
        return dataModel.getInstance(OnlineComServerImpl.class);
    }

    @Override
    public OfflineComServer newOfflineComServerInstance() {
        return dataModel.getInstance(OfflineComServerImpl.class);
    }

    @Override
    public RemoteComServer newRemoteComServerInstance() {
        return dataModel.getInstance(RemoteComServerImpl.class);
    }

    /**
     * Convert the given List of {@link ComServer comServers} to a proper list of {@link OnlineComServer}
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link OnlineComServer}
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link OnlineComServer}
     */
    private List<OnlineComServer> convertComServerListToOnlineComServers(final List<ComServer> comServers) {
        List<OnlineComServer> onlineComServers = new ArrayList<OnlineComServer>(comServers.size());
        for (ComServer comServer : comServers) {
            onlineComServers.add((OnlineComServer) comServer);
        }
        return onlineComServers;
    }

    /**
     * Convert the given List of {@link ComServer comServers} to a proper list of {@link OfflineComServer}
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link OfflineComServer}
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link OfflineComServer}
     */
    private List<OfflineComServer> convertComServerListToOfflineComServers(final List<ComServer> comServers) {
        List<OfflineComServer> offlineComServers = new ArrayList<OfflineComServer>(comServers.size());
        for (ComServer comServer : comServers) {
            offlineComServers.add((OfflineComServer) comServer);
        }
        return offlineComServers;
    }

    /**
     * Convert the given List of {@link ComServer comServers} to a proper list of {@link RemoteComServer}
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link RemoteComServer}
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link RemoteComServer}
     */
    private List<RemoteComServer> convertComServerListToRemoteComServers(final List<ComServer> comServers) {
        List<RemoteComServer> remoteComServers = new ArrayList<RemoteComServer>(comServers.size());
        for (ComServer comServer : comServers) {
            remoteComServers.add((RemoteComServer) comServer);
        }
        return remoteComServers;
    }

    /**
     * COMPORTS
     */

    @Override
    public ComPort findComPort(long id) {
        return getComPortDataMapper().getUnique("id", id).orNull();
    }

    @Override
    public List<ComPort> findComPortsByComServer(ComServer comServer) {
        return getComPortDataMapper().find("comServer", comServer);
    }

    @Override
    public ComPort findComPortByNameInComServer(String name, ComServer comServer) {
        return getComPortDataMapper().getUnique("comServer", comServer, "name", name).orNull();
    }

    @Override
    public List<OutboundComPort> findAllOutboundComPorts() {
        return convertComportListToOutBoundComPorts(getComPortDataMapper().find("class", ComPortImpl.OUTBOUND_DISCRIMINATOR));
    }

    @Override
    public List<InboundComPort> findAllInboundComPorts() {
        Condition condition = Where.where("class").isNotEqual(ComPortImpl.OUTBOUND_DISCRIMINATOR);
        return convertComportListToInBoundComPorts(getComPortDataMapper().select(condition));
    }

    private List<OutboundComPort> convertComportListToOutBoundComPorts(final List<ComPort> comPorts) {
        List<OutboundComPort> outboundComPorts = new ArrayList<>(comPorts.size());
        for (ComPort comPort : comPorts) {
            outboundComPorts.add((OutboundComPort) comPort);
        }
        return outboundComPorts;
    }

    private List<InboundComPort> convertComportListToInBoundComPorts(final List<ComPort> comPorts) {
        List<InboundComPort> inboundComPorts = new ArrayList<>(comPorts.size());
        for (ComPort comPort : comPorts) {
            inboundComPorts.add((InboundComPort) comPort);
        }
        return inboundComPorts;
    }


    @Override
    public OutboundComPort newOutbound(ComServer owner){
        OutboundComPort instance = dataModel.getInstance(OutboundComPort.class);
        ((ComPortImpl)instance).setComServer(owner);
        return instance;
    }

    @Override
    public ModemBasedInboundComPort newModemBasedInbound(ComServer owner){
        ModemBasedInboundComPort instance = dataModel.getInstance(ModemBasedInboundComPort.class);
        ((ComPortImpl)instance).setComServer(owner);
        return instance;
    }

    @Override
    public TCPBasedInboundComPort newTCPBasedInbound(ComServer owner){
        TCPBasedInboundComPort instance = dataModel.getInstance(TCPBasedInboundComPort.class);
        ((ComPortImpl)instance).setComServer(owner);
        return instance;
    }

    @Override
    public UDPBasedInboundComPort newUDPBasedInbound(ComServer owner){
        UDPBasedInboundComPort instance = dataModel.getInstance(UDPBasedInboundComPort.class);
        ((ComPortImpl)instance).setComServer(owner);
        return instance;
    }

    @Override
    public ServletBasedInboundComPort newServletBasedInbound(ComServer owner){
        ServletBasedInboundComPort instance = dataModel.getInstance(ServletBasedInboundComPort.class);
        ((ComPortImpl)instance).setComServer(owner);
        return instance;
    }

    @Override
    public ComPortPool findComPortPool(long id) {
        return getComPortPoolDataMapper().getUnique("id", id).orNull();
    }

    @Override
    public InboundComPortPool findInboundComPortPool(long id) {
        ComPortPool comPortPool = this.findComPortPool(id);
        if (comPortPool instanceof InboundComPortPool) {
            return (InboundComPortPool) comPortPool;
        }
        else {
            return null;
        }
    }

    @Override
    public OutboundComPortPool findOutboundComPortPool(int id) {
        ComPortPool comPortPool = this.findComPortPool(id);
        if (comPortPool instanceof OutboundComPortPool) {
            return (OutboundComPortPool) comPortPool;
        }
        else {
            return null;
        }
    }

    @Override
    public List<OutboundComPortPool> findOutboundComPortPoolByType(ComPortType comPortType) {
        return convertComportPoolListToOutBoundComPortPools(getComPortPoolDataMapper().find("comPortType", comPortType));
    }

    @Override
    public ComPortPool findComPortPool(String name) {
        return getComPortPoolDataMapper().getUnique("name", name).orNull();
    }

    @Override
    public List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass) {
        return convertComportPoolListToInBoundComPortPools(getComPortPoolDataMapper().find("discoveryProtocolPluggableClassId", pluggableClass.getId()));
    }

    private List<OutboundComPortPool> convertComportPoolListToOutBoundComPortPools(final List<ComPortPool> comPortPools) {
        List<OutboundComPortPool> outboundComPortPools = new ArrayList<>(comPortPools.size());
        for (ComPortPool comPortPool : comPortPools) {
            outboundComPortPools.add((OutboundComPortPool) comPortPool);
        }
        return outboundComPortPools;
    }

    private List<InboundComPortPool> convertComportPoolListToInBoundComPortPools(final List<ComPortPool> comPortPools) {
        List<InboundComPortPool> inboundComPortPools = new ArrayList<>(comPortPools.size());
        for (ComPortPool comPortPool : comPortPools) {
            inboundComPortPools.add((InboundComPortPool) comPortPool);
        }
        return inboundComPortPools;
    }

    @Override
    public InboundComPortPool newInboundComPortPool() {
        return dataModel.getInstance(InboundComPortPoolImpl.class);
    }

    @Override
    public OutboundComPortPool newOutboundComPortPool() {
        return dataModel.getInstance(OutboundComPortPoolImpl.class);
    }

    @Override
    public DataMapper<ComServer> getComServerDataMapper() {
        return dataModel.mapper(ComServer.class);
    }

    @Override
    public DataMapper<ComPort> getComPortDataMapper() {
        return dataModel.mapper(ComPort.class);
    }

    @Override
    public DataMapper<ComPortPool> getComPortPoolDataMapper() {
        return dataModel.mapper(ComPortPool.class);
    }

    @Override
    public DataMapper<ComPortPoolMember> getComPortPoolMemberDataMapper() {
        return dataModel.mapper(ComPortPoolMember.class);
    }

    @Override
    public void removeComPortFromPools(ComPort comPort) {
        for (ComPortPoolMember comPortPoolMember : getComPortPoolMemberDataMapper().find("comPort", comPort)) {
            comPortPoolMember.remove();
        }

    }

    @Override
    public List<ComPortPool> findContainingComPortPoolsForComPort(ComPort comPort) {
        List<ComPortPoolMember> comPortPoolMembers = getComPortPoolMemberDataMapper().find("comPort", comPort);
        List<ComPortPool> comPortPools = new ArrayList<>();
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            comPortPools.add(comPortPoolMember.getComPortPool());
        }
        return comPortPools;
    }

    @Override
    public List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer) {
        List<ComPortPool> comPortPools = new ArrayList<>();
        for (ComPort comPort : comServer.getComPorts()) {
            comPortPools.addAll(findContainingComPortPoolsForComPort(comPort));
        }

        return comPortPools;
    }

    @Override
    public List<ComPortPool> findAllComPortPools() {
        return getComPortPoolDataMapper().find();
    }

    @Override
    public List<InboundComPort> findInboundInPool(InboundComPortPool comPortPool) {
        return comPortPool.getComPorts();
    }

    @Override
    public List<OutboundComPort> findOutboundInPool(OutboundComPortPool comPortPool) {
        return comPortPool.getComPorts();
    }

    @Override
    public List<OutboundComPort> findOutboundComPortsWithComPortType(ComPortType comPortType) {
        return convertComportListToOutBoundComPorts(getComPortDataMapper().find("comPortType", comPortType, "class", OUTBOUND_DISCRIMINATOR));
    }

    @Override
    public List<InboundComPort> findInboundComPortsWithComPortType(ComPortType comPortType) {
        Condition condition = Where.where("class").isNotEqual(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(Where.where("comPortType").isEqualTo(comPortType));
        return convertComportListToInBoundComPorts(getComPortDataMapper().select(condition));
    }

    @Override
    public List<ComPort> findAllComPortsWithDeleted() {
        return getComPortDataMapper().find();
    }

    @Override
    public List<ComPort> findAllComPorts() {
        return getComPortDataMapper().find("obsoleteFlag", false);
    }
}
