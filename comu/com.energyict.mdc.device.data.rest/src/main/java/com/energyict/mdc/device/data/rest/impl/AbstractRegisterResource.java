package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Register;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dvy on 2/02/2017.
 */
public abstract class AbstractRegisterResource {
    protected final Clock clock;

    public AbstractRegisterResource(Clock clock) {
        this.clock = clock;
    }

    protected void addDeltaCalculationIfApplicableAndUpdateInterval(Register<?, ?> register, List<ReadingInfo> readingInfos) {
    /* And fill a delta value for cumulative reading type. The delta is the difference with the previous record.
       The Delta value won't be stored in the database yet, as it has a performance impact */
        if (!register.getRegisterSpec().isTextual()) {
            ReadingType readingTypeForCalculation = register.getCalculatedReadingType(register.getLastReadingDate().orElse(clock.instant()))
                    .isPresent() ? register.getCalculatedReadingType(register.getLastReadingDate().orElse(clock.instant())).get() : register.getReadingType();
            boolean cumulative = readingTypeForCalculation.isCumulative();
            if (cumulative) {
                List<NumericalReadingInfo> numericalReadingInfos = readingInfos.stream().map(readingInfo -> ((NumericalReadingInfo) readingInfo)).collect(Collectors
                        .toList());
                for (int i = 0; i < numericalReadingInfos.size() - 1; i++) {
                    NumericalReadingInfo previous = numericalReadingInfos.get(i + 1);
                    NumericalReadingInfo current = numericalReadingInfos.get(i);
                    if(register.getReadingType().isCumulative()){
                        current.interval.start= previous.timeStamp.toEpochMilli();
                    }
                    if (register.getCalculatedReadingType(current.timeStamp).isPresent() && previous.calculatedValue != null && current.calculatedValue != null) {
                        calculateDelta(current, previous.calculatedValue, current.calculatedValue);
                    } else if (previous.value != null && current.value != null) {
                        calculateDelta(current, previous.value, current.value);

                    }
                }
            }
        }
    }

    private void calculateDelta(NumericalReadingInfo current, BigDecimal previousVale, BigDecimal currentValue) {
        current.deltaValue = currentValue.subtract(previousVale);
        current.deltaValue = current.deltaValue.setScale(currentValue.scale(), BigDecimal.ROUND_UP);
    }
}
