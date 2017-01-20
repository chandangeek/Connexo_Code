package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeUnitConversion;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.ChannelOutputInfo;
import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.RegisterOutputInfo;

public class OutputInfoFactory {

    private final ValidationStatusFactory validationStatusFactory;
    private final ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public OutputInfoFactory(ValidationStatusFactory validationStatusFactory,
                             ReadingTypeDeliverableFactory readingTypeDeliverableFactory,
                             ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.validationStatusFactory = validationStatusFactory;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
    }

    public OutputInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            return asChannelOutputInfo(readingTypeDeliverable, effectiveMetrologyConfiguration, metrologyContract);
        } else {
            return asRegisterOutputInfo(readingTypeDeliverable, effectiveMetrologyConfiguration, metrologyContract);
        }
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
    }

    private RegisterOutputInfo asRegisterOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        RegisterOutputInfo outputInfo = new RegisterOutputInfo();
        setCommonFields(outputInfo, readingTypeDeliverable);
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        outputInfo.deliverableType = readingTypeDeliverable.getType().getName();
        channelsContainer
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel -> {
                    outputInfo.validationInfo = new UsagePointValidationStatusInfo();
                    outputInfo.validationInfo.hasSuspects = validationStatusFactory.hasSuspects(Collections.singletonList(outputChannel), channelsContainer
                            .get()
                            .getRange());
                });
        return outputInfo;
    }

    private RegisterOutputInfo asFullRegisterOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        RegisterOutputInfo outputInfo = new RegisterOutputInfo();
        setCommonFields(outputInfo, readingTypeDeliverable);
        outputInfo.deliverableType = readingTypeDeliverable.getType().getName();
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel ->
                        outputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, Collections.singletonList(outputChannel))
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
        } else if (macroPeriod.equals(MacroPeriod.WEEKLYS)) {
            timeDuration = TimeDuration.weeks(1);
        }
        outputInfo.interval = new TimeDurationInfo(timeDuration);
        outputInfo.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit()) ? "flow" : "volume";
        return outputInfo;
    }

    private OutputInfo asChannelOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        ChannelOutputInfo channelOutputInfo = asChannelCommonOutputInfo(readingTypeDeliverable);
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        channelsContainer
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel -> {
                    channelOutputInfo.validationInfo = new UsagePointValidationStatusInfo();
                    channelOutputInfo.validationInfo.hasSuspects = validationStatusFactory.hasSuspects(Collections.singletonList(outputChannel), channelsContainer.get()
                            .getRange());
                });
        return channelOutputInfo;
    }

    private OutputInfo asFullChannelOutputInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        ChannelOutputInfo channelOutputInfo = asChannelCommonOutputInfo(readingTypeDeliverable);
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel ->
                        channelOutputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, Collections.singletonList(outputChannel))
                );
        return channelOutputInfo;
    }
}
