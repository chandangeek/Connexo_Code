package com.elster.jupiter.issue.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;

public class IssueActionServiceImplTest extends BaseTest {
    @Test
    public void testIssueActionTypes() {
        Query<IssueActionType> actionTypeQuery =  getIssueActionService().getActionTypeQuery();
        List<IssueActionType> actionTypeList = actionTypeQuery.select(Condition.TRUE);
        assertThat(actionTypeList).isNotEmpty();

        Issue issue;
        IssueActionType type = new IssueActionTypeImpl(getDataModel(), getIssueActionService());
        Optional<IssueType> issueTypeRef = getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID);
        assertThat(issueTypeRef).isNotEqualTo(Optional.empty());
        try (TransactionContext context = getContext()) {
            issue = createIssueMinInfo();
            IssueActionType actionType = getIssueActionService().createActionType("fakefactoryId", "classname", issueTypeRef.get());
            Optional<IssueActionType> foundIssueTypeRef = getIssueActionService().findActionType(actionType.getId());
            assertThat(foundIssueTypeRef).isNotEqualTo(Optional.empty());
            type = foundIssueTypeRef.get();
        }
        try {
            getIssueActionService().executeAction(type, issue, Collections.emptyMap());
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Action Factory with provided factoryId: fakefactoryId doesn't exist");
        }
    }

    @Test
    public void testActionFactoryRegistration() {
        deactivateEnvironment();
        setEnvironment();
        
        IssueServiceImpl impl = IssueServiceImpl.class.cast(getIssueService());
        IssueActionFactory factory = getMockIssueActionFactory();
        impl.addIssueActionFactory(factory);
        assertThat(getIssueActionService().getRegisteredFactories().size()).isEqualTo(1);
        Optional<IssueAction> action = getIssueActionService().createIssueAction(factory.getId(), "classname");
        assertThat(action).isNotNull();
        assertThat(action.get()).isNotNull();
        impl.removeIssueActionFactory(factory);
        assertThat(getIssueActionService().getRegisteredFactories()).isEmpty();
    }
    
    @Test
    public void testActionFactoryNotFound() {
        Optional<IssueAction> action = getIssueActionService().createIssueAction("Fake factory id", "Fake issue action");
        assertThat(action.isPresent()).isFalse();
    }

    @Test
    public void testCreateIssueActionDublicate() {
        deactivateEnvironment();
        setEnvironment();
        try (TransactionContext context = getContext()) {
            Optional<IssueType> issueTypeRef = getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID);
            IssueActionType actionType = getIssueActionService().createActionType("factoryId1", "classname1", issueTypeRef.get());
            long id = actionType.getId();
            actionType = getIssueActionService().createActionType("factoryId1", "classname1", issueTypeRef.get());
            assertThat(actionType.getId()).isEqualTo(id);
            // 1 default action type + 1 factoryId1 (classname1)
            assertThat(getIssueActionService().getActionTypeQuery().select(Condition.TRUE).size()).isEqualTo(2);
        }
        try (TransactionContext context = getContext()) {
            Optional<IssueReason> reasonRef = getIssueService().findReason(ISSUE_DEFAULT_REASON);
            IssueActionType actionType = getIssueActionService().createActionType("factoryId1", "classname1", reasonRef.get());
            long id = actionType.getId();
            actionType = getIssueActionService().createActionType("factoryId1", "classname1", reasonRef.get());
            assertThat(actionType.getId()).isEqualTo(id);
            // 1 default action type + 1 factoryId1 (classname1)
            assertThat(getIssueActionService().getActionTypeQuery().select(Condition.TRUE).size()).isEqualTo(2);
        }
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.property1", strict = true)
    public void testExecuteAction() {
        IssueType issueType = getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).get();
        IssueActionType actionType = getIssueActionService().createActionType("Action Factory", "Action ClassName", issueType);
        IssueActionFactory issueActionFactory = mock(IssueActionFactory.class);
        when(issueActionFactory.getId()).thenReturn("Action Factory");
        ((IssueServiceImpl)getIssueService()).addIssueActionFactory(issueActionFactory);
        IssueAction issueAction = new TestIssueAction(getDataModel(), mock(Thesaurus.class), getPropertySpecService());
        when(issueActionFactory.createIssueAction("Action ClassName")).thenReturn(issueAction);
        
        getIssueActionService().executeAction(actionType, createIssueMinInfo(), Collections.emptyMap());
    }
    
    private static class TestIssueAction extends AbstractIssueAction {

        protected TestIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
            super(dataModel, thesaurus, propertySpecService);
        }

        @Override
        public IssueActionResult execute(Issue issue) {
            DefaultActionResult result = new IssueActionResult.DefaultActionResult();
            result.success();
            return result;
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    getPropertySpecService().stringPropertySpec("property1", true, "default"),
                    getPropertySpecService().stringPropertySpec("property2", false, "default")
            );
        }

        @Override
        public String getDisplayName() {
            return "Test issue action";
        }
    }
}
