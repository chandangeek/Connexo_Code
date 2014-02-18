package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.HostName;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.energyict.mdc.engine.model.impl.ComPortImpl.OUTBOUND_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.OFFLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.ONLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.REMOTE_COMSERVER_DISCRIMINATOR;

@Component(name = "com.energyict.mdc.engine.model", service = {EngineModelService.class, InstallService.class} )
public class EngineModelServiceImpl implements EngineModelService, InstallService, OrmClient {

    private volatile DataModel dataModel;

    public EngineModelServiceImpl() {
        super();
    }

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
        Condition condition = Where.where("name").isEqualTo(name).and(Where.where("obsoleteDate").isNull());
        return unique(getComServerDataMapper().select(condition));
   }

    @Override
    public ComServer findComServer(long id) {
        return getComServerDataMapper().getUnique("id", id).orNull();
    }

    @Override
    public Finder<ComServer> findAllComServers() {
        return DefaultFinder.of(ComServer.class, Where.where("obsoleteDate").isNull(), dataModel);
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
    public OutboundComPortPool findOutboundComPortPool(long id) {
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
    public List<OutboundComPortPool> findContainingComPortPoolsForComPort(OutboundComPort comPort) {
        List<ComPortPoolMember> comPortPoolMembers = getComPortPoolMemberDataMapper().find("comPort", comPort);
        List<OutboundComPortPool> comPortPools = new ArrayList<>();
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            comPortPools.add(comPortPoolMember.getComPortPool());
        }
        return comPortPools;
    }

    @Override
    public List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer) {
        List<ComPortPool> comPortPools = new ArrayList<>();
        for (ComPort comPort : comServer.getComPorts()) {
            if (OutboundComPort.class.isAssignableFrom(comPort.getClass())) {
                comPortPools.addAll(findContainingComPortPoolsForComPort((OutboundComPort) comPort));
            }
        }

        return comPortPools;
    }

    @Override
    public List<ComPortPool> findAllComPortPools() {
        Condition condition = Where.where("obsoleteDate").isNull();
        return getComPortPoolDataMapper().select(condition);
    }

    @Override
    public List<ComPortPool> findAllComPortPools(int from, int pageSize, String[] orderBy) {
        Finder<ComPortPool> comPortPoolFinder = DefaultFinder.of(ComPortPool.class, Where.where("obsoleteDate").isNull(), dataModel).paged(from, pageSize);
        for (String order : orderBy) {
            comPortPoolFinder.sorted(order, true);
        }

        return comPortPoolFinder.find();
    }

    @Override
    public List<InboundComPort> findInboundInPool(InboundComPortPool comPortPool) {
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (ComPort portPool : getComPortDataMapper().find("comPortPool", comPortPool)) {
            inboundComPorts.add((InboundComPort) portPool);
        }
        return inboundComPorts;
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
        Condition condition = Where.where("obsoleteDate").isNull();
        return getComPortDataMapper().select(condition);
    }

    @Override
    public ComServer parseComServerQueryResult(JSONObject comServerJSon) {
        try {
            Class<? extends ComServerImpl> comServerClass = this.getComServerClassFor(comServerJSon);
            ObjectMapper mapper = ObjectMapperFactory.newMapper();
            return mapper.readValue(new StringReader(comServerJSon.toString()), comServerClass);
        }
        catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends ComServerImpl> getComServerClassFor (JSONObject comServerJSon) throws JSONException {
        String xmlType = comServerJSon.getString("type");
        for (Class<? extends ComServerImpl> knownImplementationClass : this.knownComServerImplementationClasses()) {
            if (knownImplementationClass.getSimpleName() == xmlType || xmlType != null && knownImplementationClass.getSimpleName().equals(xmlType)) {
                return knownImplementationClass;
            }
        }
        throw new RuntimeException("The ComServer returned by the remote query API is neither online, remote nor offline but was " + xmlType);
    }

    private Set<Class<? extends ComServerImpl>> knownComServerImplementationClasses () {
        Set<Class<? extends ComServerImpl>> knownImplementationClasses = new HashSet<>();
        knownImplementationClasses.add(OnlineComServerImpl.class);
        knownImplementationClasses.add(RemoteComServerImpl.class);
        knownImplementationClasses.add(OfflineComServerImpl.class);
        return knownImplementationClasses;
    }

    @Override
    public ComPort parseComPortQueryResult(JSONObject comPortJSon) {
        try {
            Class<? extends ComPortImpl> comPortClass = this.getComPortClassFor(comPortJSon);
            ObjectMapper mapper = ObjectMapperFactory.newMapper();
            return mapper.readValue(new StringReader(comPortJSon.toString()), comPortClass);
        }
        catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends ComPortImpl> getComPortClassFor (JSONObject comPortJSon) throws JSONException {
        String xmlType = comPortJSon.getString("type");
        for (Class<? extends ComPortImpl> knownComPortImplementationClass : this.knownComPortImplementationClasses()) {
            if (knownComPortImplementationClass.getSimpleName().equals(xmlType)
                    || xmlType != null
                    && knownComPortImplementationClass.getSimpleName().equals(xmlType)) {
                return knownComPortImplementationClass;
            }
        }
        throw new RuntimeException("The ComPort returned by the remote query API is neither outbound, servlet based, TCP based, UDP based or modem based but was " + xmlType);
    }

    private Set<Class<? extends ComPortImpl>> knownComPortImplementationClasses () {
        Set<Class<? extends ComPortImpl>> knownClasses = new HashSet<>();
        knownClasses.add(OutboundComPortImpl.class);
        knownClasses.add(ServletBasedInboundComPortImpl.class);
        knownClasses.add(TCPBasedInboundComPortImpl.class);
        knownClasses.add(UDPBasedInboundComPortImpl.class);
        knownClasses.add(ModemBasedInboundComPortImpl.class);
        return knownClasses;
    }

    private <T> T unique(Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        } else if (collection.size()!=1) {
            throw new TranslatableApplicationException("XnotUnique", "The elements queried was supposed to be unique");
        }
        return collection.iterator().next();
    }
}
