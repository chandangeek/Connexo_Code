package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.BpmStartup;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StartupInfo {

    public String url;
    public String user;
    public String password;

    public StartupInfo() {
        url = BpmStartup.getInstance().getUrl();
        user = BpmStartup.getInstance().getUser();
        password = BpmStartup.getInstance().getPassword();
    }
}
