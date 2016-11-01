package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointReadingDataSelectorImplTest {

    private static final ZonedDateTime START = ZonedDateTime.of(2014, 6, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END = ZonedDateTime.of(2014, 7, 19, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> EXPORT_INTERVAL = Range.openClosed(START.toInstant(), END.toInstant());

    private Clock clock = Clock.systemDefaultZone();

    private TransactionService transactionService;

    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private IExportTask task;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private UsagePoint usagePoint1, usagePoint2;
    @Mock
    private ReadingType readingType;
    @Mock
    private Logger logger;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock(extraInterfaces = DefaultSelectorOccurrence.class)
    private IDataExportOccurrence occurrence;
    @Mock
    private RelativePeriod exportPeriod;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ChannelsContainer channelContainer1, channelContainer2;

    @Before
    public void setUp() {
        transactionService = new TransactionVerifier();

        doAnswer(invocation -> new UsagePointReadingSelectorConfigImpl(dataModel))
                .when(dataModel).getInstance(UsagePointReadingSelectorConfigImpl.class);
        doAnswer(invocation -> new ReadingTypeInDataSelector(meteringService))
                .when(dataModel).getInstance(ReadingTypeInDataSelector.class);
        doAnswer(invocation -> new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel))
                .when(dataModel).getInstance(ReadingTypeDataExportItemImpl.class);
        doAnswer(invocation -> new UsagePointReadingSelector(dataModel, transactionService, thesaurus))
                .when(dataModel).getInstance(UsagePointReadingSelector.class);
        doAnswer(invocation -> new UsagePointReadingItemDataSelector(clock, validationService, thesaurus, transactionService))
                .when(dataModel).getInstance(UsagePointReadingItemDataSelector.class);
        doAnswer(invocation -> new FakeRefAny(invocation.getArguments()[0])).when(dataModel).asRefAny(any());

        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenAnswer(invocation1 -> {
                String defaultFormat = ((MessageSeed) invocation.getArguments()[0]).getDefaultFormat();
                return MessageFormat.format(defaultFormat, invocation1.getArguments());
            });
            return messageFormat;
        });

        doReturn(Optional.of(occurrence)).when(occurrence).getDefaultSelectorOccurrence();
        when(occurrence.getTask()).thenReturn(task);
        doReturn(EXPORT_INTERVAL).when((DefaultSelectorOccurrence) occurrence).getExportedDataInterval();

        List<UsagePointMembership> memberships = Arrays.asList(mockUsagePointMember(usagePoint1), mockUsagePointMember(usagePoint2));
        when(usagePointGroup.getMembers(EXPORT_INTERVAL)).thenReturn(memberships);

        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint1 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint1.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfigurationOnUsagePoint1.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelContainer1));
        when(usagePoint1.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint1));
        when(channelContainer1.getUsagePoint()).thenReturn(Optional.of(usagePoint1));

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint2 = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint2.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfigurationOnUsagePoint2.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelContainer2));
        when(usagePoint2.getEffectiveMetrologyConfigurations(EXPORT_INTERVAL)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint2));
        when(channelContainer2.getUsagePoint()).thenReturn(Optional.of(usagePoint2));

        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
    }

    private UsagePointMembership mockUsagePointMember(UsagePoint usagePoint) {
        UsagePointMembership membership = mock(UsagePointMembership.class);
        when(membership.getUsagePoint()).thenReturn(usagePoint);
        return membership;
    }

    @Test
    public void testSelectWithComplete() {
        UsagePointReadingSelectorConfigImpl selectorConfig = UsagePointReadingSelectorConfigImpl.from(dataModel, task, exportPeriod);
        selectorConfig.startUpdate()
                .setUsagePointGroup(usagePointGroup)
                .addReadingType(readingType)
                .setExportOnlyIfComplete(true)
                .setValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .complete();
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));

        when(channelContainer1.toList(readingType, EXPORT_INTERVAL)).thenReturn(Arrays.asList(START.toInstant(), END.toInstant()));

        // Business method
        List<ExportData> exportData = selectorConfig.createDataSelector(logger).selectData(occurrence).collect(Collectors.toList());

        // Asserts
        assertThat(exportData).hasSize(1);
    }

    private static class FakeRefAny implements RefAny {
        private final Object value;

        public FakeRefAny(Object value) {
            this.value = value;
        }

        @Override
        public boolean isPresent() {
            return value != null;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Optional<?> getOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public String getComponent() {
            return "";
        }

        @Override
        public String getTableName() {
            return "";
        }

        @Override
        public Object[] getPrimaryKey() {
            return new Object[0];
        }
    }
}
