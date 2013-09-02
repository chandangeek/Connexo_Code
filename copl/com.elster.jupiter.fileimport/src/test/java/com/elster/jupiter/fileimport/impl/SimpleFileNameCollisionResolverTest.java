package com.elster.jupiter.fileimport.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleFileNameCollisionResolverTest {

    FileNameCollisionResolver resolver = new SimpleFileNameCollisionResolver();

    @Mock
    private FileSystem fileSystem;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private Path path;

    @Before
    public void setUp() {
        when(fileSystem.exists(any(Path.class))).thenReturn(false);
        when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
        when(path.toString()).thenReturn("/dir/test.txt");

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testNoConflict() {
        assertThat(resolver.resolve(path)).isEqualTo(path);
    }

    @Test
    public void testConflictOnce() {
        when(fileSystem.exists(any(Path.class))).thenReturn(true, false);

        assertThat(resolver.resolve(path)).isEqualTo(Paths.get("/dir/test1.txt"));
    }

    @Test
    public void testConflictMoreThanOnce() {
        when(fileSystem.exists(any(Path.class))).thenReturn(true, true, true, false);

        assertThat(resolver.resolve(path)).isEqualTo(Paths.get("/dir/test3.txt"));
    }

}
