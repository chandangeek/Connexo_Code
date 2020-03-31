/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationResultBuilder;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.quantity;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEstimateIT {
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(transactionService);

    private static final String IMPLEMENTATION = "Fibonacci";
    private static Injector injector;

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static TransactionService transactionService;
    private static EstimationServiceImpl estimationService;

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
                public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
                    EstimationResultBuilder builder = SimpleEstimationResult.builder();
                    estimationBlocks.stream()
                            .peek(block -> {
                                if (block.estimatables().size() > 3) {
                                    builder.addRemaining(block);
                                } else {
                                    builder.addEstimated(block);
                                }
                            })
                            .filter(block -> block.estimatables().size() <= 3)
                            .flatMap(block -> block.estimatables().stream())
                            .forEach(estimable -> estimable.setEstimation(BigDecimal.valueOf(value.getAndAdd(4))));
                    return builder.build();
                }

                @Override
                public String getDisplayName() {
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
                public NlsKey getNlsKey() {
                    return null;
                }

                @Override
                public List<String> getRequiredProperties() {
                    return Collections.emptyList();
                }

                @Override
                public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
                    return Collections.emptySet();
                }
            };
        }

        @Override
        public Estimator createTemplate(String implementation) {
            return create(implementation, Collections.emptyMap());
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }
    }

    @BeforeClass
    public static void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new UserModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new NlsModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new EstimationModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule(),
                    new AuditServiceModule(),
                    new WebServicesModule(),
                    new TokenModule(),
                    new BlackListModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(AuditService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            estimationService = (EstimationServiceImpl) injector.getInstance(EstimationService.class);
            return null;
        });
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant otherDate = ZonedDateTime.of(2014, 2, 3, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myAmrId", "myName").create();
        meter.update();
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(KILO, WATTHOUR);
        String readingTypeCode = builder.code();

        ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
        reading.addQuality("3.6.1");
        reading.addQuality("3.5.258");
        meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(reading));

        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        estimationService.addEstimatorFactory(new MyEstimatorFactory());

        EstimationRuleSet ruleSet;
        EstimationRule rule;
        ruleSet = estimationService.createEstimationRuleSet("testRuleSet", QualityCodeSystem.MDM);
        rule = ruleSet.addRule(IMPLEMENTATION, "testRule")
                .withReadingType(readingType)
                .active(true)
                .create();
        int ruleId = (int)rule.getId();

        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        assertQualities(channel, existDate, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ZEROUSAGE),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)
        }, new ReadingQualityType[0]);
        CimChannel cimChannel = channel.getCimChannel(readingType).get();
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, ruleId + 1000), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, ruleId + 1), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1000), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 2000), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), otherDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, ruleId + 11), otherDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, ruleId), otherDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), otherDate);

        final EstimationRuleSet resolved = ruleSet;
        estimationService.addEstimationResolver(new EstimationResolver() {

            @Override
            public List<EstimationRuleSet> resolve(ChannelsContainer channelsContainer) {
                return Collections.singletonList(resolved);
            }

            @Override
            public boolean isInUse(EstimationRuleSet estimationRuleSet) {
                return true;
            }

            @Override
            public boolean isEstimationActive(Meter meter) {
                return true;
            }

            @Override
            public Priority getPriority() {
                return Priority.HIGHEST;
            }
        });

        estimationService.estimate(QualityCodeSystem.MDM, meter.getCurrentMeterActivation()
                .get()
                .getChannelsContainer(), Range.all());

        // existDate qualities
        assertQualities(channel, existDate, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, ruleId)
        }, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ZEROUSAGE),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED)
        });
        // newDate qualities
        assertQualities(channel, newDate, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, ruleId)
        }, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1000),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 2000),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD)
        });
        // otherDate qualities, not estimated because of no MDM suspect
        assertQualities(channel, otherDate, new ReadingQualityType[] {
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, ruleId + 11),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, ruleId)
        }, new ReadingQualityType[0]);
        Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
        assertThat(channelReading).isPresent();
        assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(1), KILO, WATTHOUR));
        channelReading = channel.getReading(newDate);
        assertThat(channelReading).isPresent();
        assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(5), KILO, WATTHOUR));
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
