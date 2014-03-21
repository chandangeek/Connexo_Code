package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskExecutionFactory;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceDataService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
@Component(name="com.energyict.mdc.device.data", service = {DeviceDataService.class, InstallService.class}, property = "name=" + DeviceDataService.COMPONENTNAME)
public class DeviceDataServiceImpl implements DeviceDataService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    private volatile Clock clock;
    private volatile TransactionService transactionService;
    private volatile RelationService relationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MeteringService meteringService;

    @Inject
    public DeviceDataServiceImpl(OrmService ormService, TransactionService transactionService, EventService eventService, NlsService nlsService, Clock clock, Environment environment, RelationService relationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService) {
        this(ormService, transactionService, eventService, nlsService, clock, environment, relationService, protocolPluggableService, deviceConfigurationService, meteringService, false);
;    }

    public DeviceDataServiceImpl(OrmService ormService, TransactionService transactionService, EventService eventService, NlsService nlsService, Clock clock, Environment environment, RelationService relationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, boolean createMasterData) {
        super();
        this.setOrmService(ormService);
        this.setTransactionService(transactionService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setEnvironment(environment);
        this.setRelationService(relationService);
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true, createMasterData);
        }
    }

    @Override
    public InboundConnectionTask newInboundConnectionTask(Device device, PartialInboundConnectionTask partialConnectionTask, InboundComPortPool comPortPool) {
        InboundConnectionTaskImpl connectionTask = this.dataModel.getInstance(InboundConnectionTaskImpl.class);
        connectionTask.initialize(device, partialConnectionTask, comPortPool);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newAsapConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool) {
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithAsapStrategy(device, partialConnectionTask, comPortPool);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newMinimizeConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, TemporalExpression temporalExpression) {
        NextExecutionSpecs nextExecutionSpecs = this.deviceConfigurationService.newNextExecutionSpecs(temporalExpression);
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithMinimizeStrategy(device, partialConnectionTask, comPortPool, nextExecutionSpecs);
        return connectionTask;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return this.getDataModel().mapper(ConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<InboundConnectionTask> findInboundConnectionTask(long id) {
        return this.getDataModel().mapper(InboundConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id) {
        return this.getDataModel().mapper(ScheduledConnectionTask.class).getUnique("id", id);
    }

    @Override
    public ConnectionTask findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device) {
        return this.getDataModel().mapper(ConnectionTask.class).getUnique("deviceId", device.getId(), "partialConnectionTaskId", partialConnectionTask.getId()).orNull();
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        return this.getDataModel().mapper(InboundConnectionTask.class).find("deviceId", device.getId());
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        this.doSetDefaultConnectionTask(device, null);
    }

    @Override
    public void setDefaultConnectionTask(ConnectionTask newDefaultConnectionTask) {
        this.doSetDefaultConnectionTask(newDefaultConnectionTask.getDevice(), (ConnectionTaskImpl) newDefaultConnectionTask);
    }

    public void doSetDefaultConnectionTask(final Device device, final ConnectionTaskImpl newDefaultConnectionTask) {
        this.transactionService.execute(new Transaction<Object>() {
            @Override
            public Object perform() {
                List<ConnectionTask> connectionTasks = getDataModel().mapper(ConnectionTask.class).find("device", device);
                for (ConnectionTask connectionTask : connectionTasks) {
                    if (isPreviousDefault(newDefaultConnectionTask, connectionTask)) {
                        ((ConnectionTaskImpl) connectionTask).clearDefault();
                    }
                }
                if (newDefaultConnectionTask != null) {
                    newDefaultConnectionTask.setAsDefault();
                }
                defaultConnectionTaskChanged(device, newDefaultConnectionTask);
                return null;
            }
        });
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return connectionTask.isDefault()
            && (   (newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    public void defaultConnectionTaskChanged(Device device, ConnectionTask connectionTask) {
        List<ComTaskExecution> comTaskExecutions = this.findComTaskWithDefaultConnectionTaskForCompleteTopology(device);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comTaskExecution.updateToUseDefaultConnectionTask(connectionTask);
        }
    }

    /**
     * Constructs a list of {@link ComTaskExecution} which are linked to
     * the default {@link ConnectionTask} for the entire topology of the specified Device.
     *
     * @param device the Device for which we need to search the ComTaskExecution
     * @return The List of ComTaskExecution
     */
    private List<ComTaskExecution> findComTaskWithDefaultConnectionTaskForCompleteTopology(Device device) {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(device, scheduledComTasks);
        return scheduledComTasks;
    }

    private void collectComTaskWithDefaultConnectionTaskForCompleteTopology(Device device, List<ComTaskExecution> scheduledComTasks) {
        List<ComTaskExecutionFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(ComTaskExecutionFactory.class);
        for (ComTaskExecutionFactory factory : factories) {
            scheduledComTasks.addAll(factory.findComTaskExecutionsForDefaultOutboundConnectionTask(device));
        }
        Iterator downstreamDevices = device.getDownstreamDevices().iterator();
        while (downstreamDevices.hasNext()) {
            Device slave = (Device) downstreamDevices.next();
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks);
        }
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.getDataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComServer() == null) {
                ((ConnectionTaskImpl) lockedConnectionTask).setExecutingComServer(comServer);
                lockedConnectionTask.save();
                return lockedConnectionTask;
            }
            else {
                // No database lock but business lock is already set
                return null;
            }
        }
        else {
            // ConnectionTask no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public void unlockConnectionTask(ConnectionTask connectionTask) {
        this.unlockConnectionTask((ConnectionTaskImpl) connectionTask);
    }
     private void unlockConnectionTask (ConnectionTaskImpl connectionTask) {
        connectionTask.setExecutingComServer(null);
        connectionTask.save();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Device data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEnvironment (Environment environment) {
        environment.registerFinder(new ConnectionMethodFinder());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceDataService.class).toInstance(DeviceDataServiceImpl.this);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(false, true);
    }

    private void install(boolean exeuteDdl, boolean createMasterData) {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(exeuteDdl, createMasterData);
    }

    private class ConnectionMethodFinder implements CanFindByLongPrimaryKey<ConnectionMethod> {
        @Override
        public FactoryIds registrationKey() {
            return FactoryIds.CONNECTION_METHOD;
        }

        @Override
        public Class<ConnectionMethod> valueDomain() {
            return ConnectionMethod.class;
        }

        @Override
        public Optional<ConnectionMethod> findByPrimaryKey(long id) {
            return dataModel.mapper(this.valueDomain()).getUnique("id", id);
        }

    }

}