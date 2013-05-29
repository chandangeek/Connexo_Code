package com.elster.jupiter.parties.rest;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Person;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:46
 */
@XmlRootElement
public class PersonInfo {

    // party
    public long id;
    public String mRID;
    public String name;
    public String aliasName;
    public String description;
    public ElectronicAddress electronicAddress;

    //person
    public String firstName;
    public String lastName;
    public String middleName;
    public String prefix;
    public String suffix;
    public String specialNeed;
    public TelephoneNumber landLinePhone;
    public TelephoneNumber mobilePhone;

    public PersonInfo() {
    }

    public PersonInfo(Person person) {
        id = person.getId();
        mRID = person.getMRID();
        name = person.getName();
        aliasName = person.getAliasName();
        description = person.getDescription();
        electronicAddress = person.getElectronicAddress();
        firstName = person.getFirstName();
        lastName = person.getLastName();
        middleName = person.getMiddleName();
        prefix = person.getPrefix();
        suffix = person.getSuffix();
        specialNeed = person.getSpecialNeed();
        landLinePhone = person.getLandLinePhone();
        mobilePhone = person.getMobilePhone();
    }
}
