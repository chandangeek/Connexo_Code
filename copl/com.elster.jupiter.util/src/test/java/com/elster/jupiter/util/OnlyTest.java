package com.elster.jupiter.util;

import org.junit.Test;

import java.nio.file.Paths;

import static org.fest.assertions.api.Assertions.assertThat;

public class OnlyTest {

    @Test
    public void testFolder() {
        assertThat(Only.FILES.apply(Paths.get("\\"))).isFalse();
    }

    @Test
    public void test() {
        assertThat(Only.FILES.apply(Paths.get("\\"))).isFalse();
    }


}
