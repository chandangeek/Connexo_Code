package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 11/03/2016
 * Time: 12:10
 */
@RunWith(MockitoJUnitRunner.class)
public class FirmwareVersionNameSearchablePropertyTest  extends AbstractNameSearchablePropertyTest {
    private SearchablePropertyGroup parentGroup;

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.FIRMWARE_VERSION.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.FIRMWARE_VERSION)).thenReturn(messageFormat);

        this.parentGroup = new FirmwareVersionSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(FirmwareVersionSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.FIRMWARE_VERSION);
    }

    protected SearchableProperty getTestInstance() {
        return new FirmwareVersionNameSearchableProperty(propertySpecService, thesaurus).init(this.domain, this.parentGroup);
    }


}
