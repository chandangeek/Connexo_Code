/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.util.Checks;

import java.net.URI;

public class SftpFileSystemProvider extends AbstractFtpFileSystemProvider<SftpFileSystem> {

    public static final String SCHEME = "sftp";
    private String hostsFile;

    public SftpFileSystemProvider() {
        super(SCHEME);
    }

    @Override
    SftpFileSystem createNewFileSystem(URI uri) {
        return new SftpFileSystem(uri, this);
    }

    public String getHostsFile() {
        return !Checks.is(hostsFile).emptyOrOnlyWhiteSpace()
                ? hostsFile
                : ((System.getProperty("os.name").toLowerCase().contains("win")) ? System.getProperty("user.home") : "~") + "/.ssh/known_hosts";
    }

    public void setHostsFile(String hostsFile) {
        this.hostsFile = hostsFile;
    }
}
