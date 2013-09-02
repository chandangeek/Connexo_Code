package com.elster.jupiter.util;

import org.junit.Test;

import java.nio.file.Paths;

import static org.fest.assertions.api.Assertions.assertThat;

public class OnlyTest {

    @Test
    public void testFolder() {
        assertThat(new Only().onlyFiles().apply(Paths.get("\\"))).isFalse();
    }

    @Test
    public void test() {
        assertThat(new Only().onlyFiles().apply(Paths.get("\\"))).isFalse();
    }


}
