/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.impl.webservicecall.DataExportServiceCallTypeImpl;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportChildCustomPropertySet;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportChildDomainExtension;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportCustomPropertySet;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportDomainExtension;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_4SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_3SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.PathVerification;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Operator.EQUAL;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.export",
        service = {DataExportService.class, IDataExportService.class, TranslationKeyProvider.class, MessageSeedProvider.class, RelativePeriodCategoryTranslationProvider.class},
        property = "name=" + DataExportService.COMPONENTNAME,
        immediate = true)
public class DataExportServiceImpl implements IDataExportService, TranslationKeyProvider, MessageSeedProvider, RelativePeriodCategoryTranslationProvider {
    static final String DESTINATION_NAME = "DataExport";
    static final String SUBSCRIBER_NAME = "DataExport";
    static final String SUBSCRIBER_DISPLAY_NAME = "Handle data export";
    private static final String MODULE_DESCRIPTION = "Data Export";
    private static final String JAVA_TEMP_DIR_PROPERTY = "java.io.tmpdir";
    private static final String COMBINE_CREATED_UPDATED_DATA_PROPERTY = "export.webservice.сombineсreatedupdateddata";
    private static final String COMBINE_CREATED_UPDATED_DATA_DEFAULT = "true";
    private static final Map<String, String[]> ALL_DATA_TYPES_MAP = ImmutableMap.of(DATA_TYPE_PROPERTY,
            new String[]{STANDARD_EVENT_DATA_TYPE, STANDARD_READING_DATA_TYPE, STANDARD_USAGE_POINT_DATA_TYPE});

    private volatile DataModel dataModel;
    private volatile TimeService timeService;
    private volatile TaskService taskService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile MessageService messageService;
    private volatile BundleContext bundleContext;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile PropertySpecService propertySpecService;
    private volatile MailService mailService;
    private volatile FileSystem fileSystem;
    private volatile Path tempDirectory;
    private volatile ValidationService validationService;
    private volatile DataVaultService dataVaultService;
    private volatile UpgradeService upgradeService;
    private volatile FtpClientService ftpClientService;
    private volatile QueryService queryService;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile OrmService ormService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private Map<DataFormatterFactory, List<String>> dataFormatterFactories = new ConcurrentHashMap<>();

    private Map<DataSelectorFactory, String> dataSelectorFactories = new ConcurrentHashMap<>();
    private Optional<DestinationSpec> destinationSpec = Optional.empty();
    private Map<String, DataExportWebService> exportWebServices = new ConcurrentHashMap<>();
    private boolean combineCreatedAndUpdatedDataInOneWebRequest;
    private CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> serviceCallCPS;
    private CustomPropertySet<ServiceCall, WebServiceDataExportChildDomainExtension> childServiceCallCPS;

    public DataExportServiceImpl() {
    }

    @Inject
    public DataExportServiceImpl(OrmService ormService, TimeService timeService, TaskService taskService,
                                 MeteringGroupsService meteringGroupsService, MessageService messageService,
                                 NlsService nlsService, MeteringService meteringService, QueryService queryService,
                                 Clock clock, UserService userService, AppService appService, TransactionService transactionService,
                                 PropertySpecService propertySpecService, MailService mailService, BundleContext context,
                                 FileSystem fileSystem, ValidationService validationService, DataVaultService dataVaultService,
                                 FtpClientService ftpClientService, UpgradeService upgradeService, ServiceCallService serviceCallService,
                                 CustomPropertySetService customPropertySetService, ThreadPrincipalService threadPrincipalService) {
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
        setValidationService(validationService);
        setDataVaultService(dataVaultService);
        setFtpClientService(ftpClientService);
        setUpgradeService(upgradeService);
        setServiceCallService(serviceCallService);
        setCustomPropertySetService(customPropertySetService);
        setThreadPrincipalService(threadPrincipalService);
        activate(context);
    }

    @Override
    public Optional<DataFormatterFactory> getDataFormatterFactory(String name) {
        return dataFormatterFactories.keySet().stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public List<DataFormatterFactory> getAvailableFormatters() {
        List<DataFormatterFactory> dataFormatterFactories = new ArrayList<>(this.dataFormatterFactories.keySet());
        dataFormatterFactories.sort(Comparator.comparing(HasName::getName));
        return dataFormatterFactories;
    }

    @Override
    public List<DataSelectorFactory> getAvailableSelectors() {
        List<DataSelectorFactory> dataSelectorFactories = new ArrayList<>(this.dataSelectorFactories.keySet());
        dataSelectorFactories.sort(Comparator.comparing(HasName::getName));
        return dataSelectorFactories;
    }

    @Override
    public DataExportTaskBuilder newBuilder() {
        return new DataExportTaskBuilderImpl(dataModel);
    }

    @Override
    public ExportTaskFinder findExportTasks() {
        Order order = Order.descending("lastRun").nullsLast();
        return new ExportTaskFinderImpl(dataModel, order);
    }

    @Override
    public Optional<? extends ExportTask> findExportTask(long id) {
        return dataModel.mapper(IExportTask.class).getOptional(id);
    }

    @Override
    public Optional<? extends ExportTask> findExportTaskByRecurrentTask(long id) {
        Query<IExportTask> query =
                queryService.wrap(dataModel.query(IExportTask.class, RecurrentTask.class));
        Condition condition = where("recurrentTask.id").isEqualTo(id);
        return query.select(condition).stream().findFirst();
    }

    @Override
    public Optional<? extends ExportTask> findAndLockExportTask(long id, long version) {
        return dataModel.mapper(IExportTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<? extends ExportTask> findAndLockReadingTypeDataExportTaskByName(String name) {
        Optional<? extends ExportTask> exportTask = getReadingTypeDataExportTaskByName(name);
        return Optional.ofNullable(exportTask.map(et -> dataModel.mapper(IExportTask.class).lock(et.getId())).orElse(null));
    }

    @Override
    public Optional<? extends ExportTask> getReadingTypeDataExportTaskByName(String name) {
        Query<IExportTask> query =
                queryService.wrap(dataModel.query(IExportTask.class, RecurrentTask.class));
        Condition condition = where("recurrentTask.name").isEqualTo(name);
        return query.select(condition).stream().findFirst();
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
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENTNAME, MODULE_DESCRIPTION);
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFormatter(DataFormatterFactory dataFormatterFactory, Map<String, ?> map) {
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

    public void removeFormatter(DataFormatterFactory dataFormatterFactory) {
        dataFormatterFactories.remove(dataFormatterFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSelector(DataSelectorFactory dataSelectorFactory, Map<String, ?> map) {
        String dataType = (String) map.get(DATA_TYPE_PROPERTY);
        dataSelectorFactories.put(dataSelectorFactory, dataType);
    }

    public void removeSelector(DataSelectorFactory selectorFactory) {
        dataSelectorFactories.remove(selectorFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addExportWebService(DataExportWebService webService) {
        exportWebServices.put(webService.getName(), webService);
    }

    public void removeExportWebService(DataExportWebService webService) {
        exportWebServices.remove(webService.getName());
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

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setFtpClientService(FtpClientService ftpClientService) {
        this.ftpClientService = ftpClientService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Activate
    public final void activate(BundleContext context) {
        this.bundleContext = context;
        serviceCallCPS = new WebServiceDataExportCustomPropertySet(thesaurus, propertySpecService, this);
        childServiceCallCPS = new WebServiceDataExportChildCustomPropertySet(thesaurus, propertySpecService);
        customPropertySetService.addCustomPropertySet(serviceCallCPS);
        customPropertySetService.addCustomPropertySet(childServiceCallCPS);
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
                    bind(ValidationService.class).toInstance(validationService);
                    bind(DataVaultService.class).toInstance(dataVaultService);
                    bind(FtpClientService.class).toInstance(ftpClientService);
                    bind(TimeService.class).toInstance(timeService);
                    bind(MessageService.class).toInstance(messageService);
                    bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                    bind(ServiceCallService.class).toInstance(serviceCallService);
                    bind(NlsService.class).toInstance(nlsService);
                    bind(UpgradeService.class).toInstance(upgradeService);
                    bind(BundleContext.class).toInstance(bundleContext);
                    bind(QueryService.class).toInstance(queryService);
                    bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                    bind(DataModel.class).toInstance(dataModel);
                    bind(OrmService.class).toInstance(ormService);
                    bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                }
            });
            addSelector(new StandardDataSelectorFactory(thesaurus), ImmutableMap.of(DATA_TYPE_PROPERTY, STANDARD_READING_DATA_TYPE));
            addSelector(new StandardEventDataSelectorFactory(thesaurus), ImmutableMap.of(DATA_TYPE_PROPERTY, STANDARD_EVENT_DATA_TYPE));
            addSelector(new UsagePointReadingSelectorFactory(thesaurus), ImmutableMap.of(DATA_TYPE_PROPERTY, STANDARD_USAGE_POINT_DATA_TYPE));
            addFormatter(new NullDataFormatterFactory(thesaurus), ALL_DATA_TYPES_MAP);
            String tempDirectoryPath = context.getProperty(JAVA_TEMP_DIR_PROPERTY);
            if (tempDirectoryPath == null) {
                tempDirectory = fileSystem.getRootDirectories().iterator().next();
            } else {
                tempDirectory = fileSystem.getPath(tempDirectoryPath);
            }
            combineCreatedAndUpdatedDataInOneWebRequest = Boolean.parseBoolean(getProperty(COMBINE_CREATED_UPDATED_DATA_PROPERTY, COMBINE_CREATED_UPDATED_DATA_DEFAULT));
            upgradeService.register(
                    InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                            .put(version(10, 2), UpgraderV10_2.class)
                            .put(version(10, 3), UpgraderV10_3.class)
                            .put(version(10, 4), V10_4SimpleUpgrader.class)
                            .put(version(10, 4, 3), V10_4_3SimpleUpgrader.class)
                            .put(UpgraderV10_5_1.VERSION, UpgraderV10_5_1.class)
                            .put(version(10, 7), UpgraderV10_7.class)
                            .put(version(10, 7, 1), UpgraderV10_7_1.class)
                            .put(version(10, 7, 2), UpgraderV10_7_2.class)
                            .put(version(10, 8), UpgraderV10_8.class)
                            .build());

            if (transactionService.isInTransaction()) {
                failOngoingExportTaskOccurrences();
            } else {
                try (TransactionContext transactionContext = transactionService.getContext()) {
                    failOngoingExportTaskOccurrences();
                    transactionContext.commit();
                }
            }
            serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", DataExportServiceCallType.HANDLER_NAME));
            serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", DataExportServiceCallType.CHILD_HANDLER_NAME));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public final void deactivate() {
        customPropertySetService.removeCustomPropertySet(serviceCallCPS);
        customPropertySetService.removeCustomPropertySet(childServiceCallCPS);
    }

    private void failOngoingExportTaskOccurrences() {
        List<Long> dataExportTaskIds = this.findReadingTypeDataExportTasks().stream().map(ExportTask::getId).collect(Collectors.toList());
        List<? extends DataExportOccurrence> dataExportOccurrences = this.getDataExportOccurrenceFinder()
                .withExportTask(dataExportTaskIds)
                .withExportStatus(Arrays.asList(DataExportStatus.BUSY))
                .find();
        dataExportOccurrences.stream()
                .forEach(o -> o.setToFailed());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
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

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
    public Optional<DataExportOccurrence> findDataExportOccurrence(long occurrenceId) {
        return dataModel.stream(DataExportOccurrence.class).join(TaskOccurrence.class)
                .filter(Where.where("taskOccurrence.id").isEqualTo(occurrenceId)).findFirst();
    }

    @Override
    public Optional<IDataExportOccurrence> findDataExportOccurrence(ExportTask task, Instant triggerTime) {
        return dataModel.stream(IDataExportOccurrence.class).join(TaskOccurrence.class).join(IExportTask.class)
                .filter(EQUAL.compare("readingTask", task))
                .filter(EQUAL.compare("taskOccurrence.triggerTime", triggerTime))
                .findFirst();
    }

    @Override
    public DataExportOccurrenceFinder getDataExportOccurrenceFinder() {
        Condition condition = Condition.TRUE;
        Order order = Order.ascending("lastRun").nullsLast();
        return new DataExportOccurrenceFinderImpl(dataModel, condition, order);
    }

    @Override
    public void setExportDirectory(AppServer appServer, Path path) {
        DirectoryForAppServer directoryForAppServer = dataModel.mapper(DirectoryForAppServer.class).getOptional(appServer.getName()).orElseGet(() -> DirectoryForAppServer.from(dataModel, appServer));
        PathVerification.validatePathForFolders(path.toString());
        directoryForAppServer.setPath(path);
        directoryForAppServer.save();
    }

    @Override
    public void removeExportDirectory(AppServer appServer) {
        Optional<DirectoryForAppServer> appServerRef = dataModel.mapper(DirectoryForAppServer.class).getOptional(appServer.getName());
        appServerRef.ifPresent(dataModel::remove);
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
        return dataModel.stream(MeterReadingSelectorConfigImpl.class)
                .filter(EQUAL.compare("exportPeriod", relativePeriod).or(EQUAL.compare("updatePeriod", relativePeriod)))
                .map(DataSelectorConfig::getExportTask)
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
    public Optional<DataExportWebService> getExportWebService(String name) {
        return Optional.ofNullable(exportWebServices.get(name));
    }

    @Override
    public List<DataExportWebService> getExportWebServicesMatching(DataSelectorFactory selectorFactory) {
        String dataType = dataSelectorFactories.get(selectorFactory);
        return exportWebServices.values().stream()
                .filter(service -> dataType.equals(service.getSupportedDataType()))
                .collect(Collectors.toList());
    }

    @Override
    public CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> getServiceCallCPS() {
        return serviceCallCPS;
    }

    @Override
    public DataExportServiceCallType getDataExportServiceCallType() {
        return dataModel.getInstance(DataExportServiceCallTypeImpl.class);
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        SimpleTranslationKey standardDataSelectorKey = new SimpleTranslationKey(StandardDataSelectorFactory.TRANSLATION_KEY, StandardDataSelectorFactory.DISPLAY_NAME);
        SimpleTranslationKey standardEventDataSelectorKey = new SimpleTranslationKey(StandardEventDataSelectorFactory.TRANSLATION_KEY, StandardEventDataSelectorFactory.DISPLAY_NAME);
        SimpleTranslationKey aggregatedDataSelectorKey = new SimpleTranslationKey(UsagePointReadingSelectorFactory.TRANSLATION_KEY, UsagePointReadingSelectorFactory.DISPLAY_NAME);
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(DataExportStatus.values()),
                Stream.of(Privileges.values()),
                Stream.of(standardDataSelectorKey, standardEventDataSelectorKey, aggregatedDataSelectorKey),
                Arrays.stream(com.elster.jupiter.export.impl.webservicecall.TranslationKeys.values()),
                Stream.of(NullDataFormatterFactory.getNameTranslationKey()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public boolean isUsedAsADestination(EndPointConfiguration endPointConfiguration) {
        return dataModel.stream(WebServiceDestinationImpl.class)
                .filter(Where.where(WebServiceDestinationImpl.Fields.CREATE_ENDPOINT.javaFieldName()).isEqualTo(endPointConfiguration)
                        .or(Where.where(WebServiceDestinationImpl.Fields.CHANGE_ENDPOINT.javaFieldName()).isEqualTo(endPointConfiguration)))
                .findAny()
                .isPresent();
    }

    @Override
    public boolean shouldCombineCreatedAndUpdatedDataInOneWebRequest() {
        return combineCreatedAndUpdatedDataInOneWebRequest;
    }

    private String getProperty(String name, String defaultValue) {
        String value = bundleContext.getProperty(name);
        return Checks.is(value).emptyOrOnlyWhiteSpace() ? defaultValue : value;
    }
}
