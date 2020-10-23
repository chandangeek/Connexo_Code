package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.pki.SecretFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class SecretFactoryTest {

    private SecretFactory secretFactory;

    @Before
    public void setUp(){
        secretFactory = new SecretFactory();
    }


    @Test
    public void testPasswordGenerationSimple(){
        String password = secretFactory.generatePassword();

        assertTrue("Password length >=8 ", password.length()>=8);
        assertTrue("Minimum policy, use 1 special char", secretFactory.passwordMeetsRequirements(password,1,1,1,1));
        assertTrue("Minimum policy, use 2 special chars", secretFactory.passwordMeetsRequirements(password,1,1,1,2));
    }



    @Test
    public void testPasswordGenerationLengthFallback(){
        String password = secretFactory.generatePassword(0);

        assertTrue("Password length >=8 ", password.length()>=8);
        assertTrue("Minimum policy, use 1 special char", secretFactory.passwordMeetsRequirements(password,1,1,1,1));
        assertTrue("Minimum policy, use 2 special chars", secretFactory.passwordMeetsRequirements(password,1,1,1,2));
    }

    @Test
    public void testPasswordValidationHappyCase(){
        String password = "abCD12!@";

        assertTrue("Minimum policy, use 2 of each", secretFactory.passwordMeetsRequirements(password,2,2,2,2));
    }


    @Test
    public void testPasswordValidationNegativeCase(){
        assertFalse("Minimum policy, use 2 of each", secretFactory.passwordMeetsRequirements("abCD12x@",2,2,2,2));
        assertFalse("Minimum policy, use 2 of each", secretFactory.passwordMeetsRequirements("abCDaa!@",2,2,2,2));
        assertFalse("Minimum policy, use 2 of each", secretFactory.passwordMeetsRequirements("abvv12!@",2,2,2,2));
        assertFalse("Minimum policy, use 2 of each", secretFactory.passwordMeetsRequirements("AAAA12!@",2,2,2,2));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPasswordGenerationImpossibleRule(){
        secretFactory.generatePassword(8,2,3,4,5);
    }


    @Test
    public void testShuffle(){
        String input = "qwertyuiop=-0987654321asdfghjkl;' /.,mnbvcxz";
        String output = secretFactory.shuffle(input);

        assertNotEquals("output and input must be different", input,output);

        assertEquals("output must be same size", input.length(),output.length());

        for (char c : input.toCharArray()){
            assertTrue("Input characters must be in output string", output.indexOf(c)>=0);
        }
    }


    @Test
    public void testStrongPasswordGeneration(){
        String password = secretFactory.generatePassword(16, 4,4, 4,4);

        assertTrue("Strong password", password.length()==16);
    }


    @Test
    public void testOnlyUppercase(){
        String password = secretFactory.generatePassword(16, 1, 0, 0,0);

        for (char c : password.toCharArray()){
            assertFalse(SecretFactory.LOWERCASE_SET.indexOf(c)>=0);
            assertTrue(SecretFactory.UPPERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.NUMBERS_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.SPECIAL_SET.indexOf(c)>=0);
        }
    }

    @Test
    public void testOnlyLowercase(){
        String password = secretFactory.generatePassword(16, 0, 1, 0,0);

        for (char c : password.toCharArray()){
            assertTrue(SecretFactory.LOWERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.UPPERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.NUMBERS_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.SPECIAL_SET.indexOf(c)>=0);
        }
    }

    @Test
    public void testOnlyNumbers(){
        String password = secretFactory.generatePassword(16, 0, 0, 1, 0);

        for (char c : password.toCharArray()){
            assertFalse(SecretFactory.LOWERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.UPPERCASE_SET.indexOf(c)>=0);
            assertTrue(SecretFactory.NUMBERS_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.SPECIAL_SET.indexOf(c)>=0);
        }
    }

    @Test
    public void testOnlySpecialCharacters(){
        String password = secretFactory.generatePassword(16, 0, 0, 0, 1);

        for (char c : password.toCharArray()){
            assertFalse(SecretFactory.LOWERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.UPPERCASE_SET.indexOf(c)>=0);
            assertFalse(SecretFactory.NUMBERS_SET.indexOf(c)>=0);
            assertTrue(SecretFactory.SPECIAL_SET.indexOf(c)>=0);
        }
    }



    @Test
    public void testHexKeyGeneration(){
        byte[] secret = secretFactory.generateHexByteArray(16);

        assertEquals("Matches length", 16, secret.length);
    }
}
