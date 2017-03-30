/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapping.java
 *
 * Created on 18 oktober 2004, 16:42
 */

package com.energyict.protocolimpl.customerconfig;


/**
 *
 * @author  Koen
 */
public class Register {
    String name;
    int id;

    /** Creates a new instance of RegisterMapping */
    public Register(String name, int id) {
        this.name=name;
        this.id=id;
    }

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return id;
    }
}
