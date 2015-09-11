package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityLifeCycleTest {

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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
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
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        String readingTypeCode;
        ZonedDateTime dateTime = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            for (Cases testCase : Cases.values()) {
                Instant date = dateTime.plusMinutes(testCase.ordinal()).toInstant();
                ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), date);
                reading.addQuality("1.1.1", "Same");
                meterReading.addReading(reading);
            }
            meter.store(meterReading);
            assertThat(meter.getReadingQualities(Range.atLeast(Instant.EPOCH))).hasSize(Cases.values().length);
            ctx.commit();
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            for (Cases testCase : Cases.values()) {
                Instant date = dateTime.plusMinutes(testCase.ordinal()).toInstant();
                ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(testCase.sameReading ? 1 : 2), date);
                switch (testCase.readingQualityBehavior) {
                    case SAME:
                        reading.addQuality("1.1.1", "Same");
                        break;
                    case DIFFERENT:
                        reading.addQuality("1.1.2", "Different");
                        break;
                    case NONE:
                        break;
                }
                meterReading.addReading(reading);
            }
            meter.store(meterReading);
            List<? extends BaseReading> readings = meter.getReadings(
                    Range.atLeast(Instant.EPOCH),
                    meteringService.getReadingType(readingTypeCode).get());
            assertThat(readings).hasSize(Cases.values().length);
            for (int i = 0; i < Cases.values().length; i++) {
                BaseReading reading = readings.get(i);
                Cases testCase = Cases.values()[i];
                switch (testCase.readingQualityBehavior) {
                    case SAME:
                        assertThat(reading.getReadingQualities()).hasSize(1);
                        assertThat(reading.getReadingQualities().get(0).getComment()).isEqualTo("Same");
                        break;
                    case DIFFERENT:
                        assertThat(reading.getReadingQualities()).hasSize(1);
                        assertThat(reading.getReadingQualities().get(0).getComment()).isEqualTo("Different");
                        break;
                    case NONE:
                        assertThat(reading.getReadingQualities()).isEmpty();
                        break;
                }
            }
            assertThat(meter.getReadingQualities(Range.atLeast(Instant.EPOCH))).
                    hasSize((int) Arrays.stream(Cases.values())
                            .filter(testCase -> testCase.readingQualityBehavior != ReadingQualityBehavior.NONE)
                            .count());
            ctx.commit();
        }
    }

    private enum ReadingQualityBehavior {
        SAME,
        DIFFERENT,
        NONE;
    }

    private enum Cases {
        SAMEREADINGSAMEREADINGQUALITY(true, ReadingQualityBehavior.SAME),
        SAMEREADINGDIFFERENTREADINGQUALITY(true, ReadingQualityBehavior.DIFFERENT),
        SAMEREADINGNOREADINGQUALITY(true, ReadingQualityBehavior.NONE),
        DIFFERENTREADINGSAMEREADINGQUALITY(false, ReadingQualityBehavior.SAME),
        DIFFERENTREADINGDIFFERENTREADINGQUALITY(false, ReadingQualityBehavior.DIFFERENT),
        DIFFERENTREADINGNOREADINGQUALITY(false, ReadingQualityBehavior.NONE),;

        private boolean sameReading;
        private ReadingQualityBehavior readingQualityBehavior;

        private Cases(boolean sameReading, ReadingQualityBehavior readingQualityBehavior) {
            this.sameReading = sameReading;
            this.readingQualityBehavior = readingQualityBehavior;
        }
    }

}
