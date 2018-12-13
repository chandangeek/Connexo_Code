/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.pro.ProFTPClient;

import java.net.URI;

class FtpFileSystem extends ProFtpClientFileSystem<ProFTPClient, FtpFileSystem> {

    FtpFileSystem(FtpFileSystemProvider provider, URI uri) {
        super(uri, provider, new ProFTPClient());
    }

}
