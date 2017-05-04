/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.io.IOException;

abstract class Frame implements Marshalable {

    public final static byte START_FIXED = 0x10;
    public final static byte START_VARIABLE = 0x68;
    public final static byte END = 0x16;

    protected Address address;
    protected ControlField controlField;

    ByteArray rawdata;

    /** Constructor for frame to be transmitted */
    Frame( Address address, ControlField controlField ){
        this.address = address;
        this.controlField = controlField;
    }
    
    /** Constructor for received frame */
    Frame( Address address, ControlField controlField, ByteArray rawData ){
        this( address, controlField );
        this.rawdata = rawData;
    }

    boolean isPrm() {
        return controlField.isPrm();
    }

    Frame setPrm(boolean prm){
        controlField.setPrm(prm);
        return this;
    }
    
    /** Frame Count bit Valid
     * @return  boolean true for 1, false for 0 */
    boolean isFcv(){
        return controlField.isFcv();
    }

    /** Frame Count bit Valid
     * @param fcv true for 1, false for 0
     * @return this
     */
    Frame setFcv(boolean fcv){
        controlField.setFcv(fcv);
        return this;
    }

    /** Frame Count Bit
     * @return  boolean true for 1, false for 0 */
    boolean isFcb(){
        return controlField.isFcb();
    }

    /** Frame Count Bit
     * @param fcb true for 1, false for 0
     * @return this
     */
    Frame setFcb(boolean fcb){
        controlField.setFcb(fcb);
        return this;
    }
    
    FunctionCode getFunctionCode(){
        return controlField.getFunctionCode();
    }
    
    boolean isConfirmAck(){
        FunctionCode confirmAck = FunctionCode.SECONDARY[0];
        return !isPrm() && controlField.getFunctionCode().equals(confirmAck);
    }
    
    boolean isConfirmNack(){
        FunctionCode confirmAck = FunctionCode.SECONDARY[1];
        return !isPrm() && controlField.getFunctionCode().equals(confirmAck);
    }

    abstract Frame requestRespond( LinkLayer linkLayer ) throws IOException, ParseException;

    public String toString(){
        if( rawdata == null )
            return toByteArray().toHexaString();
        else
            return rawdata.toHexaString();
    }
    
}