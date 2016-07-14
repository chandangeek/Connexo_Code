package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class OutputInfoFactory {
    private final ValidationStatusFactory validationStatusFactory;

    @Inject
    public OutputInfoFactory(ValidationStatusFactory validationStatusFactory) {
        this.validationStatusFactory = validationStatusFactory;
    }

    private OutputInfo mainInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        OutputInfo outputInfo = new OutputInfo();
        TimeDuration timeDuration = null;
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        outputInfo.id = readingTypeDeliverable.getId();
        outputInfo.name = readingTypeDeliverable.getName();
        outputInfo.readingType = new ReadingTypeInfo(readingType);
        outputInfo.formula = readingTypeDeliverable.getFormula() != null ? FormulaInfo.asInfo(readingTypeDeliverable.getFormula()) : null;
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

    public OutputInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        OutputInfo outputInfo = mainInfo(readingTypeDeliverable);
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        channelsContainer
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel -> {
                    outputInfo.validationInfo = new UsagePointValidationStatusInfo();
                    outputInfo.validationInfo.hasSuspects = validationStatusFactory.hasSuspects(Collections.singletonList(outputChannel), channelsContainer.get().getRange());
                });
        return outputInfo;
    }

    public OutputInfo fullInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        OutputInfo outputInfo = mainInfo(readingTypeDeliverable);
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()))
                .ifPresent(outputChannel ->
                        outputInfo.validationInfo = validationStatusFactory.getValidationStatusInfo(effectiveMetrologyConfiguration, metrologyContract, Collections.singletonList(outputChannel))
                );
        return outputInfo;
    }
}
