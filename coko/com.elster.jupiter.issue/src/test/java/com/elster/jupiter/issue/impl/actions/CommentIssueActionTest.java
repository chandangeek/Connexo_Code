package com.elster.jupiter.issue.impl.actions;

//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.osgi.framework.BundleContext;
//
//import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
//import com.elster.jupiter.issue.share.IssueAction;
//import com.elster.jupiter.issue.share.IssueActionFactory;
//import com.elster.jupiter.issue.share.entity.Issue;
//import com.elster.jupiter.issue.share.entity.IssueStatus;
//import com.elster.jupiter.issue.share.service.IssueService;
//import com.elster.jupiter.nls.Layer;
//import com.elster.jupiter.nls.NlsService;
//import com.elster.jupiter.nls.Thesaurus;
//import com.elster.jupiter.security.thread.ThreadPrincipalService;
//import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
//import com.elster.jupiter.users.User;
//import com.elster.jupiter.users.UserService;
//import com.google.inject.AbstractModule;
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//
//public class CommentIssueActionTest {
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
//        User user = mock(User.class);
//        when(user.getId()).thenReturn(1L);
//        when(user.getName()).thenReturn("console");
//        ThreadPrincipalService principalService = injector.getInstance(ThreadPrincipalService.class);
//        principalService.set(user);
//        when(nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesarus);
//        when(thesarus.getString(Matchers.anyString(), Matchers.anyString())).thenReturn("string");
//        IssueActionFactory actionFactory = injector.getInstance(IssueDefaultActionsFactory.class);
//        action = actionFactory.createIssueAction(CommentIssueAction.class.getName());
//    }
//
//    @Test
//    public void testCreateAction() {
//        assertThat(action).isNotNull();
//        assertThat(action instanceof CommentIssueAction).isTrue();
//
//        Map<String, String> params = new HashMap<>();
//        params.put(CommentIssueAction.ISSUE_COMMENT, "comment");
////        assertThat(action.validate(params)).hasSize(0);
//        assertThat(action.getPropertySpecs()).hasSize(1);
//    }
//
//    @Test
//    public void testActionValidation() {
//        Issue issue = mock(Issue.class);
//        when(issue.getId()).thenReturn(1L);
//
//        Map<String, Object> params = new HashMap<>();
//        params.put(CommentIssueAction.ISSUE_COMMENT, null);
//        action.execute(issue, params);
//        verify(issue, times(0)).getId();
//
//        params.put(CommentIssueAction.ISSUE_COMMENT, "");
//        action.execute(issue, params);
//        verify(issue, times(0)).getId();
//
//        params.put(CommentIssueAction.ISSUE_COMMENT, " ");
//        action.execute(issue, params);
//        verify(issue, times(0)).getId();
//
//    }
//
//    @Test
//    public void testExecuteAction() {
//        Map<String, Object> inputParams = new HashMap<>();
//        inputParams.put(CommentIssueAction.ISSUE_COMMENT, "some comment");
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
//        
//        when(status.isHistorical()).thenReturn(false);
//        assertThat(action.isApplicable(issue)).isTrue();
//        
//        when(status.isHistorical()).thenReturn(true);
//        assertThat(action.isApplicable(issue)).isTrue();
//    }
//}
