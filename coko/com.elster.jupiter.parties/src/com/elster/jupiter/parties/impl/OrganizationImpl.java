package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.parties.Organization;

public class OrganizationImpl extends PartyImpl implements Organization {

	private PostalAddress postalAddress;
	private StreetAddress streetAddress;
	
	private OrganizationImpl() {
		super();
	}

	public PostalAddress getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(PostalAddress postalAddress) {
		this.postalAddress = postalAddress;
	}

	public StreetAddress getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(StreetAddress streetAddress) {
		this.streetAddress = streetAddress;
	}
}
