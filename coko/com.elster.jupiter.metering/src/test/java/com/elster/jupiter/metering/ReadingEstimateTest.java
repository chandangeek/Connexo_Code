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
                new MeteringModule("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
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
    public void testEstimate() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        String readingTypeCode;
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(KILO, WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("3.5.258");
            reading.addQuality("3.6.1");
            meter.store(MeterReadingImpl.of(reading));
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = meter.getCurrentMeterActivation().get().getChannels().get(0);
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM,QualityCodeIndex.SUSPECT),existDate).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isTrue();
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
            assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isFalse();
            assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), newDate).isPresent()).isTrue();
            Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), KILO, WATTHOUR));
            ctx.commit();
        }
    }

    @Test
    public void testEstimateOfBulkAffectsDelta() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        String readingTypeCode;
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .period(TimeAttribute.MINUTE15)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("3.5.258");
            reading.addQuality("3.6.1");
            meter.store(MeterReadingImpl.of(reading));
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = meter.getCurrentMeterActivation().get().getChannels().get(0);
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM,QualityCodeIndex.SUSPECT),existDate).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isTrue();
        // make sure that editing a value adds an editing rq, removes the suspect rq, and updates the validation rq
        // added a value adds an added rq
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
            reading1.addQuality("3.8.1"); // estimated by rule 1
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
            reading2.addQuality("3.8.2"); // estimated by rule 2
            CimChannel bulkCimChannel = channel.getCimChannel(readingType).get();
            bulkCimChannel.estimateReadings(ImmutableList.of(reading1, reading2));
            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1), existDate).isPresent()).isTrue();
            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), existDate).isPresent()).isFalse();
            assertThat(bulkCimChannel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isFalse();
            assertThat(bulkCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), newDate).isPresent()).isTrue();

            CimChannel deltaCimChannel = channel.getCimChannel(channel.getMainReadingType()).get();
            assertThat(deltaCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1), existDate).isPresent()).isTrue();
            assertThat(deltaCimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2), newDate).isPresent()).isTrue();

            Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), MetricMultiplier.ZERO, WATTHOUR));
            ctx.commit();
        }
    }


}
