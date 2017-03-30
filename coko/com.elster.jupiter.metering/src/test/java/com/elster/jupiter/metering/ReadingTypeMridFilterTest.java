/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Currency;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeMridFilterTest {

    private static final String DEFAULT_READING_TYPE = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(DEFAULT_READING_TYPE,
            "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // daily
            "0.8.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // maximum
            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // 15min interval
            "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // delta
            "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0", // reverse
            "0.0.0.1.1.7.12.0.0.0.0.0.0.0.0.0.72.0", // gas
            "0.0.0.1.1.1.37.0.0.0.0.0.0.0.0.0.72.0", // power
            "0.0.0.1.1.1.12.2.1.0.0.0.0.0.0.0.72.0", // harmonic
            "0.0.0.1.1.1.12.0.0.10.3.0.0.0.0.0.72.0", // argument
            "0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0", // TOU 2
            "0.0.0.1.1.1.12.0.0.0.0.0.3.0.0.0.72.0", // CPP 3
            "0.0.0.1.1.1.12.0.0.0.0.0.0.4.0.0.72.0", // Consumption tier
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.128.0.72.0", // Phase A
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", // multiplier 3
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.-3.72.0", // multiplier -3
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.38.0", // power
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.978" // EURO
    );

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void findBySpecificReadingTypeTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType)).find();
        assertThat(readingTypes).hasSize(1);
        assertThat(readingTypes.get(0).getMRID()).isEqualTo(DEFAULT_READING_TYPE);
    }

    @Test
    public void filterOnNoneExistingTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).setMacroPeriod(MacroPeriod.SPECIFIEDPERIOD)).find();
        assertThat(readingTypes).isEmpty();
    }

    @Test
    public void filterOnRationalNumberTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).setArgument(new RationalNumber(10, 3))).find();
        assertThat(readingTypes).hasSize(1);
        assertThat(readingTypes.get(0).getMRID()).isEqualTo("0.0.0.1.1.1.12.0.0.10.3.0.0.0.0.0.72.0");
    }

    @Test
    public void wildCardOnMacroPeriodTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMacroPeriod()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnOnMacroPeriodTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setMacroPeriod(MacroPeriod.DAILY)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardOnAggregationTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyAggregate()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.8.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnAggregationTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setAggregate(Aggregate.MAXIMUM)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.8.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardOnMeasuringTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMeasuringPeriod()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnMeasuringTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setMeasuringPeriod(TimeAttribute.MINUTE15)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardAccumulationTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyAccumulation()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnAccumulationTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setAccumulation(Accumulation.DELTADELTA)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardOnFlowDirectionTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyFlowDirection()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnFlowDirectionTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setFlowDirection(FlowDirection.REVERSE)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardOnCommodityTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCommodity()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.7.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnCommodityTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setCommodity(Commodity.NATURALGAS)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.7.12.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }


    @Test
    public void wildCardOnMeasurementKindTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMeasurementKind()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.37.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnMeasurementKindTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setMeasurementKind(MeasurementKind.POWER)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.37.0.0.0.0.0.0.0.0.0.72.0");
            }
        });
    }


    @Test
    public void wildCardOnHarmonicTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyInterharmonic()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.2.1.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnHarmonicTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setInterharmonic(new RationalNumber(2, 1))).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.2.1.0.0.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildCardOnArgumentTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyArgument()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.10.3.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnArgumentTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setArgument(new RationalNumber(10, 3))).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.10.3.0.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildcardOnTouTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyTou()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnTouTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setTou(2)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildcardOnCppTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCpp()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.3.0.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnCppTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setCpp(3)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.3.0.0.0.72.0");
            }
        });
    }

    @Test
    public void wildcardOnConsumptionTierTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyConsumptionTier()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.4.0.0.72.0");
            }
        });
    }

    @Test
    public void filterOnConsumptionTierTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setConsumptionTier(4)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.4.0.0.72.0");
            }
        });
    }

    @Test
    public void wildcardOnPhaseTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyPhases()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.128.0.72.0");
            }
        });
    }

    @Test
    public void filterOnPhaseTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setPhases(Phase.PHASEA)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.128.0.72.0");
            }
        });
    }

    @Test
    public void wildcardOnMultiplierTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMultiplier()).find();
        assertThat(readingTypes).hasSize(3);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.-3.72.0");
            }
        });
    }

    @Test
    public void filterOnMultiplierTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setMultiplier(MetricMultiplier.KILO)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            }
        });
    }

    @Test
    public void wildcardOnUnitTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyUnit()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.38.0");
            }
        });
    }

    @Test
    public void filterOnUnitTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setUnit(ReadingTypeUnit.WATT)).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.38.0");
            }
        });
    }

    @Test
    public void wildcardOnCurrencyTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType(DEFAULT_READING_TYPE).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCurrency()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(DEFAULT_READING_TYPE);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.978");
            }
        });
    }

    @Test
    public void filterOnCurrencyTest() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setCurrency(Currency.getInstance("EUR"))).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.978");
            }
        });
    }
}