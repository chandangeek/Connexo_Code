package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingTypeDeliverableImplTestIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    private static MetrologyConfiguration metrologyConfiguration;
    private static MetrologyContract metrologyContract;
    private static Formula formula;
    private static ReadingType readingType;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService().newMetrologyConfiguration("Test",
                    inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get()).create();
            metrologyContract = metrologyConfiguration.addMandatoryMetrologyContract(
                    inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose().fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.BILLING));
            FormulaBuilder formulaBuilder = inMemoryBootstrapModule.getMetrologyConfigurationService().newFormulaBuilder(Formula.Mode.AUTO);
            formula = formulaBuilder.init(formulaBuilder.constant(10)).build();
            readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "zero reading type");
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutName() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable(null, metrologyContract, readingType, formula);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithEmptyName() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("", metrologyContract, readingType, formula);
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    public void testCreateReadingTypeDeliverableWithTooLongName() {
        String[] name = new String[Table.NAME_LENGTH + 1];
        Arrays.fill(name, "a");
        String longName = Stream.of(name).collect(Collectors.joining(""));
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable(longName, metrologyContract, readingType, formula);
    }

    @Test
    @ExpectedConstraintViolation(property = "metrologyContract", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutMetrologyContract() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", null, readingType, formula);
    }

    @Test
    @ExpectedConstraintViolation(property = "readingType", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutReadingType() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, null, formula);
    }

    @Test
    @ExpectedConstraintViolation(property = "formula", messageId = "{" + MessageSeeds.Constants.REQUIRED + "}")
    public void testCreateReadingTypeDeliverableWithoutFormula() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testCreateReadingTypeDeliverableWithTheSameName() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name", metrologyContract, readingType, formula);
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name", metrologyContract, readingType, formula);
    }

    @Test
    @Transactional
    public void testCreateReadingTypeDeliverableWithTheSameNameOnDifferentMetrologyContract() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name", metrologyContract, readingType, formula);
        MetrologyContract newMetrologyContract = metrologyConfiguration.addMetrologyContract(
                inMemoryBootstrapModule.getMetrologyConfigurationService().createMetrologyPurpose()
                        .fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION));
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name", newMetrologyContract, readingType, formula);
        // assert no exception about non-unique name
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
    public void testsetNonUniqueReadingTypeDeliverableName() {
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name", metrologyContract, readingType, formula);
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("name 2", metrologyContract, readingType, formula);
        deliverable.setName("name");
        deliverable.update();
    }

    @Test
    @Transactional
    public void testCanCreateReadingTypeDeliverable() {
        String name = "deliverable";
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable(name, metrologyContract, readingType, formula);

        assertThat(deliverable.getId()).isGreaterThan(0);
        assertThat(deliverable.getName()).isEqualTo(name);
        assertThat(deliverable.getMetrologyContract()).isEqualTo(metrologyContract);
        assertThat(deliverable.getReadingType()).isEqualTo(readingType);
        assertThat(deliverable.getFormula()).isEqualTo(formula);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableById() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        Optional<ReadingTypeDeliverable> readingTypeDeliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(deliverable.getId());
        assertThat(readingTypeDeliverable).isPresent();
        assertThat(readingTypeDeliverable.get()).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterReadingType() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter()
                .withReadingTypes(readingType);
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(filter);
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterMetrologyContract() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter()
                .withMetrologyContracts(metrologyContract);
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(filter);
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testCanFindReadingTypeDeliverableByFilterMetrologyConfiuration() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter()
                .withMetrologyConfigurations(metrologyContract.getMetrologyConfiguration());
        List<ReadingTypeDeliverable> deliverables = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findReadingTypeDeliverable(filter);
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testReadingTypeDeliverableReturnedByMetrologyConfiguration() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        List<ReadingTypeDeliverable> deliverables = metrologyConfiguration.getDeliverables();
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }

    @Test
    @Transactional
    public void testReadingTypeDeliverableReturnedByMetrologyContract() {
        ReadingTypeDeliverable deliverable = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .createReadingTypeDeliverable("deliverable", metrologyContract, readingType, formula);

        List<ReadingTypeDeliverable> deliverables = metrologyContract.getDeliverables();
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0)).isEqualTo(deliverable);
    }
}
