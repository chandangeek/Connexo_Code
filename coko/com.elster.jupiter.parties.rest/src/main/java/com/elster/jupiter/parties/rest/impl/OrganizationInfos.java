/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class OrganizationInfos {

    public int total;

    public List<OrganizationInfo> organizations = new ArrayList<>();

    OrganizationInfos() {
    }

    OrganizationInfos(Organization organization) {
        add(organization);
    }

    OrganizationInfos(Iterable<? extends Organization> organizations) {
        addAll(organizations);
    }

    OrganizationInfo add(Organization organization) {
        OrganizationInfo result = new OrganizationInfo(organization);
        organizations.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Organization> organizations) {
        for (Organization each : organizations) {
            add(each);
        }
    }

}
