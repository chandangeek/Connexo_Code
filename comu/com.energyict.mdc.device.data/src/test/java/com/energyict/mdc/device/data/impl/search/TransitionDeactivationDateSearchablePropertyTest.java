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
public class TransitionDeactivationDateSearchablePropertyTest extends AbstractDateSearchablePropertyTest {

    @Before
    public void initializeMocks() {
        super.initializeMocks();

        NlsMessageFormat propertyName = mock(NlsMessageFormat.class);
        when(propertyName.format(anyVararg())).thenReturn(PropertyTranslationKeys.TRANSITION_DEACTIVATION.getDefaultFormat());
        when(this.thesaurus.getFormat(PropertyTranslationKeys.TRANSITION_DEACTIVATION)).thenReturn(propertyName);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.TRANSITION_DEACTIVATION);
    }

    protected SearchableProperty getTestInstance() {
        return new TransitionDeactivationDateSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.parentGroup);
    }
}