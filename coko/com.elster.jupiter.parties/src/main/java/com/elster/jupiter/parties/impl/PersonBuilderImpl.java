/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.parties.PersonBuilder;

import java.lang.reflect.Proxy;

class PersonBuilderImpl implements PersonBuilder {

    private final PersonImpl person;
    private PersonBuilder state;

    public PersonBuilderImpl(DataModel dataModel, String firstName, String lastName) {
        person = dataModel.getInstance(PersonImpl.class).init(firstName, lastName);
        state = new NormalBuilder();
    }

    private class NormalBuilder implements PersonBuilder {

        @Override
        public PersonBuilder setMRID(String mRID) {
            person.setMRID(mRID);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setName(String name) {
            person.setName(name);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setAliasName(String aliasName) {
            person.setAliasName(aliasName);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setDescription(String description) {
            person.setDescription(description);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setElectronicAddress(ElectronicAddress electronicAddress) {
            person.setElectronicAddress(electronicAddress);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setLandLinePhone(TelephoneNumber telephoneNumber) {
            person.setLandLinePhone(telephoneNumber);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setMiddleName(String mName) {
            person.setMiddleName(mName);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setMobilePhone(TelephoneNumber telephoneNumber) {
            person.setMobilePhone(telephoneNumber);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setPrefix(String prefix) {
            person.setPrefix(prefix);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setSpecialNeed(String specialNeed) {
            person.setSpecialNeed(specialNeed);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setSuffix(String suffix) {
            person.setSuffix(suffix);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setFirstName(String firstName) {
            person.setFirstName(firstName);
            return PersonBuilderImpl.this;
        }

        @Override
        public PersonBuilder setLastName(String lastName) {
            person.setLastName(lastName);
            return PersonBuilderImpl.this;
        }

        @Override
        public Person create() {
            state = usedState();
            person.doSave();
            return person;
        }
    }

    @Override
    public PersonBuilder setMRID(String mRID) {
        return state.setMRID(mRID);
    }

    @Override
    public PersonBuilder setName(String name) {
        return state.setName(name);
    }

    @Override
    public PersonBuilder setAliasName(String aliasName) {
        return state.setAliasName(aliasName);
    }

    @Override
    public PersonBuilder setDescription(String description) {
        return state.setDescription(description);
    }

    @Override
    public PersonBuilder setElectronicAddress(ElectronicAddress electronicAddress) {
        return state.setElectronicAddress(electronicAddress);
    }

    @Override
    public PersonBuilder setLandLinePhone(TelephoneNumber telephoneNumber) {
        return state.setLandLinePhone(telephoneNumber);
    }

    @Override
    public PersonBuilder setMiddleName(String mName) {
        return state.setMiddleName(mName);
    }

    @Override
    public PersonBuilder setMobilePhone(TelephoneNumber telephoneNumber) {
        return state.setMobilePhone(telephoneNumber);
    }

    @Override
    public PersonBuilder setPrefix(String prefix) {
        return state.setPrefix(prefix);
    }

    @Override
    public PersonBuilder setSpecialNeed(String specialNeed) {
        return state.setSpecialNeed(specialNeed);
    }

    @Override
    public PersonBuilder setSuffix(String suffix) {
        return state.setSuffix(suffix);
    }

    @Override
    public PersonBuilder setFirstName(String firstName) {
        return state.setFirstName(firstName);
    }

    @Override
    public PersonBuilder setLastName(String lastName) {
        return state.setLastName(lastName);
    }

    @Override
    public Person create() {
        return state.create();
    }

    private PersonBuilder usedState() {
        return (PersonBuilder) Proxy.newProxyInstance(
                PersonBuilder.class.getClassLoader(),
                new Class[] {PersonBuilder.class},
                (proxy, method, args) -> {
                    throw new IllegalStateException();
                });
    }
}
