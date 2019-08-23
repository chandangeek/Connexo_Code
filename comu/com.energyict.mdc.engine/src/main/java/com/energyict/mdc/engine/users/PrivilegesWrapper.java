package com.energyict.mdc.engine.users;

import com.elster.jupiter.users.Privilege;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class PrivilegesWrapper {

    private List<Privilege> list;

    public PrivilegesWrapper() {
    }

    public PrivilegesWrapper(List<Privilege> list) {
        this.list = list;
    }

    @XmlElement(type = OfflinePrivilege.class)
    public List<Privilege> getList() {
        return list;
    }

    public void setList(List<Privilege> list) {
        this.list = list;
    }
}
