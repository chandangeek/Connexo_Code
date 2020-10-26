/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.servicecall.HistoricalServiceCallIssue;
import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueFilter;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.impl.entity.OpenServiceCallIssueImpl;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
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
import com.elster.jupiter.issue.share.service.spi.IssueReasonTranslationProvider;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue.servicecall.impl.ServiceCallIssueServiceImpl",
        service = { TranslationKeyProvider.class, MessageSeedProvider.class, ServiceCallIssueService.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
        property = "name=" + ServiceCallIssueService.COMPONENT_NAME,
        immediate = true)
public class ServiceCallIssueServiceImpl implements ServiceCallIssueService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile UpgradeService upgradeService;
    private volatile QueryService queryService;
    private volatile MessageService messageService;
    private volatile ServiceCallService serviceCallService;

    private volatile DataModel dataModel;

    //for OSGI
    public ServiceCallIssueServiceImpl() {
    }

    @Inject
    public ServiceCallIssueServiceImpl(OrmService ormService, IssueService issueService, NlsService nlsService,
                                       EventService eventService, UpgradeService upgradeService, QueryService queryService,
                                       MessageService messageService, ServiceCallService serviceCallService) {
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setQueryService(queryService);
        setMessageService(messageService);
        setServiceCallService(serviceCallService);
        activate();
    }

    @Activate
    public final void activate() {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueActionService.class).toInstance(issueActionService);
                bind(ServiceCallIssueService.class).toInstance(ServiceCallIssueServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", COMPONENT_NAME),
                dataModel,
                Installer.class,
                Collections.emptyMap()
        );
    }

    @Override
    public Optional<? extends ServiceCallIssue> findIssue(long id) {
        Optional<OpenServiceCallIssue> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenServiceCallIssue> findOpenIssue(long id) {
        return find(OpenServiceCallIssue.class, id, OpenIssue.class);
    }

    @Override
    public Optional<HistoricalServiceCallIssue> findHistoricalIssue(long id) {
        return find(HistoricalServiceCallIssue.class, id, HistoricalIssue.class);
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object key, Class<?>... eagers) {
        return queryService.wrap(dataModel.query(clazz, eagers)).get(key);
    }

    @Override
    public OpenServiceCallIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenServiceCallIssueImpl issue = dataModel.getInstance(OpenServiceCallIssueImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
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
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Issue Service Call");
    }

    // To preserve the order of activation
    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenServiceCallIssue ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalServiceCallIssue ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    @Override
    public Finder<? extends ServiceCallIssue> findIssues(ServiceCallIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        eagerClasses.addAll(Arrays.asList(IssueStatus.class, User.class, IssueReason.class, IssueType.class));
        return DefaultFinder.of((Class<ServiceCallIssue>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses.size()]));
    }

    private List<Class<?>> determineMainApiClass(ServiceCallIssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(OpenServiceCallIssue.class);
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalServiceCallIssue.class);
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(ServiceCallIssue.class);
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition buildConditionFromFilter(ServiceCallIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        if (filter.getAssignee().isPresent()) {
            condition = condition.and(where("baseIssue.user").isEqualTo(filter.getAssignee().get()));
        }
        if (filter.isUnassignedOnly()) {
            condition = condition.and(where("baseIssue.user").isNull());
        }
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("baseIssue.reason").in(filter.getIssueReasons()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("baseIssue.status").in(filter.getStatuses()));
        }
        if (!filter.getRules().isEmpty()) {
            condition = condition.and(where("baseIssue.rule").in(filter.getRules()));
        }
        if (!filter.getServiceCalls().isEmpty()) {
            condition = condition.and(where("serviceCall").in(filter.getServiceCalls()));
        }
        return condition;
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
