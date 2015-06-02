package com.elster.jupiter.util.proxy;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(loaded).isFalse();
    }

    @Test
    public void testLoadedCorrectly() {
        MeaningOfLifeTheUniverseAndEverythingCalculator deepThought = LazyLoadProxy.newInstance(new LazyThoughtLoader());
        assertThat(deepThought.calculate()).isEqualTo(42);
        assertThat(loaded).isTrue();

    }

    @Test
    public void testUnwrap() {
        MeaningOfLifeTheUniverseAndEverythingCalculator deepThought = LazyLoadProxy.newInstance(new LazyThoughtLoader());
        assertThat(deepThought).isInstanceOf(Proxy.class);
        Proxy proxy = (Proxy) deepThought;
        MeaningOfLifeTheUniverseAndEverythingCalculator unwrapped = proxy.<MeaningOfLifeTheUniverseAndEverythingCalculator>unwrap();
        assertThat(unwrapped).isNotInstanceOf(Proxy.class);
        assertThat(unwrapped.calculate()).isEqualTo(42);
    }

}
