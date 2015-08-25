package com.energyict.mdc.protocol.api;

/**
 * Exception thrown when the request DLMS object is not present in the instantiated object list of the meter.
 *
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 4/06/2015 - 11:13
 */
public class NotInObjectListException extends ProtocolException {

    public NotInObjectListException() {super();}
    public NotInObjectListException(String msg) {super(msg);}
    public NotInObjectListException(Exception e) {super(e);}
    public NotInObjectListException(Exception e, String msg) {super(e, msg);}

}