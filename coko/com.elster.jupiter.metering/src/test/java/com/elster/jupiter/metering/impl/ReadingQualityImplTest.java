package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.cache.impl.OrmCacheModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityImplTest {

    private Injector injector;

    @Mock
    private LogService logService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private Principal principal;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MessageService messageService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private QueueTableSpec queueTableSpec;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destinationSpec;
    private TransactionModule transactionModule;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(MessageService.class).toInstance(messageService);
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);           
        }
    }

    @Before
    public void setUp() throws SQLException {
        transactionModule = new TransactionModule();
        injector = Guice.createInjector(
        			new MockModule(), 
        			new InMemoryBootstrapModule(), 
        			new IdsModule(), 
        			new MeteringModule(), 
        			new PartyModule(), 
        			new EventsModule(), 
        			new DomainUtilModule(), 
        			new OrmModule(),
        			new UtilModule(), 
        			new ThreadSecurityModule(principal), 
        			new PubSubModule(logService), 
        			transactionModule, 
        			new OrmCacheModule());
        when(messageService.getQueueTableSpec(anyString())).thenReturn(Optional.of(queueTableSpec));
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(destinationSpec));
        when(principal.getName()).thenReturn("Test");
        injector.getInstance(DataSource.class);
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
			@Override
			public Void perform() {
				injector.getInstance(MeteringService.class);
				return null;
			}
		});
    }

    @After
    public void tearDown() throws SQLException {
        transactionModule.closeLifeLineConnection();
    }

    @Test
    public void test() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Date date = new DateMidnight(2001, 1, 1).toDate();
                doTest(getMeteringService(), date);

            }
        });

    }

    private MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private void doTest(MeteringService meteringService, Date date) {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
        usagePoint.save();
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        MeterActivation meterActivation = usagePoint.activate(date);
        Channel channel = meterActivation.createChannel(readingType);
        ReadingStorer regularStorer = meteringService.createNonOverrulingStorer();
        regularStorer.addIntervalReading(channel, date, 0L, BigDecimal.valueOf(561561, 2));
        regularStorer.execute();
        BaseReadingRecord reading = channel.getReading(date).get();
        ReadingQuality readingQuality = channel.createReadingQuality(new ReadingQualityType("6.1"), reading);
        readingQuality.save();
    }

}
