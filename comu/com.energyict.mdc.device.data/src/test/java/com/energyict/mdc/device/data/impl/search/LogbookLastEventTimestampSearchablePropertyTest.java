/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchableProperty;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogbookLastEventTimestampSearchablePropertyTest extends AbstractDateSearchablePropertyTest {

    @Before
    public void initializeMocks() {
        super.initializeMocks();

        NlsMessageFormat propertyName = mock(NlsMessageFormat.class);
        when(propertyName.format(anyVararg())).thenReturn(PropertyTranslationKeys.LOGBOOK_LAST_EVENT.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.LOGBOOK_LAST_EVENT)).thenReturn(propertyName);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.LOGBOOK_LAST_EVENT);
    }

    protected SearchableProperty getTestInstance() {
        return new LogbookLastEventTimestampSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.parentGroup);
    }
}