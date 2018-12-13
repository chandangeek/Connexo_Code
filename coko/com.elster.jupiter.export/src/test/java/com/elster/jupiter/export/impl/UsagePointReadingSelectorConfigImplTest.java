/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointReadingSelectorConfigImplTest {

    private static final Range<Instant> EXPORT_INTERVAL = Range.all();

    private UsagePointReadingSelectorConfigImpl selectorConfig;

    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private IExportTask exportTask;
    @Mock
    private RelativePeriod exportPeriod;
    @Mock
    private DataExportOccurrence dataExportOccurrence;
    @Mock
    private DefaultSelectorOccurrence selectorOccurrence;
    @Mock
    private UsagePointGroup usagePointGroup;

    private ReadingType rt15minAPlus, rt15minAMinus, rtDailyAPlus, rtDailyAMinus, notSupportedRT;

    @Before
    public void before() {
        rt15minAPlus = mockReadingType("15.A+");
        rt15minAMinus = mockReadingType("15.A-");
        rtDailyAPlus = mockReadingType("Daily.A+");
        rtDailyAMinus = mockReadingType("Daily.A-");
        notSupportedRT = mockReadingType("NotSupported");
        when(dataModel.getInstance(UsagePointReadingSelectorConfigImpl.class)).thenReturn(new UsagePointReadingSelectorConfigImpl(dataModel));
        when(dataModel.getInstance(ReadingTypeInDataSelector.class)).thenAnswer(invocationOnMock -> new ReadingTypeInDataSelector(meteringService));
        when(dataModel.getInstance(ReadingTypeDataExportItemImpl.class)).thenAnswer(invocationOnMock -> new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel));
        when(dataModel.asRefAny(any())).thenAnswer(invocationOnMock -> {
            Object param = invocationOnMock.getArguments()[0];
            return new FakeRefAny(param);
        });
        when(dataExportOccurrence.getDefaultSelectorOccurrence()).thenReturn(Optional.of(selectorOccurrence));
        when(selectorOccurrence.getExportedDataInterval()).thenReturn(EXPORT_INTERVAL);

        selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, exportTask, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(rt15minAPlus)
                .addReadingType(rt15minAMinus)
                .addReadingType(rtDailyAPlus)
                .addReadingType(rtDailyAMinus)
                .complete();
    }

    @Test
    public void testGetActiveItems() {
        UsagePoint usagePoint = mock(UsagePoint.class);
        Membership<UsagePoint> usagePointMembership = mockUsagePointMember(usagePoint);
        when(usagePointGroup.getMembers(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(usagePointMembership));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMC));
        MetrologyContract informationContract = mock(MetrologyContract.class);
        MetrologyContract billingContract = mock(MetrologyContract.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(informationContract, billingContract);
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        ChannelsContainer informationChannelsContainer = mockChannelContainer(usagePoint, rt15minAPlus, rt15minAMinus);
        when(effectiveMC.getChannelsContainer(informationContract)).thenReturn(Optional.of(informationChannelsContainer));
        ChannelsContainer billingChannelsContainer = mockChannelContainer(usagePoint, rtDailyAPlus, rtDailyAMinus);
        when(effectiveMC.getChannelsContainer(billingContract)).thenReturn(Optional.of(billingChannelsContainer));

        // Business method
        Set<IReadingTypeDataExportItem> activeItems = selectorConfig.getActiveItems(dataExportOccurrence);

        // Asserts
        assertThat(activeItems).hasSize(4);
        Set<ReadingType> itemReadingTypes = activeItems.stream().map(IReadingTypeDataExportItem::getReadingType).collect(Collectors.toSet());
        assertThat(itemReadingTypes).contains(rt15minAPlus, rt15minAMinus, rtDailyAPlus, rt15minAMinus);
        List<IdentifiedObject> identifiedObjects = activeItems.stream().map(IReadingTypeDataExportItem::getDomainObject).distinct().collect(Collectors.toList());
        assertThat(identifiedObjects).hasSize(1);
        assertThat(identifiedObjects.get(0)).isEqualTo(usagePoint);
    }

    @Test
    public void testGetActiveItemsForTwoUsagePointsInGroup() {
        MetrologyContract billingContract = mock(MetrologyContract.class);
        MetrologyContract informationContract = mock(MetrologyContract.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(informationContract, billingContract);

        // mock up 1
        UsagePoint usagePoint1 = mock(UsagePoint.class);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint1.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMC1));
        when(effectiveMC1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        ChannelsContainer informationChannelsContainer = mockChannelContainer(usagePoint1, rt15minAPlus, rt15minAMinus);
        when(effectiveMC1.getChannelsContainer(informationContract)).thenReturn(Optional.of(informationChannelsContainer));
        when(effectiveMC1.getChannelsContainer(billingContract)).thenReturn(Optional.empty());

        // mock up 2
        UsagePoint usagePoint2 = mock(UsagePoint.class);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC2 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint2.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMC2));
        when(effectiveMC2.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        ChannelsContainer billingChannelsContainer = mockChannelContainer(usagePoint2, rtDailyAPlus, rtDailyAMinus);
        when(effectiveMC2.getChannelsContainer(billingContract)).thenReturn(Optional.of(billingChannelsContainer));
        when(effectiveMC2.getChannelsContainer(informationContract)).thenReturn(Optional.empty());

        Membership<UsagePoint> usagePointMembership1 = mockUsagePointMember(usagePoint1);
        Membership<UsagePoint> usagePointMembership2 = mockUsagePointMember(usagePoint2);
        when(usagePointGroup.getMembers(EXPORT_INTERVAL)).thenReturn(Arrays.asList(usagePointMembership1, usagePointMembership2));

        // Business method
        Set<IReadingTypeDataExportItem> activeItems = selectorConfig.getActiveItems(dataExportOccurrence);

        // Asserts
        assertThat(activeItems).hasSize(4);

        List<IReadingTypeDataExportItem> exportItems = activeItems.stream()
                .sorted(Comparator.comparing(item -> item.getReadingType().getMRID()))
                .collect(Collectors.toList());
        assertThat(exportItems.get(0).getReadingType()).isEqualTo(rt15minAPlus);
        assertThat(exportItems.get(0).getDomainObject()).isEqualTo(usagePoint1);
        assertThat(exportItems.get(1).getReadingType()).isEqualTo(rt15minAMinus);
        assertThat(exportItems.get(1).getDomainObject()).isEqualTo(usagePoint1);
        assertThat(exportItems.get(2).getReadingType()).isEqualTo(rtDailyAPlus);
        assertThat(exportItems.get(2).getDomainObject()).isEqualTo(usagePoint2);
        assertThat(exportItems.get(3).getReadingType()).isEqualTo(rtDailyAMinus);
        assertThat(exportItems.get(3).getDomainObject()).isEqualTo(usagePoint2);
    }

    private ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(meteringService.getReadingType(mrid)).thenReturn(Optional.of(readingType));
        return readingType;
    }

    private Membership<UsagePoint> mockUsagePointMember(UsagePoint usagePoint) {
        Membership<UsagePoint> membership = mock(Membership.class);
        when(membership.getMember()).thenReturn(usagePoint);
        return membership;
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(MetrologyContract... contracts) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(contracts));
        return metrologyConfiguration;
    }

    private ChannelsContainer mockChannelContainer(UsagePoint usagePoint, ReadingType... readingTypes) {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(channelsContainer.getReadingTypes(EXPORT_INTERVAL)).thenReturn(ImmutableSet.copyOf(readingTypes));
        return channelsContainer;
    }
}
