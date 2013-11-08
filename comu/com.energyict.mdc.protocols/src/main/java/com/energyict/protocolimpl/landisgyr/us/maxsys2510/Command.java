package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

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
