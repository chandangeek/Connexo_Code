package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.jayway.jsonpath.JsonModel;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceTest extends UsagePointDataRestApplicationJerseyTest {
    @Mock
    private UsagePoint usagePoint;

    @Before
    public void before() {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(1, "mc");
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfiguration));
    }

    @Test
    public void testGetUsagePointPurposes() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.empty());

        // Business method
        String json = target("/usagepoints/MRID/purposes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.purposes[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.purposes[0].name")).isEqualTo(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        assertThat(jsonModel.<Boolean>get("$.purposes[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.purposes[0].active")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.purposes[0].status.id")).isEqualTo("incomplete");
    }

    @Test
    public void testGetOutputsOfUsagePointPurpose() {
        Optional<UsagePointMetrologyConfiguration> usagePointMetrologyConfiguration = Optional.of(mockMetrologyConfigurationWithContract(1, "1test"));
        when(usagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);

        // Business method
        String json = target("/usagepoints/MRID/purposes/1/outputs").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        // channel output
        assertThat(jsonModel.<Number>get("$.outputs[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputs[0].outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.outputs[0].name")).isEqualTo("regular RT");
        assertThat(jsonModel.<Number>get("$.outputs[0].interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.outputs[0].interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.outputs[0].readingType.mRID")).isEqualTo("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[0].formula.description")).isEqualTo("Formula Description");
        // register output
        assertThat(jsonModel.<Number>get("$.outputs[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.outputs[1].outputType")).isEqualTo("register");
        assertThat(jsonModel.<String>get("$.outputs[1].name")).isEqualTo("irregular RT");
        assertThat(jsonModel.<String>get("$.outputs[1].readingType.mRID")).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[1].formula.description")).isEqualTo("Formula Description");
    }

    @Test
    public void testGetOutputById() {
        Optional<UsagePointMetrologyConfiguration> usagePointMetrologyConfiguration = Optional.of(mockMetrologyConfigurationWithContract(1, "1test"));
        when(usagePoint.getMetrologyConfiguration()).thenReturn(usagePointMetrologyConfiguration);

        // Business method
        String json = target("/usagepoints/MRID/purposes/1/outputs/1").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("regular RT");
        assertThat(jsonModel.<Number>get("$.interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.formula.description")).isEqualTo("Formula Description");
    }
}
