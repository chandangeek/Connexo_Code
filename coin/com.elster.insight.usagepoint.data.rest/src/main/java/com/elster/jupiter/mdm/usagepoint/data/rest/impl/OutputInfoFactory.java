/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeUnitConversion;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummary;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.ChannelOutputInfo;
import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.RegisterOutputInfo;

public class OutputInfoFactory {

    private final ValidationStatusFactory validationStatusFactory;
    private final ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final Thesaurus thesaurus;
    private final UsagePointDataCompletionService usagePointDataCompletionService;
    private final ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory;
    private final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

    @Inject
    public OutputInfoFactory(ValidationStatusFactory validationStatusFactory,
                             ReadingTypeDeliverableFactory readingTypeDeliverableFactory,
                             ReadingTypeInfoFactory readingTypeInfoFactory,
                             UsagePointDataCompletionService usagePointDataCompletionService,
                             ChannelDataValidationSummaryInfoFactory validationSummaryInfoFactory,
                             Thesaurus thesaurus) {
        this.validationStatusFactory = validationStatusFactory;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
        this.thesaurus = thesaurus;
        this.usagePointDataCompletionService = usagePointDataCompletionService;
        this.validationSummaryInfoFactory = validationSummaryInfoFactory;
    }

    public List<OutputInfo> deliverablesAsOutputInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract, Range<Instant> interval) {
        List<OutputInfo> result = new ArrayList<>();

        Map<ReadingTypeDeliverable, List<IChannelDataCompletionSummary>> dataCompletionStatistics = (interval != null ? usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, interval) : Collections
                .emptyMap());
        Map<ReadingTypeDeliverable, UsagePointValidationStatusInfo> validationStatusInfo = validationStatusFactory.getValidationStatusInfoForDeliverables(effectiveMetrologyConfiguration, metrologyContract, interval);
        for (ReadingTypeDeliverable readingTypeDeliverable : metrologyContract.getDeliverables()) {
            OutputInfo outputInfo;
            if (readingTypeDeliverable.getReadingType().isRegular()) {
                outputInfo = asChannelCommonOutputInfo(readingTypeDeliverable);
            } else {
                outputInfo = asRegisterCommonOutputInfo(readingTypeDeliverable);
            }
            outputInfo.validationInfo = validationStatusInfo.get(readingTypeDeliverable);
            if (dataCompletionStatistics.containsKey(readingTypeDeliverable)) {
                outputInfo.summary = validationSummaryInfoFactory.from(readingTypeDeliverable, dataCompletionStatistics.get(readingTypeDeliverable));
            }
            result.add(outputInfo);
        }
        return result;
    }


    public OutputInfo asFullInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            return asFullChannelOutputInfo(readingTypeDeliverable, effectiveMetrologyConfiguration, metrologyContract);
        } else {
            return asFullRegisterOutputInfo(readingTypeDeliverable, effectiveMetrologyConfiguration, metrologyContract);
        }
    }

    private void setCommonFields(OutputInfo outputInfo, ReadingTypeDeliverable readingTypeDeliverable) {
        outputInfo.id = readingTypeDeliverable.getId();
        outputInfo.name = readingTypeDeliverable.getName();
        outputInfo.readingType = readingTypeInfoFactory.from(readingTypeDeliverable.getReadingType());
        outputInfo.formula = readingTypeDeliverable.getFormula() != null ? FormulaInfo.asInfo(readingTypeDeliverableFactory.asInfo(readingTypeDeliverable).formula.description) : null;
        outputInfo.hasEvent = aggregatesWithEventDate.contains(readingTypeDeliverable.getReadingType().getAggregate());
        outputInfo.isCummulative = readingTypeDeliverable.getReadingType().isCumulative();
        outputInfo.isBilling = readingTypeDeliverable.getReadingType().getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD);
    }

    private RegisterOutputInfo asRegisterCommonOutputInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        RegisterOutputInfo outputInfo = new RegisterOutputInfo();
        setCommonFields(outputInfo, readingTypeDeliverable);
        outputInfo.deliverableType = readingTypeDeliverable.getType().getName();
        return outputInfo;
    }

    /*private void setValidationFields(OutputInfo outputInfo, ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                     MetrologyContract metrologyContract, Range<Instant> interval) {
        ChannelsContainer container = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT,
                        metrologyContract.getId(), effectiveMetrologyConfiguration.getUsagePoint().getName()) {
                });
        Optional.of(container).flatMap(channelContainer -> channelContainer.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel -> {
                    if (interval != null) {
                        List<IChannelDataCompletionSummary> channelDataCompletionSummaryList = container.getChannel(readingTypeDeliverable.getReadingType())
                                // channel cannot be unfound
                                .map(channel -> usagePointDataCompletionService.getDataCompletionStatistics(channel, interval))
                                .orElse(Collections.singletonList(usagePointDataCompletionService.getGeneralUsagePointDataCompletionSummary(interval)));
                        outputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, outputChannel, interval);
                        outputInfo.summary = validationSummaryInfoFactory.from(readingTypeDeliverable, channelDataCompletionSummaryList);
                    } else {
                        outputInfo.validationInfo = new UsagePointValidationStatusInfo();
                        outputInfo.validationInfo.hasSuspects = validationStatusFactory.hasSuspects(Collections.singletonList(outputChannel), container.getRange());
                    }
                });
    }*/

    private RegisterOutputInfo asFullRegisterOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        RegisterOutputInfo outputInfo = asRegisterCommonOutputInfo(readingTypeDeliverable);
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel ->
                        outputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, outputChannel, null)
                );
        return outputInfo;
    }

    private ChannelOutputInfo asChannelCommonOutputInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        ChannelOutputInfo outputInfo = new ChannelOutputInfo();
        setCommonFields(outputInfo, readingTypeDeliverable);

        ReadingType readingType = readingTypeDeliverable.getReadingType();
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        TimeDuration timeDuration = null;
        if (!measuringPeriod.equals(TimeAttribute.NOTAPPLICABLE)) {
            timeDuration = TimeDuration.minutes(measuringPeriod.getMinutes());
        } else if (macroPeriod.equals(MacroPeriod.DAILY)) {
            timeDuration = TimeDuration.days(1);
        } else if (macroPeriod.equals(MacroPeriod.MONTHLY)) {
            timeDuration = TimeDuration.months(1);
        } else if (macroPeriod.equals(MacroPeriod.YEARLY)) {
            timeDuration = TimeDuration.years(1);
        } else if (macroPeriod.equals(MacroPeriod.WEEKLYS)) {
            timeDuration = TimeDuration.weeks(1);
        }
        outputInfo.interval = new TimeDurationInfo(timeDuration);
        outputInfo.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit()) ? "flow" : "volume";
        return outputInfo;
    }

    private OutputInfo asFullChannelOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        ChannelOutputInfo channelOutputInfo = asChannelCommonOutputInfo(readingTypeDeliverable);
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel ->
                        channelOutputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, outputChannel, null)
                );
        return channelOutputInfo;
    }
}
