/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import java.net.URI;

public class FtpFileSystemProvider extends AbstractFtpFileSystemProvider<FtpFileSystem> {

    public static final String SCHEME = "ftp";

    public FtpFileSystemProvider() {
        super(SCHEME);
    }

    @Override
    FtpFileSystem createNewFileSystem(URI uri) {
        return new FtpFileSystem(this, uri);
    }
}
