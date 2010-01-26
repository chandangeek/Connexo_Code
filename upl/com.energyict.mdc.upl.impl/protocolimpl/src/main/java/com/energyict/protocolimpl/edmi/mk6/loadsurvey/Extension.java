/*
 * ExtensionControl.java
 *
 * Created on 31 maart 2006, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

import java.io.Serializable;


/**
 *
 * @author koen
 */
public class Extension implements Serializable{
    
	/** Generated SerialVersionUID */
	private static final long serialVersionUID = 7839896346244404521L;
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
