package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.domain.util.Save;
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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.*;

/**
 * Provides an implementation for the {@link TopologyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:40)
 */
@Component(name="com.energyict.mdc.device.topology", service = {TopologyService.class, ServerTopologyService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + TopologyService.COMPONENT_NAME)
public class TopologyServiceImpl implements ServerTopologyService, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile EventService eventService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;

    // For OSGi framework only
    public TopologyServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public TopologyServiceImpl(OrmService ormService, NlsService nlsService, Clock clock, EventService eventService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setClock(clock);
        this.setEventService(eventService);
        this.setConnectionTaskService(connectionTaskService);
        this.setCommunicationTaskService(communicationTaskService);
        this.activate();
        this.install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(DeviceDataServices.COMPONENT_NAME);
    }

    @Override
    public String getComponentName() {
        return TopologyService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(TopologyService.class).toInstance(TopologyServiceImpl.this);
                bind(ServerTopologyService.class).toInstance(TopologyServiceImpl.this);
            }
        };
    }

    @Override
    public void install() {
        new Installer(this.dataModel).install(true);
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
        }
        else {
            /* No default ConnectionTask on the device,
             * let's try the physical gateway if there is one. */
            Optional<Device> physicalGateway = this.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                return this.findDefaultConnectionTaskForTopology(physicalGateway.get());
            }
            else {
                return Optional.empty();
            }
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
        List<PhysicalGatewayReference> physicalGatewayReferences = this.dataModel.mapper(PhysicalGatewayReference.class).select(condition);
        return this.findUniqueReferencingDevices(physicalGatewayReferences);
    }

    private Condition getDevicesInTopologyCondition(Device device) {
        return where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(device).and(where("interval").isEffective());
    }

    private List<Device> findUniqueReferencingDevices (List<PhysicalGatewayReference> gatewayReferences) {
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
        }
        else if (allEffective.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(allEffective.get(0));
        }
    }

    @Override
    public void setPhysicalGateway(Device slave, Device gateway) {
        Instant now = this.clock.instant();
        this.getPhysicalGatewayReference(slave, now).ifPresent(r -> terminateTemporal(r, now));
        PhysicalGatewayReferenceImpl physicalGatewayReference =
                this.dataModel.getInstance(PhysicalGatewayReferenceImpl.class).createFor(Interval.startAt(now), gateway, slave);
        Save.CREATE.validate(this.dataModel, physicalGatewayReference);
        this.dataModel.persist(physicalGatewayReference);
        this.slaveTopologyChanged(slave, Optional.of(gateway));
    }

    private void terminateTemporal(PhysicalGatewayReference gatewayReference, Instant now) {
        gatewayReference.terminate(now);
        this.dataModel.update(gatewayReference);
    }

    private void slaveTopologyChanged(Device slave, Optional<Device> gateway) {
        List<ComTaskExecution> comTasksForDefaultConnectionTask = this.communicationTaskService.findComTasksByDefaultConnectionTask(slave);
        if (gateway.isPresent()) {
            this.updateComTasksToUseNewDefaultConnectionTask(slave, comTasksForDefaultConnectionTask);
        }
        else {
            this.updateComTasksToUseNonExistingDefaultConnectionTask(comTasksForDefaultConnectionTask);
        }
    }

    private void updateComTasksToUseNewDefaultConnectionTask(Device slave, List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        this.findDefaultConnectionTaskForTopology(slave).ifPresent(dct -> {
            for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
                ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
                comTaskExecutionUpdater.useDefaultConnectionTask(dct);
                comTaskExecutionUpdater.update();
            }});
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
        Instant now = this.clock.instant();
        this.getPhysicalGatewayReference(slave, now).ifPresent(r -> terminateTemporal(r, now));
        this.slaveTopologyChanged(slave, Optional.empty());
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
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

}