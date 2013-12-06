package com.energyict.mdc.common;

import com.energyict.mdc.common.Password;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 9/07/13
 * Time: 11:01
 */
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
    public void testNullable() {
        Password password = new Password("");
        assert(password.isNull());
        password = new Password(null);
        assert(password.isNull());
        password = new Password("myPassword");
        assert(!password.isNull());
    }

}
