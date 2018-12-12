/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Person;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;

import static com.elster.jupiter.util.Checks.is;
import static com.google.common.base.MoreObjects.toStringHelper;

final class PersonImpl extends PartyImpl implements Person {

	@NotNull
	private String firstName;
	@NotNull
	private String lastName;
	private String middleName;
	private String prefix;
	private String suffix;
	private String specialNeed;

	@Inject
	PersonImpl(DataModel dataModel, EventService eventService,Provider<PartyInRoleImpl> partyInRoleProvider, Provider<PartyRepresentationImpl> partyRepresentationProvider) {
		super(dataModel,eventService, partyInRoleProvider, partyRepresentationProvider);
	}

    /**
     * @param firstName should not be null nor empty
     * @param lastName should not be null nor empty
     */
	PersonImpl init(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        validateFirstName(firstName);
        validateLastName(lastName);
        return this;
    }

    private void validateLastName(String name) {
        if (is(name).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
    }

    private void validateFirstName(String name) {
        if (is(name).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
    }

    @Override
    public String getFirstName() {
		return firstName;
	}

    @Override
	public String getLastName() {
		return lastName;
	}

    @Override
	public String getMiddleName() {
		return middleName;
	}

    @Override
    public void setFirstName(String firstName) {
        validateFirstName(firstName);
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        validateLastName(lastName);
        this.lastName = lastName;
    }

    @Override
    public void setMiddleName(String mName) {
		this.middleName = mName;
	}

    @Override
	public String getPrefix() {
		return prefix;
	}

	@Override
    public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	@Override
    public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
    public String getSpecialNeed() {
		return specialNeed;
	}

	@Override
    public void setSpecialNeed(String specialNeed) {
		this.specialNeed = specialNeed;
	}

    /**
     * maps to cim naming
     */
	@Override
	public TelephoneNumber getLandLinePhone() {
		return getPhone1();
	}

    /**
     * maps to cim naming
     */
	@Override
	public TelephoneNumber getMobilePhone() {
		return getPhone2();
	}

    @Override
    public void setLandLinePhone(TelephoneNumber telephoneNumber) {
        setPhone1(telephoneNumber);
    }

    @Override
    public void setMobilePhone(TelephoneNumber telephoneNumber) {
        setPhone2(telephoneNumber);
    }

    @Override
    public String toString() {
        return toStringHelper(this).omitNullValues().add("id",getId()).add("mRID",getMRID()).add("name", getName()).toString();
    }

    @Override
    public Class<Person> getType() {
        return Person.class;
    }
}
