package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DomainInfos {
    public int total;

    public List<DomainInfo> domains = new ArrayList<>();

    public DomainInfos() {
    }

    public DomainInfos(UserDirectory directory) {
        add(directory);
    }

    public DomainInfos(Iterable<? extends UserDirectory> directories) {
        addAll(directories);
    }

    public DomainInfo add(UserDirectory directory) {
        DomainInfo result = new DomainInfo(directory);
        domains.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends UserDirectory> directories) {
        for (UserDirectory each : directories) {
            add(each);
        }
    }
}
