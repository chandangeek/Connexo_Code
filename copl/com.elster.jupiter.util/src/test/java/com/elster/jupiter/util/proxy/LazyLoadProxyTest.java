package com.elster.jupiter.util.proxy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Tom De Greyt (tgr)
 */
public class LazyLoadProxyTest {

    private interface MeaningOfLifeTheUniverseAndEverythingCalculator {

        int calculate();

    }

    private static class DeepThought implements MeaningOfLifeTheUniverseAndEverythingCalculator {

        public int calculate() {
            return 42;
        }
    }

    private class LazyThoughtLoader implements LazyLoader<MeaningOfLifeTheUniverseAndEverythingCalculator> {
        public Class<MeaningOfLifeTheUniverseAndEverythingCalculator> getImplementedInterface() {
            return MeaningOfLifeTheUniverseAndEverythingCalculator.class;
        }

        @Override
        public ClassLoader getClassLoader() {
            return MeaningOfLifeTheUniverseAndEverythingCalculator.class.getClassLoader();
        }

        public MeaningOfLifeTheUniverseAndEverythingCalculator load() {
            loaded = true;
            return new DeepThought();
        }
    }
    
    private boolean loaded;

    @Before
    public void setUp() {
        loaded = false;
    }

    @Test
    public void testNeverUseLoad() throws Exception {
        LazyLoadProxy.newInstance(new LazyThoughtLoader());
        assertFalse(loaded);
    }

    @Test
    public void testLoadedCorrectly() {
        MeaningOfLifeTheUniverseAndEverythingCalculator deepThought = LazyLoadProxy.newInstance(new LazyThoughtLoader());
        assertEquals(42, deepThought.calculate());
        assertTrue(loaded);

    }
}
