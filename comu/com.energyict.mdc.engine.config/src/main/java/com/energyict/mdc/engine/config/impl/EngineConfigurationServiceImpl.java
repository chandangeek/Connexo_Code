package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.common.TranslatableApplicationException;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.proxy.LazyLoader;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.streams.Predicates;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.OFFLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.ONLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.REMOTE_COMSERVER_DISCRIMINATOR;

@Component(name = "com.energyict.mdc.engine.config", service = {EngineConfigurationService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + EngineConfigurationService.COMPONENT_NAME)
public class EngineConfigurationServiceImpl implements EngineConfigurationService, InstallService, TranslationKeyProvider, OrmClient {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile NlsService nlsService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile UserService userService;
    private Thesaurus thesaurus;

    public EngineConfigurationServiceImpl() {
        super();
    }

    @Inject
    public EngineConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, ProtocolPluggableService protocolPluggableService, UserService userService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setUserService(userService);
        this.activate();
        this.install();
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.userService).install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "USR");
    }

    @Override
    public String getComponentName() {
        return EngineConfigurationService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(EngineConfigurationService.COMPONENT_NAME, "ComServer Engine Model");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(EngineConfigurationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService pluggableService) {
        this.protocolPluggableService = pluggableService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(NlsService.class).toInstance(nlsService);
                bind(EngineConfigurationService.class).toInstance(EngineConfigurationServiceImpl.this);
                bind(ServletBasedInboundComPort.class).to(ServletBasedInboundComPortImpl.class);
                bind(ModemBasedInboundComPort.class).to(ModemBasedInboundComPortImpl.class);
                bind(TCPBasedInboundComPort.class).to(TCPBasedInboundComPortImpl.class);
                bind(UDPBasedInboundComPort.class).to(UDPBasedInboundComPortImpl.class);
                bind(OutboundComPort.class).to(OutboundComPortImpl.class);
                bind(ComPortPoolMember.class).to(ComPortPoolMemberImpl.class);
                bind(UserService.class).toInstance(userService);
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
    public Optional<ComServer> findComServer(String name) {
        Condition condition = where("name").isEqualToIgnoreCase(name).and(where("obsoleteDate").isNull());
        return unique(getComServerDataMapper().select(condition));
    }

    @Override
    public Optional<ComServer> findComServer(long id) {
        return getComServerDataMapper().getUnique("id", id);
    }

    @Override
    public Finder<ComServer> findAllComServers() {
        return DefaultFinder.of(ComServer.class, where("obsoleteDate").isNull(), dataModel);
    }

    @Override
    public Optional<ComServer> findComServerBySystemName() {
        return this.findComServer(HostName.getCurrent());
    }

    @Override
    public List<OnlineComServer> findAllOnlineComServers() {
        Condition condition = where("class").isEqualTo(ONLINE_COMSERVER_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComServerListToOnlineComServers(getComServerDataMapper().select(condition));
    }

    @Override
    public List<RemoteComServer> findAllRemoteComServers() {
        Condition condition = where("class").isEqualTo(REMOTE_COMSERVER_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComServerListToRemoteComServers(getComServerDataMapper().select(condition));
    }

    @Override
    public List<RemoteComServer> findRemoteComServersForOnlineComServer(OnlineComServer onlineComServer) {
        Condition condition =
                where("class").isEqualTo(REMOTE_COMSERVER_DISCRIMINATOR)
                        .and(where("onlineComServer").isEqualTo(onlineComServer))
                        .and(where("obsoleteDate").isNull());
        return convertComServerListToRemoteComServers(getComServerDataMapper().select(condition));
    }

    @Override
    public List<OfflineComServer> findAllOfflineComServers() {
        Condition condition = where("class").isEqualTo(OFFLINE_COMSERVER_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComServerListToOfflineComServers(getComServerDataMapper().select(condition));
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
     * Converts the given List of {@link ComServer comServers} to a proper list of {@link OnlineComServer}.
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link OnlineComServer}.
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link OnlineComServer}
     */
    private List<OnlineComServer> convertComServerListToOnlineComServers(List<ComServer> comServers) {
        return comServers.stream().map(OnlineComServer.class::cast).collect(Collectors.toList());
    }

    /**
     * Converts the given List of {@link ComServer comServers} to a proper list of {@link OfflineComServer}.
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link OfflineComServer}.
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link OfflineComServer}
     */
    private List<OfflineComServer> convertComServerListToOfflineComServers(List<ComServer> comServers) {
        return comServers.stream().map(OfflineComServer.class::cast).collect(Collectors.toList());
    }

    /**
     * Converts the given List of {@link ComServer comServers} to a proper list of {@link RemoteComServer}.
     * A {@link ClassCastException} will be thrown if the given {@link ComServer comServer} could not be casted to {@link RemoteComServer}.
     *
     * @param comServers the given list of ComServers
     * @return a list of {@link RemoteComServer}
     */
    private List<RemoteComServer> convertComServerListToRemoteComServers(List<ComServer> comServers) {
        return comServers.stream().map(RemoteComServer.class::cast).collect(Collectors.toList());
    }

    @Override
    public Optional<? extends ComPort> findComPort(long id) {
        return getComPortDataMapper().getUnique("id", id);
    }

    @Override
    public List<ComPort> findComPortsByComServer(ComServer comServer) {
        return getComPortDataMapper().find("comServer", comServer);
    }

    @Override
    public List<OutboundComPort> findAllOutboundComPorts() {
        Condition condition = where("class").isEqualTo(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComportListToOutBoundComPorts(getComPortDataMapper().select(condition));
    }

    @Override
    public List<InboundComPort> findAllInboundComPorts() {
        Condition condition = where("class").isNotEqual(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComportListToInBoundComPorts(getComPortDataMapper().select(condition));
    }

    private List<OutboundComPort> convertComportListToOutBoundComPorts(final List<ComPort> comPorts) {
        return comPorts
                .stream()
                .map(OutboundComPort.class::cast)
                .collect(Collectors.toList());
    }

    private List<InboundComPort> convertComportListToInBoundComPorts(final List<ComPort> comPorts) {
        return comPorts
                .stream()
                .map(InboundComPort.class::cast)
                .collect(Collectors.toList());
    }


    @Override
    public Optional<? extends ComPortPool> findComPortPool(long id) {
        return getComPortPoolDataMapper().getOptional(id);
    }

    @Override
    public Optional<InboundComPortPool> findInboundComPortPool(long id) {
        return this.dataModel.mapper(InboundComPortPool.class).getOptional(id);
    }

    @Override
    public Optional<OutboundComPortPool> findOutboundComPortPool(long id) {
        return this.dataModel.mapper(OutboundComPortPool.class).getOptional(id);
    }

    @Override
    public List<OutboundComPortPool> findOutboundComPortPoolsByType(ComPortType comPortType) {
        return convertComportPoolListToOutBoundComPortPools(getComPortPoolDataMapper().
                select(where("comPortType").isEqualTo(comPortType).and(where(ComPortPoolImpl.Fields.OBSOLETEDATE.fieldName()).isNull())));
    }

    @Override
    public List<InboundComPortPool> findInboundComPortPoolsByType(ComPortType comPortType) {
        return convertComportPoolListToInBoundComPortPools(getComPortPoolDataMapper().
                select(where("comPortType").isEqualTo(comPortType).and(where(ComPortPoolImpl.Fields.OBSOLETEDATE.fieldName()).isNull())));
    }

    @Override
    public Optional<? extends ComPortPool> findComPortPoolByName(String name) {
        return this.findComPortPoolByName(name, this.getComPortPoolDataMapper());
    }

    @Override
    public Optional<OutboundComPortPool> findOutboundComPortPoolByName(String name) {
        return this.findComPortPoolByName(name, dataModel.mapper(OutboundComPortPool.class));
    }

    @Override
    public Optional<InboundComPortPool> findInboundComPortPoolByName(String name) {
        return this.findComPortPoolByName(name, dataModel.mapper(InboundComPortPool.class));
    }

    private <T extends ComPortPool> Optional<T> findComPortPoolByName(String name, DataMapper<T> mapper) {
        Condition condition = this.findComPortPoolByNameCondition(name);
        return unique(mapper.select(condition));
    }

    private Condition findComPortPoolByNameCondition(String name) {
        return where("obsoleteDate").isNull().and(where("name").isEqualTo(name));
    }

    @Override
    public List<InboundComPortPool> findComPortPoolByDiscoveryProtocol(PluggableClass pluggableClass) {
        return convertComportPoolListToInBoundComPortPools(getComPortPoolDataMapper().find("discoveryProtocolPluggableClassId", pluggableClass.getId()));
    }

    private List<OutboundComPortPool> convertComportPoolListToOutBoundComPortPools(final List<ComPortPool> comPortPools) {
        return comPortPools
                .stream()
                .filter(Predicates.not(ComPortPool::isInbound))
                .map(OutboundComPortPool.class::cast)
                .collect(Collectors.toList());
    }

    private List<InboundComPortPool> convertComportPoolListToInBoundComPortPools(final List<ComPortPool> comPortPools) {
        return comPortPools
                .stream()
                .filter(ComPortPool::isInbound)
                .map(InboundComPortPool.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public InboundComPortPool newInboundComPortPool(String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol) {
        return this.dataModel.getInstance(InboundComPortPoolImpl.class).initialize(name, comPortType, discoveryProtocol);
    }

    @Override
    public OutboundComPortPool newOutboundComPortPool(String name, ComPortType comPortType, TimeDuration taskExecutionTimeout) {
        return dataModel.getInstance(OutboundComPortPoolImpl.class).initialize(name, comPortType, taskExecutionTimeout);
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
        return comPortPoolMembers
                .stream()
                .map(ComPortPoolMember::getComPortPool)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComPortPool> findContainingComPortPoolsForComServer(ComServer comServer) {
        return DecoratedStream.decorate(comServer.getComPorts().stream())
                .filter(each -> each instanceof OutboundComPort)
                .map(OutboundComPort.class::cast)
                .flatMap(each -> this.findContainingComPortPoolsForComPort(each).stream())
                .distinct(OutboundComPortPool::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComPortPool> findAllComPortPools() {
        Condition condition = where("obsoleteDate").isNull();
        return getComPortPoolDataMapper().select(condition);
    }

    @Override
    public List<InboundComPort> findInboundInPool(InboundComPortPool comPortPool) {
        return this.dataModel
                .mapper(ComPort.class)
                .select(where("comPortPool").isEqualTo(comPortPool)
                        .and(where("obsoleteDate").isNull()))
                .stream()
                .map(InboundComPort.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComPort> findAllComPortsWithDeleted() {
        return getComPortDataMapper().find();
    }

    @Override
    public ComServer parseComServerQueryResult(JSONObject comServerJSon) {
        try {
            String comServerName = (String) comServerJSon.get("name");
            return new ComServerLazyLoader(comServerName).load();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ComPort parseComPortQueryResult(JSONObject comPortJSon) {
        try {
            Long id = Long.parseLong((String) comPortJSon.get("id"));
            return new ComPortLazyLoader(id).load();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<T> unique(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Optional.empty();
        }
        else if (collection.size() != 1) {
            throw notUniqueException();
        }
        else {
            return Optional.of(collection.iterator().next());
        }
    }

    private TranslatableApplicationException notUniqueException() {
        return new TranslatableApplicationException(thesaurus, MessageSeeds.NOT_UNIQUE);
    }

    private class ComServerLazyLoader implements LazyLoader<ComServer> {

        private final String comServerName;

        private ComServerLazyLoader(String comServerName) {
            super();
            this.comServerName = comServerName;
        }

        @Override
        public ComServer load() {
            return findComServer(this.comServerName).orElse(null);
        }

        @Override
        public ClassLoader getClassLoader() {
            return EngineConfigurationServiceImpl.class.getClassLoader();
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
                return findComPort(comPortId).orElse(null);
            } else {
                return null;
            }
        }

        @Override
        public Class<ComPort> getImplementedInterface() {
            return ComPort.class;
        }

        @Override
        public ClassLoader getClassLoader() {
            return EngineConfigurationServiceImpl.class.getClassLoader();
        }

    }

}