package com.energyict.mdc.issue.datavalidation.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssue;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.HistoricalDataValidationIssue;
import com.energyict.mdc.issue.datavalidation.MessageSeeds;
import com.energyict.mdc.issue.datavalidation.OpenDataValidationIssue;
import com.google.inject.AbstractModule;

@Component(name = "com.energyict.mdc.issue.datavalidation", service = {InstallService.class, TranslationKeyProvider.class, IssueDataValidationService.class, IssueProvider.class}, property = "name=" + IssueDataValidationService.COMPONENT_NAME, immediate = true)
public class IssueDataValidationServiceImpl implements IssueDataValidationService, TranslationKeyProvider, InstallService, IssueProvider {

    private volatile IssueService issueService;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;

    private volatile DataModel dataModel;

    //for OSGI
    public IssueDataValidationServiceImpl() {
    }
    
    @Inject
    public IssueDataValidationServiceImpl(OrmService ormService, IssueService issueService, MessageService messageService, EventService eventService, QueryService queryService, NlsService nlsService) {
        setOrmService(ormService);
        setIssueService(issueService);
        setMessageService(messageService);
        setQueryService(queryService);
        setNlsService(nlsService);
        activate();
        if(!dataModel.isInstalled()) {
            install();
        }
    }
    
    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(IssueService.class).toInstance(issueService);
                bind(QueryService.class).toInstance(queryService);
            }
        });
    }
    
    @Override
    public void install() {
        new Installer(dataModel, issueService, messageService, eventService, thesaurus).install();
    }
    
    @Override
    public List<String> getPrerequisiteModules() {
        //TODO?
        return Arrays.asList("NLS", "ISU", "MSG", "ORM", "DDC", "MTR", "CES", "DDC");
    }
    
    @Override
    public Optional<DataValidationIssue> findIssue(long id) {
        //TODO
        return Optional.empty();
    }

    @Override
    public Optional<OpenDataValidationIssue> findOpenIssue(long id) {
        //TODO
        return Optional.empty();
    }

    @Override
    public Optional<HistoricalDataValidationIssue> findHistoricalIssue(long id) {
        // TODO
        return Optional.empty();
    }

    @Override
    public OpenDataValidationIssue createIssue(Issue baseIssue) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getComponentName() {
        return IssueDataValidationService.COMPONENT_NAME;
    }
    
    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
    
    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueDataValidationService.COMPONENT_NAME, "Issue Datavalidation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }
    
    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }
    
    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    
    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        // TODO Auto-generated method stub
        return null;
    }
}
