package com.elster.jupiter.devtools.tests.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

/**
 * Copyrights EnergyICT
 * Date: 28/01/13
 * Time: 15:32
 */
public class MockitoRule implements TestRule {

    private final Object test;

    private MockitoRule(Object test) {
        this.test = test;
    }

    public static TestRule initMocks(Object testCase) {
        return new MockitoRule(testCase);
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        MockitoAnnotations.initMocks(test);
        return statement;
    }

}
