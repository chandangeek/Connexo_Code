package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    private static final String DESCRIPTION = "description";
    private static final String AUTH_NAME = "authName";

    private UserServiceImpl userService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private DataMapper<User> userFactory;

    @Before
    public void setUp() {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.getDataMapper(User.class, UserImpl.class, TableSpecs.USR_USER.name())).thenReturn(userFactory);

        userService = new UserServiceImpl();

        userService.setOrmService(ormService);

        Bus.setServiceLocator(userService);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(userService);
    }

    @Test
    public void testCreateUser() {
        User user = userService.createUser(AUTH_NAME, DESCRIPTION);

        assertThat(user.getName()).isEqualTo(AUTH_NAME);
        assertThat(user.getDescription()).isEqualTo(DESCRIPTION);

    }

    @Test
    public void testCreateUserPersists() {
        User user = userService.createUser(AUTH_NAME, DESCRIPTION);

        verify(userFactory).persist(user);

    }


}
