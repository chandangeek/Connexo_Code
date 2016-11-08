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
import com.elster.jupiter.cbo.ReadingTypeUnit;
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
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReadingConfirmIT {

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
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new BpmModule(),
                new FiniteStateMachineModule(),
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
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        String readingTypeCode;
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter", "myName").create();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("3.5.258");
            reading.addQuality("3.6.1");
            meter.store(QualityCodeSystem.MDM, MeterReadingImpl.of(reading));
            ctx.commit();
        }
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        CimChannel cimChannel = channel.getCimChannel(readingType).get();
        assertQualities(channel, existDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ZEROUSAGE)
        }, new ReadingQualityType[0]);
        try (TransactionContext ctx = transactionService.getContext()) {
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), newDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), newDate);
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            // confirm new date in MDC only; exist date is not confirmed as has no MDC suspect
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
            channel.confirmReadings(QualityCodeSystem.MDC, ImmutableList.of(reading1, reading2));
            // existDate qualities
            assertQualities(channel, existDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ZEROUSAGE)
            }, new ReadingQualityType[0]);
            // newDate qualities
            assertQualities(channel, newDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
            });
            Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(1), KILO, WATTHOUR));
            channelReading = channel.getReading(newDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), KILO, WATTHOUR));
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), existDate);
            cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.TOUSUMCHECK), existDate);
            cimChannel.findReadingQualities()
                    .atTimestamp(newDate)
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeCategory.VALIDATION, 2000)
                    .orOfAnotherTypeInSameSystems()
                    .ofQualityIndex(QualityCodeIndex.KNOWNMISSINGREAD)
                    .collect()
                    .forEach(ReadingQualityRecord::makeActual);
            // confirm exist date & new date in MDM with all systems affected
            ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(3), existDate);
            ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(3), newDate);
            channel.confirmReadings(QualityCodeSystem.MDM, ImmutableList.of(reading1, reading2));
            // existDate qualities
            assertQualities(channel, existDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC)
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ZEROUSAGE),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.TOUSUMCHECK)
            });
            // newDate qualities
            assertQualities(channel, newDate, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC),
            }, new ReadingQualityType[]{
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000),
                    ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                    ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
            });
            Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(3), KILO, WATTHOUR));
            channelReading = channel.getReading(newDate);
            assertThat(channelReading).isPresent();
            assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(3), KILO, WATTHOUR));
            ctx.commit();
        }
    }

    private static void assertQualities(Channel channel, Instant date, ReadingQualityType[] actual, ReadingQualityType[] nonActual) {
        List<ReadingQualityRecord> qualities = channel.findReadingQualities()
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
}
