/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/**
 */

public class FrameType {

    private String description;

    private FrameType( String description ){
        this.description = description;
    }

    private static FrameType create( String description ) {
        FrameType ft = new FrameType( description );
        return ft;
    }

    static final FrameType SEND_NO_REPLY = create("SEND/NO REPLY expected");
    static final FrameType REQUEST_RESPOND = create("REQUEST/RESPOND expected");
    static final FrameType SEND_CONFIRM = create("SEND/CONFIRM expected");

    static final FrameType CONFIRM = create("CONFIRM");
    static final FrameType RESPOND = create("RESPOND");

    public String toString(){
        return new StringBuffer()
            .append("FrameType [").append(description)
            .append("]").toString();
    }

}
