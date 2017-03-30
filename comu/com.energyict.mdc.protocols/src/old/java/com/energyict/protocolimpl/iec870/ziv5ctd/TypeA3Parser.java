/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Type0xa3Parser.java
 *
 * Created on 4 april 2006, 15:24
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.TimeZone;

/**
 * @author fbo
 */

public class TypeA3Parser implements TypeParser {
    
    TimeZone timeZone;
    
    /** Creates a new instance of Type0xa3Parser */
    public TypeA3Parser(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    public InformationObject parse(ByteArray byteArray) {
        ByteArray sub = byteArray.sub(7, byteArray.length()-8);
        return new InformationObjectC0( timeZone, sub );
    }
    
}
