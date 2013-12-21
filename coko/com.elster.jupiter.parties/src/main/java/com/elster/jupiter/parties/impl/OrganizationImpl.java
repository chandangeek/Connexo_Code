package com.elster.jupiter.parties.impl;

import static com.elster.jupiter.util.Checks.is;

import javax.validation.Valid;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Organization;
import com.google.common.base.Objects;

public final class OrganizationImpl extends PartyImpl implements Organization {

	@Valid
	private PostalAddress postalAddress;
	@Valid
	private StreetAddress streetAddress;

    OrganizationImpl init(String mRID) {
        validateMRID(mRID);
        setMRID(mRID.trim());
        return this;
    }
    
    static OrganizationImpl from(DataModel dataModel, String mRID) {
    	return dataModel.getInstance(OrganizationImpl.class).init(mRID);
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
    	return Objects.toStringHelper(this).add("id",getId()).add("mRID", getMRID()).add("name",getName()).toString();
    }
}
