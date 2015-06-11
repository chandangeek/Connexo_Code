package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleFileNameCollisionResolverTest {

    FileNameCollisionResolver resolver;

    @Mock
    private FileUtils fileUtils;
    @Mock
    Thesaurus thesaurus;
    private Path path;
    private Path destinationFolder;

    private java.nio.file.FileSystem testFileSystem;

    @Before
    public void setUp() throws IOException {
        testFileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path root = testFileSystem.getRootDirectories().iterator().next();
        destinationFolder = Files.createDirectory(root.resolve("dir"));
        path = destinationFolder.resolve("test.txt");

        fileUtils = new FileUtilsImpl(thesaurus);
        resolver = new SimpleFileNameCollisionResolver(fileUtils, testFileSystem);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNoConflict() {

        assertThat((Object) resolver.resolve(path)).isEqualTo(path);
    }

    @Test
    public void testConflictOnce() throws IOException {
        Files.createFile(destinationFolder.resolve("test.txt"));
        assertThat((Object) resolver.resolve(path)).isEqualTo(destinationFolder.resolve("test1.txt"));
    }

    @Test
    public void testConflictMoreThanOnce() throws IOException {
        Files.createFile( resolver.resolve(destinationFolder.resolve("test.txt")));
        Files.createFile( resolver.resolve(destinationFolder.resolve("test.txt")));
        Files.createFile( resolver.resolve(destinationFolder.resolve("test.txt")));
        assertThat((Object) resolver.resolve(path)).isEqualTo(testFileSystem.getPath("/dir/test3.txt"));
    }

}
