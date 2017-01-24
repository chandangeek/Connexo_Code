package com.energyict.protocolimpl.enermet.e120;

interface Parser {

    Response parse(E120 e120, ByteArray byteArray);
    
}
