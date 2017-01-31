/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.bpm.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointAssociationProviderTest {

    private UsagePointProcessAssociationProvider usagePointProcessAssociationProvider;

    @Mock
    TimeService timeService;
    @Mock
    OrmService ormService;
    @Mock
    BeanService beanService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(TimeService.class).toInstance(timeService);
                binder.bind(OrmService.class).toInstance(ormService);
                binder.bind(BeanService.class).toInstance(beanService);
                binder.bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                binder.bind(Thesaurus.class).toInstance(NlsModule.FakeThesaurus.INSTANCE);

                //use real service
                binder.bind(PropertySpecService.class).to(PropertySpecServiceImpl.class);
            }
        });
        this.usagePointProcessAssociationProvider = injector.getInstance(UsagePointProcessAssociationProvider.class);
    }

    @Test
    public void testGetProperties() {
        List<MetrologyConfiguration> metrologyConfigurations = Arrays.asList(
                mockMetrologyConfiguration(1L, "Residential 3phases"),
                mockMetrologyConfiguration(2L, "Residential 2phases"));
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(metrologyConfigurations);

        //Asserts
        assertThat(usagePointProcessAssociationProvider).isNotNull();
        assertThat(usagePointProcessAssociationProvider.getPropertySpecs()).hasSize(2);
        Optional<PropertySpec> metrologyConfigurationProperty = usagePointProcessAssociationProvider.getPropertySpec(TranslationKeys.METROLOGY_CONFIGURATION_PROPERTY.getKey());
        assertThat(metrologyConfigurationProperty).isPresent();
        List<?> possibleValues = metrologyConfigurationProperty.get().getPossibleValues().getAllValues();
        assertThat(possibleValues).hasSize(2);
        assertThat(possibleValues.stream().map(HasIdAndName.class::cast).map(HasIdAndName::getName).collect(Collectors.toList()))
                .containsExactly("Residential 2phases", "Residential 3phases");
    }

    @Test
    public void testMetrologyConfigurationValueFactory() {
        MetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(1050L, "Residential 3phases");
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(metrologyConfiguration));
        UsagePointProcessAssociationProvider.MetrologyConfigurationInfoValuePropertyFactory factory =
                usagePointProcessAssociationProvider.new MetrologyConfigurationInfoValuePropertyFactory();

        //Asserts
        assertThat(factory.fromStringValue("1050").getId()).isEqualTo(metrologyConfiguration.getId());
        assertThat(factory.fromStringValue("1050").getName()).isEqualTo(metrologyConfiguration.getName());
    }

    private MetrologyConfiguration mockMetrologyConfiguration(long id, String name) {
        MetrologyConfiguration mock = mock(MetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(metrologyConfigurationService.findMetrologyConfiguration(id)).thenReturn(Optional.of(mock));
        return mock;
    }
}
