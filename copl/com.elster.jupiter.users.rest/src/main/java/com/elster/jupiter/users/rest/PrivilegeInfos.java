/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
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

    public PrivilegeInfos(NlsService nlsService, Privilege privilege) {
        this();
        add(nlsService, null, privilege);
    }

    public PrivilegeInfos(NlsService nlsService, Iterable<? extends Privilege> privileges) {
        this();
        addAll(nlsService, null, privileges);
    }

    public PrivilegeInfo add(NlsService nlsService, String application, Privilege privilege) {
        PrivilegeInfo result = new PrivilegeInfo(nlsService, application, privilege);
        privileges.add(result);
        total++;
        return result;
    }

    public void addAll(NlsService nlsService, String application, Iterable<? extends Privilege> privileges) {
        for (Privilege each : privileges) {
            add(nlsService, application, each);
        }
    }

}