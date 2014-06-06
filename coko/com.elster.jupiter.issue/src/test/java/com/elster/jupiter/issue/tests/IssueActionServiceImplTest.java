package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.impl.service.IssueActionServiceImpl;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueActionServiceImplTest extends BaseTest {
    @Test
    public void testIssueActionTypes() {
        Query<IssueActionType> actionTypeQuery =  getIssueActionService().getActionTypeQuery();
        List<IssueActionType> actionTypeList = actionTypeQuery.select(Condition.TRUE);
        assertThat(actionTypeList).isEmpty();

        IssueActionType type = new IssueActionTypeImpl(getDataModel(), getIssueActionService());
        Optional<IssueType> issueTypeRef = getIssueService().findIssueType("datacollection");
        assertThat(issueTypeRef).isNotEqualTo(Optional.absent());
        try (TransactionContext context = getContext()) {
            IssueActionType actionType = getIssueActionService().createActionType("fakefactoryId", "classname", issueTypeRef.get());
            Optional<IssueActionType> foundIssueTypeRef = getIssueActionService().findActionType(actionType.getId());
            assertThat(foundIssueTypeRef).isNotEqualTo(Optional.absent());
            type = foundIssueTypeRef.get();
        }
        try {
            getIssueActionService().executeAction(type, createIssueMinInfo(), new HashMap<String, String>());
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Action Factory with provided factoryId: fakefactoryId doesn't exist");
        }
    }

    @Test
    public void testActionFactoryRegistration() {
        IssueActionServiceImpl impl = IssueActionServiceImpl.class.cast(getIssueActionService());
        IssueActionFactory factory = getMockIssueActionFactory();
        impl.addIssueActionFactory(factory, new HashMap<String, Object>());
        assertThat(getIssueActionService().getRegisteredFactories().size()).isEqualTo(1);
        IssueAction action = getIssueActionService().createIssueAction(factory.getId(), "classname");
        assertThat(action).isNotNull();
        impl.removeIssueActionFactory(factory);
        assertThat(getIssueActionService().getRegisteredFactories()).isEmpty();
    }
}
