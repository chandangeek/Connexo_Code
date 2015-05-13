package com.elster.jupiter.issue.rest;

import com.elster.jupiter.issue.rest.impl.resource.ActionResource;
import com.elster.jupiter.issue.rest.impl.resource.AssigneeResource;
import com.elster.jupiter.issue.rest.impl.resource.CreationRuleResource;
import com.elster.jupiter.issue.rest.impl.resource.IssueTypeResource;
import com.elster.jupiter.issue.rest.impl.resource.MeterResource;
import com.elster.jupiter.issue.rest.impl.resource.ReasonResource;
import com.elster.jupiter.issue.rest.impl.resource.RuleResource;
import com.elster.jupiter.issue.rest.impl.resource.StatusResource;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleOrActionValidationExceptionMapper;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Ignore("Mocks")
public class Mocks extends JerseyTest {

    protected static IssueService issueService;
    protected static IssueCreationService issueCreationService;
    protected static IssueAssignmentService issueAssignmentService;
    protected static IssueActionService issueActionService;

    protected static MeteringService meteringService;
    protected static UserService userService;
    protected static NlsService nlsService;
    protected static Thesaurus thesaurus;
    protected static TransactionService transactionService;
    protected static RestQueryService restQueryService;
    protected static SecurityContext securityContext;


    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        issueService = mock(IssueService.class);
        issueCreationService = mock(IssueCreationService.class);
        issueAssignmentService = mock(IssueAssignmentService.class);
        issueActionService = mock(IssueActionService.class);

        meteringService = mock(MeteringService.class);
        userService = mock(UserService.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
        transactionService = mock(TransactionService.class);
        restQueryService = mock(RestQueryService.class);
        securityContext = mock(SecurityContext.class);

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(issueService, issueCreationService, issueAssignmentService, issueActionService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                AssigneeResource.class,
                RuleResource.class,
                ReasonResource.class,
                StatusResource.class,
                CreationRuleResource.class,
                MeterResource.class,
                IssueTypeResource.class,
                ActionResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class,
                CreationRuleOrActionValidationExceptionMapper.class,
                SecurityRequestFilter.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(issueService).to(IssueService.class);
                bind(issueAssignmentService).to(IssueAssignmentService.class);
                bind(issueCreationService).to(IssueCreationService.class);
                bind(issueActionService).to(IssueActionService.class);

                bind(userService).to(UserService.class);
                bind(meteringService).to(MeteringService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);

                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        super.configureClient(config);
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal){
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        return status;
    }

    protected IssueStatus getDefaultStatus(){
        return mockStatus("1", "open", false);
    }

    protected IssueType mockIssueType(String uuid, String name){
        IssueType issueType = mock(IssueType.class);
        when(issueType.getUUID()).thenReturn(uuid);
        when(issueType.getName()).thenReturn(name);
        return issueType;
    }

    protected IssueType getDefaultIssueType(){
        return mockIssueType("datacollection", "Data Collection");
    }

    protected IssueReason mockReason(String key, String name, IssueType issueType){
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        return reason;
    }

    protected IssueReason getDefaultReason(){
        return mockReason("1", "Reason", getDefaultIssueType());
    }

    protected Meter mockMeter(long id, String mrid){
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getMRID()).thenReturn(mrid);
        MeterActivation meterActivation = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        return meter;
    }

    protected Meter getDefaultMeter(){
        return mockMeter(1, "0.0.0.0.0.0.0.0");
    }

    protected IssueAssignee mockAssignee(long id, String name, String type){
        IssueAssignee assignee = mock(IssueAssignee.class);
        when(assignee.getId()).thenReturn(id);
        when(assignee.getName()).thenReturn(name);
        when(assignee.getType()).thenReturn(type);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee(){
        return mockAssignee(1, "Admin", IssueAssignee.Types.USER);
    }

    protected AssignmentRule mockAssignmentRule(long id, String title, String description, long version, IssueAssignee assignee){
        AssignmentRule rule = mock(AssignmentRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getTitle()).thenReturn(title);
        when(rule.getDescription()).thenReturn(description);
        when(rule.getVersion()).thenReturn(version);
        when(rule.getAssignee()).thenReturn(assignee);
        return rule;
    }

    protected AssignmentRule getDefaultAssignmentRule(){
        IssueAssignee assignee = getDefaultAssignee();
        return mockAssignmentRule(1, "Assignment Rule", "Description", 1, assignee);
    }

    protected CreationRuleTemplate mockCreationRuleTemplate(String uuid, String name, String description, IssueType issueType, Map<String, ParameterDefinition> parameters){
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getUUID()).thenReturn(uuid);
        when(template.getName()).thenReturn(name);
        when(template.getDescription()).thenReturn(description);
        if (issueType != null) {
            String itUuid = issueType.getUUID();
            when(template.getIssueType()).thenReturn(itUuid);
        }
        when(template.getParameterDefinitions()).thenReturn(parameters);
        return template;
    }

    protected CreationRuleTemplate getDefaultCreationRuleTemplate(){
        IssueType issueType = getDefaultIssueType();
        return mockCreationRuleTemplate("0-1-2", "Template 1", "Description", issueType, null);
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

    protected User getDefaultUser(){
        return mockUser(1, "Admin");
    }

    protected IssueAction mockIssueAction(String name){
        IssueAction action = mock(IssueAction.class);
        when(action.getLocalizedName()).thenReturn(name);
        when(action.getParameterDefinitions()).thenReturn(Collections.<String, ParameterDefinition>emptyMap());
        return action;
    }

    protected IssueAction getDefaultIssueAction(){
        return mockIssueAction("Send To Inspect");
    }

    protected IssueActionType mockIssueActionType(long id, String name, IssueType issueType){
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueAction(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        return type;
    }

    protected IssueActionType getDefaultIssueActionType(){
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionType(1, "send", issueType);
    }
    
    protected Issue getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultMeter());
    }
    
     protected CreationRule mockCreationRule(long id, String name){
        Instant instant = Instant.now();
        IssueReason reason = getDefaultReason();
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        CreationRule rule = mock(CreationRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getName()).thenReturn(name);
        when(rule.getComment()).thenReturn("comment");
        when(rule.getReason()).thenReturn(reason);
        when(rule.getDueInType()).thenReturn(DueInType.DAY);
        when(rule.getDueInValue()).thenReturn(5L);
        when(rule.getActions()).thenReturn(Collections.<CreationRuleAction>emptyList());
        when(rule.getParameters()).thenReturn(null);
        when(rule.getTemplate()).thenReturn(template);
        when(rule.getModTime()).thenReturn(instant);
        when(rule.getCreateTime()).thenReturn(instant);
        when(rule.getVersion()).thenReturn(2L);
        return rule;
    }

    protected CreationRule getDefaultCreationRule(){
        return mockCreationRule(1, "Rule 1");
    }

    protected Issue mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        Issue issue = mock(Issue.class);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        return issue;
    }
    
    protected IssueComment mockComment(long id, String text, User user) {
        IssueComment comment = mock(IssueComment.class);
        when(comment.getId()).thenReturn(id);
        when(comment.getComment()).thenReturn(text);
        when(comment.getCreateTime()).thenReturn(Instant.EPOCH);
        when(comment.getVersion()).thenReturn(1L);
        when(comment.getUser()).thenReturn(user);
        return comment;
    }
}
