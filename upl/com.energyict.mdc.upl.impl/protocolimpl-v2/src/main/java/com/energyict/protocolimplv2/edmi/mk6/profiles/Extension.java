/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import java.io.Serializable;


/**
 *
 * @author koen
 */
public class Extension implements Serializable{
    
	private String name;
    private int registerId;
    
    /** Creates a new instance of ExtensionControl */
    public Extension(int registerId, String name) {
        this.setRegisterId(registerId);
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRegisterId() {
        return registerId;
    }

    private void setRegisterId(int registerId) {
        this.registerId = registerId;
    }
}