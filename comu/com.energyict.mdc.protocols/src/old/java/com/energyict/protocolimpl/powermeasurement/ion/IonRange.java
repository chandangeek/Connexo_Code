/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

public class IonRange extends IonObject {

    int start;
    int end;
    
    IonRange( int start, int end ){
        this.start = start;
        this.end = end;
    }

    int getEnd() {
        return end;
    }

    void setEnd(int end) {
        this.end = end;
    }

    int getStart() {
        return start;
    }

    void setStart(int start) {
        this.start = start;
    }
    
    ByteArray toByteArray( ){
        return 
            new ByteArray( (byte)0x74 )
                .addInt( start, 4 )
                .addInt( end, 4)
                .add( (byte)0xf3 ); 
    }

}
