package com.energyict.protocolcommon;

import com.energyict.cbo.Nullable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;

/**
 * For storing properties of type 'Password'
 * User: jbr
 * Date: 16-sep-2010
 * Time: 18:32:58
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class Password implements Nullable, Serializable {

    private String value;

    public Password() {
    }

    public Password(String value) {
        this.value = value;
    }

    @XmlAttribute
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Password) && Equality.equalityHoldsFor(((Password) o).getValue()).and(value);
    }

    @Override
    public boolean isNull() {
        return getValue() == null || getValue().trim().isEmpty();
    }

    @Override
    public String toString() {
        char[] chars = new char[value.length()];
        Arrays.fill(chars, '*');
       return new String(chars);
    }
}
