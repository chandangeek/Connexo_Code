/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordTest {

    @Test
    public void testConstructor() {
        Password password = new Password("myPassword");
        assertEquals("myPassword", password.getValue());
    }

    @Test
    public void testSetValue() {
        Password password = new Password("myPassword");
        password.setValue("mySecondPassword");
        assertEquals("mySecondPassword", password.getValue());
    }

    @Test
    public void testEquality(){
        Password password1 = new Password("myPassword");
        Password password2 = new Password("myPassword");
        assertTrue(password1.equals(password2));
        password2.setValue("myPassword2");
        assertFalse(password1.equals(password2));
        password2.setValue(null);
        assertFalse(password1.equals(password2));
        password1.setValue(null);
        assertTrue(password1.equals(password2));
        password2.setValue("myPassword2");
        assertFalse(password1.equals(password2));
    }

    @Test
    public void isEmptyTest(){
        Password password1 = new Password("myPassword");
        assertFalse("password myPassword not is empty", password1.isEmpty());

        Password password2 = new Password();
        assertTrue("password with null string is empty", password2.isEmpty());

        Password password3 = new Password("");
        assertTrue("password with empty string is empty", password3.isEmpty());
    }

}
