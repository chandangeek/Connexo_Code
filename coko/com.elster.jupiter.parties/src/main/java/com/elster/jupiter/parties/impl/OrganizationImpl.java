/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Organization;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;

import static com.elster.jupiter.util.Checks.is;

final class OrganizationImpl extends PartyImpl implements Organization {

	@Valid
	private PostalAddress postalAddress;
	@Valid
	private StreetAddress streetAddress;

	@Inject
	OrganizationImpl(DataModel dataModel, EventService eventService, Provider<PartyInRoleImpl> partyInRoleProvider, Provider<PartyRepresentationImpl> partyRepresentationProvider) {
		super(dataModel,eventService,partyInRoleProvider, partyRepresentationProvider);
	}

    OrganizationImpl init(String mRID) {
        validateMRID(mRID);
        setMRID(mRID.trim());
        return this;
    }

    private void validateMRID(String mRID) {
        if (is(mRID).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("MRID cannot be empty.");
        }
    }

    @Override
    public PostalAddress getPostalAddress() {
		return postalAddress.copy();
	}

    @Override
	public void setPostalAddress(PostalAddress postalAddress) {
		this.postalAddress = postalAddress;
	}

    @Override
	public StreetAddress getStreetAddress() {
		return streetAddress.copy();
	}

    @Override
	public void setStreetAddress(StreetAddress streetAddress) {
		this.streetAddress = streetAddress;
	}

    @Override
    public Class<Organization> getType() {
        return Organization.class;
    }

    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this).add("id",getId()).add("mRID", getMRID()).add("name",getName()).toString();
    }
}
