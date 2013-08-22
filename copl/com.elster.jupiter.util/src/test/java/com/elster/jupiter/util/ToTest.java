package com.elster.jupiter.util;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.fest.assertions.api.Assertions.assertThat;

public class ToTest {

    private static final String NAME_STRING = "name";

    @Test
    public void testName() {
        assertThat(To.NAME.apply(new MyHasName())).isEqualTo(NAME_STRING);
    }

    @Test
    public void testFile() {
        assertThat(To.FILE.apply(Paths.get("\\"))).isEqualTo(new File("\\"));
    }

    @Test
    public void testFileOnNull() {
        assertThat(To.FILE.apply(null)).isEqualTo(null);
    }

    private static class MyHasName implements HasName {

        @Override
        public String getName() {
            return NAME_STRING;
        }
    }
}
