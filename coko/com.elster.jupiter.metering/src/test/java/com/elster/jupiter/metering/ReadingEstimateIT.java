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
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.quantity;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEstimateIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;

    private TransactionService transactionService;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
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
                new CustomPropertySetsModule(),
                new BasicPropertiesModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
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
        Instant otherDate = ZonedDateTime.of(2014, 2, 3, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("2.5.258");
            reading.addQuality("2.6.1");
            meter.store(QualityCodeSystem.MDM, MeterReadingImpl.of(reading));
            ctx.commit();
        }
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        CimChannel cimChannel = channel.getCimChannel(readingType).get();
        assertQualities(cimChannel, existDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE)
        }, new ReadingQualityType[0]);
        try (TransactionContext ctx = transactionService.getContext()) {
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), otherDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), otherDate);
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
            reading1.addQuality("2.8.1"); // estimated by rule 1
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
            reading2.addQuality("2.8.2"); // estimated by rule 2
            ReadingImpl reading3 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), otherDate);
            reading3.addQuality("2.8.3"); // estimated by rule 3
            cimChannel.estimateReadings(QualityCodeSystem.MDC, ImmutableList.of(reading1, reading2, reading3));
            // existDate qualities
            assertQualities(cimChannel, existDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED)
            });
            // newDate qualities
            assertQualities(cimChannel, newDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
            });
            // otherDate qualities
            assertQualities(cimChannel, otherDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 3)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD)
            });
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
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl r1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateA);
            r1.addQuality("2.5.258");
            r1.addQuality("2.6.1");
            ReadingImpl r2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateB);
            ReadingImpl r3 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), dateC);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            meterReading.addAllReadings(Arrays.asList(r1, r2, r3));
            meter.store(QualityCodeSystem.MDM, meterReading);
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        CimChannel bulkCimChannel = channel.getCimChannel(readingType).get();
        CimChannel deltaCimChannel = channel.getCimChannel(channel.getMainReadingType()).get();
        assertQualities(bulkCimChannel, dateA, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE)
        }, new ReadingQualityType[0]);
        try (TransactionContext ctx = transactionService.getContext()) {
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 3000), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), dateA);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000), dateC);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), dateA);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), dateB);
            deltaCimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), dateC);
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), dateA);
            reading1.addQuality("2.8.1"); // estimated by rule 1
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), dateB);
            reading2.addQuality("2.8.2"); // estimated by rule 2
            bulkCimChannel.estimateReadings(QualityCodeSystem.MDC, ImmutableList.of(reading1, reading2));

            // bulk channel, date A
            assertQualities(bulkCimChannel, dateA, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE)
            });
            // bulk channel, date B
            assertQualities(bulkCimChannel, dateB, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2)
            }, new ReadingQualityType[0]);
            // delta channel, date A
            assertQualities(deltaCimChannel, dateA, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
            });
            // delta channel, date B
            assertQualities(deltaCimChannel, dateB, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD)
            });
            // delta channel, date C
            assertQualities(deltaCimChannel, dateC, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000)
            });

            Optional<BaseReadingRecord> channelReading = channel.getReading(dateA);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), MetricMultiplier.KILO, WATTHOUR));
            ctx.commit();
        }
    }

    private static void assertQualities(CimChannel cimChannel, Instant date, ReadingQualityType[] actual, ReadingQualityType[] nonActual) {
        List<ReadingQualityRecord> qualities = cimChannel.findReadingQualities()
                .atTimestamp(date)
                .collect();
        assertThat(qualities.stream()
                .filter(ReadingQualityRecord::isActual)
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList()))
                .containsOnly(actual);
        assertThat(qualities.stream()
                .filter(not(ReadingQualityRecord::isActual))
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList()))
                .containsOnly(nonActual);
    }

    private Meter createMeter(MeteringService meteringService) {
        Meter meter;
        try (TransactionContext ctx = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            meter = amrSystem.newMeter("myMeter").create();
            ctx.commit();
        }
        return meter;
    }

    private static String buildReadingType() {
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(KILO, WATTHOUR);
        return builder.code();
    }
}
