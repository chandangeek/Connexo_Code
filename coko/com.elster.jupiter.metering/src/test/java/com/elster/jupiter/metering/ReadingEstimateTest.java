package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.common.collect.ImmutableList;
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
import java.util.Optional;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.quantity;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEstimateTest {

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
                new BpmModule(),
                new FiniteStateMachineModule(),
                new MeteringModule("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new NlsModule(),
                new CustomPropertySetsModule()
        );
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
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
    public void testEstimate() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter = createMeter(meteringService);
        String readingTypeCode = buildReadingType();
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("3.5.258");
            reading.addQuality("3.6.1");
            meter.store(MeterReadingImpl.of(reading));
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = meter.getCurrentMeterActivation().get().getChannels().get(0);
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), existDate).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"), existDate).get().isActual()).isTrue();
        // make sure that editing a value adds an editing rq, removes the suspect rq, and updates the validation rq
        // added a value adds an added rq
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
            reading1.addQuality("3.8.1"); // estimated by rule 1
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
            reading2.addQuality("3.8.2"); // estimated by rule 2
            channel.getCimChannel(readingType).get().estimateReadings(ImmutableList.of(reading1, reading2));
            assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1), existDate).isPresent()).isTrue();
            assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), existDate).isPresent()).isFalse();
            assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"), existDate).get().isActual()).isFalse();
            assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), newDate).isPresent()).isTrue();
            Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), KILO, WATTHOUR));
            ctx.commit();
        }
    }

    @Test
    public void testEstimateOfBulkAffectsDeltaAndNextDelta() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter = createMeter(meteringService);
        String readingTypeCode = buildReadingType();
        Instant dateA = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant dateB = ZonedDateTime.of(2014, 2, 1, 0, 15, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant dateC = ZonedDateTime.of(2014, 2, 1, 0, 30, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl r1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateA);
            r1.addQuality("3.5.258");
            r1.addQuality("3.6.1");
            ReadingImpl r2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateB);
            ReadingImpl r3 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateC);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            meterReading.addAllReadings(Arrays.asList(r1, r2, r3));
            meter.store(meterReading);
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = meter.getCurrentMeterActivation().get().getChannels().get(0);
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), dateA).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"), dateA).get().isActual()).isTrue();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), dateA);
            reading1.addQuality("3.8.1"); // estimated by rule 1
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), dateB);
            reading2.addQuality("3.8.2"); // estimated by rule 2
            CimChannel bulkCimChannel = channel.getCimChannel(readingType).get();
            bulkCimChannel.estimateReadings(ImmutableList.of(reading1, reading2));

            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1), dateA).isPresent()).isTrue();
            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), dateA).isPresent()).isFalse();
            assertThat(bulkCimChannel.findReadingQuality(new ReadingQualityType("3.6.1"), dateA).get().isActual()).isFalse();
            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), dateB).isPresent()).isTrue();

            CimChannel deltaCimChannel = channel.getCimChannel(channel.getMainReadingType()).get();
            assertThat(deltaCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1), dateA).isPresent()).isTrue();
            assertThat(deltaCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), dateB).isPresent()).isTrue();
            assertThat(deltaCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), dateC).isPresent()).isTrue();

            Optional<BaseReadingRecord> channelReading = channel.getReading(dateA);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), MetricMultiplier.KILO, WATTHOUR));
            ctx.commit();
        }
    }

    private Meter createMeter(MeteringService meteringService) {
        Meter meter;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            meter = amrSystem.newMeter("myMeter").create();
            ctx.commit();
        }
        return meter;
    }

    private String buildReadingType() {
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(KILO, WATTHOUR);
        return builder.code();
    }
}
