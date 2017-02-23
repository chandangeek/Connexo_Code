/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.transaction.TransactionContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDeliverableImplTestIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static MetrologyConfiguration metrologyConfiguration;
    private static MetrologyContract metrologyContract;
    private static ReadingType readingType;
    private static ReadingType readingType2;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            MetrologyPurpose metrologyPurpose = inMemoryBootstrapModule.getMetrologyConfigurationService()
                    .findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();
            ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
            metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                    .newMetrologyConfiguration("Test", serviceCategory).create();
            metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose);
            readingType = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.82.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                    .orElseGet(() -> inMemoryBootstrapModule.getMeteringService()
                            .createReadingType("0.0.82.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "cons reading type"));
            readingType2 = inMemoryBootstrapModule.getMeteringService().getReadingType("0.0.83.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                    .orElseGet(() -> inMemoryBootstrapModule.getMeteringService()
                            .createReadingType("0.0.83.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "cons reading type 2"));
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void before() {
        metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyConfiguration(
                metrologyConfiguration.getId()).get();
        metrologyContract = metrologyConfiguration.getContracts().get(0);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutName() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(null, readingType, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.constant(10));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithEmptyName() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("", readingType, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.constant(10));
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @Transactional
    public void testCreateReadingTypeDeliverableWithTooLongName() {
        String[] name = new String[Table.NAME_LENGTH + 1];
        Arrays.fill(name, "a");
        String longName = Stream.of(name).collect(Collectors.joining(""));
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(longName, readingType, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.constant(10));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "readingType", messageId = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutReadingType() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", null, Formula.Mode.AUTO);

        // Business method
        builder.build(builder.constant(10));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testCreateReadingTypeDeliverableWithTheSameName() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("name", readingType, Formula.Mode.AUTO);
        builder.build(builder.constant(10));

        ReadingTypeDeliverableBuilder otherBuilder = metrologyConfiguration.newReadingTypeDeliverable("name", readingType2, Formula.Mode.AUTO);

        // Business method
        otherBuilder.build(otherBuilder.constant(10));
    }

    @Test
    @Transactional
    public void testCreateReadingTypeDeliverableWithTheSameNameOnDifferentMetrologyConfiguration() {
        ServiceCategory electricity = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("name", readingType, Formula.Mode.AUTO);
        builder.build(builder.constant(10));
        UsagePointMetrologyConfiguration otherMetrologyConfiguration =
                inMemoryBootstrapModule
                        .getMetrologyConfigurationService()
                        .newUsagePointMetrologyConfiguration("new", electricity).create();
        ReadingTypeDeliverableBuilder otherBuilder = otherMetrologyConfiguration.newReadingTypeDeliverable("name", readingType, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = otherBuilder.build(otherBuilder.constant(10));

        // Asserts
        assertThat(deliverable).isNotNull();
        assertThat(deliverable.getName()).isEqualTo("name");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + PrivateMessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testUPdateNonUniqueReadingTypeDeliverableName() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("name", readingType, Formula.Mode.AUTO);
        builder.build(builder.constant(10));
        ReadingTypeDeliverableBuilder otherBuilder = metrologyConfiguration.newReadingTypeDeliverable("otherName", readingType2, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = otherBuilder.build(otherBuilder.constant(10));

        // Business method
        deliverable.startUpdate().setName("name").complete();
    }

    @Test
    @Transactional
    public void testCanCreateTwoDeliverablesWithDifferentNames() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("name", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));
        assertThat(deliverable1.getId()).isGreaterThan(0);

        ReadingTypeDeliverableBuilder otherBuilder = metrologyConfiguration.newReadingTypeDeliverable("otherName", readingType2, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = otherBuilder.build(otherBuilder.constant(10));
        assertThat(deliverable2.getId()).isGreaterThan(0);
    }

    @Test
    @Transactional
    public void testCanCreateReadingTypeDeliverable() {
        String name = "deliverable";
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);

        // Business method
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        // Asserts
        assertThat(deliverable.getId()).isGreaterThan(0);
        assertThat(deliverable.getName()).isEqualTo(name);
        assertThat(deliverable.getMetrologyConfiguration()).isEqualTo(metrologyConfiguration);
        assertThat(deliverable.getReadingType()).isEqualTo(readingType);
        assertThat(deliverable.getFormula()).isNotNull();
    }

    @Test
    @Transactional
    public void testCanAssignReadingTypeDeliverableToMetrologyContract() {
        String name = "deliverable";
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        // Business method
        metrologyContract.addDeliverable(deliverable);

        // Asserts
        List<ReadingTypeDeliverable> deliverables = metrologyContract.getDeliverables();
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables).contains(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableById() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        // Business method
        Optional<ReadingTypeDeliverable> readingTypeDeliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(deliverable.getId());

        // Asserts
        assertThat(readingTypeDeliverable).isPresent();
        assertThat(readingTypeDeliverable.get()).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterReadingType() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter().withReadingTypes(readingType);

        // Business method
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(filter);

        // Asserts
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterMetrologyContract() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));
        metrologyContract.addDeliverable(deliverable);

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter().withMetrologyContracts(metrologyContract);

        // Business method
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService().findReadingTypeDeliverable(filter);

        // Asserts
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterMetrologyConfiuration() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter().withMetrologyConfigurations(metrologyConfiguration);

        // Business method
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(filter);

        // Asserts
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testReadingTypeDeliverableReturnedByMetrologyConfiguration() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        // Business method
        List<ReadingTypeDeliverable> deliverables = metrologyConfiguration.getDeliverables();

        // Asserts
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testReadingTypeDeliverableReturnedByMetrologyContract() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));
        metrologyContract.addDeliverable(deliverable);

        // Business method
        List<ReadingTypeDeliverable> deliverables = metrologyContract.getDeliverables();

        // Asserts
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanRemoveDeliverableFromMetrologyContract() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));
        metrologyContract.addDeliverable(deliverable);

        // Business method
        metrologyContract.removeDeliverable(deliverable);

        // Asserts
        List<ReadingTypeDeliverable> deliverables = metrologyContract.getDeliverables();
        assertThat(deliverables).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + PrivateMessageSeeds.Constants.DELIVERABLE_MUST_HAVE_THE_SAME_CONFIGURATION + "}", property = "deliverable", strict = true)
    public void testCanNotAddDeliverableFromAnotherMetrologyConfigurationToMetrologyContract() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        MetrologyConfiguration anotherConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newMetrologyConfiguration("Another configuration", serviceCategory).create();
        ReadingTypeDeliverableBuilder builder = anotherConfiguration.newReadingTypeDeliverable("Some deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));

        metrologyContract.addDeliverable(deliverable);
    }

    @Test
    @Transactional
    public void testCanRemoveDeliverableFromMetrologyConfiguration() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("Some deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.constant(10));
        assertThat(metrologyConfiguration.getDeliverables()).contains(deliverable);
        metrologyConfiguration.removeReadingTypeDeliverable(deliverable);
        metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyConfiguration(
                metrologyConfiguration.getId()).get();
        assertThat(metrologyConfiguration.getDeliverables()).isEmpty();
    }

    @Test
    @Transactional
    public void testRemovingDeliverableRemovesFormula() {
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.plus(builder.constant(42), builder.constant(17)));
        Formula formula = deliverable.getFormula();

        metrologyConfiguration.removeReadingTypeDeliverable(deliverable);

        Optional<Formula> formulaRef = inMemoryBootstrapModule.getMetrologyConfigurationService().findFormula(formula.getId());
        assertThat(formulaRef.isPresent()).isFalse();
    }

    @Test(expected = CannotDeleteReadingTypeDeliverableException.class)
    @Transactional
    public void testCanNotDeleteDeliverableWhichIsPartOfFormulaAnotherDeliverable() {
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "reading type 1");
        ReadingType readingType2 = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.71.0", "reading type 2");
        FullySpecifiedReadingTypeRequirement requirement = metrologyConfiguration.newReadingTypeRequirement("Requirement").withReadingType(readingType);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.requirement(requirement));
        builder = metrologyConfiguration.newReadingTypeDeliverable("deliverable2", readingType2, Formula.Mode.AUTO);
        builder.build(builder.deliverable(deliverable));

        metrologyConfiguration.removeReadingTypeDeliverable(deliverable);
        // exception here
    }
}