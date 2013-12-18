package com.elster.jupiter.parties.impl;

import javax.inject.Inject;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.parties.Organization;

import static com.elster.jupiter.util.Checks.is;

public final class OrganizationImpl extends PartyImpl implements Organization {

	private PostalAddress postalAddress;
	private StreetAddress streetAddress;
	
	@Inject
	private OrganizationImpl(OrmClient ormClient, EventService eventService) {
		super(ormClient,eventService);
	}

    OrganizationImpl init(String mRID) {
        validateMRID(mRID);
        setMRID(mRID);
        return this;
    }

    private void validateMRID(String mRID) {
        if (is(mRID).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("MRID cannot be empty.");
        }
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

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + getId() +
                ", mRID='" + getMRID() + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }

    @Override
    public String getType() {
        return Organization.class.getSimpleName();
    }
}
