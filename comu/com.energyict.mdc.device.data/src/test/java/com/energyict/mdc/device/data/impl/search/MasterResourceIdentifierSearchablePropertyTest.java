package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MasterResourceIdentifierSearchableProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (10:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterResourceIdentifierSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DataModel dataModel;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private DataVaultService dataVaultService;

    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(this.timeService, this.ormService);
        this.propertySpecService = new PropertySpecServiceImpl(this.jupiterPropertySpecService, this.dataVaultService, this.ormService);
    }

    @Test
    public void testGetDomain() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testSingleSelection() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testTranslation() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.DEVICE_MRID.getDefaultFormat());
        when(this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_MRID)).thenReturn(messageFormat);
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.DEVICE_MRID);
    }

    @Test
    public void specificationIsNotAReference() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test
    public void noConstraints() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(searchableProperty);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayString() {
        MasterResourceIdentifierSearchableProperty property = this.getTestInstance();
        String valueToDisplay = "displayString";

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(valueToDisplay);
    }

    private MasterResourceIdentifierSearchableProperty getTestInstance() {
        return new MasterResourceIdentifierSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain);
    }

}