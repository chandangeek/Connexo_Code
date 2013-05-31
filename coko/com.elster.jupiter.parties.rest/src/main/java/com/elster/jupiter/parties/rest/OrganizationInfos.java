package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Organization;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrganizationInfos {

    public int total;

    public List<OrganizationInfo> persons = new ArrayList<>();

    OrganizationInfos() {
    }

    OrganizationInfos(Organization organization) {
        add(organization);
    }

    OrganizationInfos(List<Organization> organizations) {
        addAll(organizations);
    }

    OrganizationInfo add(Organization organization) {
        OrganizationInfo result = new OrganizationInfo(organization);
        persons.add(result);
        total++;
        return result;
    }

    void addAll(List<Organization> organizations) {
        for (Organization each : organizations) {
            add(each);
        }
    }

}
