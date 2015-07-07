package com.elster.jupiter.export;

public interface FtpDestination {

    String TYPE_IDENTIFIER = "FTPXX";

    String getServer();

    String getUser();

    String getPassword();

    String getFileName();

    String getFileExtension();

    String getFileLocation();

    void setServer(String server);

    void setUser(String user);

    void setPassword(String password);

    void setFileName(String fileName);

    void setFileExtension(String fileExtension);

    void setFileLocation(String fileLocation);
}
