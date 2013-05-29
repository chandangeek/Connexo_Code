package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Person;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:42
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

    PersonInfos(List<Person> persons) {
        addAll(persons);
    }

    PersonInfo add(Person person) {
        PersonInfo result = new PersonInfo(person);
        persons.add(result);
        total++;
        return result;
    }

    void addAll(List<Person> persons) {
        for (Person each : persons) {
            add(each);
        }
    }

}
