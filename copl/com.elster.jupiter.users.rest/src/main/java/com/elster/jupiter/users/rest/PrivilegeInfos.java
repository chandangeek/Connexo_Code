package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
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

    public PrivilegeInfos(Thesaurus thesaurus, Privilege privilege) {
        add(thesaurus, null, privilege);
    }

    public PrivilegeInfos(Thesaurus thesaurus, Iterable<? extends Privilege> privileges) {
        addAll(thesaurus, null, privileges);
    }

    public PrivilegeInfo add(Thesaurus thesaurus, String application, Privilege privilege) {
        PrivilegeInfo result = new PrivilegeInfo(thesaurus, application, privilege);
        privileges.add(result);
        total++;
        return result;
    }

    public void addAll(Thesaurus thesaurus, String application, Iterable<? extends Privilege> privileges) {
        for (Privilege each : privileges) {
            add(thesaurus, application, each);
        }
    }

}
