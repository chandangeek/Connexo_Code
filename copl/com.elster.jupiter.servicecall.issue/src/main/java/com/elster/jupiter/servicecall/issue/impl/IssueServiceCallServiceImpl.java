/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

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
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.issue.IssueServiceCall;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.MessageSeeds;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueFilter;
import com.elster.jupiter.servicecall.issue.TranslationKeys;
import com.elster.jupiter.servicecall.issue.impl.entity.OpenIssueServiceCallImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.servicecall.issue.impl",
           service = { TranslationKeyProvider.class, MessageSeedProvider.class, IssueServiceCallService.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
           property = "name=" + IssueServiceCallService.COMPONENT_NAME,
           immediate = true)
public class IssueServiceCallServiceImpl implements IssueServiceCallService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;
    /* for dependency - startup/installation order */
    private volatile MeteringService meteringService;
//    private volatile EstimationService estimationService;

    private volatile DataModel dataModel;

    //for OSGI
    public IssueServiceCallServiceImpl() {
    }

    @Inject
    public IssueServiceCallServiceImpl(OrmService ormService, IssueService issueService, NlsService nlsService, EventService eventService, MessageService messageService, UpgradeService upgradeService) {
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
                bind(IssueServiceCallService.class).toInstance(IssueServiceCallServiceImpl.this);
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
    public Optional<? extends IssueServiceCall> findIssue(long id) {

//        Optional<OpenIssueServiceCall> issue = findOpenIssue(id);
//        if (issue.isPresent()) {
//            return issue;
//        }
//        return findHistoricalIssue(id);
        return null;
    }

    @Override
    public Optional<? extends IssueServiceCall> findAndLockIssueDataValidationByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
//        if (issue.isPresent()) {
//            return findOpenIssue(id);
//        }
//        return findHistoricalIssue(id);
        return null;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

//    @Reference
//    public void setEstimationService(EstimationService estimationService) {
//        this.estimationService = estimationService;
//    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
//        return issue instanceof OpenIssueDataValidation ? Optional.of(issue) : findOpenIssue(issue.getId());
        return null;
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
       // return issue instanceof HistoricalIssueDataValidation ? Optional.of(issue) : findHistoricalIssue(issue.getId());
        return null;
    }

    @Override
    public Finder<? extends IssueServiceCall> findAllDataValidationIssues(ServiceCallIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);

        Class<? extends IssueServiceCall> mainClass;
        Class<? extends Issue> issueEager;
        List<IssueStatus> statuses = filter.getStatuses();
//        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
//            mainClass = OpenIssueDataValidation.class;
//            issueEager = OpenIssue.class;
//        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
//            mainClass = HistoricalIssueDataValidation.class;
//            issueEager = HistoricalIssue.class;
//        } else {
            mainClass = IssueServiceCall.class;
            issueEager = Issue.class;
//        }
        return DefaultFinder.of(mainClass, condition, dataModel, issueEager, IssueStatus.class, EndDevice.class, User.class,  IssueReason.class, IssueType.class);
    }

    private Condition buildConditionFromFilter(ServiceCallIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
//        if (filter.getAssignee().isPresent()) {
//            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isEqualTo(filter.getAssignee().get());
//        }
//        if (filter.isUnassignedOnly()) {
//            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isNull();
//        }
//        condition = condition.and(assigneeCondition);
//        //filter by reason
//        if (filter.getIssueReason().isPresent()) {
//            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".reason").isEqualTo(filter.getIssueReason().get()));
//        }
//        //filter by device
//        if (filter.getDevice().isPresent()) {
//            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".device").isEqualTo(filter.getDevice().get()));
//        }
//        //filter by statuses
//        if (!filter.getStatuses().isEmpty()) {
//            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".status").in(filter.getStatuses()));
//        }
        return condition;
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
