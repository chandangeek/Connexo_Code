package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.mail.MailService;
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
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;

@Component(name = "com.elster.jupiter.export", service = {DataExportService.class, IDataExportService.class, InstallService.class,PrivilegesProvider.class}, property = "name=" + DataExportService.COMPONENTNAME, immediate = true)
public class DataExportServiceImpl implements IDataExportService, InstallService,PrivilegesProvider {

    public static final String DESTINATION_NAME = "DataExport";
    public static final String SUBSCRIBER_NAME = "DataExport";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle data export";
    public static final String MODULE_DESCRIPTION = "Data Export";
    public static final String JAVA_TEMP_DIR_PROPERTY = "java.io.tmpdir";
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
    private volatile MailService mailService;
    private volatile FileSystem fileSystem;
    private volatile Path tempDirectory;

    private Map<DataFormatterFactory, List<String>> dataFormatterFactories = new ConcurrentHashMap<>();
    private Map<DataSelectorFactory, String> dataSelectorFactories = new ConcurrentHashMap<>();
    private Optional<DestinationSpec> destinationSpec = Optional.empty();
    private QueryService queryService;

    public DataExportServiceImpl() {
    }

    @Inject
    public DataExportServiceImpl(OrmService ormService, TimeService timeService, TaskService taskService, MeteringGroupsService meteringGroupsService, MessageService messageService, NlsService nlsService, MeteringService meteringService, QueryService queryService, Clock clock, UserService userService, AppService appService, TransactionService transactionService, PropertySpecService propertySpecService, MailService mailService, BundleContext context, FileSystem fileSystem) {
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
        setMailService(mailService);
        setPropertySpecService(propertySpecService);
        setFileSystem(fileSystem);
        activate(context);
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public Optional<DataFormatterFactory> getDataFormatterFactory(String name) {
        return dataFormatterFactories.keySet().stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public List<DataFormatterFactory> getAvailableFormatters() {
        ArrayList<DataFormatterFactory> dataFormatterFactories = new ArrayList<>(this.dataFormatterFactories.keySet());
        dataFormatterFactories.sort(Comparator.comparing(HasName::getName));
        return dataFormatterFactories;
    }

    @Override
    public List<DataSelectorFactory> getAvailableSelectors() {
        ArrayList<DataSelectorFactory> dataSelectorfactories = new ArrayList<>(this.dataSelectorFactories.keySet());
        dataSelectorfactories.sort(Comparator.comparing(HasName::getName));
        return dataSelectorfactories;
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
    public List<PropertySpec> getPropertiesSpecsForFormatter(String name) {
        return getDataFormatterFactory(name)
                .map(DataFormatterFactory::getPropertySpecs)
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

    @Override
    public StructureMarker forRoot(String root) {
        return DefaultStructureMarker.createRoot(clock, root);
    }

    @Override
    public Path getTempDirectory() {
        return tempDirectory;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, MODULE_DESCRIPTION);
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFormatter(DataFormatterFactory dataFormatterFactory, Map<String, Object> map) {
        Object value = map.get(DATA_TYPE_PROPERTY);
        List<String> dataTypes;
        if (value instanceof String) {
            String dataType = (String) value;
            dataTypes = new ArrayList<>();
            dataTypes.add(dataType);
        } else if (value instanceof String[]) {
            dataTypes = Arrays.asList((String[]) value);
        } else {
            dataTypes = Collections.emptyList();
        }
        dataFormatterFactories.put(dataFormatterFactory, dataTypes);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSelector(DataSelectorFactory dataSelectorFactory, Map<String, Object> map) {
        String dataType = (String) map.get(DATA_TYPE_PROPERTY);
        dataSelectorFactories.put(dataSelectorFactory, dataType);
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

    public void removeFormatter(DataFormatterFactory dataFormatterFactory) {
        dataFormatterFactories.remove(dataFormatterFactory);
    }

    public void removeSelector(DataSelectorFactory selectorFactory) {
        dataSelectorFactories.remove(selectorFactory);
    }

    @Activate
    public final void activate(BundleContext context) {
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
                    bind(PropertySpecService.class).toInstance(propertySpecService);
                    bind(AppService.class).toInstance(appService);
                    bind(DataExportService.class).toInstance(DataExportServiceImpl.this);
                    bind(FileSystem.class).toInstance(FileSystems.getDefault());
                    bind(MailService.class).toInstance(mailService);
                    bind(FileSystem.class).toInstance(fileSystem);
                }
            });
            addSelector(new StandardDataSelectorFactory(transactionService, meteringService, thesaurus), ImmutableMap.of(DATA_TYPE_PROPERTY, STANDARD_DATA_TYPE));
//            addSelector(new SingleDeviceDataSelectorFactory(transactionService, meteringService, thesaurus, propertySpecService, timeService));
            String tempDirectoryPath = context.getProperty(JAVA_TEMP_DIR_PROPERTY);
            if (tempDirectoryPath == null) {
                tempDirectory = fileSystem.getRootDirectories().iterator().next();
            } else {
                tempDirectory = fileSystem.getPath(tempDirectoryPath);
            }
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

    @Reference
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
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
        return dataSelectorFactories.keySet().stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public LocalFileWriter getLocalFileWriter() {
        return new LocalFileWriter(this);
    }

    @Override
    public List<DataFormatterFactory> formatterFactoriesMatching(DataSelectorFactory selectorFactory) {
        String dataType = dataSelectorFactories.get(selectorFactory);
        return dataFormatterFactories.entrySet().stream()
                .filter(entry -> entry.getValue().contains(dataType))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Optional<IExportTask> getReadingTypeDataExportTaskForRecurrentTask(RecurrentTask recurrentTask) {
        return dataModel.mapper(IExportTask.class).getUnique("recurrentTask", recurrentTask);
    }

    @Override
    public String getModuleName() {
        return DataExportService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                "dataExportTask.dataExportTasks", "dataExportTask.dataExportTasks.description",
                Arrays.asList(Privileges.ADMINISTRATE_DATA_EXPORT_TASK,
                        Privileges.VIEW_DATA_EXPORT_TASK,
                        Privileges.UPDATE_DATA_EXPORT_TASK,
                        Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
                        Privileges.RUN_DATA_EXPORT_TASK)));
        return resources;
    }
}
