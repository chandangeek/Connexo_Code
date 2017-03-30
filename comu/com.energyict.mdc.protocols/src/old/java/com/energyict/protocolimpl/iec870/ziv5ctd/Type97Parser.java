/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Type97Parser.java
 *
 * Created on 12 april 2006, 16:26
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/** @author fbo */

public class Type97Parser implements TypeParser {
    
    /** Creates a new instance of Type97Parser */
    public Type97Parser() { }

    public InformationObject parse(ByteArray byteArray) {
        InformationObject97 io97 = new InformationObject97( );
        io97.setDirection( byteArray.sub(7,1).intValue(0) );
        return (InformationObject)io97;
    }
    
}
