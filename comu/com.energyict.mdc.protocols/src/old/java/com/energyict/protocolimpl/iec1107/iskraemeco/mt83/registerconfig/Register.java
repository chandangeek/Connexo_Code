/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapping.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;


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
