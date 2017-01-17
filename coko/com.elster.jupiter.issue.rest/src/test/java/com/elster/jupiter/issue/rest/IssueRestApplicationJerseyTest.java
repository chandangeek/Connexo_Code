package com.elster.jupiter.issue.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.rest.impl.IssueApplication;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    IssueService issueService;
    @Mock
    IssueCreationService issueCreationService;
    @Mock
    IssueAssignmentService issueAssignmentService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    MeteringService meteringService;
    @Mock
    UserService userService;
    @Mock
    NlsService nlsService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    IssueInfoFactoryService issueInfoFactoryService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;


    @Provider
    @javax.annotation.Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Override
    protected Application getApplication() {
        IssueApplication application = new IssueApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        when(thesaurus.join(thesaurus)).thenReturn(thesaurus);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        when(issueService.getIssueAssignmentService()).thenReturn(issueAssignmentService);
        when(nlsService.getThesaurus("ISR", Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        application.setIssueService(issueService);
        application.setMeteringService(meteringService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
        application.setIssueInfoFactoryService(issueInfoFactoryService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
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

    protected IssueType mockIssueType(String key, String name){
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        when(issueType.getPrefix()).thenReturn(name+key);
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

    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName){
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mock(User.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(workGroupId);
        when(workGroup.getName()).thenReturn(workGroupName);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(userName);
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);
        return assignee;
    }

    protected OpenIssue getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice());
    }

    protected User getDefaultUser() {
        return mockUser(1, "Admin");
    }

    protected Meter getDefaultDevice() {
        return mockMeter(1, "DefaultDevice");
    }

    protected Meter mockMeter(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getName()).thenReturn(name);
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        doReturn(Optional.empty()).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        return meter;
    }

    protected OpenIssue mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter) {
        OpenIssue issue = mock(OpenIssue.class);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        com.elster.jupiter.issue.share.Priority priority = Priority.DEFAULT;
        when(issue.getPriority()).thenReturn(priority);
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

    protected IssueAssignee getDefaultAssignee(){
        return mockAssignee(1L, "Admin", 1L, "WorkGroup");
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

    protected CreationRuleTemplate mockCreationRuleTemplate(String name, String description, IssueType issueType, List<PropertySpec> properties){
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn(name);
        when(template.getDisplayName()).thenReturn("Display Name: " + name);
        when(template.getDescription()).thenReturn(description);
        when(template.getIssueType()).thenReturn(issueType);
        when(template.getPropertySpecs()).thenReturn(properties);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXTAREA;
        PropertyInfo propertyInfo = new PropertyInfo("property", "property", null, propertyTypeInfo, false);
        if (properties != null && !properties.isEmpty()) {
            when(propertyValueInfoService.getPropertyInfos(template.getPropertySpecs())).thenReturn(Collections.singletonList(propertyInfo));
        }
        return template;
    }

    protected CreationRuleTemplate getDefaultCreationRuleTemplate() {
        IssueType issueType = getDefaultIssueType();
        return mockCreationRuleTemplate("0-1-2", "Description", issueType, mockPropertySpecs());
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

    protected IssueAction mockIssueAction(String name){
        IssueAction action = mock(IssueAction.class);
        when(action.getDisplayName()).thenReturn(name);
        List<PropertySpec> propertySpec = mockPropertySpecs();
        when(action.getPropertySpecs()).thenReturn(propertySpec);
        return action;
    }

    protected IssueAction mockIssueActionWithoutProperties(String name) {
        IssueAction action = mock(IssueAction.class);
        when(action.getDisplayName()).thenReturn(name);
        when(action.getPropertySpecs()).thenReturn(Collections.emptyList());
        return action;
    }

    protected IssueActionType mockIssueActionType(long id, String name, IssueType issueType){
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueAction(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        return type;
    }

    protected IssueActionType mockIssueActionTypWithoutProperties(long id, String name, IssueType issueType){
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueActionWithoutProperties(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        return type;
    }

    protected IssueActionType getDefaultIssueActionType(){
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionType(1, "send", issueType);
    }

    protected IssueActionType getCloseIssueActionType(){
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionTypWithoutProperties(1, "close", issueType);
    }

    protected CreationRule mockCreationRule(long id, String name) {
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
        when(rule.getActions()).thenReturn(Collections.emptyList());
        when(rule.getProperties()).thenReturn(Collections.emptyMap());
        when(rule.getTemplate()).thenReturn(template);
        when(rule.getModTime()).thenReturn(instant);
        when(rule.getCreateTime()).thenReturn(instant);
        when(rule.getVersion()).thenReturn(2L);
        return rule;
    }

    protected List<PropertySpec> mockPropertySpecs() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.isRequired()).thenReturn(true);
        return Collections.singletonList(propertySpec);
    }

    protected void mockTranslation(TranslationKeys translationKey) {
        NlsMessageFormat nlsMessageFormat = this.mockNlsMessageFormat(translationKey.getDefaultFormat());
        when(thesaurus.getFormat(translationKey)).thenReturn(nlsMessageFormat);
    }

    protected NlsMessageFormat mockNlsMessageFormat(String translation) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translation);
        return messageFormat;
    }

    protected IssueGroupFilter mockIssueGroupFilter() {
        IssueGroupFilter issueGroupFilter = mock(IssueGroupFilter.class);
        when(issueGroupFilter.using(Matchers.<Class>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.onlyGroupWithKey(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withIssueTypes(Matchers.<List<String>>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withStatuses(Matchers.<List<String>>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withMeterName(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.groupBy(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.setAscOrder(false)).thenReturn(issueGroupFilter);
        when(issueGroupFilter.from(1L)).thenReturn(issueGroupFilter);
        when(issueGroupFilter.to(2L)).thenReturn(issueGroupFilter);
        return issueGroupFilter;
    }

}
