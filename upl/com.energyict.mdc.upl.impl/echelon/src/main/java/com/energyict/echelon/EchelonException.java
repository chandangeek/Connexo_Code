package com.energyict.echelon;

public class EchelonException extends Exception {
    
    public EchelonException(String message) {
        super(message);
    }
    
    public EchelonException(Throwable cause) {
        super(cause);
    }
    
    public EchelonException(String message, Throwable cause) {
        super(message, cause);
    }

}
