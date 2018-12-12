/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchableProperty;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractReadingTypeNameSearchablePropertyTest extends AbstractNameSearchablePropertyTest {

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.READING_TYPE_NAME.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.READING_TYPE_NAME)).thenReturn(messageFormat);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.READING_TYPE_NAME);
    }
}
