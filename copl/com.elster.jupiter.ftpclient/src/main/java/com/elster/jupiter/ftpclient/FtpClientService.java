package com.elster.jupiter.ftpclient;

public interface FtpClientService {

    String COMPONENT_NAME = "FTP";

    FtpSessionFactory getFtpFactory(String host, int port, String user, String password);

    FtpSessionFactory getSftpFactory(String host, int port, String user, String password);

    FtpSessionFactory getSftpFactory(String host, int port);

    FtpSessionFactory getFtpsFactory(String host, int port, String user, String password);

}
