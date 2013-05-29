package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Person;

import static com.elster.jupiter.util.Checks.is;

public class PersonImpl extends PartyImpl implements Person {

	private String firstName;
	private String lastName;
	private String mName;
	private String prefix;
	private String suffix;
	private String specialNeed;

    private PersonImpl() {
    }

    /**
     * @param firstName should not be null nor empty
     * @param lastName should not be null nor empty
     */
	PersonImpl(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        validate();
    }

    private void validate() {
        if (is(firstName).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (is(lastName).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
    }

    public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMiddleName() {
		return mName;
	}

	@Override
    public void setMiddleName(String mName) {
		this.mName = mName;
	}

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

}
