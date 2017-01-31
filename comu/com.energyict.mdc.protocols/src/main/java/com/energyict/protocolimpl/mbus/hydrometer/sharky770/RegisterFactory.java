/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterFactory.java
 *
 * Created on 8 oktober 2007, 12:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.hydrometer.sharky770;

import com.energyict.protocolimpl.mbus.core.AbstractRegisterFactory;

/**
 *
 * @author kvds
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
    
    private final int CURRENT_DATETIME_INDEX=7;
    
    Sharky770 pN16;
            
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Sharky770 pN16) {
        super(pN16);
        this.pN16=pN16;
    }

    
    protected int getTimeIndex() {
        return CURRENT_DATETIME_INDEX;
    }
    
}
