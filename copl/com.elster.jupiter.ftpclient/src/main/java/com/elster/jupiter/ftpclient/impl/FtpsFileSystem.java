package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.ssl.SSLFTPClient;

import java.io.IOException;
import java.net.URI;

public class FtpsFileSystem extends ProFtpClientFileSystem<FtpsFileSystem> {
    FtpsFileSystem(FtpsFileSystemProvider provider, URI uri) {
        super(uri, provider, initFtpsClient());
    }

    private static SSLFTPClient initFtpsClient() {
        try {
            return new SSLFTPClient();
        } catch (FTPException e) {
            throw new IOExceptionWrapper(new IOException(e));
        }
    }
}
