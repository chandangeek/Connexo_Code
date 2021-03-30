package com.energyict.protocolimplv2.dlms.common.obis.readers;

import java.io.IOException;

public class MappingException extends Throwable {

    public MappingException(String msg) {
        super(msg);
    }

    public MappingException(IOException e) {
        super(e);
    }

}
