package com.energyict.mdc.engine.model.impl;

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
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.proxy.LazyLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.energyict.mdc.engine.model.impl.ComPortImpl.OUTBOUND_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.OFFLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.ONLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.model.impl.ComServerImpl.REMOTE_COMSERVER_DISCRIMINATOR;

@Component(name = "com.energyict.mdc.engine.model", service = {EngineModelService.class, InstallService.class}, property = "name=" + EngineModelService.COMPONENT_NAME)
public class EngineModelServiceImpl implements EngineModelService, InstallService, OrmClient {

    private volatile DataModel dataModel;
    private Thesaurus thesaurus;
    private NlsService nlsService;
    private ProtocolPluggableService protocolPluggableService;

    public EngineModelServiceImpl() {
        super();
    }

    @Inject
    public EngineModelServiceImpl(OrmService ormService, NlsService nlsService, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.activate();
        this.install();
    }

    @Override
    public void install() {
        this.createTranslations();
        dataModel.install(true, true);
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(EngineModelService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }


    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(EngineModelService.COMPONENT_NAME, "ComServer Engine Model");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(EngineModelService.COMPONENT_NAME,Layer.DOMAIN);
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService pluggableService) {
        this.protocolPluggableService = pluggableService;
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(EngineModelService.class).toInstance(EngineModelServiceImpl.this);
                bind(ServletBasedInboundComPort.class).to(ServletBasedInboundComPortImpl.class);
                bind(ModemBasedInboundComPort.class).to(ModemBasedInboundComPortImpl.class);
                bind(TCPBasedInboundComPort.class).to(TCPBasedInboundComPortImpl.class);
                bind(UDPBasedInboundComPort.class).to(UDPBasedInboundComPortImpl.class);
                bind(OutboundComPort.class).to(OutboundComPortImpl.class);
                bind(ComPortPoolMember.class).to(ComPortPoolMemberImpl.class);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
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
        List<OnlineComServer> onlineComServers = new ArrayList<>(comServers.size());
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
        List<OfflineComServer> offlineComServers = new ArrayList<>(comServers.size());
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
        List<RemoteComServer> remoteComServers = new ArrayList<>(comServers.size());
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
    public List<InboundComPortPool> findInboundComPortPoolByType(ComPortType comPortType) {
        return convertComportPoolListToInBoundComPortPools(getComPortPoolDataMapper().find("comPortType", comPortType));
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
            if (!comPortPool.isInbound()){
                outboundComPortPools.add((OutboundComPortPool) comPortPool);
            }
        }
        return outboundComPortPools;
    }

    private List<InboundComPortPool> convertComportPoolListToInBoundComPortPools(final List<ComPortPool> comPortPools) {
        List<InboundComPortPool> inboundComPortPools = new ArrayList<>(comPortPools.size());
        for (ComPortPool comPortPool : comPortPools) {
            if (comPortPool.isInbound()) {
                inboundComPortPools.add((InboundComPortPool) comPortPool);
            }
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
            String comServerName = (String) comServerJSon.get("name");
            return new ComServerLazyLoader(comServerName).load();
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ComPort parseComPortQueryResult(JSONObject comPortJSon) {
        try {
            Long id = Long.parseLong((String) comPortJSon.get("id"));
            return new ComPortLazyLoader(id).load();
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T unique(Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        } else if (collection.size()!=1) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NOT_UNIQUE);
        }
        return collection.iterator().next();
    }

    private class ComServerLazyLoader implements LazyLoader<ComServer> {

        private final String comServerName;

        private ComServerLazyLoader(String comServerName) {
            super();
            this.comServerName = comServerName;
        }

        @Override
        public ComServer load() {
            return findComServer(this.comServerName);
        }

        @Override
        public ClassLoader getClassLoader() {
            return EngineModelServiceImpl.class.getClassLoader();
        }

        @Override
        public Class<ComServer> getImplementedInterface() {
            return ComServer.class;
        }
    }

    private class ComPortLazyLoader implements LazyLoader<ComPort> {
        private final Long comPortId;

        private ComPortLazyLoader(Long id) {
            super();
            this.comPortId = id;
        }

        @Override
        public ComPort load() {
            if (this.comPortId != null) {
                return findComPort(comPortId);
            }
            else {
                return null;
            }
        }

        @Override
        public Class<ComPort> getImplementedInterface() {
            return ComPort.class;
        }

        @Override
        public ClassLoader getClassLoader() {
            return EngineModelServiceImpl.class.getClassLoader();
        }

    }
}
