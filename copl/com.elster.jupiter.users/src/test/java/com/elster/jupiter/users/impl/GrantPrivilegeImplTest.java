/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;

import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrantPrivilegeImplTest {

    private static final String NAME = "grantAnything";
    @Mock
    private PrivilegeCategory defaultCategory;
    @Mock
    private DataModel dataModel;
    @Mock
    private Resource resource;
    @Mock
    private PrivilegeCategory category1, category2;

    @Before
    public void setUp() {
        when(dataModel.getInstance(GrantPrivilegeImpl.class)).thenAnswer(invocation -> new GrantPrivilegeImpl(dataModel));
        when(dataModel.getInstance(GrantableCategory.class)).thenAnswer(invocation -> new GrantableCategory());
    }

    @After
    public void tearDown() {

    }
    @Test
    public void testCreateGrantPrivilege() {
        GrantPrivilegeImpl grantPrivilege = GrantPrivilegeImpl.from(dataModel, NAME, resource, defaultCategory);
        grantPrivilege.addGrantableCategory(category1);
        grantPrivilege.addGrantableCategory(category2);

        assertThat(grantPrivilege.getCategory()).isEqualTo(defaultCategory);
        assertThat(grantPrivilege.getName()).isEqualTo(NAME);
        assertThat(grantPrivilege.grantableCategories()).isEqualTo(ImmutableSet.of(category1, category2));
    }

}
