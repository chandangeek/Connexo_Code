package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.parties.Organization;

import static com.elster.jupiter.util.Checks.is;

public final class OrganizationImpl extends PartyImpl implements Organization {

	private PostalAddress postalAddress;
	private StreetAddress streetAddress;
	
	private OrganizationImpl() {
		super();
	}

    OrganizationImpl(String mRID) {
        super();
        validateMRID(mRID);
        setMRID(mRID);
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

}
