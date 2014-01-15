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
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.PluggableClass;
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
                bind(ServerServletBasedInboundComPort.class).to(ServletBasedInboundComPortImpl.class);
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
        return getComServerFactory().getUnique("name",name).orNull();
    }

    @Override
    public ComServer findComServer(long id) {
        return getComServerFactory().getUnique("id",id).orNull();
    }

    @Override
    public List<ComServer> findAllComServers() {
        return getComServerFactory().find();
    }

    @Override
    public ComServer findComServerBySystemName() {
        return this.findComServer(HostName.getCurrent());
    }

    @Override
    public List<OnlineComServer> findAllOnlineComServers() {
        return convertComServerListToOnlineComServers(getComServerFactory().find("class", ONLINE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public List<RemoteComServer> findAllRemoteComServers() {
        return convertComServerListToRemoteComServers(getComServerFactory().find("class", REMOTE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public List<RemoteComServer> findRemoteComServersForOnlineComServer(OnlineComServer onlineComServer) {
        return convertComServerListToRemoteComServers(getComServerFactory().find("class", REMOTE_COMSERVER_DISCRIMINATOR, "onlineComServer", onlineComServer));    }

    @Override
    public List<OfflineComServer> findAllOfflineComServers() {
        return convertComServerListToOfflineComServers(getComServerFactory().find("class", OFFLINE_COMSERVER_DISCRIMINATOR));
    }

    @Override
    public int getOfflineServerCount() {
        return getComServerFactory().find("class",OFFLINE_COMSERVER_DISCRIMINATOR).size();
    }

    @Override
    public List<RemoteComServer> findRemoteComServersWithOnlineComServer(OnlineComServer onlineComServer) {
        return convertComServerListToRemoteComServers(getComServerFactory().find("onlineServer", onlineComServer));
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
        return getComPortFactory().getUnique("id",id).orNull();
    }

    @Override
    public List<ComPort> findComPortsByComServer(ComServer comServer) {
        return getComPortFactory().find("comServer",comServer);
    }

    @Override
    public ComPort findComPortByNameInComServer(String name, ComServer comServer) {
        return getComPortFactory().getUnique("comServer",comServer,"name",name).orNull();
    }

    @Override
    public List<OutboundComPort> findAllOutboundComPorts() {
        return convertComportListToOutBoundComPorts(getComPortFactory().find("class",ComPortImpl.OUTBOUND_DISCRIMINATOR));
    }

    @Override
    public List<InboundComPort> findAllInboundComPorts() {
        Condition condition = Where.where("class").isNotEqual(ComPortImpl.OUTBOUND_DISCRIMINATOR);
        return convertComportListToInBoundComPorts(getComPortFactory().select(condition));
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
    public ServerOutboundComPort newOutbound(ComServer owner){
        ServerOutboundComPort instance = dataModel.getInstance(ServerOutboundComPort.class);
        instance.init(owner);
        return instance;
    }

    @Override
    public ServerModemBasedInboundComPort newModemBasedInbound(ComServer owner){
        ServerModemBasedInboundComPort instance = dataModel.getInstance(ServerModemBasedInboundComPort.class);
        instance.init(owner);
        return instance;
    }

    @Override
    public ServerTCPBasedInboundComPort newTCPBasedInbound(ComServer owner){
        ServerTCPBasedInboundComPort instance = dataModel.getInstance(ServerTCPBasedInboundComPort.class);
        instance.init(owner);
        return instance;
    }

    @Override
    public ServerUDPBasedInboundComPort newUDPBasedInbound(ComServer owner){
        ServerUDPBasedInboundComPort instance = dataModel.getInstance(ServerUDPBasedInboundComPort.class);
        instance.init(owner);
        return instance;
    }

    @Override
    public ServerServletBasedInboundComPort newServletBasedInbound(ComServer owner){
        ServerServletBasedInboundComPort instance = dataModel.getInstance(ServerServletBasedInboundComPort.class);
        instance.init(owner);
        return instance;
    }

    @Override
    public ComPortPool findComPortPool(long id) {
        return getComPortPoolFactory().getUnique("id",id).orNull();
    }

    @Override
    public InboundComPortPool findInboundComPortPool(int id) {
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
        return convertComportPoolListToOutBoundComPortPools(getComPortPoolFactory().find("comPortType", comPortType));
    }

    @Override
    public ComPortPool findComPortPool(String name) {
        return getComPortPoolFactory().getUnique("name",name).orNull();
    }

    @Override
    public List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass) {
        return convertComportPoolListToInBoundComPortPools(getComPortPoolFactory().find("discoveryProtocolPluggableClassId", pluggableClass.getId()));
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
    public DataMapper<ComServer> getComServerFactory() {
        return dataModel.mapper(ComServer.class);
    }

    @Override
    public DataMapper<ComPort> getComPortFactory() {
        return dataModel.mapper(ComPort.class);
    }

    @Override
    public DataMapper<ComPortPool> getComPortPoolFactory() {
        return dataModel.mapper(ComPortPool.class);
    }

    @Override
    public DataMapper<ComPortPoolMember> getComPortPoolMemberFactory() {
        return dataModel.mapper(ComPortPoolMember.class);
    }

    @Override
    public void removeComPortFromPools(ComPort comPort) {
        for (ComPortPoolMember comPortPoolMember : getComPortPoolMemberFactory().find("comPort", comPort)) {
            comPortPoolMember.remove();
        }

    }

    @Override
    public List<ComPortPool> findContainingComPortPoolsForComPort(ComPort comPort) {
        List<ComPortPoolMember> comPortPoolMembers = getComPortPoolMemberFactory().find("comPort", comPort);
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
        return getComPortPoolFactory().find();
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
        return convertComportListToOutBoundComPorts(getComPortFactory().find("comPortType", comPortType,"class",OUTBOUND_DISCRIMINATOR));
    }

    @Override
    public List<InboundComPort> findInboundComPortsWithComPortType(ComPortType comPortType) {
        Condition condition = Where.where("class").isNotEqual(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(Where.where("comPortType").isEqualTo(comPortType));
        return convertComportListToInBoundComPorts(getComPortFactory().select(condition));
    }

    @Override
    public List<ComPort> findAllWithDeleted() {
        //todo
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
