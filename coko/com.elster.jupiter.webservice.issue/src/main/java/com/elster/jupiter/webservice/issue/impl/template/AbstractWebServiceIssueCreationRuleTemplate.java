/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.template;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.WebServiceOpenIssue;
import com.elster.jupiter.webservice.issue.impl.TranslationKeys;
import com.elster.jupiter.webservice.issue.impl.WebServiceIssueServiceImpl;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

abstract class AbstractWebServiceIssueCreationRuleTemplate implements CreationRuleTemplate {
    public static final String END_POINT_CONFIGURATIONS = "webServiceIssueCreationRule.endPointConfigurations";

    private volatile WebServiceIssueServiceImpl webServiceIssueService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile EndPointConfigurationService endPointConfigurationService;

    public AbstractWebServiceIssueCreationRuleTemplate() {
        // for OSGi
    }

    public AbstractWebServiceIssueCreationRuleTemplate(WebServiceIssueServiceImpl webServiceIssueService,
                                                       IssueService issueService,
                                                       PropertySpecService propertySpecService,
                                                       EndPointConfigurationService endPointConfigurationService) {
        this();
        setWebServiceIssueService(webServiceIssueService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setEndPointConfigurationService(endPointConfigurationService);
    }

    abstract TranslationKeys getNameTranslationKey();
    abstract TranslationKeys getDescriptionTranslationKey();
    abstract WebServiceEventDescription getEventType();

    @Override
    public String getName() {
        return getNameTranslationKey().getKey();
    }

    @Override
    public String getDisplayName() {
        return getNameTranslationKey().translate(thesaurus);
    }

    @Override
    public String getDescription() {
        return getDescriptionTranslationKey().translate(thesaurus);
    }

    @Override
    public String getContent() {
        return "package com.elster.jupiter.webservice.issue\n" +
                "import com.elster.jupiter.webservice.issue.impl.event.WebServiceEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Web service issue rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : WebServiceEvent(eventType == \"" + getEventType().name() + "\", endPointConfigurationId in (@{" + END_POINT_CONFIGURATIONS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by web service issue creation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    void setWebServiceIssueService(WebServiceIssueServiceImpl webServiceIssueService) {
        this.webServiceIssueService = webServiceIssueService;
        this.thesaurus = webServiceIssueService.thesaurus();
    }

    void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        QueryStream<EndPointConfiguration> epcStream = endPointConfigurationService.streamEndPointConfigurations();
        if (getEventType() != WebServiceEventDescription.AUTH_FAILURE) {
            epcStream = epcStream.filter(OutboundEndPointConfiguration.class);
        }
        EndPointConfiguration[] possibleValues = epcStream.toArray(EndPointConfiguration[]::new);
        return Collections.singletonList(propertySpecService
                .referenceSpec(EndPointConfiguration.class)
                .named(END_POINT_CONFIGURATIONS, TranslationKeys.END_POINT_CONFIGURATIONS_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .markMultiValued(",")
                .addValues(possibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(WebServiceIssueService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public WebServiceOpenIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return webServiceIssueService.createIssue(baseIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            WebServiceOpenIssue webServiceOpenIssue = (WebServiceOpenIssue) issue.get();
            issue = Optional.of(webServiceOpenIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }
}
