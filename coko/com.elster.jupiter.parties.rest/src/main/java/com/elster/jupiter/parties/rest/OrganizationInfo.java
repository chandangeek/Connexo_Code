package com.elster.jupiter.parties.rest;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.parties.Organization;

@XmlRootElement
public class OrganizationInfo extends PartyInfo {

    public PostalAddress postalAddress;
    public StreetAddress streetAddress;
    
    public OrganizationInfo() {
    	
    }

    public OrganizationInfo(Organization organization) {
        super(organization);
        postalAddress = organization.getPostalAddress();
        streetAddress = organization.getStreetAddress();
    }

    public void update(Organization organization) {
        updateParty(organization);
        organization.setPostalAddress(postalAddress);
        organization.setStreetAddress(streetAddress);
    }

}
