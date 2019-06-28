/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueReason;
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
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.issue.HistoricalIssueServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;
import com.elster.jupiter.servicecall.issue.MessageSeeds;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;
import com.elster.jupiter.servicecall.issue.impl.entity.OpenIssueServiceCallImpl;
import com.elster.jupiter.servicecall.issue.impl.event.ServiceCallStateChangedEvent;
import com.elster.jupiter.servicecall.issue.impl.i18n.TranslationKeys;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.servicecall.issue.impl.IssueServiceCallServiceImpl",
           service = { TranslationKeyProvider.class, MessageSeedProvider.class, ServiceCallIssueService.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
           property = "name=" + ServiceCallIssueService.COMPONENT_NAME,
           immediate = true)
public class ServiceCallIssueServiceImpl implements ServiceCallIssueService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;
    private volatile QueryService queryService;

    private volatile DataModel dataModel;

    static final String ISSUE_SERVICE_CALLS_DESTINATION_NAME = "IssueSerivceCalls";
    public static final String ISSUE_SERVICE_CALLS_SUBSCRIBER_NAME = "IssueSerivceCalls";


    //for OSGI
    public ServiceCallIssueServiceImpl() {
    }

    @Inject
    public ServiceCallIssueServiceImpl(OrmService ormService, IssueService issueService, NlsService nlsService, EventService eventService, MessageService messageService, UpgradeService upgradeService, QueryService queryService) {
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setMessageService(messageService);
        setUpgradeService(upgradeService);
        setQueryService(queryService);
        activate();
    }

    @Activate
    public final void activate() {
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
    public void createIssue(ServiceCall serviceCall, DefaultState newState) {
        for (CreationRule rule : issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class).select(where("active").isEqualTo(true).and(where("reason.issueType.prefix").isEqualToIgnoreCase(SERVICE_CALL_ISSUE_PREFIX)))) {
            issueService.getIssueCreationService().processIssueCreationEvent(rule.getId(), new ServiceCallStateChangedEvent(serviceCall, newState));
        }
    }

    @Override
    public Optional<? extends ServiceCallIssue> findIssue(long id) {
        Optional<OpenIssueServiceCall> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenIssueServiceCall> findOpenIssue(long id) {
        return find(OpenIssueServiceCall.class, id, OpenIssue.class);
    }

    @Override
    public Optional<HistoricalIssueServiceCall> findHistoricalIssue(long id) {
        return find(HistoricalIssueServiceCall.class, id, HistoricalIssue.class);
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object key, Class<?>... eagers) {
        return queryService.wrap(dataModel.query(clazz, eagers)).get(key);
    }

    @Override
    public OpenIssueServiceCall createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenIssueServiceCallImpl issue = dataModel.getInstance(OpenIssueServiceCallImpl.class);
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
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenIssueServiceCall ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalIssueServiceCall ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
