package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchableProperty;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileLastReadingSearchablePropertyTest extends AbstractDateSearchablePropertyTest {

    @Before
    public void initializeMocks() {
        super.initializeMocks();

        NlsMessageFormat propertyName = mock(NlsMessageFormat.class);
        when(propertyName.format(anyVararg())).thenReturn(PropertyTranslationKeys.LOADPROFILE_LAST_READING.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.LOADPROFILE_LAST_READING)).thenReturn(propertyName);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.LOADPROFILE_LAST_READING);
    }

    protected SearchableProperty getTestInstance() {
        return new LoadProfileLastReadingSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.parentGroup);
    }
}