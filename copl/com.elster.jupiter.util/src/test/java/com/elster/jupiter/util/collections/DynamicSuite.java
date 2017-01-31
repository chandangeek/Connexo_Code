/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Using <code>Suite</code> as a runner allows you to manually
 * build a suite containing tests from many classes. It is the JUnit 4 equivalent of the JUnit 3.8.x
 * static {@link junit.framework.Test} <code>suite()</code> method. To use it, annotate a class
 * with <code>@RunWith(Suite.class)</code> and <code>@SuiteClasses({TestClass1.class, ...})</code>.
 * When you run this class, it will run all the tests in all the suite classes.
 *
 * @since 4.0
 */
public class DynamicSuite extends ParentRunner<Test> {

    private List<Field> suites = new ArrayList<>();

    public DynamicSuite(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Description describeChild(Test child) {
        return Description.createTestDescription(getTestClass().getName(), child.toString());
    }

    @Override
    protected List<Test> getChildren() {
        List<Test> children = new ArrayList<>();
        Object test = getTestInstance(getTestClass());
        for (Field field : getAllFields(getTestClass().getJavaClass())) {
            if (field.getAnnotation(Suite.class) != null) {
                Test testCase = getSuite(test, field);
                if (testCase instanceof TestSuite) {
                    TestSuite suite = (TestSuite) testCase;
                    addSuite(children, suite);
                } else {
                    children.add(testCase);
                }
            }
        }
        return children;
    }

    private void addSuite(List<Test> children, TestSuite suite) {
        Enumeration<Test> tests = suite.tests();
        while (tests.hasMoreElements()) {
            Test testCase = tests.nextElement();
            if (testCase instanceof TestSuite) {
                addSuite(children, (TestSuite) testCase);
            } else {
                children.add(testCase);
            }
        }
    }

    @Override
    protected void runChild(Test child, RunNotifier notifier) {
        Description description = describeChild(child);
        TestResult result = new TestResult();
        notifier.fireTestStarted(description);
        child.run(result);
        if (!result.wasSuccessful()) {
            if (result.failures().hasMoreElements()) {
                TestFailure testFailure = result.failures().nextElement();
                notifier.fireTestFailure(new Failure(description, testFailure.thrownException()));
            }
            if (result.errors().hasMoreElements()) {
                TestFailure testFailure = result.errors().nextElement();
                notifier.fireTestFailure(new Failure(description, testFailure.thrownException()));
            }
        }
        notifier.fireTestFinished(description);
    }


    private Test getSuite(Object testInstance, Field field) {
        try {
            field.setAccessible(true);
            return (Test) field.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private Object getTestInstance(TestClass testClass) {
        try {
            return testClass.getJavaClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private List<Field> getAllFields(Class<?> testClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = testClass;
        for (;current != null; current = current.getSuperclass()) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        return fields;
    }


}
