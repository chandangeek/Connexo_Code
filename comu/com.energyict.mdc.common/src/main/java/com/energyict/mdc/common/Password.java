/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import static com.elster.jupiter.util.Checks.is;

/**
 * For storing properties of type 'Password'.
 * User: jbr
 * Date: 16-sep-2010
 * Time: 18:32:58
 * To change this template use File | Settings | File Templates.
 */
public class Password {

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

    public int length(){
        return value.length();
    }

    public boolean isEmpty(){
        return value == null || value.isEmpty();
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Password) && is(((Password) o).getValue()).equalTo(value);
    }

}