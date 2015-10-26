package com.elster.jupiter.issue.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.rest.impl.IssueApplication;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
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
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    TransactionService transactionService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    static SecurityContext securityContext;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
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
        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        application.setIssueService(issueService);
        application.setMeteringService(meteringService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setUserService(userService);
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

    protected CreationRuleTemplate mockCreationRuleTemplate(String name, String description, IssueType issueType, List<PropertySpec> properties){
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn(name);
        when(template.getDisplayName()).thenReturn("Display Name: " + name);
        when(template.getDescription()).thenReturn(description);
        when(template.getIssueType()).thenReturn(issueType);
        when(template.getPropertySpecs()).thenReturn(properties);
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

}