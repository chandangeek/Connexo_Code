package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.G3DeviceAddressInformation;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link TopologyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:40)
 */
@Component(name = "com.energyict.mdc.device.topology", service = {TopologyService.class, ServerTopologyService.class, MessageSeedProvider.class}, property = "name=" + TopologyService.COMPONENT_NAME)
public class TopologyServiceImpl implements ServerTopologyService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile UpgradeService upgradeService;

    // For OSGi framework only
    public TopologyServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public TopologyServiceImpl(OrmService ormService, NlsService nlsService, Clock clock, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setClock(clock);
        setConnectionTaskService(connectionTaskService);
        setCommunicationTaskService(communicationTaskService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        upgradeService.register(InstallIdentifier.identifier(TopologyService.COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(TopologyService.class).toInstance(TopologyServiceImpl.this);
                bind(ServerTopologyService.class).toInstance(TopologyServiceImpl.this);
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
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTask);
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

    private Optional<PhysicalGatewayReference> getPhysicalGatewayReference(Device slave, Instant when) {
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

    @Override
    public Map<Device, Device> getPhycicalGateways(List<Device> deviceList) {
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
        this.getPhysicalGatewayReference(slave, now).ifPresent(r -> terminateTemporal(r, now));
        this.newPhysicalGatewayReference(slave, gateway, now);
        this.slaveTopologyChanged(slave, Optional.of(gateway));
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
        Optional<PhysicalGatewayReference> existingGatewayReference = this.getPhysicalGatewayReference(slave, linkingDate);
        if (existingGatewayReference.isPresent()) {
            throw DataLoggerLinkException.slaveWasAlreadyLinkedToOtherDatalogger(thesaurus, slave, existingGatewayReference.get().getGateway(), linkingDate);
        }
        validateUniqueKeyConstraintForDataloggerReference(dataLogger, linkingDate, slave);
        final DataLoggerReferenceImpl dataLoggerReference = this.newDataLoggerReference(slave, dataLogger, linkingDate);
        slaveDataLoggerChannelMap.forEach((slaveChannel, dataLoggerChannel) -> this.addChannelDataLoggerUsage(dataLoggerReference, slaveChannel, dataLoggerChannel));
        slaveDataLoggerRegisterMap.forEach((slaveRegister, dataLoggerRegister) -> this.addRegisterDataLoggerUsage(dataLoggerReference, slaveRegister, dataLoggerRegister));
        Save.CREATE.validate(this.dataModel, dataLoggerReference);
        dataLoggerReference.transferChannelDataToSlave(this);
        this.dataModel.persist(dataLoggerReference);
    }

    private void validateUniqueKeyConstraintForDataloggerReference(Device dataLogger, Instant linkingDate, Device slave) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(dataLogger)
                .and(where("interval.start").in(Range.closed(linkingDate.toEpochMilli(), linkingDate.toEpochMilli())));
        Optional<DataLoggerReference> duplicateReference = this.dataModel.mapper(DataLoggerReference.class).select(condition).stream().collect(Collectors.toList())
                .stream().filter(reference -> reference.getOrigin().getmRID().equals(slave.getmRID()) && reference.getRange().lowerEndpoint().equals(linkingDate)).findAny();
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
    public Optional<DataLoggerReference> findCurrentDataloggerReference(Device dataloggerSlaveDevice, Instant effective) {
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(dataloggerSlaveDevice).and(where("interval").isEffective(effective));
        return this.dataModel.mapper(DataLoggerReference.class).select(condition).stream().findAny(); // the business logic of the effectivity requires that there is only one object effective at a
        // given time
    }

    @Override
    public Finder<? extends DataLoggerReference> findAllEffectiveDataLoggerSlaveDevices() {
        return DefaultFinder.of(DataLoggerReferenceImpl.class, where("interval").isEffective(), dataModel());
    }

    @Override
    public Optional<Channel> getSlaveChannel(Channel dataLoggerChannel, Instant when) {
        return findDataLoggerChannelUsage(getMeteringChannel(dataLoggerChannel), when)
                .map((dataLoggerChannelUsage) -> getChannel(dataLoggerChannelUsage.getDataLoggerReference().getOrigin(), dataLoggerChannelUsage.getSlaveChannel()).get());
    }

    Optional<Channel> getChannel(Device device, com.elster.jupiter.metering.Channel channel) {
        ReadingType readingType = channel.getMainReadingType();
        return device.getChannels().stream().filter((mdcChannel) -> mdcChannel.getCalculatedReadingType(Instant.now()).orElse(mdcChannel.getReadingType()) == readingType).findFirst();
    }

    @Override
    public Optional<Register> getSlaveRegister(Register dataLoggerRegister, Instant when) {
        return findDataLoggerChannelUsage(getMeteringChannel(dataLoggerRegister), when)
                .map((dataLoggerChannelUsage) -> getRegister(dataLoggerChannelUsage.getDataLoggerReference().getOrigin(), dataLoggerChannelUsage.getSlaveChannel()).get());
    }

    Optional<Register> getRegister(Device device, com.elster.jupiter.metering.Channel channel) {
        ReadingType readingType = channel.getMainReadingType();
        return device.getRegisters().stream().filter((mdcChannel) -> mdcChannel.getCalculatedReadingType(Instant.now()).orElse(mdcChannel.getReadingType()) == readingType).findFirst();
    }

    @Override
    public boolean isReferenced(Channel dataLoggerChannel) {
        return this.isReferenced(getMeteringChannel(dataLoggerChannel));
    }

    @Override
    public boolean isReferenced(Register dataLoggerRegister) {
        return this.isReferenced(getMeteringChannel(dataLoggerRegister));
    }

    boolean isReferenced(com.elster.jupiter.metering.Channel dataLoggerChannel) {
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
    public List<DataLoggerChannelUsage> findDataLoggerChannelUsages(Channel dataLoggerChannel, Range<Instant> referencePeriod) {
        return findDataLoggerChannelUsages(getMeteringChannel(dataLoggerChannel), referencePeriod);
    }

    @Override
    public Optional<Instant> availabilityDate(Channel dataLoggerChannel) {
        return availabilityDate(getMeteringChannel(dataLoggerChannel), dataLoggerChannel.getDevice().getLifecycleDates().getReceivedDate());
    }

    @Override
    public Optional<Instant> availabilityDate(Register dataLoggerRegister) {
        return availabilityDate(getMeteringChannel(dataLoggerRegister), dataLoggerRegister.getDevice().getLifecycleDates().getReceivedDate());
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
        return dataModel.query(DataLoggerChannelUsage.class, PhysicalGatewayReference.class).select(gateway.and(effective));
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
        getPhysicalGatewayReference(slave, clock.instant()).map(dataloggerReference -> {
            terminateTemporal(dataloggerReference, when);
            this.slaveTopologyChanged(slave, Optional.empty());
            return true;
        }).orElseThrow(() -> DataLoggerLinkException.slaveWasNotLinkedAt(thesaurus, slave, when));
    }

    private void addChannelDataLoggerUsage(DataLoggerReferenceImpl dataLoggerReference, Channel slave, Channel dataLogger) {
        com.elster.jupiter.metering.Channel channelForSlave = getMeteringChannel(slave);
        com.elster.jupiter.metering.Channel channelForDataLogger = getMeteringChannel(dataLogger);
        dataLoggerReference.addDataLoggerChannelUsage(channelForSlave, channelForDataLogger);
    }

    private void addRegisterDataLoggerUsage(DataLoggerReferenceImpl dataLoggerReference, Register slave, Register dataLogger) {
        com.elster.jupiter.metering.Channel channelForSlave = getMeteringChannel(slave);
        com.elster.jupiter.metering.Channel channelForDataLogger = getMeteringChannel(dataLogger);
        dataLoggerReference.addDataLoggerChannelUsage(channelForSlave, channelForDataLogger);
    }

    com.elster.jupiter.metering.Channel getMeteringChannel(final com.energyict.mdc.device.data.Channel channel) {
        return channel.getDevice().getCurrentMeterActivation().get().getChannels().stream().filter((x) -> x.getReadingTypes().contains(channel.getReadingType()))
                .findFirst()
                .orElseThrow(() -> DataLoggerLinkException.noPhysicalChannelForReadingType(this.thesaurus, channel.getReadingType()));
    }

    com.elster.jupiter.metering.Channel getMeteringChannel(final Register register) {
        return register.getDevice().getCurrentMeterActivation().get().getChannels().stream().filter((x) -> x.getReadingTypes().contains(register.getReadingType()))
                .findFirst()
                .orElseThrow(() -> DataLoggerLinkException.noPhysicalChannelForReadingType(this.thesaurus, register.getReadingType()));
    }

    private void terminateTemporal(PhysicalGatewayReference gatewayReference, Instant now) {
        gatewayReference.terminate(now);
        this.dataModel.update(gatewayReference);
    }

    private void slaveTopologyChanged(Device slave, Optional<Device> gateway) {
        List<ComTaskExecution> comTasksForDefaultConnectionTask = this.communicationTaskService.findComTasksByDefaultConnectionTask(slave);
        if (gateway.isPresent()) {
            this.updateComTasksToUseNewDefaultConnectionTask(slave, comTasksForDefaultConnectionTask);
        } else {
            this.updateComTasksToUseNonExistingDefaultConnectionTask(comTasksForDefaultConnectionTask);
        }
    }

    private void updateComTasksToUseNewDefaultConnectionTask(Device slave, List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        this.findDefaultConnectionTaskForTopology(slave).ifPresent(dct -> {
            for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
                ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
                comTaskExecutionUpdater.useDefaultConnectionTask(dct);
                comTaskExecutionUpdater.update();
            }
        });
    }

    private void updateComTasksToUseNonExistingDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.connectionTask(null);
            comTaskExecutionUpdater.useDefaultConnectionTask(true);
            comTaskExecutionUpdater.update();
        }
    }

    @Override
    public void clearPhysicalGateway(Device slave) {
        clearPhysicalGateway(slave, this.clock.instant());
    }

    private void clearPhysicalGateway(Device slave, Instant when) {
        this.getPhysicalGatewayReference(slave, when).ifPresent(r -> terminateTemporal(r, when));
        this.slaveTopologyChanged(slave, Optional.empty());
    }

    @Override
    public G3CommunicationPath getCommunicationPath(Device source, Device target) {
        G3CommunicationPathImpl communicationPath = new G3CommunicationPathImpl(source, target);
        DataMapper<G3CommunicationPathSegment> mapper = this.dataModel.mapper(G3CommunicationPathSegment.class);
        SqlBuilder sqlBuilder = mapper.builder("cps");
        sqlBuilder.append("where cps.discriminator = ");
        sqlBuilder.addObject(CommunicationPathSegmentImpl.G3_DISCRIMINATOR);
        sqlBuilder.append("start with (cps.srcdevice = ");
        sqlBuilder.addLong(source.getId());
        sqlBuilder.append("and cps.targetdevice = ");
        sqlBuilder.addLong(target.getId());
        sqlBuilder.append(") connect by (cps.srcdevice = prior cps.nexthopdevice and cps.targetdevice = ");
        sqlBuilder.addLong(target.getId());
        sqlBuilder.append(")");
        mapper.fetcher(sqlBuilder).forEach(communicationPath::addSegment);
        return communicationPath;
    }

    @Override
    public G3CommunicationPathSegmentBuilder addCommunicationSegments(Device source) {
        return new G3CommunicationPathSegmentBuilderImpl(this, this.clock, source);
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
    public TopologyTimeline getPysicalTopologyTimeline(Device device) {
        return TopologyTimelineImpl.merge(this.findPhysicallyReferencingDevicesFor(device, Range.all()));
    }

    @Override
    public TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions) {
        return TopologyTimelineImpl.merge(this.findRecentPhysicallyReferencingDevicesFor(device, maximumNumberOfAdditions));
    }

    private List<ServerTopologyTimeslice> findRecentPhysicallyReferencingDevicesFor(Device device, int maxRecentCount) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        List<PhysicalGatewayReferenceImpl> gatewayReferences =
                this.dataModel
                        .stream(PhysicalGatewayReferenceImpl.class)
                        .filter(condition)
                        .sorted(Order.descending("interval.start"))
                        .limit(maxRecentCount)
                        .select();
        return this.toTopologyTimeslices(gatewayReferences);
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
        tableEntries.stream().forEach(neighbors::add);
        return neighbors;
    }

    private G3NeighborImpl newG3Neighbor(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
        return this.dataModel.getInstance(G3NeighborImpl.class).createFor(device, neighbor, modulationScheme, modulation, phaseInfo);
    }

    private G3NeighborImpl newG3Neighbor(G3NeighborImpl existingNeighbor) {
        return newG3Neighbor(existingNeighbor.getDevice(), existingNeighbor.getNeighbor(), existingNeighbor.getModulationScheme(), existingNeighbor.getModulation(), existingNeighbor.getPhaseInfo());
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
            throw new IllegalStateException("Expecting at most 1 effective G3DeviceAddressInformation entity for device with mRID " + device.getmRID());
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
        public G3NeighborBuilder addNeighbor(Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            G3NeighborBuilderImpl builder = this.deviceId2NeighborBuilderMap
                    .computeIfAbsent(
                            neighbor.getId(),
                            id -> this.creator(newG3Neighbor(this.device, neighbor, modulationScheme, modulation, phaseInfo)));
            builder.startEditing(modulationScheme, modulation, phaseInfo);
            return builder;
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

        private void startEditing(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            this.state.startEditing(this, modulationScheme, modulation, phaseInfo);
        }

        private boolean different(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            return !modulationScheme.equals(this.neighborTableEntry.getModulationScheme())
                    || !modulation.equals(this.neighborTableEntry.getModulation())
                    || !phaseInfo.equals(this.neighborTableEntry.getPhaseInfo());
        }

        void prepareForUpdateOrTerminateOldAndStartNew(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            if (this.different(modulationScheme, modulation, phaseInfo)) {
                this.terminateOldAndStartNew(modulationScheme, modulation, phaseInfo);
            } else {
                this.prepareForUdate();
            }
        }

        private void terminateOldAndStartNew(ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo) {
            G3NeighborImpl newG3Neighbor = newG3Neighbor(this.neighborTableEntry.getDevice(), this.neighborTableEntry.getNeighbor(), modulationScheme, modulation, phaseInfo);
            this.neighborTableEntry.terminate(newG3Neighbor.getEffectiveStart());
            this.oldNeighborTableEntry = Optional.of(this.neighborTableEntry);
            this.neighborTableEntry = newG3Neighbor;
            this.state = G3NeighborBuildState.CREATE;
            this.modulationScheme(modulationScheme);
            this.modulation(modulation);
            this.phaseInfo(phaseInfo);
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

        void setToneMapTimeToLiveFromSeconds(int seconds) {
            this.neighborTableEntry.setToneMapTimeToLiveFromSeconds(seconds);
        }

    }

}