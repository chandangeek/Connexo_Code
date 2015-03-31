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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DerivedFieldTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {       
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
        }
    }
   
    private ZoneId defaultZone = ZoneId.systemDefault();

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
    public void testDelta()  {
        IdsService idsService = injector.getInstance(IdsService.class);
        TimeSeries ts = null;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	RecordSpec recordSpec = idsService.newRecordSpec("XXX", 1, "Delta");
        	recordSpec.addDerivedFieldSpec("Delta", "Total" , FieldType.NUMBER , FieldDerivationRule.DELTAFROMPREVIOUS);
        	recordSpec.persist();
        	Vault vault = idsService.getVault("IDS", 1).get();
	        ts = vault.createRegularTimeSeries(recordSpec, defaultZone, Duration.ofMinutes(15), 0);
	        TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
	        ZonedDateTime dateTime = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, defaultZone);
	        storer.add(ts, dateTime, null, BigDecimal.valueOf(10));
	        ZonedDateTime last = dateTime.plusMinutes(45);
	        storer.add(ts, last, null, BigDecimal.valueOf(500));
	        storer.execute();
	        storer = idsService.createOverrulingStorer();
	        dateTime = dateTime.plus(ts.interval());
	        storer.add(ts, dateTime, null, BigDecimal.valueOf(100));
	        dateTime = dateTime.plus(ts.interval());
	        storer.add(ts, dateTime, null, BigDecimal.valueOf(200));
	        storer.execute();
	        ctx.commit();
        }
        ZonedDateTime dateTime = ZonedDateTime.of(2014,1,1,0,0,0,0,defaultZone);
        List<TimeSeriesEntry> entries = ts.getEntries(Range.openClosed(dateTime.minusMinutes(15).toInstant(),dateTime.plusMinutes(60).toInstant()));
        assertThat(entries).hasSize(4);
        assertThat(entries.get(0).getBigDecimal(0)).isNull();
        assertThat(entries.get(1).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(90));
        assertThat(entries.get(2).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(100));
        assertThat(entries.get(3).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(300));
        assertThat(entries.get(3).getVersion()).isEqualTo(2);
        try(TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
            dateTime = dateTime.plusMinutes(15);
	        storer.add(ts, dateTime, null, BigDecimal.valueOf(50));
	        dateTime = dateTime.plusMinutes(15);
	        storer.execute();
	        ctx.commit();
        }
    }
   
    
   
}
