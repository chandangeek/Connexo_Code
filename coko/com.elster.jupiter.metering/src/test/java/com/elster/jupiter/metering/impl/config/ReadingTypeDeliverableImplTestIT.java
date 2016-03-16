package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}
