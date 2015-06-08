package com.elster.jupiter.fileimport.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleFileNameCollisionResolverTest {

    FileNameCollisionResolver resolver;

    @Mock
    private FileSystem fileSystem;
    @Mock
    private Path path;

    @Before
    public void setUp() {
        resolver = new SimpleFileNameCollisionResolver(fileSystem);

        when(fileSystem.exists(any(Path.class))).thenReturn(false);
        when(path.toString()).thenReturn("/dir/test.txt");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNoConflict() {
        assertThat((Object) resolver.resolve(path)).isEqualTo(path);
    }

    @Test
    public void testConflictOnce() {
        when(fileSystem.exists(any(Path.class))).thenReturn(true, false);

        assertThat((Object) resolver.resolve(path)).isEqualTo(Paths.get("/dir/test1.txt"));
    }

    @Test
    public void testConflictMoreThanOnce() {
        when(fileSystem.exists(any(Path.class))).thenReturn(true, true, true, false);

        assertThat((Object) resolver.resolve(path)).isEqualTo(Paths.get("/dir/test3.txt"));
    }

}
