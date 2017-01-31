/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchableProperty;
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
public class LogbookNameSearchablePropertyTest extends AbstractNameSearchablePropertyTest {
    private SearchablePropertyGroup parentGroup;

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.LOGBOOK_NAME.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.LOGBOOK_NAME)).thenReturn(messageFormat);

        this.parentGroup = new LogbookSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(LogbookSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.LOGBOOK_NAME);
    }

    protected SearchableProperty getTestInstance() {
        return new LogbookNameSearchableProperty(propertySpecService, thesaurus).init(this.domain, this.parentGroup);
    }
}
