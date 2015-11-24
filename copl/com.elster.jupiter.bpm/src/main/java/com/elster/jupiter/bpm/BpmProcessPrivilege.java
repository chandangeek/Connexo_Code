package com.elster.jupiter.bpm;


public interface BpmProcessPrivilege {

    String getPrivilegeName();

    String getApplication();

    long getProcessId();

    void setPrivilegeName(String privilegeName);

    void setApplication(String application);

    void persist();

    void delete();
}
