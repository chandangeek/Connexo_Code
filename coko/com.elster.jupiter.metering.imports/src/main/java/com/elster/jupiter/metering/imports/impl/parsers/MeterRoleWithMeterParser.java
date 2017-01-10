package com.elster.jupiter.metering.imports.impl.parsers;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.imports.impl.FieldParser;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.util.Pair;

import java.util.Optional;

public class MeterRoleWithMeterParser implements FieldParser<Pair> {

    MeterRoleParser meterRoleParser;
    MeterParser meterParser;

    public static final String VALUE_UNIT_SEPARATOR = ":";

    public MeterRoleWithMeterParser(MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        meterRoleParser = new MeterRoleParser(metrologyConfigurationService);
        meterParser = new MeterParser(meteringService);
    }

    @Override
    public Class<Pair> getValueType() {
        return Pair.class;
    }

    @Override
    public Pair<MeterRole, Meter> parse(String value) throws ValueParserException {
        String[] meterRoleWithMeters = value.split(VALUE_UNIT_SEPARATOR);
        MeterRole meterRole = meterRoleParser.parse(meterRoleWithMeters[0]);
        Meter meter = meterParser.parse(meterRoleWithMeters[1]);

        return Pair.of(meterRole, meter);
    }

    private class MeterParser implements FieldParser<Meter> {

        private MeteringService meteringService;

        public MeterParser(MeteringService meteringService) {
            this.meteringService = meteringService;
        }

        @Override
        public Meter parse(String value) throws ValueParserException {
            Optional<Meter> meter = meteringService.findMeterByName(value);
            if (meter.isPresent()) {
                return meter.get();
            }

            return null;
        }

        @Override
        public Class<Meter> getValueType() {
            return Meter.class;
        }
    }

    private class MeterRoleParser implements FieldParser<MeterRole> {

        private MetrologyConfigurationService metrologyConfigurationService;

        public MeterRoleParser(MetrologyConfigurationService metrologyConfigurationService) {
            this.metrologyConfigurationService = metrologyConfigurationService;
        }

        @Override
        public Class<MeterRole> getValueType() {
            return MeterRole.class;
        }

        @Override
        public MeterRole parse(String value) throws ValueParserException {
            Optional<MeterRole> meterRole = metrologyConfigurationService.findMeterRole(value);
            if (meterRole.isPresent()) {
                return meterRole.get();
            }

            return null;
        }
    }
}
