/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySetSearchablePropertyGroupTest {

    @Mock
    private CustomPropertySet<?, ?> customPropertySet;

    @Before
    public void setUp() {
        when(this.customPropertySet.getId()).thenReturn("custom.property.set");
        when(this.customPropertySet.getName()).thenReturn("Name");
    }

    private CustomPropertySetSearchablePropertyGroup getTestInstance() {
        return new CustomPropertySetSearchablePropertyGroup(this.customPropertySet);
    }


    @Test
    public void testGetId() {
        assertThat(getTestInstance().getId()).isEqualTo("custom.property.set");
    }

    @Test
    public void testGetDisplayName() {
        assertThat(getTestInstance().getDisplayName()).isEqualTo("Name");
    }
}
