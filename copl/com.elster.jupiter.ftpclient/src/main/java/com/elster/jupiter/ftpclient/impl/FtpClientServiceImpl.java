package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.ftpclient.IOConsumer;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.util.Collections;

@Component(name = "com.elster.jupiter.ftpclient", service = {FtpClientService.class}, property = "name=" + FtpClientService.COMPONENT_NAME, immediate = true)
public class FtpClientServiceImpl implements FtpClientService {

    private final FtpFileSystemProvider ftpProvider = new FtpFileSystemProvider();
    private final FtpsFileSystemProvider ftpsProvider = new FtpsFileSystemProvider();
    private final SftpFileSystemProvider sftpProvider = new SftpFileSystemProvider();

    @Inject
    public FtpClientServiceImpl() {
    }

    @Override
    public FtpSessionFactory getFtpFactory(String host, int port, String user, String password) {
        return new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                try {
                    URI uri = new URI(FtpFileSystemProvider.SCHEME, user + ':' + password, host, port, null, null, null);
                    try (FileSystem fileSystem = ftpProvider.newFileSystem(uri, Collections.emptyMap())) {
                        ftpSessionBehavior.accept(fileSystem);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public FtpSessionFactory getSftpFactory(String host, int port, String user, String password) {
        return new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                try {
                    URI uri = new URI(FtpFileSystemProvider.SCHEME, user + ':' + password, host, port, null, null, null);
                    try (FileSystem fileSystem = sftpProvider.newFileSystem(uri, Collections.emptyMap())) {
                        ftpSessionBehavior.accept(fileSystem);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public FtpSessionFactory getSftpFactory(String host, int port) {
        return new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                try {
                    URI uri = new URI(FtpFileSystemProvider.SCHEME, null, host, port, null, null, null);
                    try (FileSystem fileSystem = sftpProvider.newFileSystem(uri, Collections.emptyMap())) {
                        ftpSessionBehavior.accept(fileSystem);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public FtpSessionFactory getFtpsFactory(String host, int port, String user, String password) {
        return new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                try {
                    URI uri = new URI(FtpFileSystemProvider.SCHEME, user + ':' + password, host, port, null, null, null);
                    try (FileSystem fileSystem = ftpsProvider.newFileSystem(uri, Collections.emptyMap())) {
                        ftpSessionBehavior.accept(fileSystem);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
