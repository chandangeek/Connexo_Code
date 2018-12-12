/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Person;

import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement
public class PersonInfo extends PartyInfo {

    public String firstName;
    public String lastName;
    public String mName;
    public String prefix;
    public String suffix;
    public String specialNeed;
    public TelephoneNumber landLinePhone;
    public TelephoneNumber mobilePhone;

    public PersonInfo() {
    }

    public PersonInfo(Person person) {
        super(person);
        firstName = person.getFirstName();
        lastName = person.getLastName();
        mName = person.getMiddleName();
        prefix = person.getPrefix();
        suffix = person.getSuffix();
        specialNeed = person.getSpecialNeed();
        landLinePhone = person.getLandLinePhone();
        mobilePhone = person.getMobilePhone();
    }

    public void update(Person person) {
        updateParty(person);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setMiddleName(mName);
        person.setPrefix(prefix);
        person.setSuffix(suffix);
        person.setSpecialNeed(specialNeed);
        person.setLandLinePhone(landLinePhone);
        person.setMobilePhone(mobilePhone);
    }

}
