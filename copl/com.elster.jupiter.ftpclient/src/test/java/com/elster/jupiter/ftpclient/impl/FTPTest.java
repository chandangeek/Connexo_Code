package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class FTPTest {

    public static final int SERVER_CONTROL_PORT = 40407;
    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";
    public static final String REMOTE_CONTENT = "abcdef 1234567890";
    private FakeFtpServer fakeFtpServer;
    private FileSystem fakeFileSystem;

    @Before
    public void setUp() {
        com.enterprisedt.util.license.License.setLicenseDetails("EnergyICTnv", "326-1363-8168-7486");
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(USERNAME, PASSWORD, "c:\\data"));

        fakeFileSystem = new WindowsFakeFileSystem();
        fakeFileSystem.add(new DirectoryEntry("c:\\data"));
        fakeFileSystem.add(new FileEntry("c:\\data\\file1.txt", REMOTE_CONTENT));
        fakeFileSystem.add(new FileEntry("c:\\data\\run.exe"));
        fakeFtpServer.setFileSystem(fakeFileSystem);
        fakeFtpServer.setServerControlPort(SERVER_CONTROL_PORT);

        fakeFtpServer.start();

    }

    @After
    public void tearDown() {
        fakeFtpServer.stop();
    }

    @Test
    public void testListDirectoryContents() throws IOException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path root = fileSystem.getPath("/");

            List<Path> paths = Files.list(root).collect(Collectors.toList());

            assertThat(paths).contains(fileSystem.getPath("file1.txt"), fileSystem.getPath("run.exe"));
        });
    }

    @Test
    public void testUploadByWritingToOutputStream() throws IOException, InterruptedException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path newFile = fileSystem.getPath("./newfile.txt");
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(newFile))) {
                writer.write("content");
            }
        });

        assertThat(fakeFileSystem.exists("c:\\data\\newfile.txt")).isTrue();
        assertThat(fakeFileSystem.getEntry("c:\\data\\newfile.txt").getSize()).isEqualTo("content".length());
    }

    @Test
    public void testUploadByCopyFromOtherFileSystem() throws IOException, InterruptedException {
        java.nio.file.FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());

        Path sourceFile = jimfs.getPath("/temp.txt");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(sourceFile))) {
            writer.write("content");
        }

        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path newFile = fileSystem.getPath("./newfile.txt");
            Files.copy(sourceFile, newFile);
        });

        assertThat(fakeFileSystem.exists("c:\\data\\newfile.txt")).isTrue();
        assertThat(fakeFileSystem.getEntry("c:\\data\\newfile.txt").getSize()).isEqualTo("content".length());
    }

    @Test
    public void testCreateDirectory() throws IOException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path path = fileSystem.getPath("/a");
            Files.createDirectory(path);
        });

        assertThat(fakeFileSystem.exists("c:\\data\\a")).isTrue();

    }

    @Test
    public void testDownloadByReadingToInputStream() throws IOException, InterruptedException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path newFile = fileSystem.getPath("file1.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(newFile)))) {
                String line = reader.readLine();
                assertThat(line).isEqualTo(REMOTE_CONTENT);
                assertThat(reader.readLine()).isNull();
            }
        });

    }

    @Ignore // FakeFtpServer doesn't support the necessary commands atm, works on filezilla ftp. test kept as documentation
    @Test
    public void testDownloadByCopyToOtherFileSystem() throws IOException, InterruptedException {
        java.nio.file.FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());

        Path jimfsPath = jimfs.getPath("/file1.txt");

        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path remoteFile = fileSystem.getPath("file1.txt");
            Files.copy(remoteFile, jimfsPath);
        });

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(jimfsPath)))) {
            String line = reader.readLine();
            assertThat(line).isEqualTo(REMOTE_CONTENT);
            assertThat(reader.readLine()).isNull();
        }

    }

    @Ignore // currently not supported : there's no ftp command for this. can't simultaneously up and download so should download to local, then upload to remote
    @Test
    public void testCopyOnFtpFileSystem() throws IOException, InterruptedException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path remoteFile = fileSystem.getPath("file1.txt");
            Path target = fileSystem.getPath("file2.txt");
            Files.copy(remoteFile, target);
        });

        assertThat(fakeFileSystem.exists("c:\\data\\file2.txt")).isTrue();
        assertThat(fakeFileSystem.getEntry("c:\\data\\file2.txt").getSize()).isEqualTo(REMOTE_CONTENT.length());

    }

    @Test
    public void testDelete() throws IOException {
        FtpSessionFactory ftpFactory = new FtpClientServiceImpl().getFtpFactory("localhost", SERVER_CONTROL_PORT, USERNAME, PASSWORD);

        ftpFactory.runInSession(fileSystem -> {
            Path remoteFile = fileSystem.getPath("file1.txt");
            Files.delete(remoteFile);
        });

        assertThat(fakeFileSystem.exists("c:\\data\\file1.txt")).isFalse();
    }

}