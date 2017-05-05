/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test {@link MainCheckValidator}
 */
@RunWith(MockitoJUnitRunner.class)
abstract public class MainCheckValidatorTest {

    /*
    @Mock
    protected Thesaurus thesaurus;
    */
    Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    MetrologyPurpose CHECK_PURPOSE;

    @Mock
    MetrologyPurpose validatingMetrologyPurpose;

    protected PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    protected Range<Instant> range = Range.closed(Utils.instant("20160101000000"),Utils.instant("20160207000000"));

    MainCheckValidator initValidator(ValidationConfiguration validationConfiguration) {
        MainCheckValidator validator = new MainCheckValidator(thesaurus, propertySpecService, validationConfiguration.rule
                .createProperties(), validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService);
        validator.init(validationConfiguration.checkChannel, validationConfiguration.readingType, range);
        return validator;
    }

    /**
     * Describes configuration to be validated
     */
    class ValidationConfiguration {

        // internal properties - input
        ValidatorRule rule;
        ChannelReadings mainChannelReadings;
        ValidatedChannelReadings checkChannelReadings;

        // external mocks - output
        ReadingType readingType;
        Channel mainChannel;
        Channel checkChannel;
        ValidationService validationService;
        MetrologyConfigurationService metrologyConfigurationService;

        public ValidationConfiguration(ValidatorRule rule, ChannelReadings mainChannelReadings, ValidatedChannelReadings checkChannelReadings) {
            this.rule = rule;
            this.mainChannelReadings = mainChannelReadings;
            this.checkChannelReadings = checkChannelReadings;
            mockAll();
        }

        void mockAll() {
            readingType = mock(ReadingType.class);
            when(readingType.getMRID()).thenReturn("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            when(readingType.getFullAliasName()).thenReturn("[Daily] Secondary Delta A+ (kWh)");
            mainChannel = mock(Channel.class);
            UsagePoint usagePoint = mock(UsagePoint.class);
            when(usagePoint.getName()).thenReturn("Usage point name");
            MetrologyContractChannelsContainer channelsContainer = mock(MetrologyContractChannelsContainer.class);
            MetrologyContract validatingMetrologyContract = mock(MetrologyContract.class);
            when(validatingMetrologyContract.getMetrologyPurpose()).thenReturn(validatingMetrologyPurpose);
            when(channelsContainer.getMetrologyContract()).thenReturn(validatingMetrologyContract);
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
            UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);

            MetrologyContract metrologyContract = mock(MetrologyContract.class);
            when(metrologyContract.getMetrologyPurpose()).thenReturn(rule.checkPurpose);
            when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
            when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
            when(usagePoint.getEffectiveMetrologyConfigurations(range)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
            when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
            when(mainChannel.getChannelsContainer()).thenReturn(channelsContainer);
            when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)).thenReturn(Optional
                    .of(channelsContainer));

            checkChannel = checkChannelReadings.mockChannel(range);

            when(checkChannel.getChannelsContainer()).thenReturn(channelsContainer);

            when(channelsContainer.getChannel(readingType)).thenReturn(rule.noCheckChannel?Optional.empty():Optional.of(checkChannel));

            validationService = mock(ValidationService.class);
            ValidationEvaluator validationEvaluator = checkChannelReadings.mockEvaluator();
            when(validationService.getEvaluator()).thenReturn(validationEvaluator);

            metrologyConfigurationService = mock(MetrologyConfigurationService.class);
            when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Collections.singletonList(rule.checkPurpose));

        }
    }
}
