package com.elster.jupiter.pki;


import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help generate secure passwords and random hex strings.
 * Includes also methods for shuffling a string and a password policy validator.
 */
public class SecretFactory {

    public static final int DEFAULT_PASSWORD_LENGTH = 10;
    public static final int DEFAULT_HLSECRET_SIZE = 32;

    public static final String UPPERCASE_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE_SET = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS_SET = "0123456789";
    public static final String SPECIAL_SET = "!@#$%^&*_=+-/.?<>)";


    /**
     * Generate a password with default password length (10 characters) and minimum complexity:
     * use uppercase, lowercase, numbers, at least 2 special characters.
     *
     * For a better complexity please use the option with defined complexity.
     *
     * @return random password
     */
    public String generatePassword(){
        return generatePassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Generate a password with a specific length and minimum complexity
     * use uppercase, lowercase, numbers, at least 2 special characters
     *
     * @param passwordLength length of password to generate
     * @return random password
     */
    public String generatePassword(int passwordLength){
        return generatePassword(passwordLength, 1, 1, 1, 2);
    }


    /**
     * Generate a password using the parameters found in a {@link KeyType}
     * No other restrictions are imposed on password complexity.
     *
     * @param keyType definition of password complexity policy
     * @return random password
     */
    public String generatePassword(KeyType keyType){
        return generatePassword(keyType.getPasswordLength(),
                keyType.useUpperCaseCharacters()?1:0,
                keyType.useLowerCaseCharacters()?1:0,
                keyType.useNumbers()?1:0,
                keyType.useSpecialCharacters()?1:0);
    }


    /**
     * Generate a password with imposed complexity policy, i.e. with the guaranteed numbers of character types requested.
     * A character set can be completely omitted if the parameters is set to zero
     *
     * Example:
     *          generatePassword(8, 1, 1, 1, 1) -> will use all characters, eg. aBcd3^r4
     *          generatePassword(8, 1, 1, 1, 5) -> will impose at lest 3 special characters, eg. aB#d3^r*
     *          generatePassword(8, 1, 0, 0, 0) -> will use only uppercase, eg. REJKLFFR
     *          generatePassword(8, 1, 7, 0, 0) -> will use exactly 1 uppercase, eg. sbcdAdsa
     *
     * @param passwordLength total password length to be generated
     * @param useUpperCase 0 - do not use this set
     *                     >= 1 use this minimum amount of characters in the set
     * @param useLowerCase 0 do not use this set
     *                     >= 1 use this minimum amount of characters in the set
     * @param useNumbers   0 do not use this set
     *                     >= 1  use this minimum amount of characters in the set
     * @param useSpecialChars  0 do not use this set
     *                         > 1 use this minimum amount of characters in the set
     * @return the generated password with requested rules.
     */
    public String generatePassword(int passwordLength, int useUpperCase, int useLowerCase, int useNumbers, int useSpecialChars){
        if (passwordLength==0){
            passwordLength = DEFAULT_PASSWORD_LENGTH;
        }

        if (useLowerCase + useUpperCase + useNumbers + useSpecialChars > passwordLength){
            String msg = String.format("The imposed password policy cannot be met! " +
                    "You requested minimum %d lower case + %d uppercase + %d numbers + %d special characters," +
                    "while the total password length should be %d. That doesn't sum up!" , useLowerCase, useUpperCase, useNumbers, useSpecialChars, passwordLength);
            throw new IllegalArgumentException(msg);
        }

        String allPossibleChars = getPossiblePasswordChars(useUpperCase, useLowerCase, useNumbers, useSpecialChars);

        String password = "";
        password += randomChars(UPPERCASE_SET, useUpperCase);
        password += randomChars(LOWERCASE_SET, useLowerCase);
        password += randomChars(NUMBERS_SET, useNumbers);
        password += randomChars(SPECIAL_SET, useSpecialChars);

        password = shuffle(password);

        password += randomChars(allPossibleChars, passwordLength - password.length());

        return shuffle(password);
    }

    /**
     * Shuffle a string using secure random.
     *
     * @param inputString - string to be shuffled
     * @return shuffled string
     */
    public String shuffle(final String inputString) {

        SecureRandom random = new SecureRandom();

        List<Character> input = new ArrayList<>();
        for (char c : inputString.toCharArray()){
            input.add(c);
        }

        String output = "";
        while(input.size()>0){
            int randomPos = (int)(random.nextDouble()*input.size());
            output += input.remove(randomPos);
        }
        return output;
    }


    /**
     * Extract random chars from an input string.
     *
     * @param possibleChars available set of characters
     * @param length number of random characters to extract
     * @return a random subset of input
     */
    public String randomChars(String possibleChars, int length){
        SecureRandom random = new SecureRandom();
        String output  = "";
        for (int i = 0; i < length; i++) {
            output += possibleChars.charAt((int) (random.nextDouble() * possibleChars.length()));
        }
        return output;
    }



    /**
     * Validator for a password, to be used for checking if a certain complexity policy is met.
     * Can be used to validate the user input or imported passwords.
     *
     * @param password the password to be validated
     * @param minUpperCase expected minimum number of uppercase characters
     * @param minLowerCase expected minimum number of lowercase characters
     * @param minNumbers expected minimum number of number-characters
     * @param minSpecialChars expected minimum number of special characters
     * @return true if the password meets the criteria, false otherwise
     */
    public boolean passwordMeetsRequirements(final String password, int minUpperCase, int minLowerCase, int minNumbers, int minSpecialChars) {
        int uppercase = 0;
        int lowercase = 0;
        int special = 0;
        int numbers = 0;
        for (char c : password.toCharArray()) {
            if (UPPERCASE_SET.indexOf(c) >= 0) {
                uppercase++;
            }
            if (LOWERCASE_SET.indexOf(c) >= 0) {
                lowercase++;
            }
            if (NUMBERS_SET.indexOf(c) >= 0) {
                numbers++;
            }
            if (SPECIAL_SET.indexOf(c) >= 0) {
                special++;
            }
        }

        return (lowercase>=minLowerCase)
                && (uppercase >= minUpperCase)
                && (numbers >= minNumbers)
                && (special >= minSpecialChars);
    }

    /**
     * Generate a random hex-array with a requested length.
     * Use this to generate keys, secure HLS secrets, etc.
     *
     * @param keySizeInBytes the requested length in BYTES (not bits)
     * @return the random secret
     */
    public byte[] generateHexByteArray(int keySizeInBytes) {
        if (keySizeInBytes == 0){
            keySizeInBytes = DEFAULT_HLSECRET_SIZE;
        }
        SecureRandom random = new SecureRandom();
        byte[] hexString = new byte[keySizeInBytes];
        for (int i=0; i < keySizeInBytes ; i++) {
            hexString[i] = (byte) (random.nextDouble()*0x100);
        }

        return hexString;
    }


    /**
     * Builds a concatenated set of characters out of desired types.
     */
    private String getPossiblePasswordChars(int minUpperCase, int minLowerCase, int minNumbers, int minSpecialChars) {
        String possibleChars = "";
        if (minUpperCase > 0) {
            possibleChars+=UPPERCASE_SET;
        }
        if (minLowerCase > 0) {
            possibleChars+=LOWERCASE_SET;
        }
        if (minNumbers > 0) {
            possibleChars+=NUMBERS_SET;
        }
        if (minSpecialChars > 0) {
            possibleChars+=SPECIAL_SET;
        }
        return possibleChars;
    }


}