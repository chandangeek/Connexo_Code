package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Generates DataLogger specific reading type<br/>
 */
public class ReadingTypeGeneratorForDataLogger extends AbstractReadingTypeGenerator {

    public ReadingTypeGeneratorForDataLogger() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return IntStream.iterate(1, i -> i + 1)
                .limit(32)
                .mapToObj(DataLoggerReadingTypeTemplate::new);
    }

    private class DataLoggerReadingTypeTemplate implements ReadingTypeTemplate {

        int argumentNominator;

        DataLoggerReadingTypeTemplate(int argumentNominator) {
            this.argumentNominator = argumentNominator;
        }

        @Override
        public String getName() {
            return "Generic pulse " + argumentNominator;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            // "0.0.0.1.0.0.142.0.0.X.1.0.0.0.0.0.111.0" with X:  1 -> 32
            return ReadingTypeCodeBuilder.of(Commodity.NOTAPPLICABLE)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .measure(MeasurementKind.RELAYCYCLE)
                    .argument(this.argumentNominator, 1)
                    .in(ReadingTypeUnit.COUNT);
        }
    }

}
