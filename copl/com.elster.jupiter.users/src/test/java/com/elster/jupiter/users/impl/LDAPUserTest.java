/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.users.LdapUser;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LDAPUserTest extends EqualsContractTest {
    private LdapUser ldapUser;

    private static final String USER_NAME = "userName";
    private static final String OTHER_USER_NAME = "otherserName";
    private static final String USER_DN = "ou_group, uid=userName";
    private static final boolean STATUS_ACTIVE = true;

    @Before
    public void equalsContractSetUp() {
        super.equalsContractSetUp();
    }

    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (ldapUser == null) {
            ldapUser =  new LdapUserImpl().init(USER_NAME, USER_DN, STATUS_ACTIVE);
        }
        return ldapUser;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new LdapUserImpl().init(USER_NAME, USER_DN, STATUS_ACTIVE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        LdapUser ldapUserC =  new LdapUserImpl().init(OTHER_USER_NAME, USER_DN, STATUS_ACTIVE);
        return Collections.singletonList(ldapUserC);
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
