package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;

@Component(name = "com.elster.jupiter.export", service = {DataExportService.class, IDataExportService.class, InstallService.class}, property = "name=" + DataExportService.COMPONENTNAME, immediate = true)
public class DataExportServiceImpl implements IDataExportService, InstallService {

    public static final String DESTINATION_NAME = "DataExport";
    public static final String SUBSCRIBER_NAME = "DataExport";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle data export";
    private volatile DataModel dataModel;
    private volatile TimeService timeService;
    private volatile TaskService taskService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile PropertySpecService propertySpecService;

    private List<DataProcessorFactory> dataProcessorFactories = new CopyOnWriteArrayList<>();
    private List<DataSelectorFactory> dataSelectorFactories = new CopyOnWriteArrayList<>();
    private Optional<DestinationSpec> destinationSpec = Optional.empty();
    private QueryService queryService;

    public DataExportServiceImpl() {
    }

    @Inject
    public DataExportServiceImpl(OrmService ormService, TimeService timeService, TaskService taskService, MeteringGroupsService meteringGroupsService, MessageService messageService, NlsService nlsService, MeteringService meteringService, QueryService queryService, Clock clock, UserService userService, AppService appService, TransactionService transactionService, PropertySpecService propertySpecService) {
        setOrmService(ormService);
        setTimeService(timeService);
        setTaskService(taskService);
        setMeteringGroupsService(meteringGroupsService);
        setMessageService(messageService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setQueryService(queryService);
        setClock(clock);
        setUserService(userService);
        setAppService(appService);
        setTransactionService(transactionService);
        setPropertySpecService(propertySpecService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public Optional<DataProcessorFactory> getDataFormatterFactory(String name) {
        return dataProcessorFactories.stream()
                .filter(f -> name.equals(f.getName()))
                .findFirst();
    }

    @Override
    public List<DataProcessorFactory> getAvailableProcessors() {
        return Collections.unmodifiableList(dataProcessorFactories);
    }

    @Override
    public List<DataSelectorFactory> getAvailableSelectors() {
        return Collections.unmodifiableList(dataSelectorFactories);
    }

    @Override
    public DataExportTaskBuilder newBuilder() {
        return new DataExportTaskBuilderImpl(dataModel);
    }

    @Override
    public Optional<? extends ExportTask> findExportTask(long id) {
        return dataModel.mapper(IExportTask.class).getOptional(id);
    }

    @Override
    public Query<? extends ExportTask> getReadingTypeDataExportTaskQuery() {
        return queryService.wrap(dataModel.query(IExportTask.class));
    }

    @Override
    public List<PropertySpec> getPropertiesSpecsForProcessor(String name) {
        return getDataProcessorFactory(name)
                .map(DataProcessorFactory::getPropertySpecs)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<PropertySpec> getPropertiesSpecsForDataSelector(String name) {
        return getDataSelectorFactory(name)
                .map(DataSelectorFactory::getPropertySpecs)
                .orElse(Collections.emptyList());
    }

    @Override
    public DestinationSpec getDestination() {
        if (!destinationSpec.isPresent()) {
            destinationSpec = messageService.getDestinationSpec(DESTINATION_NAME);
        }
        return destinationSpec.orElse(null);
    }

    @Override
    public Optional<DataProcessorFactory> getDataProcessorFactory(String name) {
        return dataProcessorFactories.stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public void install() {
        Installer installer = new Installer(dataModel, messageService, timeService, thesaurus, userService);
        installer.install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, TimeService.COMPONENT_NAME, MeteringService.COMPONENTNAME, TaskService.COMPONENTNAME, MeteringGroupsService.COMPONENTNAME, MessageService.COMPONENTNAME, NlsService.COMPONENTNAME, AppService.COMPONENT_NAME);
    }

    @Override
    public List<IExportTask> findReadingTypeDataExportTasks() {
        return dataModel.mapper(IExportTask.class).find();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Data Export");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addProcessor(DataProcessorFactory dataProcessorFactory) {
        dataProcessorFactories.add(dataProcessorFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSelector(DataSelectorFactory dataSelectorFactory) {
        dataSelectorFactories.add(dataSelectorFactory);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void removeProcessor(DataProcessorFactory dataProcessorFactory) {
        dataProcessorFactories.remove(dataProcessorFactory);
    }

    public void removeSelector(DataSelectorFactory selectorFactory) {
        dataSelectorFactories.remove(selectorFactory);
    }

    @Activate
    public final void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(IDataExportService.class).toInstance(DataExportServiceImpl.this);
                    bind(TaskService.class).toInstance(taskService);
                    bind(MeteringService.class).toInstance(meteringService);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(Clock.class).toInstance(clock);
                    bind(UserService.class).toInstance(userService);
                    bind(TransactionService.class).toInstance(transactionService);
                }
            });
            addSelector(new StandardDataSelectorFactory(transactionService, meteringService, thesaurus));
            addSelector(new SingleDeviceDataSelectorFactory(transactionService, meteringService, thesaurus, propertySpecService, timeService));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public final void deactivate() {
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Override
    public IDataExportOccurrence createExportOccurrence(TaskOccurrence taskOccurrence) {
        IExportTask task = getReadingTypeDataExportTaskForRecurrentTask(taskOccurrence.getRecurrentTask()).orElseThrow(IllegalArgumentException::new);
        return DataExportOccurrenceImpl.from(dataModel, taskOccurrence, task);
    }

    @Override
    public Optional<IDataExportOccurrence> findDataExportOccurrence(TaskOccurrence occurrence) {
        return dataModel.query(IDataExportOccurrence.class, IExportTask.class).select(EQUAL.compare("taskOccurrence", occurrence)).stream().findFirst();
    }

    @Override
    public Optional<IDataExportOccurrence> findDataExportOccurrence(ExportTask task, Instant triggerTime) {
        return dataModel.stream(IDataExportOccurrence.class).join(TaskOccurrence.class).join(IExportTask.class)
                .filter(EQUAL.compare("readingTask", task))
                .filter(EQUAL.compare("taskOccurrence.triggerTime", triggerTime))
                .findFirst();
    }

    @Override
    public void setExportDirectory(AppServer appServer, Path path) {
        DirectoryForAppServer directoryForAppServer = dataModel.mapper(DirectoryForAppServer.class).getOptional(appServer.getName()).orElseGet(() -> DirectoryForAppServer.from(dataModel, appServer));
        directoryForAppServer.setPath(path);
        directoryForAppServer.save();
    }

    @Override
    public void removeExportDirectory(AppServer appServer) {
        Optional<DirectoryForAppServer> appServerRef = dataModel.mapper(DirectoryForAppServer.class).getOptional(appServer.getName());
        appServerRef.ifPresent(as -> dataModel.remove(as));
    }

    @Override
    public Optional<Path> getExportDirectory(AppServer appServer) {
        return dataModel.mapper(DirectoryForAppServer.class).getOptional(appServer.getName()).flatMap(DirectoryForAppServer::getPath);
    }

    @Override
    public Map<AppServer, Path> getAllExportDirecties() {
        return dataModel.mapper(DirectoryForAppServer.class).find().stream()
                .filter(dfa -> dfa.getPath().isPresent())
                .collect(Collectors.toMap(DirectoryForAppServer::getAppServer, dfa -> dfa.getPath().get()));
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public List<ExportTask> findExportTaskUsing(RelativePeriod relativePeriod) {
        return dataModel.stream(IExportTask.class)
                .filter(EQUAL.compare("exportPeriod", relativePeriod).or(EQUAL.compare("updatePeriod", relativePeriod)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DataSelectorFactory> getDataSelectorFactory(String name) {
        return dataSelectorFactories.stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    private Optional<IExportTask> getReadingTypeDataExportTaskForRecurrentTask(RecurrentTask recurrentTask) {
        return dataModel.mapper(IExportTask.class).getUnique("recurrentTask", recurrentTask);
    }
}
