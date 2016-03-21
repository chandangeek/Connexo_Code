package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.util.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

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
        assertThat(getMeteringService().getAvailableReadingTypes()).hasSize(0).overridingErrorMessage("We should have started with 0 reading types");

        ReadingTypeGeneratorForElectricity readingTypeGeneratorForElectricity = new ReadingTypeGeneratorForElectricity();
        List<Pair<String, String>> readingTypes = readingTypeGeneratorForElectricity.generateReadingTypes();
        getMeteringService().createAllReadingTypes(readingTypes);

        assertThat(getMeteringService().getAvailableReadingTypes()).hasSize(readingTypes.size()).overridingErrorMessage("Expected " + readingTypes.size() + " reading types");
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