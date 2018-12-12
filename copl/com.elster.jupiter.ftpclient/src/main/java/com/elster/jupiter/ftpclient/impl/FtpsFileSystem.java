/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.ssl.SSLFTPCertificateStore;
import com.enterprisedt.net.ftp.ssl.SSLFTPClient;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStoreException;

public class FtpsFileSystem extends ProFtpClientFileSystem<SSLFTPClient, FtpsFileSystem> {
    FtpsFileSystem(FtpsFileSystemProvider provider, URI uri) {
        super(uri, provider, initFtpsClient());
    }

    private static SSLFTPClient initFtpsClient() {
        try {
            SSLFTPClient sslftpClient = new SSLFTPClient();
            sslftpClient.setImplicitFTPS(false);
            SSLFTPCertificateStore store = new SSLFTPCertificateStore();
            store.importDefaultKeyStore();
            sslftpClient.setRootCertificateStore(store);
            return sslftpClient;
        } catch (FTPException | KeyStoreException e) {
            throw new IOExceptionWrapper(new IOException(e));
        } catch (IOException e) {
            throw new IOExceptionWrapper(e);
        }
    }

    @Override
    protected void postConnectCommands(SSLFTPClient ftpClient) throws IOException, FTPException {
        ftpClient.auth(SSLFTPClient.AUTH_TLS);
    }
}
