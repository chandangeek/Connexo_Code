package com.elster.jupiter.ftpclient.impl;

import java.net.URI;

public class FtpsFileSystemProvider extends AbstractFtpFileSystemProvider<FtpsFileSystem> {

    public static final String SCHEME = "ftps";

    public FtpsFileSystemProvider() {
        super(SCHEME);
    }

    @Override
    FtpsFileSystem createNewFileSystem(URI uri) {
        return new FtpsFileSystem(this, uri);
    }
}
