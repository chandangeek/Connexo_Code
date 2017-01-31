/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.energyict.mdc.masterdata.MasterDataService;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelIntervalSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TimeService timeService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private MasterDataService masterDataService;

    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;
    private SearchablePropertyGroup channelGroup;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.CHANNEL_INTERVAL.getDefaultFormat());
        when(this.thesaurus.getFormat(PropertyTranslationKeys.CHANNEL_INTERVAL)).thenReturn(messageFormat);

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        this.channelGroup = new ChannelSearchablePropertyGroup(this.thesaurus);
    }

    @Test
    public void testGetDomain() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testGroup() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(ChannelSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testVisibility() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.CHANNEL_INTERVAL);
    }

    @Test
    public void testSpecification() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ChannelIntervalSearchableProperty.TimeDurationWrapper.class);
    }

    @Test
    public void testPossibleValues() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test
    public void testPropertyHasNoConstraints() {
        ChannelIntervalSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(0);
    }

    private ChannelIntervalSearchableProperty getTestInstance() {
        return new ChannelIntervalSearchableProperty(this.propertySpecService, this.masterDataService, this.thesaurus).init(this.domain, this.channelGroup);
    }
}