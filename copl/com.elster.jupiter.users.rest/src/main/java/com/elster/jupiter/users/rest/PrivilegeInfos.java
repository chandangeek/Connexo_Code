package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PrivilegeInfos {

    public int total;

    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public PrivilegeInfos() {
    }

    public PrivilegeInfos(Privilege privilege) {
        add(null, privilege);
    }

    public PrivilegeInfos(Iterable<? extends Privilege> privileges) {
        addAll(null, privileges);
    }

    public PrivilegeInfo add(String application, Privilege privilege) {
        PrivilegeInfo result = new PrivilegeInfo(application, privilege);
        privileges.add(result);
        total++;
        return result;
    }

    public void addAll(String application, Iterable<? extends Privilege> privileges) {
        for (Privilege each : privileges) {
            add(application, each);
        }
    }

}
