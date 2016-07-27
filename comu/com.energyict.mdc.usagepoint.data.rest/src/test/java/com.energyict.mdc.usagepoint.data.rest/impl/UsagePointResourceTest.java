package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends UsagePointApplicationJerseyTest {

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private State deviceState;
    @Mock
    private MeterInfoFactory meterInfoFactory;

    private ReadingType readingType;

    @Before
    public void before() {
        readingType = mockReadingType("11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        when(meteringService.findUsagePoint("testUP")).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMRID()).thenReturn("testUP");
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getStart()).thenReturn(Instant.ofEpochMilli(1410774620100L));
        when(meterActivation.getEnd()).thenReturn(null);
        when(meter.getMRID()).thenReturn("testD");
        when(deviceState.getName()).thenReturn("Decommissioned");
        when(device.getSerialNumber()).thenReturn("123");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getState()).thenReturn(deviceState);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("testDT");
        when(deviceService.findByUniqueMrid("testD")).thenReturn(Optional.of(device));
        doReturn(Collections.singletonList(meterActivation)).when(usagePoint).getMeterActivations();
    }

    @Test
    public void testDevicesHistory() {
        String json = target("/usagepoints/testUP/history/devices").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String>get("$.devices[0].mRID")).isEqualTo("testD");
        assertThat(jsonModel.<String>get("$.devices[0].serialNumber")).isEqualTo("123");
        assertThat(jsonModel.<String>get("$.devices[0].state")).isEqualTo("Decommissioned");
        assertThat(jsonModel.<Number>get("$.devices[0].start")).isEqualTo(1410774620100L);
        assertThat(jsonModel.<Boolean>get("$.devices[0].active")).isEqualTo(true);
        assertThat(jsonModel.<Number>get("$.devices[0].deviceType.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devices[0].deviceType.name")).isEqualTo("testDT");
    }

    @Test
    public void testGetNoChannels() {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        //Business method
        String json = target("/usagepoints/testUP/channels").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.channels")).hasSize(0);
    }

    @Test
    public void testGetChannels() {
        EffectiveMetrologyConfigurationOnUsagePoint mc = mockEffectiveMetrologyConfiguration();
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(mc));

        //Business method
        String json = target("/usagepoints/testUP/channels").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.channels")).hasSize(1);
        assertThat(jsonModel.<Long>get("$.channels[0].dataUntil")).isEqualTo(1467710958704L);
        assertThat(jsonModel.<Integer>get("$.channels[0].interval.count")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.channels[0].interval.timeUnit")).isEqualTo("days");
        assertThat(jsonModel.<String>get("$.channels[0].readingType.mRID")).isEqualTo("11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        assertThat(jsonModel.<List>get("$.channels[0].deviceChannels")).hasSize(2);
        assertThat(jsonModel.<Long>get("$.channels[0].deviceChannels[0].from")).isEqualTo(1410774620100L);
        assertThat(jsonModel.<Long>get("$.channels[0].deviceChannels[0].until")).isNull();
        assertThat(jsonModel.<String>get("$.channels[0].deviceChannels[0].mRID")).isEqualTo("testD");
        assertThat(jsonModel.<String>get("$.channels[0].deviceChannels[0].channel.name")).isEqualTo("testR");
        assertThat(jsonModel.<Integer>get("$.channels[0].deviceChannels[0].channel.id")).isEqualTo(1);
        assertThat(jsonModel.<Long>get("$.channels[0].deviceChannels[1].from")).isEqualTo(1410515420000L);
        assertThat(jsonModel.<Long>get("$.channels[0].deviceChannels[1].until")).isEqualTo(1410774620100L);
        assertThat(jsonModel.<String>get("$.channels[0].deviceChannels[1].mRID")).isEqualTo("testOldDevice");
        assertThat(jsonModel.<String>get("$.channels[0].deviceChannels[1].channel.name")).isEqualTo("testR");
        assertThat(jsonModel.<Integer>get("$.channels[0].deviceChannels[1].channel.id")).isNull();
    }

    private EffectiveMetrologyConfigurationOnUsagePoint mockEffectiveMetrologyConfiguration() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        Channel channel = mock(Channel.class);
        ReadingTypeRequirementNode readingTypeRequirementNode = mock(ReadingTypeRequirementNode.class);
        ReadingTypeRequirement readingTypeRequirement = mock(ReadingTypeRequirement.class);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        Formula formula = mock(Formula.class);
        ExpressionNode expressionNode = mock(ExpressionNode.class);
        TemporalAmount intervalLength = mock(TemporalAmount.class);
        com.energyict.mdc.device.data.Channel deviceChannel = mock(com.energyict.mdc.device.data.Channel.class);

        Meter oldMeter = mock(Meter.class);
        when(oldMeter.getMRID()).thenReturn("testOldDevice");
        MeterActivation oldMeterActivation = mock(MeterActivation.class);
        when(oldMeterActivation.getMeter()).thenReturn(Optional.of(oldMeter));
        when(oldMeterActivation.getStart()).thenReturn(Instant.ofEpochMilli(1410515420000L));
        when(oldMeterActivation.getEnd()).thenReturn(Instant.ofEpochMilli(1410774620100L));
        when(oldMeterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        Device oldDevice = mock(Device.class);
        when(deviceService.findByUniqueMrid("testOldDevice")).thenReturn(Optional.of(oldDevice));
        when(oldDevice.getChannels()).thenReturn(Collections.emptyList());

        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(channel.getLastDateTime()).thenReturn(Instant.ofEpochMilli(1467710958704L));
        when(intervalLength.get(ChronoUnit.DAYS)).thenReturn(1L);
        when(channel.getIntervalLength()).thenReturn(Optional.of(intervalLength));
        when(metrologyConfiguration.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(deliverable.getReadingType()).thenReturn(readingType);
        doReturn(Arrays.asList(oldMeterActivation, meterActivation)).when(usagePoint).getMeterActivations();
        when(deliverable.getFormula()).thenReturn(formula);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(formula.getExpressionNode()).thenReturn(expressionNode);
        doReturn(Collections.singletonList(readingType)).when(channel).getReadingTypes();
        when(readingTypeRequirement.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(channel));
        when(readingTypeRequirementNode.getReadingTypeRequirement()).thenReturn(readingTypeRequirement);
        when(expressionNode.accept(any(ExpressionNode.Visitor.class))).then(visitor -> ((ExpressionNode.Visitor) visitor.getArguments()[0]).visitRequirement(readingTypeRequirementNode));
        when(device.getChannels()).thenReturn(Collections.singletonList(deviceChannel));
        when(deviceChannel.getReadingType()).thenReturn(readingType);
        when(deviceChannel.getId()).thenReturn(1L);

        return effectiveMetrologyConfiguration;
    }
}