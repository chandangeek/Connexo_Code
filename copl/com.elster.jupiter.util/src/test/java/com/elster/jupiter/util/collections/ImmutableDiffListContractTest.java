/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import junit.framework.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(DynamicSuite.class)
public class ImmutableDiffListContractTest {

    @Suite
    private Test onlyOriginalsTests = onlyOriginalsTests();
    @Suite
    private Test onlyNewTests = onlyNewTests();
    @Suite
    private Test mixedTests = mixedTests();

    public static Test onlyOriginalsTests() {
        return ListTestSuiteBuilder.using(
                // This class is responsible for creating the collection
                // And providing data, which can be put into the collection
                // Here we use a abstract generator which will create strings
                // which will be put into the collection
                new TestStringListGenerator() {
                    @Override
                    protected List<String> create(String[] elements) {
                        // Fill here your collection with the given elements
                        return new ImmutableDiffList<>(ArrayDiffList.fromOriginal(Arrays.asList(elements)));
                    }
                })
                // The name of the test suite
                .named("Test DiffList with only originals")
                        // Here we give a hit what features our collection supports
                .withFeatures(CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionSize.ANY)
                .createTestSuite();
    }

    public static Test onlyNewTests() {
        return ListTestSuiteBuilder.using(
                // This class is responsible for creating the collection
                // And providing data, which can be put into the collection
                // Here we use a abstract generator which will create strings
                // which will be put into the collection
                new TestStringListGenerator() {
                    @Override
                    protected List<String> create(String[] elements) {
                        // Fill here your collection with the given elements
                        return new ImmutableDiffList<>(ArrayDiffList.withAllNew(Arrays.asList(elements)));
                    }
                })
                // The name of the test suite
                .named("Test DiffList with only additions")
                        // Here we give a hit what features our collection supports
                .withFeatures(CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionSize.ANY)
                .createTestSuite();
    }

    public static Test mixedTests() {
        return ListTestSuiteBuilder.using(
                // This class is responsible for creating the collection
                // And providing data, which can be put into the collection
                // Here we use a abstract generator which will create strings
                // which will be put into the collection
                new TestStringListGenerator() {
                    @Override
                    protected List<String> create(String[] elements) {
                        ArrayDiffList<String> list = ArrayDiffList.fromOriginal(Collections.<String>emptyList());
                        for (int i = 0; i < elements.length; i++) {
                            String element = elements[i];
                            if (i%2 == 0) {
                                list.add(element);
                            } else {
                                list.addAsOriginal(element);
                            }
                        }
                        return new ImmutableDiffList<>(list);
                    }
                })
                // The name of the test suite
                .named("Test DiffList with originals and additions")
                        // Here we give a hit what features our collection supports
                .withFeatures(CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionSize.ANY)
                .createTestSuite();
    }
}
