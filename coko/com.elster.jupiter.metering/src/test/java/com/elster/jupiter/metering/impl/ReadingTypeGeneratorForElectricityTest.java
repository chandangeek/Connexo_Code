package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.config.FormulaImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeGeneratorForElectricityTest {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Test
    @Transactional
    public void generateTest() {

        ReadingTypeGeneratorForElectricity readingTypeGeneratorForElectricity = new ReadingTypeGeneratorForElectricity();
        List<Pair<String, String>> readingTypes = readingTypeGeneratorForElectricity.generateReadingTypes();
        getMeteringService().createAllReadingTypes(readingTypes);
        List<String> availableReadingTypes = getMeteringService().getAvailableReadingTypes().stream().map(ReadingType::getMRID).collect(Collectors.toList());

        assertThat(readingTypes.stream()
                .allMatch(rt -> availableReadingTypes.stream().anyMatch(e -> e.equalsIgnoreCase(rt.getFirst()))))
                .isTrue();
    }

    @Test
    @Transactional
    public void aliasPrefixTest() {
        ReadingTypeGeneratorForElectricity readingTypeGeneratorForElectricity = new ReadingTypeGeneratorForElectricity();
        List<Pair<String, String>> readingTypes = readingTypeGeneratorForElectricity.generateReadingTypes();
        getMeteringService().createAllReadingTypes(readingTypes);

        getMeteringService().getAvailableReadingTypes().stream().forEach(readingType ->
        {
            if (readingType.getAccumulation().equals(Accumulation.DELTADELTA)) {
                assertThat(readingType.getFullAliasName().contains("Delta ")).isTrue();
            } else if (readingType.getAccumulation().equals(Accumulation.BULKQUANTITY)) {
                assertThat(readingType.getFullAliasName().contains("Bulk ")).isTrue();
            } else if (readingType.getAccumulation().equals(Accumulation.SUMMATION)) {
                assertThat(readingType.getFullAliasName().contains("Sum ")).isTrue();
            }
        });
    }

    private MeteringServiceImpl getMeteringService() {
        return (MeteringServiceImpl) inMemoryBootstrapModule.getMeteringService();
    }
}