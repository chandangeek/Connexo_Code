package com.elster.jupiter.issue.impl.actions;

//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.osgi.framework.BundleContext;
//
//import com.elster.jupiter.issue.impl.actions.CloseIssueAction;
//import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
//import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
//import com.elster.jupiter.issue.share.IssueAction;
//import com.elster.jupiter.issue.share.IssueActionFactory;
//import com.elster.jupiter.issue.share.entity.Issue;
//import com.elster.jupiter.issue.share.entity.IssueStatus;
//import com.elster.jupiter.issue.share.service.IssueService;
//import com.elster.jupiter.nls.Layer;
//import com.elster.jupiter.nls.NlsService;
//import com.elster.jupiter.nls.Thesaurus;
//import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
//import com.elster.jupiter.users.UserService;
//import com.google.inject.AbstractModule;
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//@Ignore
//@RunWith(MockitoJUnitRunner.class)
//public class CloseIssueActionTest {
//    
//    private Injector injector;
//
//    @Mock
//    private BundleContext bundleContext;
//    @Mock
//    private UserService userService;
//    @Mock
//    private NlsService nlsService;
//    @Mock
//    private Thesaurus thesarus;
//    @Mock
//    private IssueService issueService;
//
//    private IssueAction action;
//    
//    @Before
//    public void setUp() {
//        injector = Guice.createInjector(
//                new ThreadSecurityModule(),
//                new AbstractModule() {
//            @Override
//            protected void configure() {
//                bind(BundleContext.class).toInstance(bundleContext);
//                bind(UserService.class).toInstance(userService);
//                bind(NlsService.class).toInstance(nlsService);
//                bind(IssueService.class).toInstance(issueService);
//            }
//        });
//        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesarus);
//        when(thesarus.getString(Matchers.anyString(), Matchers.anyString())).thenReturn("string");
//
//        IssueStatus status = mock(IssueStatus.class);
//        when(issueService.findStatus(IssueStatus.RESOLVED)).thenReturn(Optional.of(status));
//        when(status.isHistorical()).thenReturn(true);
//        IssueActionFactory actionFactory = injector.getInstance(IssueDefaultActionsFactory.class);
//        action = actionFactory.createIssueAction(CloseIssueAction.class.getName());
//    }
//
//    @Test
//    public void testCreateAction() {
//        assertThat(action).isNotNull();
//        assertThat(action instanceof CloseIssueAction).isTrue();
//
//        Map<String, String> params = new HashMap<>();
//        params.put(Parameter.CLOSE_STATUS.getKey(), "status.resolved");
//        assertThat(action.validate(params)).hasSize(0);
//    }
//
//    @Test
//    public void testActionValidation() {
//        when(issueService.findStatus("zxcv")).thenReturn(Optional.empty());
//        
//        Map<String, String> params = new HashMap<>();
//        params.put(Parameter.CLOSE_STATUS.getKey(), "zxcv");
//        assertThat(action.validate(params)).hasSize(1);
//    }
//
//    public void testExecuteAction() {
//        Map<String, String> inputParams = new HashMap<>();
//        inputParams.put(Parameter.COMMENT.getKey(), "some comment");
//
//        Issue issue = mock(Issue.class);
//        when(issue.getId()).thenReturn(1L);
//
//        action.execute(issue, inputParams);
//    }
//    
//    @Test
//    public void testActionAvailabilityDependingOnStatsus() {
//        Issue issue = mock(Issue.class);
//        IssueStatus status = mock(IssueStatus.class);
//        when(issue.getStatus()).thenReturn(status);
//        when(status.getKey()).thenReturn(IssueStatus.OPEN);
//        
//        assertThat(action.isApplicable(issue)).isTrue();
//        when(status.getKey()).thenReturn(IssueStatus.IN_PROGRESS);
//        assertThat(action.isApplicable(issue)).isFalse();
//        when(status.getKey()).thenReturn(IssueStatus.RESOLVED);
//        assertThat(action.isApplicable(issue)).isFalse();
//        when(status.getKey()).thenReturn(IssueStatus.WONT_FIX);
//        assertThat(action.isApplicable(issue)).isFalse();
//    }
//}
