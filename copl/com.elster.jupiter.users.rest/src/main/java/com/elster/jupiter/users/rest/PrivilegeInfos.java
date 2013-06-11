package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.rest.PrivilegeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PrivilegeInfos {

    public int total;

    public List<PrivilegeInfo> privileges = new ArrayList<>();

    PrivilegeInfos() {
    }

    PrivilegeInfos(Privilege privilege) {
        add(privilege);
    }

    PrivilegeInfos(List<Privilege> privileges) {
        addAll(privileges);
    }

    PrivilegeInfo add(Privilege privilege) {
        PrivilegeInfo result = new PrivilegeInfo(privilege);
        privileges.add(result);
        total++;
        return result;
    }

    void addAll(List<Privilege> privileges) {
        for (Privilege each : privileges) {
            add(each);
        }
    }

}
