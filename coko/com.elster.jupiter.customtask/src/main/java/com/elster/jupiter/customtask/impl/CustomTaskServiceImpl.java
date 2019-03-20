/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskBuilder;
import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

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
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.customtask",
        service = {CustomTaskService.class, ICustomTaskService.class, TranslationKeyProvider.class, MessageSeedProvider.class, RelativePeriodCategoryTranslationProvider.class},
        property = "name=" + CustomTaskService.COMPONENTNAME,
        immediate = true)
public class CustomTaskServiceImpl implements ICustomTaskService, TranslationKeyProvider, MessageSeedProvider, RelativePeriodCategoryTranslationProvider {

    private static final String MODULE_DESCRIPTION = "Custom task";
    private volatile DataModel dataModel;
    private volatile TimeService timeService;
    private volatile TaskService taskService;
    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile PropertySpecService propertySpecService;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private QueryService queryService;

    private Optional<DestinationSpec> destinationSpec = Optional.empty();
    private List<CustomTaskFactory> customTaskFactories = new ArrayList<>();

    public CustomTaskServiceImpl() {
    }

    @Inject
    public CustomTaskServiceImpl(OrmService ormService, TimeService timeService, TaskService taskService, MessageService messageService, NlsService nlsService,
                                 Clock clock, AppService appService, TransactionService transactionService, PropertySpecService propertySpecService,
                                 UserService userService, BundleContext context, UpgradeService upgradeService) {
        setOrmService(ormService);
        setTimeService(timeService);
        setTaskService(taskService);
        setMessageService(messageService);
        setNlsService(nlsService);
        setClock(clock);
        setAppService(appService);
        setTransactionService(transactionService);
        setPropertySpecService(propertySpecService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setQueryService(queryService);
        activate(context);
    }

    @Override
    public List<CustomTaskFactory> getAvailableCustomTasks(String application) {
        List<CustomTaskFactory> taskFactories = new ArrayList<>(this.customTaskFactories);
        return taskFactories.stream()
                .filter(customTaskFactory -> customTaskFactory.targetApplications().contains(application))
                .sorted(Comparator.comparing(HasName::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CustomTaskFactory> getCustomTaskFactory(String name){
        return this.customTaskFactories.stream()
                .filter(customTaskFactory -> customTaskFactory.getName().compareToIgnoreCase(name) == 0)
                .findFirst();
    }

    @Override
    public CustomTaskOccurrence createCustomTaskOccurrence(TaskOccurrence taskOccurrence) {
        ICustomTask task = getCustomTaskForRecurrentTask(taskOccurrence.getRecurrentTask()).orElseThrow(IllegalArgumentException::new);
        CustomTaskOccurrenceImpl customTaskOccurrence = CustomTaskOccurrenceImpl.from(dataModel, taskOccurrence, task);
        customTaskOccurrence.persist();
        return customTaskOccurrence;
    }

    @Override
    public Optional<CustomTaskOccurrence> findCustomTaskOccurrence(TaskOccurrence occurrence) {
        return dataModel.query(CustomTaskOccurrence.class, ICustomTask.class).select(EQUAL.compare("taskOccurrence", occurrence)).stream().findFirst();
    }

    @Override
    public CustomTaskBuilder newBuilder() {
        return new CustomTaskBuilderImpl(dataModel);
    }

    @Override
    public Optional<? extends CustomTask> findCustomTask(long id) {
        return dataModel.mapper(ICustomTask.class).getOptional(id);
    }

    @Override
    public Optional<? extends CustomTask> findCustomTaskByRecurrentTask(long id) {
        Query<ICustomTask> query =
                queryService.wrap(dataModel.query(ICustomTask.class, RecurrentTask.class));
        Condition condition = where("recurrentTask.id").isEqualTo(id);
        return query.select(condition).stream().findFirst();
    }

    @Override
    public Optional<? extends CustomTask> findAndLockCustomTask(long id, long version) {
        return dataModel.mapper(ICustomTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public DestinationSpec getDestination(String taskType) {
        return messageService.getDestinationSpec(taskType).orElse(null);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, MODULE_DESCRIPTION);
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomTaskFactory(CustomTaskFactory taskFactory) {
        customTaskFactories.add(taskFactory);
    }

    public void removeCustomTaskFactory(CustomTaskFactory taskFactory) {
        customTaskFactories.remove(taskFactory);
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
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Activate
    public final void activate(BundleContext context) {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ICustomTaskService.class).toInstance(CustomTaskServiceImpl.this);
                    bind(TaskService.class).toInstance(taskService);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(Clock.class).toInstance(clock);
                    bind(TransactionService.class).toInstance(transactionService);
                    bind(PropertySpecService.class).toInstance(propertySpecService);
                    bind(AppService.class).toInstance(appService);
                    bind(CustomTaskService.class).toInstance(CustomTaskServiceImpl.this);
                    bind(TimeService.class).toInstance(timeService);
                    bind(MessageService.class).toInstance(messageService);
                    bind(UserService.class).toInstance(userService);

                }
            });
            upgradeService.register(
                    InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    Collections.emptyMap());

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
        this.thesaurus = nlsService.getThesaurus(CustomTaskService.COMPONENTNAME, Layer.DOMAIN);
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
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public Optional<CustomTaskOccurrence> findCustomTaskOccurrence(long occurrenceId) {
        return dataModel.stream(CustomTaskOccurrence.class).join(TaskOccurrence.class)
                .filter(Where.where("taskOccurrence.id").isEqualTo(occurrenceId)).findFirst();
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
       return Stream.of(
                Stream.of(TranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public boolean usedByAutorescheduleTask(Long comTaskId) {
        Condition condition = where("name").isEqualTo("comTaskSelector");
        List<CustomTaskProperty> properties = dataModel.query(CustomTaskProperty.class)
                .select(condition);

        return properties.stream().filter(property -> propertyContainsId(property, comTaskId)).findFirst().isPresent();
    }

    private boolean propertyContainsId (CustomTaskProperty property, Long comTaskId) {
        String value = property.getStringValue().replace("[","").replace("]","");
        ArrayList<String> comTaskIds = new ArrayList<>(Arrays.asList(value.split(", ")));

        return comTaskIds.contains(comTaskId + "");
    }

    @Override
    public List<CustomTaskProperty> findPropertyByComTaskId(Long comTaskId, int skip, int limit) {
        Condition condition = where("name").isEqualTo("comTaskSelector");

        return dataModel.query(CustomTaskProperty.class)
                .select(condition)
                .stream()
                .filter(property -> propertyContainsId(property, comTaskId))
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomTaskProperty> findPropertyByDeviceGroupName(String endDeviceGroupName, int skip, int limit) {
        Condition condition = where("name").isEqualTo("groupSelector");

        return  dataModel.query(CustomTaskProperty.class)
                .select(condition)
                .stream()
                .filter(property -> property.getStringValue().equals(endDeviceGroupName))
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private Optional<ICustomTask> getCustomTaskForRecurrentTask(RecurrentTask recurrentTask) {
        return dataModel.mapper(ICustomTask.class).getUnique("recurrentTask", recurrentTask);
    }
}
