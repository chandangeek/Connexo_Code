package com.energyict.mdc.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.elster.jupiter.util.Checks.is;

/**
 * For storing properties of type 'Password'.
 * User: jbr
 * Date: 16-sep-2010
 * Time: 18:32:58
 * To change this template use File | Settings | File Templates.
 */
public class Password implements Nullable {

    private String value;

    public Password() {
    }

    public Password(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte[] calculateHash(int saltPara) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new ApplicationException(ex);
        }
        String source = "" + (saltPara / 3) + value + saltPara;
        return md.digest(source.getBytes());
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Password) && is(((Password) o).getValue()).equalTo(value);
    }

    @Override
    public boolean isNull() {
        return is(getValue()).emptyOrOnlyWhiteSpace();
    }

}