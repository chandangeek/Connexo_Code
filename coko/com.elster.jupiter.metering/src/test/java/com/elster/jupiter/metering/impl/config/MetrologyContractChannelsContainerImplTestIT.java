package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

public class MetrologyContractChannelsContainerImplTestIT {

    private static final String MAIN_READING_TYPE_MRID = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(MAIN_READING_TYPE_MRID);
    private static MeterRole meterRole;
    private static MetrologyPurpose metrologyPurpose;
    private static ServiceCategory serviceCategory;
    private static ReadingType readingType;

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN);
        metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
        serviceCategory.addMeterRole(meterRole);
        readingType = inMemoryBootstrapModule.getMeteringService().getReadingType(MAIN_READING_TYPE_MRID).get();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCanCreateMetrologyConfigurationChannelsContainer() {
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("MC", serviceCategory).create();
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("RTD", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable readingTypeDeliverable = builder.build(builder.constant(1080L));
        MetrologyContract metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
        metrologyContract.addDeliverable(readingTypeDeliverable);

        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP", inMemoryBootstrapModule.getClock().instant()).create();
        usagePoint.apply(metrologyConfiguration);

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("metrologyConfiguration").isEqualTo(metrologyConfiguration).and(where("usagePoint").isEqualTo(usagePoint)))
                .get(0);

        assertThat(effectiveMetrologyConfiguration.getChannelsContainers()).hasSize(1);
        ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainers().get(0);
        assertThat(channelsContainer.getClass()).isEqualTo(MetrologyContractChannelsContainerImpl.class);
        Channel channel = channelsContainer.createChannel(readingType);
        assertThat(channel.getChannelsContainer()).isEqualTo(channelsContainer);
        channel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), readingType, inMemoryBootstrapModule.getClock().instant());
    }
}
