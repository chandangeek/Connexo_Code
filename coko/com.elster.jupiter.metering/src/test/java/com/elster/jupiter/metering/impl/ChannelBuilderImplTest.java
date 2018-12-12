/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelBuilderImplTest {

    public static final String RT_PRIMARY_DELTA_CODE = "rtPrimaryDeltaCode";
    private ChannelBuilderImpl channelBuilder;

    private Clock clock = Clock.systemDefaultZone();

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private IdsService idsService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Meter meter;
    @Mock
    private MeterConfiguration configuration;
    @Mock
    private MeterReadingTypeConfiguration readingTypeConfigRegular, readingTypeConfigRegister;
    @Mock
    private IReadingType rtSecondaryBulk, rtSecondaryDelta, rtPrimaryBulk, rtPrimaryDelta, rtPrimaryRegister, rtSecondaryRegister;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private DataMapper<IReadingType> readingTypeMapper;

    @Before
    public void setUp() {
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.empty());
        when(meter.getConfiguration(any())).thenReturn(Optional.of(configuration));
        when(configuration.getReadingTypeConfigs()).thenReturn(Arrays.asList(readingTypeConfigRegular, readingTypeConfigRegister));
        when(readingTypeConfigRegular.getMeasured()).thenReturn(rtSecondaryBulk);
        when(readingTypeConfigRegular.getCalculated()).thenReturn(Optional.of(rtPrimaryBulk));
        when(readingTypeConfigRegister.getMeasured()).thenReturn(rtSecondaryRegister);
        when(readingTypeConfigRegister.getCalculated()).thenReturn(Optional.of(rtPrimaryRegister));

        when(rtPrimaryBulk.isCumulative()).thenReturn(true);
        when(rtPrimaryBulk.isRegular()).thenReturn(true);
        when(rtSecondaryBulk.isCumulative()).thenReturn(true);
        when(rtSecondaryBulk.isRegular()).thenReturn(true);
        when(rtPrimaryRegister.isCumulative()).thenReturn(false);
        when(rtPrimaryRegister.isRegular()).thenReturn(false);
        when(rtSecondaryRegister.isCumulative()).thenReturn(false);
        when(rtSecondaryRegister.isRegular()).thenReturn(false);
        when(rtPrimaryBulk.builder()).thenReturn(ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_PRIMARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .period(TimeAttribute.MINUTE15)
        );
        when(rtSecondaryBulk.builder()).thenReturn(ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .period(TimeAttribute.MINUTE15)
        );
        when(rtSecondaryRegister.builder()).thenReturn(ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.INSTANTANEOUS)
                .period(TimeAttribute.NOTAPPLICABLE)
        );
        when(rtPrimaryDelta.isBulkQuantityReadingType(rtPrimaryBulk)).thenReturn(true);
        when(rtSecondaryDelta.isBulkQuantityReadingType(rtSecondaryBulk)).thenReturn(true);
        when(dataModel.mapper(IReadingType.class)).thenReturn(readingTypeMapper);
        when(readingTypeMapper.getOptional("0.0.2.4.0.2.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(rtPrimaryDelta));
        when(readingTypeMapper.getOptional("0.0.2.4.0.1.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(rtSecondaryDelta));
        when(readingTypeMapper.getOptional("0.0.0.12.0.2.0.0.0.0.0.0.0.0.0.0.0.0")).thenReturn(Optional.of(rtSecondaryRegister));
        when(dataModel.getInstance(ReadingTypeInChannel.class)).thenAnswer(invocation -> new ReadingTypeInChannel(dataModel, meteringService));
        when(rtSecondaryBulk.getIntervalLength()).thenReturn(Optional.of(Duration.of(15, ChronoUnit.MINUTES)));
        when(rtPrimaryDelta.getIntervalLength()).thenReturn(Optional.of(Duration.of(15, ChronoUnit.MINUTES)));
        when(rtSecondaryDelta.getIntervalLength()).thenReturn(Optional.of(Duration.of(15, ChronoUnit.MINUTES)));
        when(rtSecondaryRegister.getIntervalLength()).thenReturn(Optional.empty());
        when(rtSecondaryRegister.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rtPrimaryRegister.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rtPrimaryRegister.getIntervalLength()).thenReturn(Optional.empty());
        when(idsService.getVault(eq(MeteringService.COMPONENTNAME), anyInt())).thenReturn(Optional.of(vault));
        when(channelsContainer.getZoneId()).thenReturn(TimeZoneNeutral.getMcMurdo());
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, RecordSpecs.BULKQUANTITYINTERVAL.ordinal() + 1)).thenReturn(Optional.of(recordSpec));
        when(idsService.getRecordSpec(MeteringService.COMPONENTNAME, RecordSpecs.BASEREGISTER_WITH_MULTIPLIED_REGISTER.ordinal() + 1)).thenReturn(Optional.of(recordSpec));

        this.channelBuilder = new ChannelBuilderImpl(dataModel, () -> new ChannelImpl(dataModel, idsService, meteringService, clock, eventService));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBuildWithMultiplier() {
        channelBuilder.channelsContainer(channelsContainer);

        channelBuilder.readingTypes(rtSecondaryBulk);
        ChannelImpl channel = channelBuilder.build();

        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(rtPrimaryDelta, rtSecondaryBulk));
        assertThat(channel.getDerivationRule(rtPrimaryDelta)).isEqualTo(DerivationRule.MULTIPLIED_DELTA);
        assertThat(channel.getDerivationRule(rtSecondaryBulk)).isEqualTo(DerivationRule.MEASURED);
    }

    @Test
    public void testBuildWithoutMultiplier() {
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());

        channelBuilder.channelsContainer(channelsContainer);

        channelBuilder.readingTypes(rtSecondaryBulk);
        ChannelImpl channel = channelBuilder.build();

        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(rtSecondaryDelta, rtSecondaryBulk));
        assertThat(channel.getDerivationRule(rtSecondaryDelta)).isEqualTo(DerivationRule.DELTA);
        assertThat(channel.getDerivationRule(rtSecondaryBulk)).isEqualTo(DerivationRule.MEASURED);
    }

    @Test
    public void testDontAddIfExplicitlyAdded() {
        channelBuilder.channelsContainer(channelsContainer);

        channelBuilder.readingTypes(rtSecondaryDelta, rtSecondaryBulk);
        ChannelImpl channel = channelBuilder.build();

        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(rtSecondaryDelta, rtSecondaryBulk));
        assertThat(channel.getDerivationRule(rtSecondaryDelta)).isEqualTo(DerivationRule.MEASURED);
        assertThat(channel.getDerivationRule(rtSecondaryBulk)).isEqualTo(DerivationRule.MEASURED);
    }

    @Test
    public void testBuildRegisterWithMultiplier() {
        channelBuilder.channelsContainer(channelsContainer);

        channelBuilder.readingTypes(rtSecondaryRegister);
        ChannelImpl channel = channelBuilder.build();

        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(rtPrimaryRegister, rtSecondaryRegister));
        assertThat(channel.getDerivationRule(rtPrimaryRegister)).isEqualTo(DerivationRule.MULTIPLIED);
        assertThat(channel.getDerivationRule(rtSecondaryRegister)).isEqualTo(DerivationRule.MEASURED);
    }

}