/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Type47Parser.java
 *
 * Created on 12 april 2006, 15:00
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/**
 *
 * @author fbo
 */
public class Type47Parser implements TypeParser {
    
    /** Creates a new instance of Type47Parser */
    public Type47Parser() {
    }

    public InformationObject parse(ByteArray byteArray) {
        InformationObject47 io47 = new InformationObject47();
        io47.setMonth( byteArray.sub( 6, 1 ).bitValue(0,3));
        io47.setYear( byteArray.sub( 6, 1 ).bitValue(4,7 ));
        io47.setManufacturerCode( byteArray.sub( 7, 1 ).intValue(0,1) );
        io47.setProductCode( byteArray.sub(8,4).toHexaString(false) );
        return (InformationObject)io47;
    }
    
}
