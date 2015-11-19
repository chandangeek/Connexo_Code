package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.*;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 18.11.15
 * Time: 15:40
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeMridFilterTest {

    private final String defaultReadingType = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(
                            defaultReadingType, // default

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
                            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.38.0", // power
                            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.978" // EURO
                    ),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void findByAllWildCardsTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter()).find();
        assertThat(readingTypes).hasSize(17);
    }

    @Test
    public void findBySpecificReadingTypeTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType)).find();
        assertThat(readingTypes).hasSize(1);
        assertThat(readingTypes.get(0).getMRID()).isEqualTo(defaultReadingType);
    }

    @Test
    public void filterOnNoneExistingTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).setMacroPeriod(MacroPeriod.SPECIFIEDPERIOD)).find();
        assertThat(readingTypes).isEmpty();
    }

    @Test
    public void filterOnRationalNumberTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).setArgument(new RationalNumber(10, 3))).find();
        assertThat(readingTypes).hasSize(1);
        assertThat(readingTypes.get(0).getMRID()).isEqualTo("0.0.0.1.1.1.12.0.0.10.3.0.0.0.0.0.72.0");
    }

    @Test
    public void wildCardOnMacroPeriodTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMacroPeriod()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyAggregate()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMeasuringPeriod()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyAccumulation()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyFlowDirection()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCommodity()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMeasurementKind()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyInterharmonic()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyArgument()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyTou()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCpp()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyConsumptionTier()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyPhases()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyMultiplier()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
            }
        });
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            }
        });
    }

    @Test
    public void filterOnMultiplierTest() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyUnit()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType readingType = meteringService.getReadingType(defaultReadingType).get();
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(ReadingTypeMridFilter.fromTemplateReadingType(readingType).anyCurrency()).find();
        assertThat(readingTypes).hasSize(2);
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals(defaultReadingType);
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
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        List<ReadingType> readingTypes = meteringService.getReadingTypesByMridFilter(new ReadingTypeMridFilter().setCurrency(Currency.getInstance("EUR"))).find();
        assertThat(readingTypes).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType value) {
                return value.getMRID().equals("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.978");
            }
        });
    }
}