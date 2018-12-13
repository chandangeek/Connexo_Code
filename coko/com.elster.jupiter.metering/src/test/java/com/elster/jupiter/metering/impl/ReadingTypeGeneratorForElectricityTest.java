/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@Ignore // install framework ro revise this
public class ReadingTypeGeneratorForElectricityTest {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

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