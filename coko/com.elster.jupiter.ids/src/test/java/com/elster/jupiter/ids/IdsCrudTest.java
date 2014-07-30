package com.elster.jupiter.ids;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
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
    
    private static final boolean printSql = true;

    @BeforeClass
    public static void setUp() throws SQLException {
        injector = Guice.createInjector(
        			new MockModule(), 
        			inMemoryBootstrapModule,  
        			new OrmModule(),
        			new UtilModule(), 
        			new ThreadSecurityModule(), 
        			new PubSubModule(), 
        			new IdsModule(),
        			new TransactionModule(printSql));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(IdsService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCrud()  {
        IdsService idsService = injector.getInstance(IdsService.class);
        assertThat(idsService.getVault("IDS", 1)).isPresent();
        assertThat(idsService.getRecordSpec("IDS", 1)).isPresent();
        assertThat(idsService.getRecordSpec("IDS", 1).get().getFieldSpecs()).isNotEmpty();
        Vault vault = idsService.getVault("IDS",1).get();
        RecordSpec recordSpec = idsService.getRecordSpec("IDS", 1).get();
        TimeSeries ts = null;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
	        ts = vault.createRegularTimeSeries(recordSpec, TimeZone.getDefault(), IntervalLengthUnit.MINUTE.withLength(15), 0);
	        TimeSeriesDataStorer storer = idsService.createStorer(true);
	        DateTime dateTime = new DateTime(2014, 1, 1, 0, 0,0);
	        storer.add(ts, dateTime.toDate(),BigDecimal.valueOf(10));
	        dateTime = dateTime.plus(15*60000L);
	        storer.add(ts, dateTime.toDate(),BigDecimal.valueOf(20));
	        storer.execute();
	        ctx.commit();
        }
        DateTime dateTime = new DateTime(2014,1,1,0,0);
        List<TimeSeriesEntry> entries = ts.getEntries(new Interval(dateTime.minus(15*60000L).toDate(),dateTime.plus(15*60000L).toDate()));
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(10));
        assertThat(entries.get(1).getBigDecimal(0)).isEqualTo(BigDecimal.valueOf(20));
        assertThat(ts.getEntriesBefore(new DateTime(2015,1,1,0,0).toDate(),1)).hasSize(1);
        assertThat(ts.getEntriesBefore(new DateTime(2015,1,1,0,0).toDate(),2)).hasSize(2);
        assertThat(ts.getEntriesOnOrBefore(new DateTime(2014,1,1,0,0).toDate(),2)).hasSize(1);
        assertThat(ts.getEntriesOnOrBefore(new DateTime(2015,1,1,0,0).toDate(),2)).hasSize(2);
        assertThat(ts.getEntries(Interval.sinceEpoch())).hasSize(2);
    }
   
    
   
}
