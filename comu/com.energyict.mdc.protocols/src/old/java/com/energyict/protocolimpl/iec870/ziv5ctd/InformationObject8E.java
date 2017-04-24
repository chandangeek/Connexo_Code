/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject8E.java
 *
 * Created on 11 april 2006, 15:13
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;

/**
 *
 * @author fbo
 */
public class InformationObject8E {
    
    String manufacturer;
    String model;
    String firmwareVersion;
    String serialNumber;
    Date dateOfStandard;
    String protocolVersion;
    String battery;
    String comPort1;
          
    
    /** Creates a new instance of InformationObject8E */
    public InformationObject8E( ByteArray byteArray ) {
        
    }
    
}
