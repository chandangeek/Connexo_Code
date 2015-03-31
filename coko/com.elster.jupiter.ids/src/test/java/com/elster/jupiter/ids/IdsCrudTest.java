package com.elster.jupiter.ids;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class IdsCrudTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

        }
    }

    private ZoneId zoneId = ZoneId.systemDefault();

    @BeforeClass
    public static void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new NlsModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new IdsModule(),
                new TransactionModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(IdsService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCrud() {
        IdsService idsService = injector.getInstance(IdsService.class);
        assertThat(idsService.getVault("IDS", 1).isPresent()).isTrue();
        assertThat(idsService.getRecordSpec("IDS", 1).isPresent()).isTrue();
        assertThat(idsService.getRecordSpec("IDS", 1).get().getFieldSpecs()).isNotEmpty();
        Vault vault = idsService.getVault("IDS", 1).get();
        RecordSpec recordSpec = idsService.getRecordSpec("IDS", 1).get();
        TimeSeries ts = null;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            ts = vault.createRegularTimeSeries(recordSpec, TimeZone.getDefault(), Duration.ofMinutes(15), 0);
            TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
            ZonedDateTime dateTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
            storer.add(ts, dateTime, BigDecimal.valueOf(10));
            dateTime = dateTime.plusMinutes(15);
            storer.add(ts, dateTime, BigDecimal.valueOf(20));
            storer.execute();
            assertThat(storer.processed(ts, dateTime.toInstant())).isTrue();
            storer = idsService.createOverrulingStorer();
            storer.add(ts, dateTime, BigDecimal.valueOf(20));
            storer.execute();
            assertThat(storer.processed(ts, dateTime.toInstant())).isFalse();
            ctx.commit();
        }
        ZonedDateTime dateTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        Range<Instant> interval = Range.openClosed(dateTime.minusMinutes(15).toInstant(), dateTime.plusMinutes(15).toInstant());
        //reread ts
        ts = idsService.getTimeSeries(ts.getId()).get();
        List<TimeSeriesEntry> entries = ts.getEntries(interval);
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(10));
        assertThat(entries.get(1).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(20));
        assertThat(ts.getEntriesBefore(ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, zoneId).toInstant(), 1)).hasSize(1);
        assertThat(ts.getEntriesBefore(ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, zoneId).toInstant(), 2)).hasSize(2);
        assertThat(ts.getEntriesOnOrBefore(ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, zoneId).toInstant(), 2)).hasSize(1);
        assertThat(ts.getEntriesOnOrBefore(ZonedDateTime.of(2015, 1, 1, 0, 0, 0, 0, zoneId).toInstant(), 2)).hasSize(2);
        assertThat(ts.getEntries(Range.all())).hasSize(2);
        assertThat(ts.getLastDateTime()).isEqualTo(dateTime.plusMinutes(15).toInstant());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Range<Instant> range = Range.openClosed(dateTime.toInstant(), dateTime.plusMinutes(15).toInstant());
            ts.removeEntries(range);
            interval = Range.openClosed(dateTime.toInstant(), dateTime.plusMinutes(15).toInstant());
            assertThat(ts.getEntries(interval)).isEmpty();
            assertThat(ts.getEntries(Range.atLeast(Instant.EPOCH))).hasSize(1);
            assertThat(ts.getLastDateTime()).isEqualTo(dateTime.toInstant());
        }
    }

    @Test
    public void testText() {
        IdsService idsService = injector.getInstance(IdsService.class);
        TimeSeries ts;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            Vault vault = idsService.newVault("IDS", 2, "TEXT", 1, 1, false);
            vault.persist();
            vault.activate(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, zoneId).toInstant());
            RecordSpec spec = idsService.newRecordSpec("IDS", 2, "text");
            spec.addFieldSpec("Value1", FieldType.NUMBER);
            spec.addFieldSpec("Value2", FieldType.TEXT);
            spec.persist();
            ts = vault.createIrregularTimeSeries(spec, TimeZone.getDefault());
            TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
            ZonedDateTime dateTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, zoneId);
            storer.add(ts, dateTime, BigDecimal.valueOf(10), "Text1");
            dateTime = dateTime.plusMinutes(15);
            storer.add(ts, dateTime, BigDecimal.valueOf(20), "Text2");
            storer.execute();
            ctx.commit();
        }
        ZonedDateTime dateTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, zoneId);
        List<TimeSeriesEntry> entries = ts.getEntries(Range.openClosed(dateTime.minusMinutes(15).toInstant(), dateTime.plusMinutes(15).toInstant()));
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getString(1)).isEqualTo("Text1");
        assertThat(entries.get(1).getString(1)).isEqualTo("Text2");
    }

}
