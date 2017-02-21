/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterObisCodeSearchablePropertyTest extends AbstractObisCodeSearchablePropertyTest {

    private RegisterSearchablePropertyGroup registerSearchablePropertyGroup;

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.REGISTER_OBISCODE.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.REGISTER_OBISCODE)).thenReturn(messageFormat);
        this.registerSearchablePropertyGroup = new RegisterSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        RegisterObisCodeSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(RegisterSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testTranslation() {
        RegisterObisCodeSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.REGISTER_OBISCODE);
    }

    protected RegisterObisCodeSearchableProperty getTestInstance() {
        return new RegisterObisCodeSearchableProperty(propertySpecService, thesaurus).init(domain, registerSearchablePropertyGroup);
    }
}
