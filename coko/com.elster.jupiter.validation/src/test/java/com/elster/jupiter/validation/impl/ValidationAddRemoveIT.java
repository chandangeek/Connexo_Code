package com.elster.jupiter.validation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.*;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.*;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationAddRemoveIT {

    private static final String MIN_MAX = "minMax";
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final Instant date1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator minMax;
    @Mock
    private Validator conseqZero;
    @Mock
    private PropertySpec min, max, conZero;

    private BigDecimalFactory valueFactory = new BigDecimalFactory();
    private MeterActivation meterActivation;
    private Meter meter;
    private String readingType;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new ValidationModule(),
                    new NlsModule(),
                    new EventsModule(),
                    new UserModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(validatorFactory.create(eq(MIN_MAX), any(Map.class))).thenReturn(minMax);
        when(minMax.getReadingQualityTypeCode()).thenReturn(Optional.empty());
        when(minMax.getPropertySpecs()).thenReturn(Arrays.asList(min, max));
        when(min.getName()).thenReturn(MIN);
        when(min.getValueFactory()).thenReturn(valueFactory);
        when(max.getName()).thenReturn(MAX);
        when(max.getValueFactory()).thenReturn(valueFactory);

        injector.getInstance(TransactionService.class).execute(() -> {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            readingType = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                    .flow(FlowDirection.FORWARD)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.DELTADELTA)
                    .code();
            ReadingType readingType1 = meteringService.getReadingType(readingType).get();
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("2331");
            meter.save();
            meterActivation = meter.activate(date1);
            meterActivation.createChannel(readingType1);

            ValidationServiceImpl validationService = (ValidationServiceImpl) injector.getInstance(ValidationService.class);
            validationService.addResource(validatorFactory);

            final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            ValidationRule minMaxRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
            minMaxRule.addReadingType(readingType1);
            minMaxRule.addProperty(MIN, BigDecimal.valueOf(1));
            minMaxRule.addProperty(MAX, BigDecimal.valueOf(100));
            minMaxRule.activate();
            validationRuleSet.save();

            validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                @Override
                public List<ValidationRuleSet> resolve(MeterActivation meterActivation) {
                    return Arrays.asList(validationRuleSet);
                }

                @Override
                public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                    return false;
                }
            });

            validationService.activateValidation(meter);
            validationService.enableValidationOnStorage(meter);
            return null;
        });
    }


    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testValidation() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(10L), date1.plusSeconds(900 * 1)));
            	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(11L), date1.plusSeconds(900 * 2)));
            	meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(12L), date1.plusSeconds(900 * 3)));
            	meter.store(meterReading);
                DataModel valDataModel = injector.getInstance(OrmService.class).getDataModel(ValidationService.COMPONENTNAME).get();
                List<IMeterActivationValidation> meterActivationValidations = valDataModel.mapper(IMeterActivationValidation.class).find("meterActivation", meterActivation);
                IChannelValidation channelValidation = meterActivationValidations.get(0).getChannelValidations().iterator().next();
                assertThat(channelValidation.getLastChecked()).isEqualTo(date1.plusSeconds(900*3));
                Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
                List<BaseReadingRecord> readings = channel.getReadings(Range.all());
                channel.removeReadings(readings.subList(1,readings.size()));
                meterActivationValidations = valDataModel.mapper(IMeterActivationValidation.class).find("meterActivation", meterActivation);
                channelValidation = meterActivationValidations.get(0).getChannelValidations().iterator().next();
                assertThat(channelValidation.getLastChecked()).isEqualTo(date1.plusSeconds(900*1));
            }
        });
    }

}
