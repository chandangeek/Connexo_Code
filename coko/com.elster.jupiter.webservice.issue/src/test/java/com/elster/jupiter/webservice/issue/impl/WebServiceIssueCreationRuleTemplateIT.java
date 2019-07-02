/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.webservice.issue.WebServiceIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssueFilter;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;
import com.elster.jupiter.webservice.issue.impl.template.AuthFailureIssueCreationRuleTemplate;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebServiceIssueCreationRuleTemplateIT extends BaseIT {
    @Test
    @Transactional
    public void testTemplateGetters() {
        assertThat(template.getIssueType().getId()).isEqualTo(issueService.findIssueType(WebServiceIssueService.ISSUE_TYPE_NAME).get().getId());
    }

    @Test
    @Transactional
    public void testTemplatePropertySpecs() {
        List<PropertySpec> propertySpecs = template.getPropertySpecs();

        assertThat(propertySpecs).hasSize(1);

        PropertySpec propertySpec = propertySpecs.get(0);
        assertThat(propertySpec.getName()).isEqualTo(AuthFailureIssueCreationRuleTemplate.END_POINT_CONFIGURATIONS);

        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues.getAllValues()).hasSize(1);

        EndPointConfiguration value = (EndPointConfiguration) possibleValues.getAllValues().get(0);
        assertThat(value.getId()).isEqualTo(endPointConfiguration.getId());
        assertThat(value.getName()).isEqualTo(endPointConfiguration.getName());
    }

    @Test
    @Transactional
    public void testCreateIssue() {
        Message message = mockWebServiceEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        messageHandler.process(message);

        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue webServiceIssue = issues.get(0);
        assertThat(webServiceIssue.getWebServiceCallOccurrence()).isEqualTo(webServiceCallOccurrence);
    }

    @Test
    @Transactional
    public void testOutboundEndPointIssue() {
        EndPointConfiguration altEndPointConfiguration = createOutboundEndPointConfiguration();
        WebServiceCallOccurrence occurrence = createWebServiceCallOccurrence(altEndPointConfiguration);

        Message message = mockWebServiceEvent(WebServiceEventDescription.AUTH_FAILURE, occurrence);
        messageHandler.process(message);

        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(0); // nothing has been created because device configuration in not mentioned in the rule

        CreationRule rule = createRuleForEndPointConfigurations("Rule #2", endPointConfiguration, altEndPointConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
        messageHandler.process(message);
        issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getWebServiceCallOccurrence()).isEqualTo(occurrence);
        assertThat(issues.get(0).getRule().getId()).isEqualTo(rule.getId());
    }

    @Test
    @Transactional
    public void testCreateNewIssueWhileOneExists() {
        Message message = mockWebServiceEvent(WebServiceEventDescription.AUTH_FAILURE, webServiceCallOccurrence);
        messageHandler.process(message);

        List<? extends WebServiceIssue> issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(1);
        WebServiceIssue webServiceIssue = issues.get(0);
        assertThat(webServiceIssue.getWebServiceCallOccurrence()).isEqualTo(webServiceCallOccurrence);

        WebServiceCallOccurrence occurrence = createWebServiceCallOccurrence(endPointConfiguration);
        message = mockWebServiceEvent(WebServiceEventDescription.AUTH_FAILURE, occurrence);
        messageHandler.process(message);

        issues = webServiceIssueService.findAllWebServiceIssues(new WebServiceIssueFilter()).find();
        assertThat(issues).hasSize(2);
        assertThat(issues.stream().map(WebServiceIssue::getWebServiceCallOccurrence).collect(Collectors.toList())).containsOnly(webServiceCallOccurrence, occurrence);
    }
}
