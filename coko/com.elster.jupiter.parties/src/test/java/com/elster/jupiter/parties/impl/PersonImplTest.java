package com.elster.jupiter.parties.impl;

import org.junit.Before;

import static org.fest.reflect.core.Reflection.field;

public class PersonImplTest extends PartyImplTest {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "LastName";
    private static final long ID = 5661L;
    private static final long OTHER_ID = 999L;
    private PersonImpl person = new PersonImpl(FIRST_NAME, LAST_NAME);

    @Before
    public void setUp() {
        field("id").ofType(Long.TYPE).in(person).set(ID);
    }

    @Override
    protected PartyImpl getInstanceToTest() {
        return person;
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        return person;
    }

    @Override
    protected Object getInstanceEqualToA() {
        PersonImpl equalInstance = new PersonImpl(FIRST_NAME, LAST_NAME);
        field("id").ofType(Long.TYPE).in(equalInstance).set(ID);
        return equalInstance;
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        PersonImpl equalInstance = new PersonImpl(FIRST_NAME, LAST_NAME);
        field("id").ofType(Long.TYPE).in(equalInstance).set(OTHER_ID);
        return equalInstance;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
