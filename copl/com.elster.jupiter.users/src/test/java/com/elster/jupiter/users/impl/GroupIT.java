package com.elster.jupiter.users.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Group;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class GroupIT extends EqualsContractTest {
    private Group group;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private Publisher publisher;

    private static final String TEST_GROUP_NAME = "groupName";
    private static final String TEST_GROUP_DESCRIPTION = "groupName";
    private static final long ID = 0;
    private static final long OTHER_ID = 1;

    @Before
    public void equalsContractSetUp() {
         super.equalsContractSetUp();
    }

    @After
    public void tearDown() {
    }
    private void setId(Object entity, long id) {

        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (group == null) {
            group =  new GroupImpl(mock(QueryService.class),dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
            setId(group, ID);
        }
        return group;
    }

    @Override
    protected Object getInstanceEqualToA() {
      Group  groupB =  new GroupImpl(mock(QueryService.class), dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
        setId(groupB, ID);
        return groupB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Group  groupC = new  GroupImpl(mock(QueryService.class),dataModel, userService, threadPrincipalService, thesaurus, publisher).init(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
        setId(groupC, OTHER_ID);
        return Collections.singletonList(groupC);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

}

