package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.UtilModule;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Assertions;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.quantity;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEstimateTest {

    public static final String IMPLEMENTATION = "Fibonacci";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private TransactionService transactionService;
    private EstimationServiceImpl estimationService;

    private static class MyEstimatorFactory implements EstimatorFactory {
        @Override
        public List<String> available() {
            return Collections.singletonList(IMPLEMENTATION);
        }

        @Override
        public Estimator create(String implementation, Map<String, Object> props) {
            return new Estimator() {
                private AtomicLong value = new AtomicLong(1);

                @Override
                public EstimationResult estimate(List<EstimationBlock> estimationBlock) {
                    SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
                    List<EstimationBlock> estimated = new ArrayList<>();
                    List<EstimationBlock> notEstimated = new ArrayList<>();
                    estimationBlock.stream()
                            .peek(block -> {
                                if (block.estimatables().size() > 3) {
                                    builder.addRemaining(block);
                                } else {
                                    builder.addEstimated(block);
                                }
                            })
                            .filter(block -> block.estimatables().size() <= 3)
                            .flatMap(block -> block.estimatables().stream())
                            .forEach(estimatable -> estimatable.setEstimation(BigDecimal.valueOf(value.getAndAdd(4))));
                    return builder.build();
                }

                @Override
                public String getDisplayName() {
                    return IMPLEMENTATION;
                }

                @Override
                public String getDisplayName(String property) {
                    return IMPLEMENTATION;
                }

                @Override
                public String getDefaultFormat() {
                    return "";
                }

                @Override
                public List<PropertySpec> getPropertySpecs() {
                    return Collections.emptyList();
                }

                @Override
                public PropertySpec getPropertySpec(String name) {
                    return null;
                }

                @Override
                public NlsKey getNlsKey() {
                    return null;
                }

                @Override
                public NlsKey getPropertyNlsKey(String property) {
                    return null;
                }

                @Override
                public String getPropertyDefaultFormat(String property) {
                    return null;
                }

                @Override
                public List<Pair<? extends NlsKey, String>> getExtraTranslations() {
                    return null;
                }

                @Override
                public List<String> getRequiredProperties() {
                    return null;
                }
            };
        }

        @Override
        public Estimator createTemplate(String implementation) {
            return create(implementation, Collections.emptyMap());
        }
    }

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
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule(false),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new NlsModule(),
                    new TimeModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new EstimationModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(FiniteStateMachineService.class);
                injector.getInstance(MeteringService.class);
                estimationService = (EstimationServiceImpl) injector.getInstance(EstimationService.class);
                return null;
            }
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
            meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(KILO, WATTHOUR);
            readingTypeCode = builder.code();
            ctx.commit();
        }
        try (TransactionContext ctx = transactionService.getContext()) {
            ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
            reading.addQuality("3.6.1");
            reading.addQuality("3.5.258");
            meter.store(MeterReadingImpl.of(reading));
            ctx.commit();
        }
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        try (TransactionContext ctx = transactionService.getContext()) {
            MeterActivation meterActivation = meter.getCurrentMeterActivation().get();
            Channel channel = meterActivation.getChannels().get(0);
            channel.getCimChannel(readingType).get().createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), newDate).save();
            channel.getCimChannel(readingType).get().createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), newDate).save();
            ctx.commit();
        }
        Channel channel = meter.getCurrentMeterActivation().get().getChannels().get(0);
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM,QualityCodeIndex.SUSPECT),existDate).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isTrue();
        // make sure that editing a value adds an editing rq, removes the suspect rq, and updates the validation rq
        // added a value adds an added rq

        estimationService.addEstimatorFactory(new MyEstimatorFactory());

        EstimationRuleSet ruleSet = null;
        EstimationRule rule = null;
        try (TransactionContext ctx = transactionService.getContext()) {
            ruleSet = estimationService.createEstimationRuleSet("testRuleSet");
            rule = ruleSet.addRule(IMPLEMENTATION, "testRule");
            rule.addReadingType(readingType);
            rule.activate();
            ruleSet.save();
        }

        final EstimationRuleSet resolved = ruleSet;
        estimationService.addEstimationResolver(new EstimationResolver() {
            @Override
            public List<EstimationRuleSet> resolve(MeterActivation meterActivation) {
                return Collections.singletonList(resolved);
            }

            @Override
            public boolean isInUse(EstimationRuleSet estimationRuleSet) {
                return true;
            }

            @Override
            public Priority getPriority() {
                return Priority.HIGHEST;
            }
        });

        try (TransactionContext ctx = transactionService.getContext()) {
//        	ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
//            reading1.addQuality("3.8.1"); // estimated by rule 1
//        	ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
//            reading2.addQuality("3.8.2"); // estimated by rule 2
//            channel.getCimChannel(readingType).get().estimateReadings(ImmutableList.of(reading1, reading2));

            estimationService.estimate(meter.getCurrentMeterActivation().get(), Range.<Instant>all());

            ctx.commit();
        }
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, (int) rule.getId()), existDate).isPresent()).isTrue();
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), existDate).isPresent()).isFalse();
        assertThat(channel.findReadingQuality(new ReadingQualityType("3.6.1"),existDate).get().isActual()).isFalse();
        assertThat(channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, (int) rule.getId()), newDate).isPresent()).isTrue();
        Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
        assertThat(channelReading).isPresent();
        Assertions.assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(1), KILO, WATTHOUR));
        channelReading = channel.getReading(newDate);
        assertThat(channelReading).isPresent();
        Assertions.assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(5), KILO, WATTHOUR));
    }
}
