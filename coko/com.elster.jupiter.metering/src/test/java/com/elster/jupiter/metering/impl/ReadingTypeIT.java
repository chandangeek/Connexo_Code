package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // no macro period, no measuring period
            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // no macro period, measuring period =  15 min
            "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = day, no measuring period
            "13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = month, no measuring period
            "11.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = day, measuring period =  15 min
            "24.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0" // macro period = weekly, no measuring period
    );
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testFindEquidistantReadingTypes() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        List<ReadingType> equidistantTypes = meteringService.getAvailableEquidistantReadingTypes();
        List<ReadingType> nonEquidistantTypes = meteringService.getAvailableNonEquidistantReadingTypes();
        List<ReadingType> allTypes = meteringService.getAvailableReadingTypes();

        assertThat(equidistantTypes).isNotEmpty().isSubsetOf(allTypes);
        assertThat(nonEquidistantTypes).isNotEmpty().isSubsetOf(allTypes);

        List<ReadingType> intersection = new ArrayList<>(equidistantTypes);
        intersection.retainAll(nonEquidistantTypes);
        assertThat(intersection).isEmpty();

        List<ReadingType> union = new ArrayList<>(equidistantTypes);
        union.addAll(nonEquidistantTypes);
        assertThat(union).containsAll(allTypes);
    }
}