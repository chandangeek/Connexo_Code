package com.elster.us.protocolimpl.landisgyr.quad4;

abstract class Command {
    
    public abstract ByteArray toByteArray();
    
    boolean isBlockCommand() {
        return false;
    }
    
    boolean isBlockAcknowledgmentCommand(){
        return false;
    }
    
    boolean isStandardCommand(){
        return false;
    }
    
}
