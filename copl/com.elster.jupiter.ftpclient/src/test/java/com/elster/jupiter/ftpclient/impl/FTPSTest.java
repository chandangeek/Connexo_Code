/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.ssl.SSLFTPCertificateStore;
import com.enterprisedt.net.ftp.ssl.SSLFTPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStoreException;


public class FTPSTest {

    public static final int SERVER_CONTROL_PORT = 40407;
    public static final String USERNAME = "tgr";
    public static final String PASSWORD = "tgr";
    public static final String REMOTE_CONTENT = "abcdef 1234567890";
    private FileSystem fakeFileSystem;

    @Before
    public void setUp() {
        com.enterprisedt.util.license.License.setLicenseDetails("EnergyICTnv", "326-1363-8168-7486");

        fakeFileSystem = new WindowsFakeFileSystem();
        fakeFileSystem.add(new DirectoryEntry("c:\\data"));
        fakeFileSystem.add(new FileEntry("c:\\data\\file1.txt", REMOTE_CONTENT));
        fakeFileSystem.add(new FileEntry("c:\\data\\run.exe"));

    }

    @After
    public void tearDown() {
    }

    @Ignore // local test with real FTP server
    @Test
    public void testUploadByWritingToOutputStream() throws IOException, InterruptedException {
        try {
            System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\tgr\\Work\\Certificates\\truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "tgrtgr");

            SSLFTPClient sslftpClient = new SSLFTPClient();

            sslftpClient.setRemoteHost("localhost");
            sslftpClient.setRemotePort(21);
            sslftpClient.setImplicitFTPS(false);
            SSLFTPCertificateStore store = new SSLFTPCertificateStore();
            store.importDefaultKeyStore();
            sslftpClient.setRootCertificateStore(store);

            sslftpClient.connect();
            sslftpClient.auth(SSLFTPClient.AUTH_TLS);

            sslftpClient.login("tgr", "tgr");


            ByteArrayInputStream inputStream = new ByteArrayInputStream("Contents".getBytes());

            sslftpClient.put(inputStream, "TTT.txt");

        } catch (FTPException | KeyStoreException e) {
            throw new IOExceptionWrapper(new IOException(e));
        }


    }


}