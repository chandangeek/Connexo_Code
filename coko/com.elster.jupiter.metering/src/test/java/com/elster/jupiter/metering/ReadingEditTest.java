package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEditTest {

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
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        String readingTypeCode;
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter").create();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("2.5.258");
            reading.addQuality("2.6.1");
            meter.store(MeterReadingImpl.of(reading));
            ctx.commit();
        }
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        assertThat(channel.findReadingQualities()
                .atTimestamp(existDate)
                .ofQualitySystem(QualityCodeSystem.MDC)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .collect()).hasSize(1);
        assertThat(channel.findReadingQualities()
                .atTimestamp(existDate)
                .ofQualitySystem(QualityCodeSystem.MDC)
                .ofQualityIndex(QualityCodeIndex.ZEROUSAGE)
                .collect().get(0).isActual()).isTrue();
        // make sure that editing a value adds an editing rq, removes the suspect rq, and updates the validation rq
        // added a value adds an added rq
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
        	ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
            channel.editReadings(ImmutableList.of(reading1,reading2));
            assertThat(channel.findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.EDITGENERIC)
                    .atTimestamp(existDate)
                    .collect()).hasSize(1);
            assertThat(channel.findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.SUSPECT)
                    .atTimestamp(existDate)
                    .collect()).isEmpty();
            assertThat(channel.findReadingQualities()
                    .atTimestamp(existDate)
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.ZEROUSAGE)
                    .collect().get(0).isActual()).isFalse();
            assertThat(channel.findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.ADDED)
                    .atTimestamp(newDate)
                    .collect()).hasSize(1);
            ctx.commit();
        }
        // make sure if you edit an added reading the reading quality remains added
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(3), newDate);
            channel.editReadings(ImmutableList.of(reading2));
            assertThat(channel.findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.EDITGENERIC)
                    .atTimestamp(newDate)
                    .collect()).isEmpty();
            assertThat(channel.findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.ADDED)
                    .atTimestamp(newDate)
                    .collect()).hasSize(1);
            ctx.commit();
        }
    }
}
