package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;

/**
 */
final class CreatePersonTransaction implements Transaction<Person> {
    private final PersonInfo info;
    private final PartyService partyService;

    @Inject
    CreatePersonTransaction(PersonInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    public Person perform() {
        Person person = partyService.newPerson(info.firstName, info.lastName);
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
