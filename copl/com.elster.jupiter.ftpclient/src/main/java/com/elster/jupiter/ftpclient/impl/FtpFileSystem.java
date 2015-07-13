package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.pro.ProFTPClient;

import java.net.URI;

class FtpFileSystem extends ProFtpClientFileSystem<FtpFileSystem> {

    FtpFileSystem(FtpFileSystemProvider provider, URI uri) {
        super(uri, provider, new ProFTPClient());
    }

}
