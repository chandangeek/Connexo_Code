/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.issue.task.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.issue.task.TaskIssueFilter;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.entity.OpenTaskIssueImpl;
import com.elster.jupiter.issue.task.impl.database.TableSpecs;
import com.elster.jupiter.issue.task.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.task.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.task.impl.install.Installer;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue.task",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class, TaskIssueService.class, IssueProvider.class, IssueGroupTranslationProvider.class},
        property = "name=" + TaskIssueService.COMPONENT_NAME,
        immediate = true)
public class TaskIssueServiceImpl implements TranslationKeyProvider, MessageSeedProvider, TaskIssueService, IssueProvider, IssueGroupTranslationProvider {
    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile PropertySpecService propertySpecService;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

    // For OSGi framework
    public TaskIssueServiceImpl() {
    }

    // For unit testing
    @Inject
    public TaskIssueServiceImpl(IssueService issueService,
                                MessageService messageService,
                                NlsService nlsService,
                                OrmService ormService,
                                QueryService queryService,
                                EventService eventService,
                                UpgradeService upgradeService,
                                PropertySpecService propertySpecService,
                                BundleContext bundleContext
    ) {
        this();
        setMessageService(messageService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setQueryService(queryService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setPropertySpecService(propertySpecService);
        activate(bundleContext);
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueActionService.class).toInstance(issueActionService);
                bind(QueryService.class).toInstance(queryService);
                bind(EventService.class).toInstance(eventService);
                bind(TaskIssueService.class).toInstance(TaskIssueServiceImpl.this);
            }
        });

        TaskIssueProcessAssociationProvider processAssociationProvider = new TaskIssueProcessAssociationProvider(thesaurus, issueService, propertySpecService);
        registrations.add(bundleContext.registerService(ProcessAssociationProvider.class, processAssociationProvider,
                new Hashtable<>(ImmutableMap.of("name", TaskIssueProcessAssociationProvider.NAME))));

        upgradeService.register(identifier("Pulse", TaskIssueService.COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Deactivate
    public void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TaskIssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(TaskIssueService.COMPONENT_NAME, "Task Issue");
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Optional<? extends TaskIssue> findIssue(long id) {
        Optional<OpenTaskIssue> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<? extends TaskIssue> findAndLockIssueTaskIssueByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
        if (issue.isPresent()) {
            return findOpenIssue(id);
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenTaskIssue> findOpenIssue(long id) {
        return find(OpenTaskIssue.class, id, OpenIssue.class);
    }

    @Override
    public Optional<HistoricalTaskIssue> findHistoricalIssue(long id) {
        return find(HistoricalTaskIssue.class, id, HistoricalIssue.class);
    }

    @Override
    public OpenTaskIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenTaskIssueImpl issue = dataModel.getInstance(OpenTaskIssueImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object key, Class<?>... eagers) {
        return queryService.wrap(dataModel.query(clazz, eagers)).get(key);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public Finder<? extends TaskIssue> findIssues(TaskIssueFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers == null) {
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, User.class, IssueReason.class, IssueType.class));
        } else {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        return DefaultFinder.of((Class<TaskIssue>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses
                .size()]));
    }

    private List<Class<?>> determineMainApiClass(TaskIssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            eagerClasses.add(OpenTaskIssue.class);
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalTaskIssue.class);
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(TaskIssue.class);
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition buildConditionFromFilter(TaskIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            assigneeCondition = where("baseIssue.user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where("baseIssue.user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("baseIssue.reason").in(filter.getIssueReasons()));
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("baseIssue.device").in(filter.getDevices()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("baseIssue.status").in(filter.getStatuses()));
        }
        return condition;
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
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenTaskIssue ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalTaskIssue ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    @Override
    public Set<String> getIssueTypeIdentifiers() {
        return Collections.singleton(TaskIssueService.TASK_ISSUE);
    }
}
