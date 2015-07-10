package com.elster.jupiter.users;

public interface LdapUserDirectory extends UserDirectory {

    void setDirectoryUser(String directoryUser);

    String getDirectoryUser();

    String getUrl();

    String getSecurity();

    void setSecurity(String security);

    String getBackupUrl();

    void setBackupUrl(String backupUrl);

    void setUrl(String url);

    String getPassword();

    void setPassword(String password);

    String getBaseUser();

    void setBaseUser(String baseUser);

    String getBaseGroup();

    void setBaseGroup(String baseGroup);

}
