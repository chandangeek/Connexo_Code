package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Person;

public class PersonImpl extends PartyImpl implements Person {
	private String firstName;
	private String lastName;
	private String mName;
	private String prefix;
	private String suffix;
	private String specialNeed;
	
	private PersonImpl() {
		super();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return mName;
	}

	public void setMiddleName(String mName) {
		this.mName = mName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getSpecialNeed() {
		return specialNeed;
	}

	public void setSpecialNeed(String specialNeed) {
		this.specialNeed = specialNeed;
	}

	@Override
	public TelephoneNumber getLandLinePhone() {
		return getPhone1();
	}

	@Override
	public TelephoneNumber getMobilePhone() {
		return getPhone2();
	}


}
