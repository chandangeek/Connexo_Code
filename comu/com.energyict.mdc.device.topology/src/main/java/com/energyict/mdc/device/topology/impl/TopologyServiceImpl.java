/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_2SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_3SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Expression;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.G3DeviceAddressInformation;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.G3NodeState;
import com.energyict.mdc.device.topology.G3Topology;
import com.energyict.mdc.device.topology.G3TopologyBuilder;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;
import com.energyict.mdc.device.topology.impl.kpi.RegisteredDevicesKpiServiceImpl;
import com.energyict.mdc.device.topology.impl.kpi.TranslationKeys;
import com.energyict.mdc.device.topology.impl.utils.ChannelDataTransferor;
import com.energyict.mdc.device.topology.impl.utils.DeviceEventInfo;
import com.energyict.mdc.device.topology.impl.utils.MeteringChannelProvider;
import com.energyict.mdc.device.topology.impl.utils.Utils;
import com.energyict.mdc.device.topology.kpi.Privileges;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link TopologyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:40)
 */
@Component(name = "com.energyict.mdc.device.topology", service = {TopologyService.class, ServerTopologyService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = "name=" + TopologyService.COMPONENT_NAME)
public class TopologyServiceImpl implements ServerTopologyService, MessageSeedProvider, TranslationKeyProvider {

    private static final long ETERNITY = 1_000_000_000_000_000_000L;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile UpgradeService upgradeService;
    private volatile QueryService queryService;
    private volatile EventService eventService;
    private MeteringChannelProvider meteringChannelProvider;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile KpiService kpiService;
    private volatile UserService userService;
    private RegisteredDevicesKpiService registeredDevicesKpiService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(TopologyServiceImpl.class.getName());

    // For OSGi framework only
    public TopologyServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public TopologyServiceImpl(BundleContext bundleContext, OrmService ormService, NlsService nlsService, Clock clock, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, UpgradeService upgradeService, QueryService queryService, EventService eventService, MessageService messageService, TaskService taskService, KpiService kpiService, UserService userService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setClock(clock);
        setConnectionTaskService(connectionTaskService);
        setCommunicationTaskService(communicationTaskService);
        setUpgradeService(upgradeService);
        setQueryService(queryService);
        setEventService(eventService);
        setMessageService(messageService);
        setTaskService(taskService);
        setKpiService(kpiService);
        setUserService(userService);
        meteringChannelProvider = new MeteringChannelProvider(thesaurus);
        activate(bundleContext);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(G3NodeState.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        createRealServices();
        this.dataModel.register(this.getModule());
        upgradeService.register(InstallIdentifier.identifier("MultiSense", TopologyService.COMPONENT_NAME), dataModel, Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 2), V10_2SimpleUpgrader.class)
                        .put(version(10, 4), UpgraderV10_4.class)
                        .put(version(10, 4, 3), V10_4_3SimpleUpgrader.class)
                        .put(version(10, 4, 8), UpgraderV10_4_8.class)
                        .put(version(10, 4, 21), UpgraderV10_4_21.class)
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .put(version(10, 9, 4), UpgraderV10_9_4.class)
                        .build());
        this.registerRealServices(bundleContext);
    }

    private void createRealServices() {
        this.registeredDevicesKpiService = new RegisteredDevicesKpiServiceImpl(this, clock);
    }

    private void registerRealServices(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(RegisteredDevicesKpiService.class, this.registeredDevicesKpiService, null));
    }

    @Deactivate
    public void stop() throws Exception {
        this.serviceRegistrations.forEach(ServiceRegistration::unregister);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(EventService.class).toInstance(eventService);
                bind(TopologyService.class).toInstance(TopologyServiceImpl.this);
                bind(ServerTopologyService.class).toInstance(TopologyServiceImpl.this);
                bind(MessageService.class).toInstance(messageService);
                bind(KpiService.class).toInstance(kpiService);
                bind(TaskService.class).toInstance(taskService);
                bind(RegisteredDevicesKpiService.class).toInstance(registeredDevicesKpiService);
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    @Override
    public DataModel dataModel() {
        return this.dataModel;
    }

    @Override
    public void setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(Device device, ConnectionTask defaultConnectionTask) {
        List<ComTaskExecution> comTaskExecutions = this.findComTaskExecutionsWithDefaultConnectionTaskForCompleteTopology(device);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTask);
            LOGGER.info("CXO-11731: Update comtask execution from setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology");
            comTaskExecutionUpdater.update();
        }
    }

    /**
     * Constructs a list of {@link ComTaskExecution}s which are linked to
     * the default {@link ConnectionTask} for the entire topology of the specified master Device.
     *
     * @param device The master Device
     * @return The List of ComTaskExecution
     */
    private List<ComTaskExecution> findComTaskExecutionsWithDefaultConnectionTaskForCompleteTopology(Device device) {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(device, scheduledComTasks);
        return scheduledComTasks;
    }

    private void collectComTaskWithDefaultConnectionTaskForCompleteTopology(Device device, List<ComTaskExecution> scheduledComTasks) {
        List<ComTaskExecution> comTaskExecutions = this.communicationTaskService.findComTaskExecutionsWithDefaultConnectionTask(device);
        scheduledComTasks.addAll(comTaskExecutions);
        for (Device slave : this.findPhysicalConnectedDevices(device)) {
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks);
        }
    }

    @Override
    public void setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(Device device, ConnectionTask connectionTask) {
        if (connectionTask.getPartialConnectionTask().getConnectionFunction().isPresent()) {
            ConnectionFunction connectionFunction = connectionTask.getPartialConnectionTask().getConnectionFunction().get();
            List<ComTaskExecution> comTaskExecutions = this.findComTaskExecutionsWithConnectionFunctionForCompleteTopology(device, connectionFunction);
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
                comTaskExecutionUpdater.useConnectionTaskBasedOnConnectionFunction(connectionTask);
                LOGGER.info("CXO-11731: Update comtask execution from setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology");
                comTaskExecutionUpdater.update();
            }
        }
    }

    @Override
    public void recalculateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(Device device, ConnectionFunction connectionFunction) {
        List<ComTaskExecution> comTaskExecutions = this.findComTaskExecutionsWithConnectionFunctionForCompleteTopology(device, connectionFunction);
        Optional<ConnectionTask> connectionTaskOptional = this.findConnectionTaskWithConnectionFunctionForTopology(device, connectionFunction);

        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            if (connectionTaskOptional.isPresent()) {
                comTaskExecutionUpdater.useConnectionTaskBasedOnConnectionFunction(connectionTaskOptional.get());
            } else {
                comTaskExecutionUpdater.setConnectionFunction(connectionFunction); // Set to use a ConnectionFunction not used on any ConnectionTask
            }
            LOGGER.info("CXO-11731: Update comtask execution from recalculateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology");
            comTaskExecutionUpdater.update();
        }
    }

    private List<ComTaskExecution> findComTaskExecutionsWithConnectionFunctionForCompleteTopology(Device device, ConnectionFunction connectionFunction) {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        this.collectComTaskWithConnectionFunctionForCompleteTopology(device, scheduledComTasks, connectionFunction);
        return scheduledComTasks;
    }

    private void collectComTaskWithConnectionFunctionForCompleteTopology(Device device, List<ComTaskExecution> scheduledComTasks, ConnectionFunction connectionFunction) {
        List<ComTaskExecution> comTaskExecutions = this.communicationTaskService.findComTaskExecutionsWithConnectionFunction(device, connectionFunction);
        scheduledComTasks.addAll(comTaskExecutions);
        for (Device slave : this.findPhysicalConnectedDevices(device)) {
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks);
        }
    }

    @Override
    public List<ConnectionTask<?, ?>> findAllConnectionTasksForTopology(Device device) {
        List<ConnectionTask<?, ?>> allConnectionTasks = new ArrayList<>(device.getConnectionTasks()); // Should be a mutable list
        Optional<Device> physicalGateway = this.getPhysicalGateway(device);
        physicalGateway.ifPresent(gateway -> allConnectionTasks.addAll(this.findAllConnectionTasksForTopology(gateway)));

        return allConnectionTasks;
    }

    @Override
    public Optional<ConnectionTask> findDefaultConnectionTaskForTopology(final Device device) {
        Optional<ConnectionTask> connectionTask = this.connectionTaskService.findDefaultConnectionTaskForDevice(device);
        if (connectionTask.isPresent()) {
            return connectionTask;
        } else {
            /* No default ConnectionTask on the device,
             * let's try the physical gateway if there is one. */
            Optional<Device> physicalGateway = this.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                return this.findDefaultConnectionTaskForTopology(physicalGateway.get());
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskWithConnectionFunctionForTopology(Device device, ConnectionFunction connectionFunction) {
        Optional<ConnectionTask> connectionTask = this.connectionTaskService.findConnectionTaskByDeviceAndConnectionFunction(device, connectionFunction);
        if (connectionTask.isPresent()) {
            return connectionTask;
        } else {
            /* No matching ConnectionTask found on the device,
             * let's try the physical gateway if there is one. */
            Optional<Device> physicalGateway = this.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                return this.findConnectionTaskWithConnectionFunctionForTopology(physicalGateway.get(), connectionFunction);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public DeviceTopology getPhysicalTopology(Device root, Range<Instant> period) {
        return this.buildTopology(root, period, this::findPhysicallyReferencingDevicesFor);
    }

    private List<ServerTopologyTimeslice> findPhysicallyReferencingDevicesFor(Device device, Range<Instant> period) {
        Condition condition = this.getDevicesInTopologyInIntervalCondition(device, period);
        List<PhysicalGatewayReferenceImpl> gatewayReferences = this.dataModel.mapper(PhysicalGatewayReferenceImpl.class).select(condition);
        return this.toTopologyTimeslices(new ArrayList<>(gatewayReferences));
    }

    private Condition getDevicesInTopologyInIntervalCondition(Device device, Range<Instant> period) {
        return where("gateway").isEqualTo(device).and(where("interval").isEffective(period));
    }

    private List<ServerTopologyTimeslice> toTopologyTimeslices(List<PhysicalGatewayReferenceImpl> gatewayReferences) {
        return gatewayReferences.stream().map(this::toTopologyTimeslice).collect(Collectors.toList());
    }

    private ServerTopologyTimeslice toTopologyTimeslice(PhysicalGatewayReference gatewayReference) {
        return new SimpleTopologyTimesliceImpl(gatewayReference.getOrigin(), gatewayReference.getInterval());
    }

    private DeviceTopologyImpl buildTopology(Device root, Range<Instant> period, FirstLevelTopologyTimeslicer firstLevelTopologyTimeslicer) {
        List<ServerTopologyTimeslice> firstLevelTopologyEntries = firstLevelTopologyTimeslicer.firstLevelTopologyTimeslices(root, period);
        Range<Instant> spanningInterval = this.intervalSpanOf(firstLevelTopologyEntries);
        DeviceTopologyImpl topology = new DeviceTopologyImpl(root, period.intersection(spanningInterval));
        for (TopologyTimeslice firstLevelTopologyEntry : firstLevelTopologyEntries) {
            for (Device device : firstLevelTopologyEntry.getDevices()) {
                topology.addChild(this.buildTopology(device, period.intersection(firstLevelTopologyEntry.getPeriod()), firstLevelTopologyTimeslicer));
            }
        }
        return topology;
    }

    private Range<Instant> intervalSpanOf(List<ServerTopologyTimeslice> topologyEntries) {
        List<Instant> timeslicePeriodEndPoints =
                topologyEntries
                        .stream()
                        .map(TopologyTimeslice::getPeriod)
                        .flatMap(this::periodEndPoints)
                        .collect(Collectors.toList());
        if (timeslicePeriodEndPoints.isEmpty()) {
            return Range.all();
        } else {
            return Range.encloseAll(timeslicePeriodEndPoints);
        }
    }

    private Stream<Instant> periodEndPoints(Range<Instant> period) {
        return Stream.of(this.lowerEndpoint(period), this.upperEndpoint(period));
    }

    private Instant lowerEndpoint(Range<Instant> period) {
        if (period.hasLowerBound()) {
            return period.lowerEndpoint();
        } else {
            return Instant.MIN;
        }
    }

    private Instant upperEndpoint(Range<Instant> period) {
        if (period.hasUpperBound()) {
            return period.upperEndpoint();
        } else {
            return Instant.MAX;
        }
    }

    @Override
    public List<Channel> getAllChannels(LoadProfile loadProfile) {
        List<Channel> channels = loadProfile.getChannels();
        channels.addAll(
                this.findPhysicalConnectedDevices(loadProfile.getDevice())
                        .stream()
                        .filter(Device::isLogicalSlave)
                        .flatMap(slave -> slave.getChannels().stream())
                        .filter(c -> c.getLoadProfile().getLoadProfileTypeId() == loadProfile.getLoadProfileTypeId())
                        .collect(Collectors.toList()));
        return channels;
    }

    @Override
    public List<Device> findPhysicalConnectedDevices(Device gateway) {
        Condition condition = this.getDevicesInTopologyCondition(gateway);
        List<PhysicalGatewayReferenceImpl> physicalGatewayReferences = this.dataModel.mapper(PhysicalGatewayReferenceImpl.class).select(condition);
        return this.findUniqueReferencingDevices(new ArrayList<>(physicalGatewayReferences));
    }

    private List<Device> findUniqueReferencingDevices(List<PhysicalGatewayReference> gatewayReferences) {
        Map<Long, Device> devicesById = new HashMap<>();
        for (PhysicalGatewayReference reference : gatewayReferences) {
            devicesById.put(reference.getOrigin().getId(), reference.getOrigin());
        }
        return new ArrayList<>(devicesById.values());
    }

    @Override
    public Optional<Device> getPhysicalGateway(Device slave) {
        return this.getPhysicalGateway(slave, this.clock.instant());
    }

    @Override
    public Optional<Device> getPhysicalGateway(Device slave, Instant when) {
        return this.getPhysicalGatewayReference(slave, when).map(PhysicalGatewayReference::getGateway);
    }

    @Override
    public Stream<PhysicalGatewayReference> getLastPhysicalGateways(Device slave, int numberOfDevices) {
        return DefaultFinder.of(PhysicalGatewayReference.class, where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(slave), dataModel).paged(0, numberOfDevices).sorted("interval.start", false).stream();
    }

    @Override
    public List<Device> getSlaveDevices(Device device) {
        List<Device> slaveDevices = new ArrayList<>();
        TopologyTimeline timeline = getPhysicalTopologyTimeline(device);
        slaveDevices.addAll(timeline.getAllDevices().stream()
                .filter(d -> hasNotEnded(timeline, d))
                .sorted(new DeviceRecentlyAddedComporator(timeline))
                .collect(Collectors.toList()));
        return slaveDevices;
    }

    @Override
    public List<G3Neighbor> getSlaveDevices(Device gateway, long pageStart) {
        LOGGER.info("Building the full Slave devices list for " + gateway.getSerialNumber());

        G3TopologyBuilder g3TopologyBuilder = new G3TopologyBuilder(this, gateway);

        G3Topology g3Topology = g3TopologyBuilder.build();

        List<G3Neighbor> g3NeighborList = g3Topology.getReferences();

        LOGGER.info("Returning a list of " + g3NeighborList.size() + " references");

        return g3NeighborList;
    }

    private static class DeviceRecentlyAddedComporator implements Comparator<Device> {

        private TopologyTimeline timeline;

        DeviceRecentlyAddedComporator(TopologyTimeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public int compare(Device d1, Device d2) {
            Optional<Instant> d1AddTime = this.timeline.mostRecentlyAddedOn(d1);
            Optional<Instant> d2AddTime = this.timeline.mostRecentlyAddedOn(d2);
            if (!d1AddTime.isPresent() && !d2AddTime.isPresent()) {
                return 0;
            } else if (!d1AddTime.isPresent() && d2AddTime.isPresent()) {
                return 1;
            } else if (!d2AddTime.isPresent() && d1AddTime.isPresent()) {
                return -1;
            }
            return -1 * d1AddTime.get().compareTo(d2AddTime.get());
        }

    }

    private static boolean hasNotEnded(TopologyTimeline timeline, Device device) {
        List<TopologyTimeslice> x1 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device)).collect(Collectors.toList());

        List<TopologyTimeslice> x2 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .collect(Collectors.toList());

        Optional<TopologyTimeslice> first = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .findFirst();
        return first.filter(topologyTimeslice -> !topologyTimeslice.getPeriod().hasUpperBound()).isPresent();
    }

    private static boolean contains(TopologyTimeslice timeslice, Device device) {
        return timeslice.getDevices()
                .stream()
                .anyMatch(d -> d.getId() == device.getId());
    }

    @Override
    public Subquery IsLinkedToMaster(Device device) {
        return queryService.wrap(this.dataModel.query(PhysicalGatewayReference.class))
                .asSubquery(where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(device)
                        .and
                                (where("interval").isEffective(Instant.now())), PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName());
    }

    @Override
    public Optional<PhysicalGatewayReference> getPhysicalGatewayReference(Device slave, Instant when) {
        DataMapper<PhysicalGatewayReference> mapper = this.dataModel.mapper(PhysicalGatewayReference.class);
        List<PhysicalGatewayReference> allEffective =
                mapper.select(where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(slave)
                                .and(where("interval").isEffective(when)),
                        Order.ascending("interval.start"));
        if (allEffective.size() > 1) {
            throw new IllegalStateException("More than one effective physical gateway for device " + slave.getId());
        } else if (allEffective.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(allEffective.get(0));
        }
    }

    public List<PhysicalGatewayReference> getPhysicalGateWayReferencesFrom(Device slave, Instant when) {
        DataMapper<PhysicalGatewayReference> mapper = this.dataModel.mapper(PhysicalGatewayReference.class);
        return mapper.select(where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(slave)
                        .and(where("interval.end").isGreaterThan(when.toEpochMilli())),
                Order.ascending("interval.start"));
    }

    @Override
    public Map<Device, Device> getPhysicalGateways(List<Device> deviceList) {
        if (deviceList.isEmpty()) {
            return Collections.emptyMap();
        }
        DataMapper<PhysicalGatewayReference> mapper = this.dataModel.mapper(PhysicalGatewayReference.class);
        List<PhysicalGatewayReference> allEffective =
                mapper.select(ListOperator.IN.contains(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName(), deviceList)
                        .and(where("interval").isEffective(this.clock.instant())), Order.ascending("interval.start"));
        return allEffective.stream().collect(Collectors.toMap(PhysicalGatewayReference::getOrigin, PhysicalGatewayReference::getGateway));
    }

    @Override
    public void setPhysicalGateway(Device slave, Device gateway) {
        Instant now = this.clock.instant();
        getPhysicalGatewayReference(slave, now).ifPresent(r -> terminateTemporal(r, now));
        newPhysicalGatewayReference(slave, gateway, now);
        eventService.postEvent(EventType.REGISTERED_TO_GATEWAY.topic(), new DeviceEventInfo(slave.getId()));
        slaveTopologyChanged(slave, Optional.of(gateway));
    }

    private PhysicalGatewayReferenceImpl newPhysicalGatewayReference(Device slave, Device gateway, Instant start) {
        PhysicalGatewayReferenceImpl physicalGatewayReference =
                this.dataModel.getInstance(PhysicalGatewayReferenceImpl.class).createFor(slave, gateway, Interval.of(Range.atLeast(start)));
        Save.CREATE.validate(this.dataModel, physicalGatewayReference);
        this.dataModel.persist(physicalGatewayReference);
        return physicalGatewayReference;
    }

    @Override
    public void setDataLogger(Device slave, Device dataLogger, Instant linkingDate, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap) {
        Instant linkDate = Utils.generalizeLinkingDate(linkingDate);
        Optional<PhysicalGatewayReference> existingGatewayReference = this.getPhysicalGatewayReference(slave, linkDate);
        if (existingGatewayReference.isPresent()) {
            throw DataLoggerLinkException.slaveWasAlreadyLinkedToOtherDatalogger(thesaurus, slave, existingGatewayReference.get().getGateway(), linkDate);
        }
        validateUniqueKeyConstraintForDataloggerReference(dataLogger, linkDate, slave);

        List<MeterActivation> dataLoggerMeterActivations = dataLogger.getMeterActivations(Range.atLeast(linkDate));
        Collections.reverse(dataLoggerMeterActivations);
        List<MeterActivation> slaveMeterActivations = slave.getMeterActivations(Range.atLeast(linkDate));
        Collections.reverse(slaveMeterActivations);

        createNecessaryDataLoggerReferences(slave, dataLogger, slaveDataLoggerChannelMap, slaveDataLoggerRegisterMap, linkDate, dataLoggerMeterActivations, slaveMeterActivations);
    }

    @Override
    public Optional<Device> getDataLogger(Device slave, Instant when) {
        return findDataloggerReference(slave, when).map(DataLoggerReference::getGateway);
    }

    private void createNecessaryDataLoggerReferences(Device slave, Device dataLogger, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap, Instant linkDate, List<MeterActivation> dataLoggerMeterActivations, List<MeterActivation> slaveMeterActivations) {
        Instant start = linkDate;
        Instant end = null;
        for (MeterActivation slaveMeterActivation : slaveMeterActivations) {
            List<MeterActivation> overLappingDataLoggerMeterActivations = Utils.getOverLappingDataLoggerMeterActivations(slaveMeterActivation, dataLoggerMeterActivations);
            for (MeterActivation dataLoggerMeterActivation : overLappingDataLoggerMeterActivations) {
                findOrCreateNewDataLoggerReference(slave, dataLogger, slaveDataLoggerChannelMap, slaveDataLoggerRegisterMap, start, slaveMeterActivation, dataLoggerMeterActivation);

                if (slaveMeterActivation.getEnd() != null) {
                    end = slaveMeterActivation.getEnd();
                }
                if (dataLoggerMeterActivation.getEnd() != null) {
                    if (end == null || end.isAfter(dataLoggerMeterActivation.getEnd())) {
                        end = dataLoggerMeterActivation.getEnd();
                    }
                }
                if (end != null) {
                    start = end;
                    clearDataLogger(slave, end);
                    end = null;
                }
            }
        }
    }

    private DataLoggerReference findOrCreateNewDataLoggerReference(Device slave, Device dataLogger, Map<Channel, Channel> slaveDataLoggerChannelMap, Map<Register, Register> slaveDataLoggerRegisterMap, Instant start, MeterActivation slaveMeterActivation, MeterActivation dataLoggerMeterActivation) {
        Optional<DataLoggerReference> existingDataLoggerReference = findDataloggerReference(slave, start);
        if (!existingDataLoggerReference.isPresent()) {
            final DataLoggerReferenceImpl dataLoggerReference = this.newDataLoggerReference(slave, dataLogger, start);
            slaveDataLoggerChannelMap.forEach((slaveChannel, dataLoggerChannel) -> this.addChannelDataLoggerUsage(dataLoggerReference, slaveChannel, dataLoggerChannel, dataLoggerMeterActivation, slaveMeterActivation));
            slaveDataLoggerRegisterMap.forEach((slaveRegister, dataLoggerRegister) -> this.addRegisterDataLoggerUsage(dataLoggerReference, slaveRegister, dataLoggerRegister, dataLoggerMeterActivation, slaveMeterActivation));
            Save.CREATE.validate(this.dataModel, dataLoggerReference);
            ChannelDataTransferor dataTransferor = new ChannelDataTransferor();
            dataLoggerReference.getDataLoggerChannelUsages().stream().forEach(dataTransferor::transferChannelDataToSlave);
            this.dataModel.persist(dataLoggerReference);
            return dataLoggerReference;
        } else {
            return existingDataLoggerReference.get();
        }
    }

    private void validateUniqueKeyConstraintForDataloggerReference(Device dataLogger, Instant linkingDate, Device slave) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(dataLogger)
                .and(where("interval.start").in(Range.closed(linkingDate.toEpochMilli(), linkingDate.toEpochMilli())));
        Optional<DataLoggerReference> duplicateReference = this.dataModel.mapper(DataLoggerReference.class).select(condition).stream().collect(Collectors.toList())
                .stream().filter(reference -> reference.getOrigin().getName().equals(slave.getName()) && reference.getRange().lowerEndpoint().equals(linkingDate)).findAny();
        if (duplicateReference.isPresent()) {
            throw DataLoggerLinkException.slaveWasPreviouslyLinkedAtSameTimeStamp(thesaurus, slave, dataLogger, linkingDate);
        }
    }

    @Override
    public List<Device> findDataLoggerSlaves(Device dataLogger) {
        Condition condition = this.getDevicesInTopologyCondition(dataLogger);
        List<DataLoggerReferenceImpl> dataLoggerReferences = this.dataModel.mapper(DataLoggerReferenceImpl.class).select(condition);
        return this.findUniqueReferencingDevices(new ArrayList<>(dataLoggerReferences));
    }

    @Override
    public boolean isDataLoggerSlaveCandidate(Device device) {
        if (!device.getDeviceType().isDataloggerSlave()) {
            return false;
        }
        Condition dataLoggerReferencesHavingThisDeviceAsOrigin = where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(device).and(where("interval").isEffective());
        return this.dataModel.mapper(DataLoggerReferenceImpl.class).select(dataLoggerReferencesHavingThisDeviceAsOrigin).isEmpty();
    }

    @Override
    public Optional<DataLoggerReference> findDataloggerReference(Device dataloggerSlaveDevice, Instant effective) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(dataloggerSlaveDevice).and(where("interval").isEffective(effective));
        return this.dataModel.mapper(DataLoggerReference.class).select(condition).stream().findAny(); // the business logic of the effectivity requires that there is only one object effective at a
        // given time
    }

    @Override
    public Optional<DataLoggerReference> findLastDataloggerReference(Device dataloggerSlaveDevice) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(dataloggerSlaveDevice);
        return this.dataModel.stream(DataLoggerReference.class).filter(condition).sorted(Order.descending("interval.start")).limit(1).findFirst();
    }

    @Override
    public Finder<? extends DataLoggerReference> findAllEffectiveDataLoggerSlaveDevices() {
        return DefaultFinder.of(DataLoggerReferenceImpl.class, where("interval").isEffective(), dataModel());
    }

    @Override
    public Optional<Channel> getSlaveChannel(Channel dataLoggerChannel) {
        return this.getSlaveChannel(dataLoggerChannel, clock.instant());
    }

    @Override
    public Optional<Channel> getSlaveChannel(Channel dataLoggerChannel, Instant when) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerChannel)
                .flatMap(meteringChannel -> findDataLoggerChannelUsage(meteringChannel, when))
                .map((dataLoggerChannelUsage) -> getChannel(dataLoggerChannelUsage.getPhysicalGatewayReference().getOrigin(), dataLoggerChannelUsage.getSlaveChannel()).get());
    }

    private Optional<Channel> getChannel(Device device, com.elster.jupiter.metering.Channel channel) {
        return device.getChannels().stream().filter(mdcChannel -> channel.getReadingTypes().contains(mdcChannel.getReadingType())).findFirst();
    }

    @Override
    public Optional<Register> getSlaveRegister(Register dataLoggerRegister, Instant when) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerRegister)
                .flatMap(meteringChannel -> findDataLoggerChannelUsage(meteringChannel, when))
                .map((dataLoggerChannelUsage) -> getRegister(dataLoggerChannelUsage.getPhysicalGatewayReference().getOrigin(), dataLoggerChannelUsage.getSlaveChannel()).get());
    }

    private Optional<Register> getRegister(Device device, com.elster.jupiter.metering.Channel channel) {
        return device.getRegisters().stream().filter(mdcRegister -> channel.getReadingTypes().contains(mdcRegister.getReadingType())).findFirst();
    }

    @Override
    public boolean isReferenced(Channel dataLoggerChannel) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerChannel)
                .map(this::isReferenced)
                .orElse(false);
    }

    @Override
    public boolean isReferenced(Register dataLoggerRegister) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerRegister)
                .map(this::isReferenced)
                .orElse(false);
    }

    @Override
    public List<Pair<Channel, Range<Instant>>> getDataLoggerChannelTimeLine(Channel channel, Range<Instant> range) {
        List<DataLoggerChannelUsage> dataLoggerChannelUsagesForChannels = findDataLoggerChannelUsagesForChannels(channel, range);
        if (dataLoggerChannelUsagesForChannels.isEmpty()) { // it's probably not a datalogger or no channels have been linked before
            return Collections.singletonList(Pair.of(channel, range));
        } else {
            List<Pair<Channel, Range<Instant>>> channelTimeLine = new ArrayList<>();
            constructTimeLine(channel, range, dataLoggerChannelUsagesForChannels, channelTimeLine, getSlaveChannel());
            return channelTimeLine;
        }
    }

    @Override
    public List<Pair<Register, Range<Instant>>> getDataLoggerRegisterTimeLine(Register register, Range<Instant> range) {
        List<DataLoggerChannelUsage> dataLoggerChannelUsagesForRegisters = findDataLoggerChannelUsagesForRegisters(register, range);
        if (dataLoggerChannelUsagesForRegisters.isEmpty()) {
            return Collections.singletonList(Pair.of(register, range));
        } else {
            List<Pair<Register, Range<Instant>>> registerTimeLine = new ArrayList<>();
            constructTimeLine(register, range, dataLoggerChannelUsagesForRegisters, registerTimeLine, getSlaveRegister());
            return registerTimeLine;
        }
    }

    /**
     * Constructs a timeLine based on the given dataLoggerDataSource and the slave resources which were potentially present in the requested range
     *
     * @param dataLoggerDataSource    the Data source (channel or register) of the datalogger (or just a  plain device)
     * @param range                   the range in where we need to construct the timeline
     * @param dataLoggerChannelUsages the list of usages that are valid for the given range
     * @param timeLine                the timeline object which we need to populate
     * @param slaveItem               the function that will look up the corresponding slave data source for a specific dataLoggerDataSource
     * @param <T>                     the generic type of a Channel/Register
     */
    private <T> void constructTimeLine(T dataLoggerDataSource, Range<Instant> range, List<DataLoggerChannelUsage> dataLoggerChannelUsages, List<Pair<T, Range<Instant>>> timeLine, Function<DataLoggerChannelUsage, Optional<T>> slaveItem) {
        dataLoggerChannelUsages.stream().filter(dataLoggerChannelUsage1 -> !dataLoggerChannelUsage1.getRange().isEmpty()).forEach(dataLoggerChannelUsage -> {
            Optional<Pair<T, Range<Instant>>> lastListItem = getLastListItem(timeLine);
            // if we don't have a consecutive 'range', then add a timeLine entry for the original 'dataLoggerDataSource
            if (lastListItem.isPresent()) {
                if ((!lastListItem.get().getLast().lowerEndpoint().equals(dataLoggerChannelUsage.getRange().upperEndpoint()))) {
                    timeLine.add(Pair.of(dataLoggerDataSource, Range.openClosed(dataLoggerChannelUsage.getRange().upperEndpoint(), lastListItem.get().getLast().lowerEndpoint())));
                }
            } else { // we need to add the first item
                if (!range.hasUpperBound() && dataLoggerChannelUsage.getRange().hasUpperBound()) {
                    timeLine.add(Pair.of(dataLoggerDataSource, Range.atLeast(dataLoggerChannelUsage.getRange().upperEndpoint())));
                } else if (range.hasUpperBound() && dataLoggerChannelUsage.getRange().hasUpperBound() && range.upperEndpoint()
                        .isAfter(dataLoggerChannelUsage.getRange().upperEndpoint())) { // the end of the range is larger then the last linked slave
                    timeLine.add(Pair.of(dataLoggerDataSource, Range.openClosed(dataLoggerChannelUsage.getRange().upperEndpoint(), range.upperEndpoint())));
                }
            }
            slaveItem.apply(dataLoggerChannelUsage).ifPresent(slaveChannel -> timeLine.add(Pair.of(slaveChannel, getLowerBoundClippedRange(dataLoggerChannelUsage.getRange(), range))));
        });
        Optional<Pair<T, Range<Instant>>> lastListItem = getLastListItem(timeLine);
        if (lastListItem.isPresent() && range.lowerEndpoint().isBefore(lastListItem.get().getLast().lowerEndpoint())) {
            timeLine.add(Pair.of(dataLoggerDataSource, Range.openClosed(range.lowerEndpoint(), lastListItem.get().getLast().lowerEndpoint())));
        }
    }

    private Range<Instant> getLowerBoundClippedRange(Range<Instant> slaveRange, Range<Instant> requestedRange) {
        Instant lowerEndPoint;
        Instant upperEndPoint = null;
        if (requestedRange.lowerEndpoint().isBefore(slaveRange.lowerEndpoint())) {
            lowerEndPoint = slaveRange.lowerEndpoint();
        } else {
            lowerEndPoint = requestedRange.lowerEndpoint();
        }
        if (slaveRange.hasUpperBound()) {
            if (requestedRange.hasUpperBound() && slaveRange.upperEndpoint().isAfter(requestedRange.upperEndpoint())) {
                upperEndPoint = requestedRange.upperEndpoint();
            } else {
                upperEndPoint = slaveRange.upperEndpoint();
            }
        } else if (requestedRange.hasUpperBound()) {
            upperEndPoint = requestedRange.upperEndpoint();
        }
        if (upperEndPoint != null) {
            return Range.openClosed(lowerEndPoint, upperEndPoint);
        } else {
            return Range.atLeast(lowerEndPoint);
        }
    }

    private Function<DataLoggerChannelUsage, Optional<Channel>> getSlaveChannel() {
        return dataLoggerChannelUsage -> dataLoggerChannelUsage.getPhysicalGatewayReference()
                .getOrigin()
                .getChannels()
                .stream()
                .filter(channel -> dataLoggerChannelUsage.getSlaveChannel().getReadingTypes().contains(channel.getReadingType()))
                .findAny();
    }

    private Function<DataLoggerChannelUsage, Optional<Register>> getSlaveRegister() {
        return dataLoggerChannelUsage -> dataLoggerChannelUsage.getPhysicalGatewayReference()
                .getOrigin()
                .getRegisters()
                .stream()
                .filter(register -> dataLoggerChannelUsage.getSlaveChannel().getReadingTypes().contains(register.getReadingType()))
                .findAny();
    }

    private <T> Optional<T> getLastListItem(List<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(list.get(list.size() - 1));
        }
    }

    public boolean isReferenced(com.elster.jupiter.metering.Channel dataLoggerChannel) {
        return !findDataLoggerChannelUsage(dataLoggerChannel).isEmpty();
    }

    private List<DataLoggerChannelUsage> findDataLoggerChannelUsage(com.elster.jupiter.metering.Channel dataLoggerChannel) {
        Condition gateway = where(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.fieldName()).isEqualTo(dataLoggerChannel);
        Condition effective = where(DataLoggerChannelUsageImpl.Field.PHYSICALGATEWAYREF.fieldName() + "." + AbstractPhysicalGatewayReferenceImpl.Field.INTERVAL.fieldName()).isEffective();
        return dataModel.query(DataLoggerChannelUsage.class, PhysicalGatewayReference.class).select(gateway.and(effective));
    }

    private Optional<DataLoggerChannelUsage> findDataLoggerChannelUsage(com.elster.jupiter.metering.Channel dataLoggerChannel, Instant when) {
        Condition gateway = where(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.fieldName()).isEqualTo(dataLoggerChannel);
        Condition effective = where(DataLoggerChannelUsageImpl.Field.PHYSICALGATEWAYREF.fieldName() + "." + AbstractPhysicalGatewayReferenceImpl.Field.INTERVAL.fieldName()).isEffective(when);
        List<DataLoggerChannelUsage> found = dataModel.query(DataLoggerChannelUsage.class, PhysicalGatewayReference.class).select(gateway.and(effective));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(found.get(0));
    }

    @Override
    public List<DataLoggerChannelUsage> findDataLoggerChannelUsagesForChannels(Channel dataLoggerChannel, Range<Instant> referencePeriod) {
        Optional<com.elster.jupiter.metering.Channel> meteringChannel = meteringChannelProvider.getMeteringChannel(dataLoggerChannel);
        if (meteringChannel.isPresent()) {
            List<MeterActivation> meterActivations = dataLoggerChannel.getDevice().getMeterActivations(referencePeriod);
            return meterActivations.stream()
                    .flatMap(meterActivation -> meterActivation.getChannelsContainer()
                            .getChannels()
                            .stream()
                            .filter(channel -> channel.getMainReadingType().equals(meteringChannel.get().getMainReadingType())))
                    .flatMap(channel1 -> findDataLoggerChannelUsages(channel1, referencePeriod).stream())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<DataLoggerChannelUsage> findDataLoggerChannelUsagesForRegisters(Register<?, ?> dataLoggerRegister, Range<Instant> referencePeriod) {
        Optional<com.elster.jupiter.metering.Channel> meteringChannel = meteringChannelProvider.getMeteringChannel(dataLoggerRegister);
        if (meteringChannel.isPresent()) {
            return findDataLoggerChannelUsages(meteringChannel.get(), referencePeriod);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Instant> availabilityDate(Channel dataLoggerChannel) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerChannel).flatMap(meteringChannel ->
                availabilityDate(meteringChannel, dataLoggerChannel.getDevice().getLifecycleDates().getReceivedDate()));
    }

    @Override
    public Optional<Instant> availabilityDate(Register dataLoggerRegister) {
        return meteringChannelProvider.getMeteringChannel(dataLoggerRegister).flatMap(meteringChannel ->
                availabilityDate(meteringChannel, dataLoggerRegister.getDevice().getLifecycleDates().getReceivedDate()));
    }

    private Optional<Instant> availabilityDate(com.elster.jupiter.metering.Channel dataLoggerChannel, Optional<Instant> atLeast) {
        List<DataLoggerChannelUsage> usages = this.findDataLoggerChannelUsages((dataLoggerChannel));
        if (usages.isEmpty()) {
            return Optional.of(atLeast.orElse(Instant.EPOCH));
        } else {
            usages.sort(Comparator.<DataLoggerChannelUsage>comparingLong((usage) -> usage.getRange().hasUpperBound() ? usage.getRange().upperEndpoint().toEpochMilli() : Long.MAX_VALUE).reversed());
            if (usages.get(0).getRange().hasUpperBound()) {
                return Optional.of(usages.get(0).getRange().upperEndpoint());
            } else {
                return Optional.empty();
            }
        }
    }

    private List<DataLoggerChannelUsage> findDataLoggerChannelUsages(com.elster.jupiter.metering.Channel dataLoggerChannel, Range<Instant> referencePeriod) {
        Condition gateway = where(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.fieldName()).isEqualTo(dataLoggerChannel);
        Condition effective = where(DataLoggerChannelUsageImpl.Field.PHYSICALGATEWAYREF.fieldName() + "." + AbstractPhysicalGatewayReferenceImpl.Field.INTERVAL.fieldName()).isEffective(referencePeriod);
        return dataModel.query(DataLoggerChannelUsage.class, PhysicalGatewayReference.class).select(gateway.and(effective), Order.descending("PGRSTARTTIME"));

    }

    private List<DataLoggerChannelUsage> findDataLoggerChannelUsages(com.elster.jupiter.metering.Channel dataLoggerChannel) {
        Condition gateway = where(DataLoggerChannelUsageImpl.Field.GATEWAY_CHANNEL.fieldName()).isEqualTo(dataLoggerChannel);
        return dataModel.query(DataLoggerChannelUsage.class, PhysicalGatewayReference.class).select(gateway);
    }

    private Condition getDevicesInTopologyCondition(Device device) {
        return where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(device).and(where("interval").isEffective());
    }

    private DataLoggerReferenceImpl newDataLoggerReference(Device slave, Device gateway, Instant start) {
        return this.dataModel.getInstance(DataLoggerReferenceImpl.class).createFor(slave, gateway, Interval.of(Range.atLeast(start)));
    }

    public void clearDataLogger(Device slave, Instant when) {
        Instant unlinkTimeStamp = Utils.generalizeLinkingDate(when);
        List<PhysicalGatewayReference> physicalGatewayReferences = getPhysicalGateWayReferencesFrom(slave, when);
        if (!physicalGatewayReferences.isEmpty()) {
            terminateTemporal(physicalGatewayReferences.get(0), unlinkTimeStamp);
            this.slaveTopologyChanged(slave, Optional.empty());
            if (physicalGatewayReferences.size() > 1) {
                for (int i = 1; i < physicalGatewayReferences.size(); i++) {
                    PhysicalGatewayReference gatewayReference = physicalGatewayReferences.get(i);
                    terminateTemporal(gatewayReference, gatewayReference.getRange().lowerEndpoint());
                }
            }
        } else {
            throw DataLoggerLinkException.slaveWasNotLinkedAt(thesaurus, slave, unlinkTimeStamp);
        }
    }

    private void addChannelDataLoggerUsage(DataLoggerReferenceImpl dataLoggerReference, Channel slave, Channel dataLogger, MeterActivation dataLoggerMeterActivation, MeterActivation slaveMeterActivation) {
        com.elster.jupiter.metering.Channel channelForSlave = meteringChannelProvider.getMeteringChannel(slave, slaveMeterActivation);
        com.elster.jupiter.metering.Channel channelForDataLogger = meteringChannelProvider.getMeteringChannel(dataLogger, dataLoggerMeterActivation);
        dataLoggerReference.addDataLoggerChannelUsage(channelForSlave, channelForDataLogger);
    }

    private void addRegisterDataLoggerUsage(DataLoggerReferenceImpl dataLoggerReference, Register slave, Register dataLogger, MeterActivation dataLoggerMeterActivation, MeterActivation slaveMeterActivation) {
        com.elster.jupiter.metering.Channel channelForSlave = meteringChannelProvider.getMeteringChannel(slave, slaveMeterActivation);
        com.elster.jupiter.metering.Channel channelForDataLogger = meteringChannelProvider.getMeteringChannel(dataLogger, dataLoggerMeterActivation);
        dataLoggerReference.addDataLoggerChannelUsage(channelForSlave, channelForDataLogger);
    }

    public void terminateTemporal(PhysicalGatewayReference gatewayReference, Instant now) {
        gatewayReference.terminate(now);
        this.dataModel.update(gatewayReference);
    }

    public void slaveTopologyChanged(Device slave, Optional<Device> gateway) {
        List<ComTaskExecution> comTasksForDefaultConnectionTask = this.communicationTaskService.findComTasksByDefaultConnectionTask(slave);
        Map<ConnectionFunction, List<ComTaskExecution>> comTasksUsingConnectionFunction = this.communicationTaskService.findComTasksUsingConnectionFunction(slave);
        if (gateway.isPresent()) {
            this.updateComTasksToUseNewDefaultConnectionTask(slave, comTasksForDefaultConnectionTask);
            this.updateComTasksToUseNewConnectionTaskBasedOnConnectionFunction(slave, comTasksUsingConnectionFunction);
        } else {
            this.updateComTasksToUseNonExistingDefaultConnectionTask(comTasksForDefaultConnectionTask);
            this.updateComTasksToUseNonExistingConnectionTaskBasedOnConnectionFunction(comTasksUsingConnectionFunction);
        }
    }

    private void updateComTasksToUseNewDefaultConnectionTask(Device slave, List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        Optional<ConnectionTask> defaultConnectionTaskForTopology = this.findDefaultConnectionTaskForTopology(slave);
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            if (defaultConnectionTaskForTopology.isPresent()) {
                comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTaskForTopology.get());
            } else {
                comTaskExecutionUpdater.useDefaultConnectionTask(true);
            }
            LOGGER.info("CXO-11731: Update comtask execution from updateComTasksToUseNewDefaultConnectionTask");
            comTaskExecutionUpdater.update();
        }
    }

    private void updateComTasksToUseNewConnectionTaskBasedOnConnectionFunction(Device slave, Map<ConnectionFunction, List<ComTaskExecution>> comTasksUsingConnectionFunction) {
        comTasksUsingConnectionFunction.forEach((connectionFunction, affectedComTaskExecutions) -> {
            Optional<ConnectionTask> connectionTaskOptional = this.findConnectionTaskWithConnectionFunctionForTopology(slave, connectionFunction);
            for (ComTaskExecution comTaskExecution : affectedComTaskExecutions) {
                ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
                if (connectionTaskOptional.isPresent()) {
                    comTaskExecutionUpdater.useConnectionTaskBasedOnConnectionFunction(connectionTaskOptional.get());
                } else {
                    comTaskExecutionUpdater.setConnectionFunction(connectionFunction);
                }
                LOGGER.info("CXO-11731: Update comtask execution from updateComTasksToUseNewConnectionTaskBasedOnConnectionFunction");
                comTaskExecutionUpdater.update();
            }
        });
    }

    private void updateComTasksToUseNonExistingDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(true);
            LOGGER.info("CXO-11731: Update comtask execution from updateComTasksToUseNonExistingDefaultConnectionTask");
            comTaskExecutionUpdater.update();
        }
    }

    private void updateComTasksToUseNonExistingConnectionTaskBasedOnConnectionFunction(Map<ConnectionFunction, List<ComTaskExecution>> comTasksUsingConnectionFunction) {
        List<ComTaskExecution> allComtaskExecutions = new ArrayList<>();
        comTasksUsingConnectionFunction.values().stream().forEach(allComtaskExecutions::addAll);
        for (ComTaskExecution comTaskExecution : allComtaskExecutions) {
            ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.setConnectionFunction(comTaskExecution.getConnectionFunction().orElse(null));
            LOGGER.info("CXO-11731: Update comtask execution from updateComTasksToUseNonExistingConnectionTaskBasedOnConnectionFunction");
            comTaskExecutionUpdater.update();
        }
    }

    @Override
    public void clearPhysicalGateway(Device slave) {
        clearPhysicalGateway(slave, this.clock.instant());
    }

    private void clearPhysicalGateway(Device slave, Instant when) {
        this.getPhysicalGatewayReference(slave, when).ifPresent(r -> {
            terminateTemporal(r, when);
            eventService.postEvent(EventType.UNREGISTERED_FROM_GATEWAY.topic(), new DeviceEventInfo((slave.getId()), r.getGateway().getId()));
        });
        this.slaveTopologyChanged(slave, Optional.empty());
    }

    @Override
    public G3CommunicationPath getCommunicationPath(Device source, Device target) {
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(source, target);
        DataMapper<G3CommunicationPathSegment> mapper = this.dataModel.mapper(G3CommunicationPathSegment.class);
        SqlBuilder sqlBuilder = getCommunicationPathSqlBuilder(source, target, mapper);
        try (Fetcher<G3CommunicationPathSegment> fetcher = mapper.fetcher(sqlBuilder)) {
            fetcher.forEach(communicationPath::addSegment);
        }
        return communicationPath;
    }

    private static SqlBuilder getCommunicationPathSqlBuilder(Device source, Device target, DataMapper<G3CommunicationPathSegment> mapper) {
        SqlBuilder sqlBuilder = mapper.builder(" cps ");
        sqlBuilder.append("where cps.discriminator = ");
        sqlBuilder.addObject(CommunicationPathSegmentImpl.G3_DISCRIMINATOR);
        sqlBuilder.append(" start with (cps.srcdevice = ");
        sqlBuilder.addLong(source.getId());
        sqlBuilder.append(" and cps.targetdevice = ");
        sqlBuilder.addLong(target.getId());
        sqlBuilder.append(" and cps.endtime = ");
        sqlBuilder.addLong(ETERNITY);
        sqlBuilder.append(") connect by (cps.srcdevice = prior cps.nexthopdevice and cps.targetdevice = ");
        sqlBuilder.addLong(target.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public Stream<G3CommunicationPathSegment> getUniqueG3CommunicationPathSegments(Device gateway) {
        Condition condition = this.getDevicesInTopologyCondition(gateway);
        Subquery subQuery = this.dataModel.query(PhysicalGatewayReferenceImpl.class).asSubquery(condition, PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName());
        Condition targetIsASlave = ListOperator.IN.contains(subQuery, "target");
        return this.dataModel.query(G3CommunicationPathSegment.class)
                .select(targetIsASlave.and(where("interval").isEffective()).and(where("nextHop").isNull())).stream();
    }

    public Stream<G3CommunicationPathSegment> getAllG3CommunicationPathSegments(Device gateway) {
        Condition condition = where(G3CommunicationPathSegmentImpl.Field.SOURCE.fieldName()).isEqualTo(gateway).and(where("interval").isEffective());
        return this.dataModel.query(G3CommunicationPathSegment.class)
                .select(condition).stream();
    }

    @Override
    public G3CommunicationPathSegmentBuilder addCommunicationSegments() {
        return new G3CommunicationPathSegmentBuilderImpl(this, this.clock);
    }

    G3CommunicationPathSegment addCommunicationSegment(Instant now, Device source, Device target, Device intermediateHop, Duration timeToLive, int cost) {
        Optional<Device> nextHop;
        if (intermediateHop == null || intermediateHop.getId() == target.getId()) {
            nextHop = Optional.empty();
        } else {
            nextHop = Optional.of(intermediateHop);
        }
        return this.addCommunicationSegment(now, source, target, nextHop, timeToLive, cost);
    }

    private G3CommunicationPathSegment addCommunicationSegment(Instant now, Device source, Device target, Optional<Device> intermediateHop, Duration timeToLive, int cost) {
        DataMapper<G3CommunicationPathSegment> mapper = this.dataModel.mapper(G3CommunicationPathSegment.class);
        G3CommunicationPathSegmentImpl segment;
        if (intermediateHop.isPresent()) {
            segment = this.dataModel
                    .getInstance(G3CommunicationPathSegmentImpl.class)
                    .createIntermediate(source, target, Interval.of(Range.atLeast(now)), intermediateHop.get(), timeToLive.getSeconds(), cost);
        } else {
            segment = this.dataModel
                    .getInstance(G3CommunicationPathSegmentImpl.class)
                    .createFinal(source, target, Interval.of(Range.atLeast(now)), timeToLive.getSeconds(), cost);
        }
        Save.CREATE.validate(this.dataModel, segment);
        mapper.persist(segment);
        return segment;
    }

    @Override
    public void clearOldCommunicationPathSegments(Device source, Instant now) {
        getAllG3CommunicationPathSegments(source).forEach(s -> terminateG3CommunicationPathSegment(s, now));
    }

    public void terminateG3CommunicationPathSegment(G3CommunicationPathSegment pathSegment, Instant now) {
        pathSegment.terminate(now);
        this.dataModel.update(pathSegment);
    }

    @Override
    public TopologyTimeline getPhysicalTopologyTimeline(Device device) {
        return TopologyTimelineImpl.merge(this.findPhysicallyReferencingDevicesFor(device, Range.all()));
    }

    @Override
    public List<PhysicalGatewayReference> getPhysicalGatewayReferencesFor(Device device, Range<Instant> range) {
        Condition condition = this.getDevicesInTopologyInIntervalCondition(device, range);
        return this.dataModel.mapper(PhysicalGatewayReference.class).select(condition);
    }

    @Override
    public TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions) {
        return TopologyTimelineImpl.merge(this.findRecentPhysicallyReferencingDevicesFor(device, maximumNumberOfAdditions));
    }

    private List<ServerTopologyTimeslice> findRecentPhysicallyReferencingDevicesFor(Device device, int maxRecentCount) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        try (QueryStream<PhysicalGatewayReferenceImpl> steams = this.dataModel.stream(PhysicalGatewayReferenceImpl.class)) {
            List<PhysicalGatewayReferenceImpl> gatewayReferences = steams.filter(condition).sorted(Order.descending("interval.start"))
                    .limit(maxRecentCount)
                    .select();
            return this.toTopologyTimeslices(gatewayReferences);
        }
    }

    @Override
    public int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, Device device, Interval interval) {
        if (CommunicationErrorType.CONNECTION_SETUP_FAILURE.equals(errorType)) {
            /* Slaves always setup the connection via the master.
             * The logging records the failure against the master
             * so there is no way to count the number of slave devices
             * that had a connection setup failure. */
            return 0;
        } else {
            int numberOfDevices = 0;
            List<TopologyTimeslice> communicationTopologies = this.getPhysicalTopology(device, interval.toClosedRange()).timelined().getSlices();
            for (TopologyTimeslice topologyTimeslice : communicationTopologies) {
                List<Device> devices = new ArrayList<>(topologyTimeslice.getDevices());
                devices.add(device);
                numberOfDevices = numberOfDevices + this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(errorType, devices, topologyTimeslice.getPeriod());
            }
            return numberOfDevices;
        }
    }

    private int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, List<Device> devices, Range<Instant> range) {
        switch (errorType) {
            case CONNECTION_FAILURE: {
                return this.countNumberOfDevicesWithConnectionFailures(range, devices);
            }
            case COMMUNICATION_FAILURE: {
                return this.countNumberOfDevicesWithCommunicationFailuresInGatewayTopology(devices, range);
            }
            case CONNECTION_SETUP_FAILURE: {
                // Intended fall-through
            }
            default: {
                throw new RuntimeException("Unsupported CommunicationErrorType " + errorType);
            }
        }
    }

    private int countNumberOfDevicesWithConnectionFailures(Range<Instant> range, List<Device> devices) {
        return this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                devices,
                range,
                where(this.comSessionSuccessIndicatorFieldName()).isEqualTo(ComSession.SuccessIndicator.Broken));
    }

    private int countNumberOfDevicesWithCommunicationFailuresInGatewayTopology(List<Device> devices, Range<Instant> range) {
        return this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                devices,
                range,
                where(this.comSessionSuccessIndicatorFieldName()).isNotEqual(ComSession.SuccessIndicator.Success));
    }

    private String comSessionSuccessIndicatorFieldName() {
        return "comSession.successIndicator";
    }

    private int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(List<Device> devices, Range<Instant> range, Condition successIndicatorCondition) {
        return this.communicationTaskService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(devices, range, successIndicatorCondition);
    }

    @Override
    public G3NeighborhoodBuilder buildG3Neighborhood(Device device) {
        return new G3NeighborhoodBuilderImpl(device, this.findG3NeighborTableEntries(device));
    }

    private List<G3NeighborImpl> findG3NeighborTableEntries(Device device) {
        Condition condition = this.getEffectiveG3NeighborTableEntriesCondition(device);
        return this.dataModel.mapper(G3NeighborImpl.class).select(condition);
    }

    private Condition getEffectiveG3NeighborTableEntriesCondition(Device device) {
        return where("device").isEqualTo(device).and(where("interval").isEffective());
    }

    private List<G3NeighborImpl> findG3NeighborTableEntries(Device device, Instant instant) {
        Condition condition = this.getEffectiveG3NeighborTableEntriesCondition(device, instant);
        return this.dataModel.mapper(G3NeighborImpl.class).select(condition);
    }

    private Condition getEffectiveG3NeighborTableEntriesCondition(Device device, Instant instant) {
        return where("device").isEqualTo(device).and(where("interval").isEffective(instant));
    }

    @Override
    public List<Device> findDevicesInG3Neighborhood(Device device) {
        return this.collectDevicesFrom(this.findG3NeighborTableEntries(device));
    }

    @Override
    public List<Device> findDevicesInG3Neighborhood(Device device, Instant instant) {
        return this.collectDevicesFrom(this.findG3NeighborTableEntries(device, instant));
    }

    private List<Device> collectDevicesFrom(List<G3NeighborImpl> g3Neighbors) {
        return g3Neighbors
                .stream()
                .map(G3Neighbor::getNeighbor)
                .collect(Collectors.toList());
    }

    @Override
    public List<G3Neighbor> findG3Neighbors(Device device) {
        return this.safeCast(this.findG3NeighborTableEntries(device));
    }

    @Override
    public List<G3Neighbor> findG3Neighbors(Device device, Instant when) {
        return this.safeCast(this.findG3NeighborTableEntries(device, when));
    }

    private List<G3Neighbor> safeCast(List<G3NeighborImpl> tableEntries) {
        List<G3Neighbor> neighbors = new ArrayList<>(tableEntries.size());
        tableEntries.forEach(neighbors::add);
        return neighbors;
    }

    public G3NeighborImpl newG3Neighbor(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
        return this.dataModel.getInstance(G3NeighborImpl.class).createFor(device, neighbor, modulationScheme, modulation, phaseInfo, g3NodeState);
    }

    @Override
    public G3Neighbor reverseCloneG3Neighbor(G3Neighbor original) {
        return this.dataModel.getInstance(G3NeighborImpl.class).reverseClone(original);
    }

    private G3NeighborImpl newG3Neighbor(G3NeighborImpl existingNeighbor) {
        return newG3Neighbor(existingNeighbor.getDevice(), existingNeighbor.getNeighbor(), existingNeighbor.getModulationScheme(),
                existingNeighbor.getModulation(), existingNeighbor.getPhaseInfo(), existingNeighbor.getState());
    }

    @Override
    public Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device) {
        Condition condition = this.getEffectiveG3DeviceAddressCondition(device);
        return this.atMostOne(this.dataModel.mapper(G3DeviceAddressInformation.class).select(condition), device);
    }

    private Condition getEffectiveG3DeviceAddressCondition(Device device) {
        return where("device").isEqualTo(device).and(where("interval").isEffective());
    }

    @Override
    public Optional<G3DeviceAddressInformation> getG3DeviceAddressInformation(Device device, Instant when) {
        Condition condition = this.getEffectiveG3DeviceAddressCondition(device, when);
        return this.atMostOne(this.dataModel.mapper(G3DeviceAddressInformation.class).select(condition), device);
    }

    private Condition getEffectiveG3DeviceAddressCondition(Device device, Instant instant) {
        return where("device").isEqualTo(device).and(where("interval").isEffective(instant));
    }

    private Optional<G3DeviceAddressInformation> atMostOne(List<G3DeviceAddressInformation> addressInformations, Device device) {
        if (addressInformations.isEmpty()) {
            return Optional.empty();
        } else if (addressInformations.size() > 1) {
            throw new IllegalStateException("Expecting at most 1 effective G3DeviceAddressInformation entity for device with name " + device.getName());
        } else {
            return Optional.of(addressInformations.get(0));
        }
    }

    @Override
    public G3DeviceAddressInformation setG3DeviceAddressInformation(Device device, String ipv6Address, int ipv6ShortAddress, int logicalDeviceId) {
        Optional<G3DeviceAddressInformation> currentAddressInfo = this.getG3DeviceAddressInformation(device);
        if (currentAddressInfo.isPresent()) {
            G3DeviceAddressInformationImpl candidate = (G3DeviceAddressInformationImpl) currentAddressInfo.get();
            if (candidate.differentFrom(ipv6Address, ipv6ShortAddress, logicalDeviceId)) {
                G3DeviceAddressInformationImpl newAddressInformation = this.createG3DeviceAddressInformation(device, ipv6Address, ipv6ShortAddress, logicalDeviceId);
                candidate.terminate(newAddressInformation.getEffectiveStart());
                candidate.save();
                return newAddressInformation;
            } else {
                return candidate;
            }
        } else {
            return this.createG3DeviceAddressInformation(device, ipv6Address, ipv6ShortAddress, logicalDeviceId);
        }
    }

    private G3DeviceAddressInformationImpl createG3DeviceAddressInformation(Device device, String ipv6Address, int ipv6ShortAddress, int logicalDeviceId) {
        G3DeviceAddressInformationImpl newAddressInformation = this.dataModel.getInstance(G3DeviceAddressInformationImpl.class).createFrom(device, ipv6Address, ipv6ShortAddress, logicalDeviceId);
        newAddressInformation.save();
        return newAddressInformation;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(TopologyService.COMPONENT_NAME, "Device topology");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN);
        meteringChannelProvider = new MeteringChannelProvider(thesaurus);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }


    private interface FirstLevelTopologyTimeslicer {
        List<ServerTopologyTimeslice> firstLevelTopologyTimeslices(Device device, Range<Instant> period);
    }

    private class G3NeighborhoodBuilderImpl implements G3NeighborhoodBuilder {
        private final Device device;
        private final Map<Long, G3NeighborBuilderImpl> deviceId2NeighborBuilderMap;

        private G3NeighborhoodBuilderImpl(Device device, List<G3NeighborImpl> neighbors) {
            super();
            this.device = device;
            /* All existing neighbors are terminated by default unless
             * client code explicitly adds them again. */
            this.deviceId2NeighborBuilderMap =
                    neighbors
                            .stream()
                            .collect(Collectors.toMap(
                                    n -> n.getNeighbor().getId(),
                                    this::terminator));
        }

        private G3NeighborBuilderImpl creator(G3NeighborImpl neighborTableEntry) {
            return new G3NeighborBuilderImpl(neighborTableEntry, G3NeighborBuildState.CREATE);
        }

        private G3NeighborBuilderImpl terminator(G3NeighborImpl neighborTableEntry) {
            return new G3NeighborBuilderImpl(neighborTableEntry, G3NeighborBuildState.TERMINATE);
        }

        @Override
        public G3NeighborBuilder addNeighbor(Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            G3NeighborBuilderImpl builder = this.deviceId2NeighborBuilderMap
                    .computeIfAbsent(
                            neighbor.getId(),
                            id -> this.creator(newG3Neighbor(this.device, neighbor, modulationScheme, modulation, phaseInfo, g3NodeState)));
            builder.startEditing(modulationScheme, modulation, phaseInfo, g3NodeState);
            return builder;
        }

        @Deprecated
        public G3NeighborBuilder addNeighbor(Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            throw new UnsupportedOperationException("Deprecated method");
        }

        @Override
        public List<G3Neighbor> complete() {
            return this.deviceId2NeighborBuilderMap
                    .values()
                    .stream()
                    .map(G3NeighborBuilderImpl::complete)
                    .flatMap(Functions.asStream())
                    .collect(Collectors.toList());
        }

    }

    class G3NeighborBuilderImpl implements G3NeighborBuilder {
        private G3NeighborImpl neighborTableEntry;
        private Optional<G3NeighborImpl> oldNeighborTableEntry;
        private G3NeighborBuildState state;

        private G3NeighborBuilderImpl(G3NeighborImpl neighborTableEntry, G3NeighborBuildState state) {
            super();
            this.neighborTableEntry = neighborTableEntry;
            this.oldNeighborTableEntry = Optional.empty();
            this.state = state;
        }

        private void startEditing(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            this.state.startEditing(this, modulationScheme, modulation, phaseInfo, g3NodeState);
        }

        private boolean different(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            return !modulationScheme.equals(this.neighborTableEntry.getModulationScheme())
                    || !modulation.equals(this.neighborTableEntry.getModulation())
                    || !phaseInfo.equals(this.neighborTableEntry.getPhaseInfo())
                    || !g3NodeState.equals(this.neighborTableEntry.getState());
        }

        void prepareForUpdateOrTerminateOldAndStartNew(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            if (this.different(modulationScheme, modulation, phaseInfo, g3NodeState)) {
                this.terminateOldAndStartNew(modulationScheme, modulation, phaseInfo, g3NodeState);
            } else {
                this.prepareForUdate();
            }
        }

        private void terminateOldAndStartNew(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
            G3NeighborImpl newG3Neighbor = newG3Neighbor(this.neighborTableEntry.getDevice(), this.neighborTableEntry.getNeighbor(), modulationScheme, modulation, phaseInfo, g3NodeState);
            this.neighborTableEntry.terminate(newG3Neighbor.getEffectiveStart());
            this.oldNeighborTableEntry = Optional.of(this.neighborTableEntry);
            this.neighborTableEntry = newG3Neighbor;
            this.state = G3NeighborBuildState.CREATE;
            this.modulationScheme(modulationScheme);
            this.modulation(modulation);
            this.phaseInfo(phaseInfo);
            this.g3NodeState(g3NodeState);
        }

        void terminateOldAndStartNew() {
            G3NeighborImpl newG3Neighbor = newG3Neighbor(this.neighborTableEntry);
            this.neighborTableEntry.terminate(newG3Neighbor.getEffectiveStart());
            this.oldNeighborTableEntry = Optional.of(this.neighborTableEntry);
            this.neighborTableEntry = newG3Neighbor;
            this.state = G3NeighborBuildState.CREATE;
        }

        private void prepareForUdate() {
            this.state = G3NeighborBuildState.UPDATE;
        }

        private Optional<G3Neighbor> complete() {
            Optional<G3Neighbor> neighbor = this.state.complete(this.neighborTableEntry, this.oldNeighborTableEntry);
            this.state = G3NeighborBuildState.COMPLETE;
            return neighbor;
        }

        private G3NeighborBuilderImpl modulationScheme(ModulationScheme modulationScheme) {
            this.neighborTableEntry.setModulationScheme(modulationScheme);
            return this;
        }

        private G3NeighborBuilderImpl modulation(Modulation modulation) {
            this.neighborTableEntry.setModulation(modulation);
            return this;
        }

        private G3NeighborBuilderImpl phaseInfo(PhaseInfo phaseInfo) {
            this.neighborTableEntry.setPhaseInfo(phaseInfo);
            return this;
        }

        private G3NeighborBuilderImpl g3NodeState(G3NodeState g3NodeState) {
            this.neighborTableEntry.setState(g3NodeState);
            return this;
        }

        @Override
        public G3NeighborBuilder txGain(int txGain) {
            this.state.setTxGain(this, txGain);
            return this;
        }

        void setTxGain(int txGain) {
            this.neighborTableEntry.setTxGain(txGain);
        }

        @Override
        public G3NeighborBuilder txResolution(int txResolution) {
            this.state.setTxResolution(this, txResolution);
            return this;
        }

        void setTxResolution(int txResolution) {
            this.neighborTableEntry.setTxResolution(txResolution);
        }

        @Override
        public G3NeighborBuilder txCoefficient(int txCoefficient) {
            this.state.setTxCoefficient(this, txCoefficient);
            return this;
        }

        void setTxCoefficient(int txCoefficient) {
            this.neighborTableEntry.setTxCoefficient(txCoefficient);
        }

        @Override
        public G3NeighborBuilder linkQualityIndicator(int linkQualityIndicator) {
            this.state.setLinkQualityIndicator(this, linkQualityIndicator);
            return this;
        }

        void setLinkQualityIndicator(int linkQualityIndicator) {
            this.neighborTableEntry.setLinkQualityIndicator(linkQualityIndicator);
        }

        @Override
        public G3NeighborBuilder timeToLiveSeconds(int seconds) {
            this.state.setTimeToLiveFromSeconds(this, seconds);
            return this;
        }

        void setTimeToLiveFromSeconds(int seconds) {
            this.neighborTableEntry.setTimeToLiveFromSeconds(seconds);
        }

        @Override
        public G3NeighborBuilder toneMap(long toneMap) {
            this.state.setToneMap(this, toneMap);
            return this;
        }

        void setToneMap(long toneMap) {
            this.neighborTableEntry.setToneMap(toneMap);
        }

        @Override
        public G3NeighborBuilder toneMapTimeToLiveSeconds(int seconds) {
            this.state.setToneMapTimeToLiveSeconds(this, seconds);
            return this;
        }

        @Override
        public G3NeighborBuilder macPANId(long macPANId) {
            this.state.setMacPANId(this, macPANId);
            return this;
        }

        void setMacPANId(long macPANId) {
            this.neighborTableEntry.setMacPANId(macPANId);
        }

        @Override
        public G3NeighborBuilder nodeAddress(String nodeAddress) {
            this.state.setNodeAddress(this, nodeAddress);
            return this;
        }

        void setNodeAddress(String nodeAddress) {
            this.neighborTableEntry.setNodeAddress(nodeAddress);
        }

        @Override
        public G3NeighborBuilder shortAddress(int shortAddress) {
            this.state.setShortAddress(this, shortAddress);
            return this;
        }

        void setShortAddress(int shortAddress) {
            this.neighborTableEntry.setShortAddress(shortAddress);
        }

        @Override
        public G3NeighborBuilder lastUpdate(Instant lastUpdate) {
            this.state.setLastUpdate(this, lastUpdate);
            return this;
        }

        void setLastUpdate(Instant lastUpdate) {
            this.neighborTableEntry.setLastUpdate(lastUpdate);
        }

        @Override
        public G3NeighborBuilder lastPathRequest(Instant lastPathRequest) {
            this.state.setLastPathRequest(this, lastPathRequest);
            return this;
        }

        void setLastPathRequest(Instant lastPathRequest) {
            this.neighborTableEntry.setLastPathRequest(lastPathRequest);
        }

        @Override
        public G3NeighborBuilder roundTrip(long roundTrip) {
            this.state.setRoundTrip(this, roundTrip);
            return this;
        }

        void setRoundTrip(long roundTrip) {
            this.neighborTableEntry.setRoundTrip(roundTrip);
        }

        @Override
        public G3NeighborBuilder linkCost(int linkCost) {
            this.state.setLinkCost(this, linkCost);
            return this;
        }

        void setLinkCost(int linkCost) {
            this.neighborTableEntry.setLinkCost(linkCost);
        }

        void setToneMapTimeToLiveFromSeconds(int seconds) {
            this.neighborTableEntry.setToneMapTimeToLiveFromSeconds(seconds);
        }

    }

}
