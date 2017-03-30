/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterInfo.java
 *
 * Created on 28 mei 2004, 11:41
 */

package com.energyict.mdc.protocol.api.device.data;

/**
 * @author Koen
 */
public class RegisterInfo {

    String info;

    /**
     * Creates a new instance of RegisterInfo
     */
    public RegisterInfo(String info) {
        this.info = info;
    }

    public String toString() {
        return info;
    }

}
