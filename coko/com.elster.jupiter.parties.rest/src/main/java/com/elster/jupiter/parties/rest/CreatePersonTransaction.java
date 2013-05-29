package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Person;

/**
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 9:03
 */
public class CreatePersonTransaction implements Runnable {
    private final PersonInfo info;
    private Person person;

    CreatePersonTransaction(PersonInfo info) {
        this.info = info;
    }

    Person execute() {
        Bus.getTransactionService().execute(this);
        return person;
    }

    @Override
    public void run() {
        person = Bus.getPartyService().newPerson(info.firstName, info.lastName);
        person.setMRID(info.mRID);
        person.setName(info.name);
        person.setAliasName(info.aliasName);
        person.setDescription(info.description);
        person.setElectronicAddress(info.electronicAddress);

        person.setMiddleName(info.middleName);
        person.setPrefix(info.prefix);
        person.setSuffix(info.suffix);
        person.setSpecialNeed(info.specialNeed);
        person.setLandLinePhone(info.landLinePhone);
        person.setMobilePhone(info.mobilePhone);

        person.save();
    }
}
