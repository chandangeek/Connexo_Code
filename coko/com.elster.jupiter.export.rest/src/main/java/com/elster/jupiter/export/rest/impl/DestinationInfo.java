/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class DestinationInfo {
    public DestinationType type;

    public long id;

    public String fileName;
    public String fileExtension;

    // file
    public String fileLocation;

    // email
    public String recipients;
    public String subject;

    // (s)ftp(s)
    public String server;
    public String user;
    public String password;
    public int port;

    // web service
    public IdWithNameInfo createEndPoint;
    public IdWithNameInfo changeEndPoint;
}
