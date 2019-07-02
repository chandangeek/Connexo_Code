/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceOpenIssueImpl;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEvent;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventHandlerFactory;
import com.elster.jupiter.webservice.issue.impl.template.AuthFailureIssueCreationRuleTemplate;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseIT {
    protected static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    protected static final Instant FIXED_TIME = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);
    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    protected AuthFailureIssueCreationRuleTemplate template;
    protected IssueService issueService;
    protected IssueCreationService issueCreationService;
    protected WebServiceIssueService webServiceIssueService;
    protected MessageHandler messageHandler;

    protected Group userRole;
    protected EndPointConfiguration endPointConfiguration;
    protected WebServiceCallOccurrence webServiceCallOccurrence;
    protected CreationRule creationRule;

    @BeforeClass
    public static void initialize() throws SQLException {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase(false);
        when(inMemoryPersistence.getClock().instant()).thenReturn(FIXED_TIME);
    }

    private static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(UTC_TIME_ZONE.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @AfterClass
    public static void dropDataBase() throws SQLException {
        inMemoryPersistence.dropDatabase();
    }

    private static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        template = inMemoryPersistence.getService(AuthFailureIssueCreationRuleTemplate.class);
        ((IssueServiceImpl) issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        webServiceIssueService = inMemoryPersistence.getService(WebServiceIssueService.class);
        messageHandler = inMemoryPersistence.getService(WebServiceEventHandlerFactory.class).newMessageHandler();

        userRole = createUserRole();
        endPointConfiguration = createInboundEndPointConfiguration(userRole);
        webServiceCallOccurrence = createWebServiceCallOccurrence(endPointConfiguration);

        creationRule = createRuleForEndPointConfigurations("Rule #1", endPointConfiguration);
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }

    protected Message mockWebServiceEvent(WebServiceEventDescription eventType, WebServiceCallOccurrence occurrence) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", eventType.getTopics().stream().findAny().get());
        map.put("id", occurrence.getId());
        String payload = inMemoryPersistence.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    protected IssueEvent mockIssueEvent(WebServiceEventDescription eventType, WebServiceCallOccurrence occurrence) {
        WebServiceEvent event = mock(WebServiceEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        when(event.getEndDevice()).thenReturn(Optional.empty());
        when(event.getUsagePoint()).thenReturn(Optional.empty());
        when(event.getEventType()).thenReturn(eventType.name());
        doAnswer(invocation -> {
            invocation.getArgumentAt(0, WebServiceOpenIssueImpl.class).setWebServiceCallOccurrence(occurrence);
            return null;
        }).when(event).apply(any(WebServiceOpenIssueImpl.class));
        return event;
    }

    protected EndPointConfiguration createInboundEndPointConfiguration(Group userRole) {
        EndPointConfigurationService endPointConfigurationService = inMemoryPersistence.getService(EndPointConfigurationService.class);
        return endPointConfigurationService.newInboundEndPointConfiguration("InboundEndpoint", "InboundService", "resource")
                .logLevel(LogLevel.FINEST)
                .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION)
                .group(userRole)
                .create();
    }

    protected EndPointConfiguration createOutboundEndPointConfiguration() {
        EndPointConfigurationService endPointConfigurationService = inMemoryPersistence.getService(EndPointConfigurationService.class);
        return endPointConfigurationService.newOutboundEndPointConfiguration("OutboundEndpoint", "OutboundService", "http://localhost:8008/resource")
                .logLevel(LogLevel.FINEST)
                .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION)
                .username("user")
                .password("qwerty123")
                .create();
    }

    protected Group createUserRole() {
        return inMemoryPersistence.getService(UserService.class)
                .createGroup("UserRole", "description");
    }

    protected WebServiceCallOccurrence createWebServiceCallOccurrence(EndPointConfiguration endPointConfiguration) {
        WebServiceCallOccurrence occurrence = endPointConfiguration.createEndPointOccurrence(FIXED_TIME, "giveMe", "APP", "<Envelope><Body><GiveMe>try</GiveMe></Body></Envelope>");
        occurrence.setStatus(WebServiceCallOccurrenceStatus.FAILED);
        occurrence.setEndTime(FIXED_TIME.plusSeconds(1));
        occurrence.save();
        return occurrence;
    }

    protected CreationRule createRuleForEndPointConfigurations(String name, EndPointConfiguration... endPointConfigurations) {
        IssueCreationService.CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        props.put(AuthFailureIssueCreationRuleTemplate.END_POINT_CONFIGURATIONS, Arrays.asList(endPointConfigurations));
        return ruleBuilder.setTemplate(AuthFailureIssueCreationRuleTemplate.NAME)
                .setName(name)
                .setIssueType(issueService.findIssueType(WebServiceIssueService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .activate()
                .setDueInTime(DueInType.YEAR, 5)
                .setProperties(props)
                .complete();
    }
}
