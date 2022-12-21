/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
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
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.webservice.issue.WebServiceHistoricalIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssueFilter;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceIssueImpl;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceOpenIssueImpl;

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
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.webservice.issue.impl.WebServiceIssueServiceImpl",
           service = { TranslationKeyProvider.class, MessageSeedProvider.class, WebServiceIssueService.class, WebServiceIssueServiceImpl.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
           property = "name=" + WebServiceIssueService.COMPONENT_NAME,
           immediate = true)
public class WebServiceIssueServiceImpl implements WebServiceIssueService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;

    private volatile DataModel dataModel;

    //for OSGI
    public WebServiceIssueServiceImpl() {
    }

    @Inject
    public WebServiceIssueServiceImpl(OrmService ormService,
                                      IssueService issueService,
                                      NlsService nlsService,
                                      EventService eventService,
                                      MessageService messageService,
                                      UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setMessageService(messageService);
        setUpgradeService(upgradeService);
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
                bind(WebServiceIssueService.class).toInstance(WebServiceIssueServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", WebServiceIssueService.COMPONENT_NAME),
                dataModel,
                Installer.class,
                Collections.emptyMap());
    }

    @Override
    public Optional<? extends WebServiceIssue> findIssue(long id) {
        Optional<? extends WebServiceIssue> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<? extends WebServiceIssue> findAndLockWebServiceIssue(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
        if (issue.isPresent()) {
            return findOpenIssue(id);
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<WebServiceOpenIssue> findOpenIssue(long id) {
        return dataModel.query(WebServiceOpenIssue.class, OpenIssue.class)
                        .select(Where.where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".id").isEqualTo(id))
                        .stream()
                        .findFirst();
    }

    @Override
    public Optional<WebServiceHistoricalIssue> findHistoricalIssue(long id) {
        return dataModel.query(WebServiceHistoricalIssue.class, HistoricalIssue.class)
                        .select(Where.where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".id").isEqualTo(id))
                        .stream()
                        .findFirst();
    }

    @Override
    public WebServiceOpenIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        WebServiceOpenIssueImpl issue = dataModel.getInstance(WebServiceOpenIssueImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    @Override
    public String getComponentName() {
        return WebServiceIssueService.COMPONENT_NAME;
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
        dataModel = ormService.newDataModel(WebServiceIssueService.COMPONENT_NAME, "Web service issues");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceIssueService.COMPONENT_NAME, Layer.DOMAIN);
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
        return issue instanceof WebServiceOpenIssue ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof WebServiceHistoricalIssue ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    @Override
    public Finder<? extends WebServiceIssue> findAllWebServiceIssues(WebServiceIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);

        Class<? extends WebServiceIssue> mainClass;
        Class<? extends Issue> issueEager;
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            mainClass = WebServiceOpenIssue.class;
            issueEager = OpenIssue.class;
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            mainClass = WebServiceHistoricalIssue.class;
            issueEager = HistoricalIssue.class;
        } else {
            mainClass = WebServiceIssue.class;
            issueEager = Issue.class;
        }
        return DefaultFinder.of(mainClass, condition, dataModel,
                issueEager, IssueStatus.class, User.class, IssueReason.class, IssueType.class, WebServiceCallOccurrence.class, EndPointConfiguration.class);
    }

    private Condition buildConditionFromFilter(WebServiceIssueFilter filter) {
        //filter by assignee
        Condition condition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            condition = where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            condition = where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".user").isNull();
        }
        //filter by reason
        if (filter.getIssueReason().isPresent()) {
            condition = condition.and(where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".reason").isEqualTo(filter.getIssueReason().get()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where(WebServiceIssueImpl.Fields.BASE_ISSUE.fieldName() + ".status").in(filter.getStatuses()));
        }
        //filter by occurrences
        if (!filter.getWebServiceCallOccurrenceIds().isEmpty()) {
            condition = condition.and(where(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.fieldName() + ".id").in(filter.getWebServiceCallOccurrenceIds()));
        }
        //filter by end point configurations
        if (!filter.getEndPointConfigurationIds().isEmpty()) {
            condition = condition.and(where(WebServiceIssueImpl.Fields.WSC_OCCURRENCE.fieldName() + ".endPointConfiguration.id").in(filter.getEndPointConfigurationIds()));
        }
        return condition;
    }

    @Override
    public Set<String> getIssueTypeIdentifiers() {
        return Collections.singleton(WebServiceIssueService.ISSUE_TYPE_NAME);
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
