package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FtpPathTest extends EqualsContractTest {

    private FtpPath instanceA;
    @Mock
    private FtpFileSystem fileSystem;
    @Mock
    private FtpFileSystemProvider ftpjFileSystemProvider;
    private URI uri;

    @Before
    public void equalsContractSetUp() {
        try {
            doReturn(fileSystem).when(ftpjFileSystemProvider).newFileSystem(any(URI.class), any());
            when(fileSystem.getPath(any(), anyVararg())).thenAnswer(invocation -> {
                ArrayList<String> paths = new ArrayList<>();
                Object[] arguments = invocation.getArguments();
                for (int i = 0; i < arguments.length; i++) {
                    paths.addAll(Arrays.asList((String) arguments[i]));
                }
                return new FtpPath(fileSystem, paths);
            });

            com.enterprisedt.util.license.License.setLicenseDetails("EnergyICTnv", "326-1363-8168-7486");
            try {
                uri = new URI("ftp", "test:test", "localhost", 21, null, null, null);
                when(fileSystem.getUri()).thenReturn(uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            super.equalsContractSetUp();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @After
    public void tearDownFileSystem() throws IOException {
        fileSystem.close();
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = fileSystem.getPath("/dir/sub1/sub2/file.txt");
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return fileSystem.getPath("/dir/sub1/sub2/file.txt");
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                fileSystem.getPath("/"),
                fileSystem.getPath("dir/sub1/sub2/file.txt"),
                fileSystem.getPath("/dir/sub3/sub2/file.txt"),
                fileSystem.getPath("")
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testCreation() {
        FtpPath sub4 = fileSystem.getPath("/temp/sub1", "sub2/sub3", "sub4", "/sub5/sub6");

        assertThat(sub4.toString()).isEqualTo("/temp/sub1/sub2/sub3/sub4/sub5/sub6");
        assertThat(sub4.getNameCount()).isEqualTo(7);
    }


    @Test
    public void testGetFileSystem() throws Exception {
        Path path = fileSystem.getPath("/directory");

        assertThat(path.getFileSystem()).isSameAs(fileSystem);
    }

    @Test
    public void testIsAbsoluteTrue() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.isAbsolute()).isTrue();
    }

    @Test
    public void testIsAbsoluteFalse() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2");

        assertThat(path.isAbsolute()).isFalse();
    }

    @Test
    public void testIsAbsoluteFalseForEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.isAbsolute()).isFalse();
    }

    @Test
    public void testGetRootNull() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2");

        assertThat(path.getRoot()).isNull();
    }

    @Test
    public void testGetRootOfEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.getRoot()).isNull();
    }

    @Test
    public void testGetRootNotNull() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.getRoot()).isEqualTo(fileSystem.getPath("/"));
    }

    @Test
    public void testGetFileName() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.getFileName()).isEqualTo(fileSystem.getPath("sub2"));
    }

    @Test
    public void testGetFileNameOnRoot() throws Exception {
        Path path = fileSystem.getPath("/");

        assertThat(path.getFileName()).isNull();
    }

    @Test
    public void testGetFileNameOnEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.getFileName()).isEqualTo(path);
    }

    @Test
    public void testGetParent() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.getParent()).isEqualTo(fileSystem.getPath("/directory/sub1"));
    }

    @Test
    public void testGetParentOfRoot() throws Exception {
        Path path = fileSystem.getPath("/");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void testGetParentOfEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void testGetParentOfRelative() throws Exception {
        Path path = fileSystem.getPath("granite");

        assertThat(path.getParent()).isNull();
    }

    @Test
    public void testGetNameCountTrivial() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.getNameCount()).isEqualTo(3);
    }

    @Test
    public void testGetNameCountOfEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.getNameCount()).isEqualTo(0);
    }

    @Test
    public void testGetNameCountRoot() throws Exception {
        Path path = fileSystem.getPath("/");

        assertThat(path.getNameCount()).isEqualTo(0);

    }

    @Test
    public void testGetNameCountSingle() throws Exception {
        Path path = fileSystem.getPath("fileName.txt");

        assertThat(path.getNameCount()).isEqualTo(1);
    }

    @Test
    public void testGetName() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2");

        assertThat(path.getName(0)).isEqualTo(fileSystem.getPath("directory"));
    }

    @Test
    public void testSubpath() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.subpath(1, 3)).isEqualTo(fileSystem.getPath("sub1/sub2"));
    }

    @Test
    public void testStartsWithTrue() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.startsWith(fileSystem.getPath("directory/sub1"))).isTrue();
    }

    @Test
    public void testStartsWithFalse() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.startsWith(fileSystem.getPath("directory/sub2"))).isFalse();
    }

    @Test
    public void testStartsWithOnEmptyFalse() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.startsWith(fileSystem.getPath("directory/sub2"))).isFalse();
    }

    @Test
    public void testEndsWithTrue() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.endsWith(fileSystem.getPath("sub3/sub4"))).isTrue();
    }

    @Test
    public void testEndsWithFalse() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.endsWith(fileSystem.getPath("sub2/sub4"))).isFalse();
    }

    @Test
    public void testNormalize() throws Exception {
        Path path = fileSystem.getPath("directory/../sub1/sub2/./sub3/sub6//sub5/../././../sub4");

        assertThat(path.normalize()).isEqualTo(fileSystem.getPath("sub1/sub2/sub3/sub4"));

    }

    @Test
    public void testNormalizeRoot() throws Exception {
        Path path = fileSystem.getPath("/");

        assertThat(path.normalize()).isEqualTo(fileSystem.getPath("/"));

    }

    @Test
    public void testNormalizeEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.normalize()).isEqualTo(fileSystem.getPath(""));

    }

    @Test
    public void testResolve() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.resolve(fileSystem.getPath("sub5/sub6"))).isEqualTo(fileSystem.getPath("directory/sub1/sub2/sub3/sub4/sub5/sub6"));
    }

    @Test
    public void testResolveAbsolute() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.resolve(fileSystem.getPath("/sub5/sub6"))).isEqualTo(fileSystem.getPath("/sub5/sub6"));
    }

    @Test
    public void testResolveSibling() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.resolveSibling(fileSystem.getPath("sub5/sub6"))).isEqualTo(fileSystem.getPath("directory/sub1/sub2/sub3/sub5/sub6"));
    }

    @Test
    public void testResolveVsRoot() throws Exception {
        Path path = fileSystem.getPath("/");

        assertThat(path.resolveSibling(fileSystem.getPath("sub5/sub6"))).isEqualTo(fileSystem.getPath("sub5/sub6"));
    }

    @Test
    public void testResolveEmpty() throws Exception {
        Path path = fileSystem.getPath("");

        assertThat(path.resolveSibling(fileSystem.getPath("sub5/sub6"))).isEqualTo(fileSystem.getPath("sub5/sub6"));
    }

    @Test
    public void testRelativize() throws Exception {
//TODO
    }

    @Test
    public void testToUri() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.toUri()).isEqualTo(new URI("ftp", "localhost", "/directory/sub1/sub2/sub3/sub4", null));
    }

    @Test
    public void testToAbsolutePath() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.toAbsolutePath()).isEqualTo(fileSystem.getPath("/directory/sub1/sub2/sub3/sub4"));
    }

    @Test
    public void testToRealPath() throws Exception {

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToFile() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        path.toFile();
    }

    @Test
    public void testIterator() throws Exception {
        Path path = fileSystem.getPath("directory/sub1/sub2/sub3/sub4");

        assertThat(path.iterator()).contains(
                fileSystem.getPath("directory"),
                fileSystem.getPath("sub1"),
                fileSystem.getPath("sub2"),
                fileSystem.getPath("sub3"),
                fileSystem.getPath("sub4")
        );

    }

    @Test
    public void testIteratorOfAbsolute() throws Exception {
        Path path = fileSystem.getPath("/directory/sub1/sub2/sub3/sub4");

        assertThat(path.iterator()).contains(
                fileSystem.getPath("directory"),
                fileSystem.getPath("sub1"),
                fileSystem.getPath("sub2"),
                fileSystem.getPath("sub3"),
                fileSystem.getPath("sub4")
        );

    }

    @Test
    public void testCompareTo() throws Exception {
        ArrayList<FtpPath> paths = new ArrayList<>(Arrays.asList(fileSystem.getPath("directory"),
                fileSystem.getPath("/sub2"),
                fileSystem.getPath("sub3"),
                fileSystem.getPath("sub1"),
                fileSystem.getPath("/sub4")));

        paths.sort(Comparator.<FtpPath>naturalOrder());

        assertThat(paths).contains(
                fileSystem.getPath("directory"),
                fileSystem.getPath("sub1"),
                fileSystem.getPath("sub3"),
                fileSystem.getPath("/sub2"),
                fileSystem.getPath("/sub4")
        );

    }

}