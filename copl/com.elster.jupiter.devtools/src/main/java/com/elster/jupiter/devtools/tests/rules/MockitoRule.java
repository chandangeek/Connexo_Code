/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

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
