package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.GrantRefusedException;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.Expects.expect;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DualControlPrivilegeSubscriberTest {


    private DualControlPrivilegeSubscriber dualControlPrivilegeSubscriber;

    @Mock
    private Privilege grantPrivilege, approvePrivilege, innocuousPrivilege;
    @Mock
    private Group grantGroup, approveGroup, innocuousGroup;
    @Mock
    private PrivilegeCategory grantCategory, approveCategory, innocuousCategory;
    @Mock
    private UserService userService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PrivilegeThesaurus privilegeThesaurus;

    @Before
    public void setUp() {
        when(grantGroup.getPrivileges(null)).thenReturn(Collections.singletonList(grantPrivilege));
        when(approveGroup.getPrivileges(null)).thenReturn(Collections.singletonList(approvePrivilege));
        when(innocuousGroup.getPrivileges(null)).thenReturn(Collections.singletonList(innocuousPrivilege));
        when(grantPrivilege.getCategory()).thenReturn(grantCategory);
        when(approvePrivilege.getCategory()).thenReturn(approveCategory);
        when(innocuousPrivilege.getCategory()).thenReturn(innocuousCategory);
        when(approveCategory.getName()).thenReturn(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY);
        when(grantCategory.getName()).thenReturn(DualControlService.DUAL_CONTROL_GRANT_CATEGORY);
        when(innocuousCategory.getName()).thenReturn("Innocuous");
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);;
        when(thesaurus.getFormat((TranslationKey) any())).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format()).thenReturn("");
        when(privilegeThesaurus.translatePrivilegeKey(anyString())).thenReturn("privilegeKey");

        dualControlPrivilegeSubscriber = new DualControlPrivilegeSubscriber(thesaurus, privilegeThesaurus);
        dualControlPrivilegeSubscriber.setUserService(userService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDisallowUserToGroup() {

        User user = mock(User.class);

        when(user.getPrivileges()).thenReturn(Collections.singleton(grantPrivilege));


        expect(() -> {
            dualControlPrivilegeSubscriber.handle(user, approveGroup);
        })
                .toThrow(GrantRefusedException.class);

    }

    @Test
    public void testDisallowPrivilegeToGroup1() {

        User user = mock(User.class);
        when(userService.findUsers(innocuousGroup)).thenReturn(Collections.singleton(user));
        when(user.getPrivileges()).thenReturn(Collections.singleton(grantPrivilege));

        expect(() -> {
            dualControlPrivilegeSubscriber.handle(innocuousGroup, approvePrivilege);
        })
                .toThrow(GrantRefusedException.class);

    }

    @Test
    public void testDisallowPrivilegeToGroup2() {

        User user = mock(User.class);
        when(userService.findUsers(innocuousGroup)).thenReturn(Collections.singleton(user));
        when(user.getPrivileges()).thenReturn(Collections.singleton(approvePrivilege));

        expect(() -> {
            dualControlPrivilegeSubscriber.handle(innocuousGroup, grantPrivilege);
        })
                .toThrow(GrantRefusedException.class);

    }

}