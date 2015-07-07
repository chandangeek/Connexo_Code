package com.elster.jupiter.ftpclient;

public interface FtpClientService {

    FtpSessionFactory getFtpFactory(String host, int port, String user, String password);

    FtpSessionFactory getSFtpFactory(String host, int port, String user, String password);

    FtpSessionFactory getFtpsFactory(String host, int port, String user, String password);

}
