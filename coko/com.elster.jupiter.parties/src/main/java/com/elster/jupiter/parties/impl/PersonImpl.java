package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Person;

import static com.elster.jupiter.util.Checks.is;

public final class PersonImpl extends PartyImpl implements Person {

	private String firstName;
	private String lastName;
	private String mName;
	private String prefix;
	private String suffix;
	private String specialNeed;

    @SuppressWarnings("unused")
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
        validateFirstName(firstName);
        validateLastName(lastName);
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
		return mName;
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
		this.mName = mName;
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

}
