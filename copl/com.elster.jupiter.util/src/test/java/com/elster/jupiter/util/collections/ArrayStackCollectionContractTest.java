package com.elster.jupiter.util.collections;

import com.google.common.collect.testing.CollectionTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestCollectionGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(DynamicSuite.class)
public class ArrayStackCollectionContractTest {

    @Suite
    private Test basicCollectionTests = basicCollectionTests();

    public static Test basicCollectionTests() {
        return CollectionTestSuiteBuilder.using(
                // This class is responsible for creating the collection
                // And providing data, which can be put into the collection
                // Here we use a abstract generator which will create strings
                // which will be put into the collection
                new TestCollectionGenerator<String>() {
                    @Override
                    public SampleElements<String> samples() {
                        return new SampleElements<>("A", "B", "C", "D", "E");
                    }

                    @Override
                    public Collection<String> create(Object... elements) {
                        // Fill here your collection with the given elements
                        ArrayStack<String> stack = new ArrayStack<>(elements.length);
                        for (Object element : elements) {
                            stack.push((String) element);
                        }
                        return stack;
                    }

                    @Override
                    public String[] createArray(int length) {
                        return new String[length];
                    }

                    @Override
                    public Iterable<String> order(List<String> insertionOrder) {
                        List<String> reverted = new ArrayList<>(insertionOrder);
                        Collections.reverse(reverted);
                        return reverted;
                    }
                })
                // The name of the test suite
                .named("Test DiffList with only originals")
                        // Here we give a hit what features our collection supports
                .withFeatures(ListFeature.GENERAL_PURPOSE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY,
                        CollectionFeature.KNOWN_ORDER)
                .createTestSuite();
    }


}
