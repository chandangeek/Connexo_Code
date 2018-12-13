/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Person;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 */
@XmlRootElement
public class PersonInfos {

    public int total;

    public List<PersonInfo> persons = new ArrayList<>();

    PersonInfos() {
    }

    PersonInfos(Person person) {
        add(person);
    }

    PersonInfos(Iterable<? extends Person> persons) {
        addAll(persons);
    }

    PersonInfo add(Person person) {
        PersonInfo result = new PersonInfo(person);
        persons.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Person> persons) {
        for (Person each : persons) {
            add(each);
        }
    }

}
