/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_3SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_3SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_9SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_6_1SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_7_1SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComPortPoolMember;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.engine.config.ComServerAliveStatus;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.OFFLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.ONLINE_COMSERVER_DISCRIMINATOR;
import static com.energyict.mdc.engine.config.impl.ComServerImpl.REMOTE_COMSERVER_DISCRIMINATOR;

@Component(name = "com.energyict.mdc.engine.config", service = {EngineConfigurationService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = "name=" + EngineConfigurationService.COMPONENT_NAME)
public class EngineConfigurationServiceImpl implements EngineConfigurationService, MessageSeedProvider, TranslationKeyProvider, OrmClient {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile NlsService nlsService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;
    private BundleContext bundleContext = null;
    private Thesaurus thesaurus;

    public final static String COM_SERVER_STATUS_ALIVE_FREQ_PROP = "com.energyict.mdc.comserver.status.update.freq.minutes";

    public EngineConfigurationServiceImpl() {
        super();
    }

    @Inject
    public EngineConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, ProtocolPluggableService protocolPluggableService,
                                          UserService userService, UpgradeService upgradeService, Clock clock, BundleContext bundleContext) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setUserService(userService);
        this.setUpgradeService(upgradeService);
        setClock(clock);
        activate(bundleContext);
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
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
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
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        dataModel.register(getModule());
        upgradeService.register(identifier("MultiSense", EngineConfigurationService.COMPONENT_NAME), dataModel, Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 2), UpgraderV10_2.class)
                        .put(version(10, 3), V10_3SimpleUpgrader.class)
                        .put(version(10, 4, 3), V10_4_3SimpleUpgrader.class)
                        .put(version(10, 4, 9), V10_4_9SimpleUpgrader.class)
                        .put(version(10, 6, 1), V10_6_1SimpleUpgrader.class)
                        .put(version(10, 7, 1), V10_7_1SimpleUpgrader.class).build());

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
    public Optional<ComServer> findAndLockComServerByIdAndVersion(long id, long version) {
        return getComServerDataMapper().lockObjectIfVersion(version, id);
    }

    @Override
    public Finder<ComServer> findAllComServers() {
        return DefaultFinder.of(ComServer.class, where("obsoleteDate").isNull(), dataModel);
    }

    @Override
    public Finder<ComServer> filterComServers(Condition condition) {
        return DefaultFinder.of(ComServer.class, condition.and(where("obsoleteDate").isNull()), dataModel);
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
    public List<OfflineComServer> findOfflineComServersForOnlineComServer(OnlineComServer onlineComServer) {
        Condition condition =
                where("class").isEqualTo(OFFLINE_COMSERVER_DISCRIMINATOR)
                        .and(where("onlineComServer").isEqualTo(onlineComServer))
                        .and(where("obsoleteDate").isNull());
        return convertComServerListToOfflineComServers(getComServerDataMapper().select(condition));
    }

    @Override
    public List<OfflineComServer> findAllOfflineComServers() {
        Condition condition = where("class").isEqualTo(OFFLINE_COMSERVER_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComServerListToOfflineComServers(getComServerDataMapper().select(condition));
    }

    @Override
    public Optional<ComServer> findComServerByServerNameAndEventRegistrationPort(String serverName, int eventRegistrationPort) {
        Condition condition = where("serverName").isEqualToIgnoreCase(serverName)
                .and(where("eventRegistrationPort").isEqualTo(eventRegistrationPort))
                .and(where("obsoleteDate").isNull());
        return unique(dataModel.mapper(ComServer.class).select(condition));
    }

    @Override
    public Optional<ComServer> findComServerByServerNameAndStatusPort(String serverName, int statusPort) {
        Condition condition = where("serverName").isEqualToIgnoreCase(serverName)
                .and(where("statusPort").isEqualTo(statusPort))
                .and(where("obsoleteDate").isNull());
        return unique(dataModel.mapper(ComServer.class).select(condition));
    }

    @Override
    public OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> newOnlineComServerBuilder() {
        return dataModel.getInstance(OnlineComServerImpl.OnlineComServerBuilderImpl.class);
    }

    @Override
    public OfflineComServer.OfflineComServerBuilder<? extends OfflineComServer> newOfflineComServerBuilder() {
        return dataModel.getInstance(OfflineComServerImpl.OfflineComServerBuilderImpl.class);
    }

    @Override
    public RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> newRemoteComServerBuilder() {
        return dataModel.getInstance(RemoteComServerImpl.RemoteComServerBuilderImpl.class);
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
    public Optional<? extends ComPort> findAndLockComPortByIdAndVersion(long id, long version) {
        return getComPortDataMapper().lockObjectIfVersion(version, id);
    }

    @Override
    public List<ComPort> findComPortsByComServer(ComServer comServer) {
        return getComPortDataMapper().find("comServer", comServer);
    }

    @Override
    public List<OutboundComPort> findAllOutboundComPorts() {
        Condition condition = where("class").isEqualTo(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(where("obsoleteDate").isNull());
        return convertComportListToOutBoundComPorts(getComPortDataMapper().select(condition, Order.ascending("id")));
    }

    public List<OutboundComPort> findAllOutboundComPortsByComServer(ComServer comServer) {
//        Condition condition = where("class").isEqualTo(ComPortImpl.OUTBOUND_DISCRIMINATOR).and(where("obsoleteDate").isNull())
//                .and(where("comServer").isEqualTo(comServer.getId()));
        return convertComportListToOutBoundComPorts(findAllOutboundComPorts()
                .stream()
                .filter(cp -> cp.getComServer().equals(comServer)).collect(Collectors.toList()));
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
    public Optional<? extends ComPortPool> findAndLockComPortPoolByIdAndVersion(long id, long version) {
        return getComPortPoolDataMapper().lockObjectIfVersion(version, id);
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

    private <T extends ComPort> Optional<T> findComPortByName(String name, DataMapper<T> mapper) {
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
    public InboundComPortPool newInboundComPortPool(String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol, Map<String, Object> properties) {
        final InboundComPortPoolImpl inboundComPortPool = this.dataModel.getInstance(InboundComPortPoolImpl.class).initialize(name, comPortType, discoveryProtocol);
        properties.entrySet()
                .forEach(entry -> inboundComPortPool.setProperty(entry.getKey(), entry.getValue()));
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    @Override
    public OutboundComPortPool newOutboundComPortPool(String name, ComPortType comPortType, TimeDuration taskExecutionTimeout, long pctHighPrioTasks) {
        final OutboundComPortPoolImpl outboundComPortPool = dataModel.getInstance(OutboundComPortPoolImpl.class).initialize(name, comPortType, taskExecutionTimeout, pctHighPrioTasks);
        outboundComPortPool.save();
        return outboundComPortPool;
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
    public DataMapper<ComServerAliveStatus> getComServerAliveDataMapper() {
        return dataModel.mapper(ComServerAliveStatus.class);
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
    public List<Long> findContainingActiveComPortPoolsForComPort(ComPort comPort) {
        List<ComPortPoolMember> comPortPoolMembers = getComPortPoolMemberDataMapper().find("comPort", comPort);
        return comPortPoolMembers
                .stream()
                .map(ComPortPoolMember::getComPortPool)
                .filter(ComPortPool::isActive)
                .map(ComPortPool::getId)
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
    public long calculateMaxPriorityConnections(ComPortPool comPortPool, long pctHighPrioTasks) {
        float tempMaxPriorityConnections = 0;

        for (ComPort comPort : comPortPool.getComPorts()) {
            long numberOfPortPoolsUsedByThePort = getNumberOfPortPoolsUsedByThePort(comPort);
            if (numberOfPortPoolsUsedByThePort != 0) {
                tempMaxPriorityConnections += (float) comPort.getNumberOfSimultaneousConnections() / numberOfPortPoolsUsedByThePort;
            }
        }
        return (long) Math.ceil(tempMaxPriorityConnections * ((float) pctHighPrioTasks / 100));
    }

    private long getNumberOfPortPoolsUsedByThePort(ComPort comPort) {
        long comPortInUseByPortPools = 0;
        for (ComPortPool comPortPool : findAllComPortPools()) {
            comPortInUseByPortPools += comPortPool.getComPorts().stream().filter(temp -> temp.getId() == comPort.getId()).count();
        }

        return comPortInUseByPortPools;
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
    public List<ComPort> findAllComPortsIncludingObsolete() {
        return getComPortDataMapper().find();
    }

    private <T> Optional<T> unique(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Optional.empty();
        } else if (collection.size() != 1) {
            throw notUniqueException();
        } else {
            return Optional.of(collection.iterator().next());
        }
    }

    private TranslatableApplicationException notUniqueException() {
        return new TranslatableApplicationException(thesaurus, MessageSeeds.NOT_UNIQUE);
    }

    @Override
    public ComServer lockComServer(ComServer comServer) {
        return getComServerDataMapper().lock(comServer.getId());
    }

    @Override
    public ComPort lockComPort(ComPort comPort) {
        return getComPortDataMapper().lock(comPort.getId());
    }

    @Override
    public ComServerAliveStatus findOrCreateAliveStatus(ComServer comServer) {
        Optional<ComServerAliveStatus> comServerAliveOptional = getComServerAliveDataMapper().getUnique("comServer", comServer);
        if (comServerAliveOptional.isPresent()) {
            return comServerAliveOptional.get();
        } else {
            ComServerAliveStatusImpl comServerAlive = this.dataModel.getInstance(ComServerAliveStatusImpl.class).initialize(comServer, clock.instant(), getComServerStatusAliveFrequency());
            comServerAlive.save();
            return comServerAlive;
        }
    }

    @Override
    public Optional<ComServerAliveStatus> getAliveStatus(ComServer comServer) {
        Optional<ComServerAliveStatus> comServerAliveStatus = getComServerAliveDataMapper().getUnique("comServer", comServer);
        comServerAliveStatus.ifPresent(
                status ->
                        status.setRunning(clock.instant().getEpochSecond() - status.getLastActiveTime().getEpochSecond() <= status.getUpdateFrequencyMinutes() * 60)
        );
        return comServerAliveStatus;
    }

    @Override
    public Integer getComServerStatusAliveFrequency() {
        try {
            String val = bundleContext.getProperty(COM_SERVER_STATUS_ALIVE_FREQ_PROP);
            return val == null ? ComServerAliveStatusImpl.DEFAULT_FREQUENCY_MINUTES : Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return ComServerAliveStatusImpl.DEFAULT_FREQUENCY_MINUTES;
        }
    }
}