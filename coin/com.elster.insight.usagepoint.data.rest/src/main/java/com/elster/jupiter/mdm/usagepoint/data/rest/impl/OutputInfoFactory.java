package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.time.TimeDuration;

import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.ChannelOutputInfo;
import static com.elster.jupiter.mdm.usagepoint.data.rest.impl.OutputInfo.RegisterOutputInfo;

public class OutputInfoFactory {

    public OutputInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        if (readingTypeDeliverable.getReadingType().isRegular()) {
            return asChannelOutputInfo(readingTypeDeliverable);
        } else {
            return asRegisterOutputInfo(readingTypeDeliverable);
        }
    }

    private void setCommonFields(OutputInfo outputInfo, ReadingTypeDeliverable readingTypeDeliverable) {
        outputInfo.id = readingTypeDeliverable.getId();
        outputInfo.name = readingTypeDeliverable.getName();
        outputInfo.readingType = new ReadingTypeInfo(readingTypeDeliverable.getReadingType());
        outputInfo.formula = readingTypeDeliverable.getFormula() != null ? FormulaInfo.asInfo(readingTypeDeliverable.getFormula()) : null;
    }

    private RegisterOutputInfo asRegisterOutputInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        RegisterOutputInfo outputInfo = new RegisterOutputInfo();
        setCommonFields(outputInfo, readingTypeDeliverable);
        return outputInfo;
    }

    private ChannelOutputInfo asChannelOutputInfo(ReadingTypeDeliverable readingTypeDeliverable) {
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
}
