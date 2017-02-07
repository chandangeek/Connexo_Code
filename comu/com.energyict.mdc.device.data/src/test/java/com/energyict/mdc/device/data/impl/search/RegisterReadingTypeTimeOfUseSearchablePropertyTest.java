/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RegisterReadingTypeTimeOfUseSearchablePropertyTest extends AbstractReadingTypeTimeOfUseSearchablePropertyTest{

    private RegisterSearchablePropertyGroup registerSearchablePropertyGroup;

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        this.registerSearchablePropertyGroup = new RegisterSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        RegisterReadingTypeTimeOfUseSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(RegisterSearchablePropertyGroup.GROUP_NAME);
    }

    protected RegisterReadingTypeTimeOfUseSearchableProperty getTestInstance() {
        return new RegisterReadingTypeTimeOfUseSearchableProperty(propertySpecService, thesaurus).init(this.domain, registerSearchablePropertyGroup);
    }
}
