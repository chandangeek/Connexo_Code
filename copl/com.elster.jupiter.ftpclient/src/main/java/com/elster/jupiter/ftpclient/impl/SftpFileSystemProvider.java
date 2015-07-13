package com.elster.jupiter.ftpclient.impl;

import java.net.URI;

public class SftpFileSystemProvider extends AbstractFtpFileSystemProvider<SftpFileSystem> {

    public static final String SCHEME = "ftps";

    public SftpFileSystemProvider() {
        super(SCHEME);
    }

    @Override
    SftpFileSystem createNewFileSystem(URI uri) {
        return new SftpFileSystem(uri, this);
    }
}
