/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Organization;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrganizationInfo extends PartyInfo {

    public TelephoneNumber phone1;
    public TelephoneNumber phone2;
    public PostalAddress postalAddress;
    public StreetAddress streetAddress;
    
    public OrganizationInfo() {
    	
    }

    public OrganizationInfo(Organization organization) {
        super(organization);
        postalAddress = organization.getPostalAddress();
        streetAddress = organization.getStreetAddress();
        phone1 = organization.getPhone1();
        phone2  = organization.getPhone2();
    }

    public void update(Organization organization) {
        updateParty(organization);
        organization.setPostalAddress(postalAddress);
        organization.setStreetAddress(streetAddress);
        organization.setPhone1(phone1);
        organization.setPhone2(phone2);
    }

}
