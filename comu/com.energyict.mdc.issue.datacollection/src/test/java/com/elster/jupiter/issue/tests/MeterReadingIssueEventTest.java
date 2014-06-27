package com.elster.jupiter.issue.tests;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Calendar;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.datacollection.MeterReadingIssueEvent;
import com.elster.jupiter.issue.datacollection.impl.IssueDataCollectionModule;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.TrendPeriodUnit;
import com.elster.jupiter.issue.datacollection.impl.install.InstallServiceImpl;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingIssueEventTest {
    
    private Meter meter;
    private String readingTypeCode;
    
    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private ReadingType readingType;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mock(KnowledgeBaseFactoryService.class));
            bind(KnowledgeBuilderFactoryService.class).toInstance(mock(KnowledgeBuilderFactoryService.class));

            //TODO think about including this lines into IssueModule class
            TaskService taskService = mock(TaskService.class);
            bind(TaskService.class).toInstance(taskService);

            RecurrentTaskBuilder builder = mock(RecurrentTaskBuilder.class);
            when(taskService.newBuilder()).thenReturn(builder);
            when(builder.build()).thenReturn(mock(RecurrentTask.class));
        }
    }

    @Before
    public void setUp(){
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
                new NlsModule(),
                new UserModule(),
                new IssueModule(),
                new IssueDataCollectionModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            // initialize Issue tables
            injector.getInstance(com.elster.jupiter.issue.impl.service.InstallServiceImpl.class);
            injector.getInstance(InstallServiceImpl.class);
            ctx.commit();
        }
        
        readingTypeCode = ReadingTypeCodeBuilder
                .of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .accumulate(DELTADELTA)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .period(TimeAttribute.MINUTE15)
                .code();
        readingType = injector.getInstance(OrmService.class).getDataModel("MTR").get().mapper(ReadingType.class).getOptional(readingTypeCode).get();
        
        AmrSystem amrSystem = getMeteringService().findAmrSystem(ModuleConstants.MDC_AMR_SYSTEM_ID).get();
        meter = amrSystem.newMeter("test device");
        try (TransactionContext context = getContext()) {
            meter.save();
            context.commit();
        }
    }
    
    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }
    
    @Test
    public void testNoReadings() {
        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }
    
    @Test
    public void testOnlyOneReading() {
        try (TransactionContext context = getContext()) {
            MeterReadingImpl meterReading = new MeterReadingImpl();
            DateTime dateTime = getCurrentDateTime();
            
            IntervalBlockImpl block = new IntervalBlockImpl(readingTypeCode);
            block.addIntervalReading(new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(0)));
            
            meterReading.addIntervalBlock(block);
            meter.store(meterReading);
            readingType = getMeteringService().getReadingType(readingTypeCode).get();
            context.commit();
        }

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.HOURS.getId())).isEqualTo(0.0);
    }

    @Test
    public void testComputeMaxSlope() {
        try (TransactionContext context = getContext()) {
            MeterReadingImpl meterReading = new MeterReadingImpl();
            DateTime dateTime = getCurrentDateTime().minusHours(1);
            
            IntervalBlockImpl block = new IntervalBlockImpl(readingTypeCode);
            
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(0).toDate(), BigDecimal.valueOf(10)));
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(15).toDate(), BigDecimal.valueOf(11)));
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(30).toDate(), BigDecimal.valueOf(12)));
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(45).toDate(), BigDecimal.valueOf(13)));
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(60).toDate(), BigDecimal.valueOf(14)));

            meterReading.addIntervalBlock(block);
            meter.store(meterReading);
            readingType = getMeteringService().getReadingType(readingTypeCode).get();
            context.commit();
        }

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(1, TrendPeriodUnit.DAYS.getId())).isEqualTo(4.0);
    }
    
    @Test
    public void testComputeMaxSlopeForLimitedPeriod() {        
        try (TransactionContext context = getContext()) {
            DateTime dateTime = getCurrentDateTime();
            
            MeterReadingImpl meterReading1 = new MeterReadingImpl();
            dateTime.minusDays(2);//readings two days ago
            IntervalBlockImpl block1 = new IntervalBlockImpl(readingTypeCode);
            block1.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(0).toDate(), BigDecimal.valueOf(-100500)));
            block1.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(15).toDate(), BigDecimal.valueOf(100500)));
            meterReading1.addIntervalBlock(block1);
            meter.store(meterReading1);
            
            MeterReadingImpl meterReading2 = new MeterReadingImpl();
            dateTime.minusHours(1);//readings 1 hour before
            IntervalBlockImpl block2 = new IntervalBlockImpl(readingTypeCode);
            block2.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(0).toDate(), BigDecimal.valueOf(0)));
            block2.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(15).toDate(), BigDecimal.valueOf(25)));
            block2.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(30).toDate(), BigDecimal.valueOf(50)));
            block2.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(45).toDate(), BigDecimal.valueOf(75)));
            block2.addIntervalReading(new IntervalReadingImpl(dateTime.plusMinutes(60).toDate(), BigDecimal.valueOf(100)));
            meterReading2.addIntervalBlock(block2);
            
            meter.store(meterReading2);
            context.commit();
        }

        MeterReadingIssueEvent event = new MeterReadingIssueEvent(meter, readingType, null, null);
        assertThat(event.computeMaxSlope(2, TrendPeriodUnit.HOURS.getId())).isEqualTo(100.0);
    }
    
    private MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private TransactionContext getContext() {
        return injector.getInstance(TransactionService.class).getContext();
    }

    private static DateTime getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return new DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                0, 0);
    }
}
