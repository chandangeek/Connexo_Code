package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.*;

import java.util.*;

import static com.elster.jupiter.cbo.Accumulation.*;
import static com.elster.jupiter.cbo.Commodity.*;
import static com.elster.jupiter.cbo.FlowDirection.*;
import static com.elster.jupiter.cbo.MeasurementKind.*;
import static com.elster.jupiter.cbo.MetricMultiplier.*;
import static com.elster.jupiter.cbo.ReadingTypeUnit.*;
import static com.elster.jupiter.cbo.TimeAttribute.*;
import static com.elster.jupiter.cbo.MacroPeriod.*;


public final class ReadingTypeGenerator {

    private ReadingTypeGenerator(MeteringServiceImpl meteringService) {
        this.meteringService = meteringService;
    }

    private enum Root {
        DELTA_A_FORWARD_ALL_PHASES(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR).accumulate(DELTADELTA), "Delta A+ all phases"),
        DELTA_A_REVERSE_ALL_PHASES(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(WATTHOUR).accumulate(DELTADELTA), "Delta A- all phases"),
        BULK_A_FORWARD_ALL_PHASES(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR).accumulate(BULKQUANTITY), "Bulk A+ all phases"),
        BULK_A_REVERSE_ALL_PHASES(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(WATTHOUR).accumulate(BULKQUANTITY), "Bulk A- all phases"),
        DELTA_A_FORWARD_PHASE_A(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR).accumulate(DELTADELTA).phase(Phase.PHASEA), "Delta A+ phase A"),
        DELTA_A_REVERSE_PHASE_A(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(WATTHOUR).accumulate(DELTADELTA).phase(Phase.PHASEA), "Delta A- phase A"),
        BULK_A_FORWARD_PHASE_A(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR).accumulate(BULKQUANTITY).phase(Phase.PHASEA), "Bulk A+ phase A"),
        BULK_A_REVERSE_PHASE_A(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(WATTHOUR).accumulate(BULKQUANTITY).phase(Phase.PHASEA), "Bulk A- phase A"),

        CUMULATIVE_MONTHLY_A_FORWARD(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR).accumulate(CUMULATIVE).period(MONTHLY), "Monthly billing A+"),
        INSTANT_ACTIVE_FORWARD_ALL_PHASES(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(CURRENT).in(AMPERE).accumulate(INSTANTANEOUS), "Active current all phases"),
        INSTANT_VOLTAGE_FORWARD_PHASE_A(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(RMSVOLTAGE).in(VOLT).accumulate(INSTANTANEOUS).phase(Phase.PHASEA), "Voltage phase A"),
        INSTANT_VOLTAGE_FORWARD_PHASE_B(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(RMSVOLTAGE).in(VOLT).accumulate(INSTANTANEOUS).phase(Phase.PHASEB), "Voltage phase B"),
        INSTANT_VOLTAGE_FORWARD_PHASE_C(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(RMSVOLTAGE).in(VOLT).accumulate(INSTANTANEOUS).phase(Phase.PHASEC), "Voltage phase C"),

        DELTA_GAS_FORWARD(ReadingTypeCodeBuilder.of(NATURALGAS).flow(FORWARD).measure(VOLUME).in(CUBICMETER).accumulate(DELTADELTA), "Delta gas volume"),
        BULK_GAS_FORWARD(ReadingTypeCodeBuilder.of(NATURALGAS).flow(FORWARD).measure(VOLUME).in(CUBICMETER).accumulate(BULKQUANTITY), "Bulk gas volume"),

        ACTIVE_FIRMWARE_VERSION(ReadingTypeCodeBuilder.of(DEVICE).measure(ASSETNUMBER).in(ENCODEDVALUE), "Active firmware version"),
        AMR_PROFILE_STATUS_CODE(ReadingTypeCodeBuilder.of(DEVICE).measure(DIAGNOSTIC).in(BOOLEANARRAY), "AMR Profile status code"),
        ALARM_REGISTER(ReadingTypeCodeBuilder.of(DEVICE).measure(ALARM).in(BOOLEANARRAY), "Alarm register");

        private final ReadingTypeCodeBuilder builder;
        private final String name;

        Root(ReadingTypeCodeBuilder builder , String name) {
            this.builder = builder;
            this.name = name;
        }
    }

    private final List<ReadingTypeImpl> readingTypes = new ArrayList<>();
    private final MeteringServiceImpl meteringService;
	
	static List<ReadingTypeImpl> generate(MeteringServiceImpl meteringService) {
		return new ReadingTypeGenerator(meteringService).readingTypes();
	}

    private void generate(final Generator generator, final List<Root> templatesGroup) {
        templatesGroup.stream().forEach(template -> generator.generate(template, this.readingTypes));
    }

	private List<ReadingTypeImpl> readingTypes() {
        List<Root> templatesGroup = Arrays.asList(Root.DELTA_A_FORWARD_ALL_PHASES,
                                 Root.DELTA_A_REVERSE_ALL_PHASES,
                                 Root.BULK_A_FORWARD_ALL_PHASES,
                                 Root.BULK_A_REVERSE_ALL_PHASES,
                                 Root.DELTA_A_FORWARD_PHASE_A,
                                 Root.DELTA_A_REVERSE_PHASE_A,
                                 Root.BULK_A_FORWARD_PHASE_A,
                                 Root.BULK_A_REVERSE_PHASE_A);
        Generator gen = new TimeAttributeGenerator(new TimeOfUseGenerator(new MetricMultiplierGenerator(true)));
        generate(gen, templatesGroup);

        templatesGroup = Arrays.asList(Root.CUMULATIVE_MONTHLY_A_FORWARD, Root.INSTANT_ACTIVE_FORWARD_ALL_PHASES);
        gen = new TimeOfUseGenerator(new MetricMultiplierGenerator(true));
        generate(gen, templatesGroup);

        templatesGroup = Arrays.asList(Root.INSTANT_VOLTAGE_FORWARD_PHASE_A, Root.INSTANT_VOLTAGE_FORWARD_PHASE_B, Root.INSTANT_VOLTAGE_FORWARD_PHASE_C);
        gen = new MetricMultiplierGenerator(true);
        generate(gen, templatesGroup);

        templatesGroup = Arrays.asList(Root.DELTA_GAS_FORWARD, Root.BULK_GAS_FORWARD);
        gen = new TimeAttributeGenerator(new TimeOfUseGenerator(true));
        generate(gen, templatesGroup);

        templatesGroup = Arrays.asList(Root.ACTIVE_FIRMWARE_VERSION, Root.AMR_PROFILE_STATUS_CODE, Root.ALARM_REGISTER);
        gen = new AsIsGenerator(true);
        generate(gen, templatesGroup);

        return readingTypes;
	}

    private interface Generator {
        void generate(Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes);
    }

    private abstract class AbstractGenerator implements Generator {
        private Generator next;
        private boolean writable;

        protected AbstractGenerator(Generator next, boolean writable) {
            this.next = next;
            this.writable = writable;
        }

        @Override
        public void generate(Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            if(next != null) {
                next.generate(readingTypeTemplate, readingTypes);
            }
            generateInternal(readingTypeTemplate, readingTypes);
        }

        protected void write(String code, String name, List<ReadingTypeImpl> readingTypes) {
            if(this.writable) {
                readingTypes.add(meteringService.createReadingType(code, name));
            }
        }

        protected abstract void generateInternal(Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes);
    }

    private abstract class AbstractIterativeGenerator<T> implements Generator {
        private Generator next;
        private List<T> list;
        private boolean writable;

        protected AbstractIterativeGenerator(Generator next, List<T> list, boolean writable) {
            this.next = next;
            this.list = list;
            this.writable = writable;
        }

        @Override
        public void generate(Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            Iterator<T> iterator = list.iterator();
            while (iterator.hasNext()) {
                T elem = iterator.next();
                generateInternal(elem, readingTypeTemplate, readingTypes);
                if(next != null) {
                    next.generate(readingTypeTemplate, readingTypes);
                }
            }
        }

        protected void write(String code, String name, List<ReadingTypeImpl> readingTypes) {
            if(this.writable) {
                readingTypes.add(meteringService.createReadingType(code, name));
            }
        }

        protected abstract void generateInternal(T elem, Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes);
    }

    private class AsIsGenerator extends AbstractGenerator {
        public AsIsGenerator() {
            this(null, false);
        }

        public AsIsGenerator(Generator next) {
            this(next, false);
        }

        public AsIsGenerator(boolean writable) {
            this(null, writable);
        }

        public AsIsGenerator(Generator next, boolean writable) {
            super(next, writable);
        }

        @Override
        protected void generateInternal(Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            String code = readingTypeTemplate.builder.code();
            String name = readingTypeTemplate.name;
            this.write(code, name, readingTypes);
        }
    }

    private class TimeAttributeGenerator extends AbstractIterativeGenerator<TimeAttribute> {
        public TimeAttributeGenerator() {
            this(null, false);
        }

        public TimeAttributeGenerator(Generator next) {
            this(next, false);
        }

        public TimeAttributeGenerator(boolean writable) {
            this(null, writable);
        }

        public TimeAttributeGenerator(Generator next, boolean writable) {
            super(next, Arrays.asList(TimeAttribute.NOTAPPLICABLE, MINUTE1,MINUTE2,MINUTE3,MINUTE5,MINUTE10,MINUTE15,MINUTE20,MINUTE30,MINUTE60,HOUR24), writable);
        }

        @Override
        protected void generateInternal(TimeAttribute elem, Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            String code = readingTypeTemplate.builder.period(elem).code();
            String name = readingTypeTemplate.name;
            this.write(code, name, readingTypes);
        }
    }

    private class TimeOfUseGenerator extends AbstractIterativeGenerator<Integer> {
        public TimeOfUseGenerator() {
            this(null, false);
        }

        public TimeOfUseGenerator(Generator next) {
            this(next, false);
        }

        public TimeOfUseGenerator(boolean writable) {
            this(null, writable);
        }

        public TimeOfUseGenerator(Generator next, boolean writable) {
            super(next, Arrays.asList(0, 1, 2), writable);
        }

        @Override
        protected void generateInternal(Integer elem, Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            String code = readingTypeTemplate.builder.tou(elem).code();
            String name = readingTypeTemplate.name;
            this.write(code, name, readingTypes);
        }
    }

    private class MetricMultiplierGenerator extends AbstractIterativeGenerator<MetricMultiplier> {
        public MetricMultiplierGenerator() {
            this(null, false);
        }

        public MetricMultiplierGenerator(Generator next) {
            this(next, false);
        }

        public MetricMultiplierGenerator(boolean writable) {
            this(null, writable);
        }

        public MetricMultiplierGenerator(Generator next, boolean writable) {
            super(next, Arrays.asList(ZERO, KILO), writable);
        }

        @Override
        protected void generateInternal(MetricMultiplier elem, Root readingTypeTemplate, List<ReadingTypeImpl> readingTypes) {
            String code = readingTypeTemplate.builder.in(elem).code();
            String name = readingTypeTemplate.name;
            this.write(code, name, readingTypes);
        }
    }
}
