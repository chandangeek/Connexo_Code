/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(name = "com.elster.insight.issue.datavalidation.DataValidationEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {
                "subscriber=" + DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER,
                "destination=" + EventService.JUPITER_EVENTS
        },
        immediate = true)
public class DataValidationEventHandlerFactory implements MessageHandlerFactory {

    public static final String AQ_DATA_VALIDATION_EVENT_SUBSCRIBER = "IssueCreationUPDV";

    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile UsagePointIssueDataValidationService usagePointIssueDataValidationService;

    //for OSGI
    public DataValidationEventHandlerFactory() {
    }

    @Inject
    public DataValidationEventHandlerFactory(JsonService jsonService, IssueService issueService, NlsService nlsService, MeteringService meteringService, UsagePointIssueDataValidationService usagePointIssueDataValidationService) {
        setJsonService(jsonService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setUsagePointIssueDataValidationService(usagePointIssueDataValidationService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UsagePointIssueDataValidationService.class).toInstance(usagePointIssueDataValidationService);
            }
        });
        return new UsagePointDataValidationEventHandler(injector);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointIssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUsagePointIssueDataValidationService(UsagePointIssueDataValidationService usagePointIssueDataValidationService) {
        this.usagePointIssueDataValidationService = usagePointIssueDataValidationService;
    }
}