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
    public TelephoneNumber phone1;
    public TelephoneNumber phone2;

    //person
    public String firstName;
    public String lastName;
    public String mName;
    public String prefix;
    public String suffix;
    public String specialNeed;

    public PersonInfo() {
    }

    public PersonInfo(Person person) {
        id = person.getId();
        mRID = person.getMRID();
        name = person.getName();
        aliasName = person.getAliasName();
        description = person.getDescription();

    }
}
