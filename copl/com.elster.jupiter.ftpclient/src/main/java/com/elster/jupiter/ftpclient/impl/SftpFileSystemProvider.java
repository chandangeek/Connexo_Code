/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import java.net.URI;

public class SftpFileSystemProvider extends AbstractFtpFileSystemProvider<SftpFileSystem> {

    public static final String SCHEME = "sftp";

    public SftpFileSystemProvider() {
        super(SCHEME);
    }

    @Override
    SftpFileSystem createNewFileSystem(URI uri) {
        return new SftpFileSystem(uri, this);
    }
}
