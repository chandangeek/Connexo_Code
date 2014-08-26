package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.joda.time.DateTime;
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
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingStorerTest {
    private static final String EVENTTYPECODE = "3.7.12.242";

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
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule()
        );
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
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testBulk() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	Meter meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
        			.period(TimeAttribute.MINUTE15)
        			.accumulate(Accumulation.BULKQUANTITY)
        			.flow(FlowDirection.FORWARD)
        			.measure(MeasurementKind.ENERGY)
        			.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
        	String intervalReadingTypeCode = builder.code();
        	MeterReadingImpl meterReading = new MeterReadingImpl();
        	IntervalBlockImpl block = new IntervalBlockImpl(intervalReadingTypeCode);
        	meterReading.addIntervalBlock(block);
        	DateTime dateTime = new DateTime(2014,1,1,0,0,0);
        	block.addIntervalReading(new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(1000)));
        	block.addIntervalReading(new IntervalReadingImpl(dateTime.plus(15*60*1000L).toDate(), BigDecimal.valueOf(1100)));
        	String registerReadingTypeCode = builder.period(TimeAttribute.NOTAPPLICABLE).code();
        	ReadingImpl reading = new ReadingImpl(registerReadingTypeCode, BigDecimal.valueOf(1200), dateTime.toDate());
        	reading.addQuality("1.1.1","Whatever");
        	meterReading.addReading(reading);
        	meter.store(meterReading);
        	
            List<? extends BaseReadingRecord> readings = meter.getMeterActivations().get(0).getReadings(
            		new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()),
            		meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0)).isNull();
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            readings = meter.getMeterActivations().get(0).getReadings(
            		new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()),
            		meteringService.getReadingType(registerReadingTypeCode).get());
            assertThat(readings).hasSize(1);
            assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(meter.getReadingsBefore(dateTime.toDate(), meteringService.getReadingType(intervalReadingTypeCode).get(),10)).isEmpty();
            assertThat(meter.getReadingsOnOrBefore(dateTime.toDate(), meteringService.getReadingType(intervalReadingTypeCode).get(),10)).hasSize(1);
            List<Channel> channels = meter.getMeterActivations().get(0).getChannels();
            Optional<Channel> channel = Optional.absent();
            for (Channel candidate : channels) {
            	if (candidate.getMainReadingType().getMRID().equals(registerReadingTypeCode)) {
            		channel = Optional.of(candidate);
            	}
            }
            assertThat(channel).isPresent();
            assertThat(channel.get().findReadingQuality(dateTime.toDate())).hasSize(1);
            //update reading quality
            meterReading = new MeterReadingImpl();
            reading = new ReadingImpl(registerReadingTypeCode, BigDecimal.valueOf(1200), dateTime.toDate());
            String newComment = "Whatever it was";
        	reading.addQuality("1.1.1",newComment);
        	meterReading.addReading(reading);
        	meter.store(meterReading);
            assertThat(channel.get().findReadingQuality(dateTime.toDate())).hasSize(1);
            assertThat(channel.get().findReadingQuality(dateTime.toDate()).get(0).getComment()).isEqualTo(newComment);
            ctx.commit();
        }
   
    }

    @Test
    public void testDelta() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	Meter meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	String readingTypeCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
        			.period(TimeAttribute.MINUTE15)
        			.accumulate(Accumulation.DELTADELTA)
        			.flow(FlowDirection.FORWARD)
        			.measure(MeasurementKind.ENERGY)
        			.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
        			.code();
        	MeterReadingImpl meterReading = new MeterReadingImpl();
        	IntervalBlockImpl block = new IntervalBlockImpl(readingTypeCode);
        	meterReading.addIntervalBlock(block);
        	DateTime dateTime = new DateTime(2014,1,1,0,0,0);
        	block.addIntervalReading(new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(1000)));
        	ProfileStatus status = ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW);
        	block.addIntervalReading(new IntervalReadingImpl(dateTime.plus(15*60*1000L).toDate(), BigDecimal.valueOf(1100),status));
        	meter.store(meterReading);
            List<BaseReadingRecord> readings = meter.getMeterActivations().get(0).getChannels().get(0).getReadings(new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()));
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1100));
            assertThat(((IntervalReadingRecord) readings.get(1)).getProfileStatus()).isEqualTo(status);
            ctx.commit();
        }
    }

    @Test
    public void testIdempotency() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            String intervalReadingTypeCode = builder.code();
            MeterReadingImpl meterReading = new MeterReadingImpl();
            IntervalBlockImpl block = new IntervalBlockImpl(intervalReadingTypeCode);
            meterReading.addIntervalBlock(block);
            DateTime dateTime = new DateTime(2014,1,1,0,0,0);
            block.addIntervalReading(new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(1000)));
            block.addIntervalReading(new IntervalReadingImpl(dateTime.plus(15*60*1000L).toDate(), BigDecimal.valueOf(1100)));
            String registerReadingTypeCode = builder.period(TimeAttribute.NOTAPPLICABLE).code();
            Reading reading = new ReadingImpl(registerReadingTypeCode, BigDecimal.valueOf(1200), dateTime.toDate());
            meterReading.addReading(reading);

            EndDeviceEventImpl endDeviceEvent = new EndDeviceEventImpl(EVENTTYPECODE, dateTime.toDate());
            HashMap<String, String> eventData = new HashMap<>();
            eventData.put("A", "B");
            endDeviceEvent.setEventData(eventData);
            meterReading.addEndDeviceEvent(endDeviceEvent);

            meter.store(meterReading);
            meter.store(meterReading);

            List<? extends BaseReadingRecord> readings = meter.getMeterActivations().get(0).getReadings(
                    new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()),
                    meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0)).isNull();
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            readings = meter.getMeterActivations().get(0).getReadings(
                    new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()),
                    meteringService.getReadingType(registerReadingTypeCode).get());
            assertThat(readings).hasSize(1);
            assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(readings.get(0).getQuantities().get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(((Reading) readings.get(0)).getText()).isNull();
            ctx.commit();
        }
    }

    @Test
    public void testAddRegularReading() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	Meter meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	String intervalReadingTypeCode = "32.12.2.4.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
        	MeterReadingImpl meterReading = new MeterReadingImpl();
        	DateTime dateTime = new DateTime(2014,1,1,0,0,0);
        	Reading reading = new ReadingImpl(intervalReadingTypeCode, BigDecimal.valueOf(1200), dateTime.toDate());
        	meterReading.addReading(reading);
        	meter.store(meterReading);     	
            ctx.commit();
        }
    }
    
    @Test
    public void testAddTextRegister() {
    	MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	Meter meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	String readingTypeCode = "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
        	MeterReadingImpl meterReading = new MeterReadingImpl();
        	DateTime dateTime = new DateTime(2014,1,1,0,0,0);
        	Reading reading = new ReadingImpl(readingTypeCode, "Sample text", dateTime.toDate());
        	meterReading.addReading(reading);
        	meter.store(meterReading);     	
        	List<? extends BaseReadingRecord >readings = meter.getMeterActivations().get(0).getReadings(
                      new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()),
                      meteringService.getReadingType(readingTypeCode).get());
        	assertThat(((ReadingRecord) readings.get(0)).getText()).isEqualTo("Sample text");
          	assertThat(readings.get(0).getValue()).isNull();
            ctx.commit();
        }
    }

}
