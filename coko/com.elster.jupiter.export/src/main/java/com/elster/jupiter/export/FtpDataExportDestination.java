/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

public interface FtpDataExportDestination extends DataExportDestination {

    String getServer();

    String getUser();

    String getPassword();

    String getFileName();

    String getFileExtension();

    String getFileLocation();

    int getPort();

    void setServer(String server);

    void setUser(String user);

    void setPassword(String password);

    void setFileName(String fileName);

    void setFileExtension(String fileExtension);

    void setFileLocation(String fileLocation);

    void setPort(int port);
}
