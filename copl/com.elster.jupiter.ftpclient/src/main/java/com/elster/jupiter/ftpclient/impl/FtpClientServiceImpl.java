package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.ftpclient.IOConsumer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.util.Collections;

public class FtpClientServiceImpl implements FtpClientService {

    private final EdtFtpjFileSystemProvider ftpProvider = new EdtFtpjFileSystemProvider();

    @Override
    public FtpSessionFactory getFtpFactory(String host, int port, String user, String password) {
        return new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                try {
                    URI uri = new URI(EdtFtpjFileSystemProvider.SCHEME, user + ':' + password, host, port, null, null, null);
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
    public FtpSessionFactory getSFtpFactory(String host, int port, String user, String password) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public FtpSessionFactory getFtpsFactory(String host, int port, String user, String password) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
