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
        add(privilege);
    }

    public PrivilegeInfos(Iterable<? extends Privilege> privileges) {
        addAll(privileges);
    }

    public PrivilegeInfo add(Privilege privilege) {
        PrivilegeInfo result = new PrivilegeInfo(privilege);
        privileges.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends Privilege> privileges) {
        for (Privilege each : privileges) {
            add(each);
        }
    }

}
