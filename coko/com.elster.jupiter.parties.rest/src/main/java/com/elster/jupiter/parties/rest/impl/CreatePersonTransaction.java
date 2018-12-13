/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

        return partyService.newPerson(info.firstName, info.lastName)
                .setMRID(info.mRID)
                .setName(info.name)
                .setAliasName(info.aliasName)
                .setDescription(info.description)
                .setElectronicAddress(info.electronicAddress)
                .setMiddleName(info.mName)
                .setPrefix(info.prefix)
                .setSuffix(info.suffix)
                .setSpecialNeed(info.specialNeed)
                .setLandLinePhone(info.landLinePhone)
                .setMobilePhone(info.mobilePhone)
                .create();
    }
}
