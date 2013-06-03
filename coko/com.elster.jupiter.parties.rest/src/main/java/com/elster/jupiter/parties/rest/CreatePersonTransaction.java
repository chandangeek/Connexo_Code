package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.Transaction;

/**
 */
final class CreatePersonTransaction implements Transaction<Person> {
    private final PersonInfo info;

    CreatePersonTransaction(PersonInfo info) {
        this.info = info;
    }

    @Override
    public Person perform() {
        Person person = Bus.getPartyService().newPerson(info.firstName, info.lastName);
        person.setMRID(info.mRID);
        person.setName(info.name);
        person.setAliasName(info.aliasName);
        person.setDescription(info.description);
        person.setElectronicAddress(info.electronicAddress);

        person.setMiddleName(info.mName);
        person.setPrefix(info.prefix);
        person.setSuffix(info.suffix);
        person.setSpecialNeed(info.specialNeed);
        person.setLandLinePhone(info.landLinePhone);
        person.setMobilePhone(info.mobilePhone);

        person.save();

        return person;
    }
}
