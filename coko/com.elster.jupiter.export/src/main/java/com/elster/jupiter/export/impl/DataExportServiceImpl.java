package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(name = "com.elster.jupiter.export", service = {DataExportService.class, InstallService.class}, property = "name=" + DataExportService.COMPONENTNAME, immediate = true)
public class DataExportServiceImpl implements IDataExportService, InstallService {

    public static final String DESTINATION_NAME = "DataExport";
    private volatile DataModel dataModel;
    private volatile TimeService timeService;
    private volatile TaskService taskService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;

    private List<DataProcessorFactory> dataProcessorFactories = new CopyOnWriteArrayList<>();
    private DestinationSpec destinationSpec;
    private QueryService queryService;

    public DataExportServiceImpl() {
    }

    @Inject
    public DataExportServiceImpl(OrmService ormService, TimeService timeService, TaskService taskService, MeteringGroupsService meteringGroupsService, MessageService messageService, NlsService nlsService, MeteringService meteringService, QueryService queryService) {
        setOrmService(ormService);
        setTimeService(timeService);
        setTaskService(taskService);
        setMeteringGroupsService(meteringGroupsService);
        setMessageService(messageService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setQueryService(queryService);
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
    public DataExportTaskBuilder newBuilder() {
        return new DataExportTaskBuilderImpl(dataModel);
    }

    @Override
    public Optional<? extends ReadingTypeDataExportTask> findExportTask(long id) {
        return dataModel.mapper(IReadingTypeDataExportTask.class).getOptional(id);
    }

    @Override
    public Query<? extends ReadingTypeDataExportTask> getReadingTypeDataExportTaskQuery() {
        return queryService.wrap(dataModel.query(IReadingTypeDataExportTask.class));
    }

    @Override
    public List<PropertySpec<?>> getPropertiesSpecsForProcessor(String name) {
        return getDataProcessorFactory(name)
                .map(DataProcessorFactory::createTemplateDataFormatter)
                .map(DataProcessor::getPropertySpecs)
                .orElse(Collections.emptyList());
    }

    @Override
    public DestinationSpec getDestination() {
        return destinationSpec;
    }

    @Override
    public Optional<DataProcessorFactory> getDataProcessorFactory(String name) {
        return dataProcessorFactories.stream()
                .filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
            destinationSpec.save();
            destinationSpec.activate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            timeService.createRelativePeriodCategory("DataExport");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, TimeService.COMPONENT_NAME, MeteringService.COMPONENTNAME, TaskService.COMPONENTNAME, MeteringGroupsService.COMPONENTNAME, MessageService.COMPONENTNAME, NlsService.COMPONENTNAME);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(DataProcessorFactory dataProcessorFactory) {
        dataProcessorFactories.add(dataProcessorFactory);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
        if (dataModel.isInstalled()) {
            destinationSpec = messageService.getDestinationSpec(DESTINATION_NAME).get();
        }
    }

    public void removeResource(DataProcessorFactory dataProcessorFactory) {
        dataProcessorFactories.remove(dataProcessorFactory);
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(IDataExportService.class).toInstance(DataExportServiceImpl.this);
                bind(TaskService.class).toInstance(taskService);
                bind(MeteringService.class).toInstance(meteringService);
            }
        });
    }

    @Deactivate
    public final void deactivate() {
    }


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
}
