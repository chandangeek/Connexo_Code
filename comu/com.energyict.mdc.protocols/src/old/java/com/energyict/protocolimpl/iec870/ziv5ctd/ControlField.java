package com.energyict.protocolimpl.iec870.ziv5ctd;


/**
 *
 * PRM Primary Message
 * 0 = message from secondary (responding) station
 * 1 = message from primary (initiating) station
 *
 * FCB Frame Count Bit
 * 0, 1 = alternating bit for successive SEND/CONFIRM or REQUEST/RESPOND services
 * per station.
 *
 * The primary station alternates the FCB bit for each new SEND/CONFIRM or
 * REQUEST/RESPOND transmission services directed at the same secondary station.
 * Thus the primary station keeps a copy of the frame count bit per secondary
 * station.  If an expected reply is timed out or garbled, then the same
 * SEND/CONFIRM or REQUEST/RESPOND services is repeated with the same frame
 * count bit.
 *
 * FCV Frame Count Bit Valid
 * O = alternating function of FCB is invalid
 * 1 = alternating function of FCB is valid
 * SEND/NO REPLY services, broadcast messages and other transmission services
 * that ignore the deletion of duplication or loss of information output do not
 * alternate the FCB bit and indicate this by a cleared FCV bit.
 *
 * DFC Data Flow Control
 * 0 = further messages are acceptable
 * 1 = further messages may cause data overflow
 * Secondary stations indicate to the message initiating station that an
 * immediate succession of a further message may cause a buffer overflow.
 *
 * ACD Access demand
 * There are two classes of message data provided, namely class 1 and class 2
 * 0 = no access demand for class 1 data transmission
 * 1 = access demand for class 1 data transmission
 *
 * Secondary stations indicate to the primary station the wish for class 1
 * data transimission.
 *
 * */

public class ControlField implements Marshalable {

    FunctionCode functionCode;
    byte control;

    private ControlField( ){ }

    ControlField( FunctionCode functionCode ) {
        this.functionCode = functionCode;
        this.control = functionCode.toByte();
    }

    static ControlField parse( byte aByte ){
        ControlField controlField = new ControlField();

        controlField.control = aByte;

        if( controlField.isPrm() ) {
            controlField.functionCode = FunctionCode.PRIMARY[aByte & 0x0F];
        }
        else {
            controlField.functionCode = FunctionCode.SECONDARY[aByte & 0x0F];
        }

        return controlField;
    }

    /** Frame Count Bit
     * @return  boolean true for 1, false for 0 */
    public boolean isFcb(){
        return (control & 0x20) != 0;
    }


    /** Frame Count Bit
     * @param fcb true for 1, false for 0
     * @return this
     */
    public ControlField setFcb(boolean fcb){
        if( fcb ){
            control = (byte) (control | 0x20 );
        } else {
            control = (byte) (control & ~0x20 );
        }
        return this;
    }

    /** Frame Count bit Valid
     * @return  boolean true for 1, false for 0 */
    public boolean isFcv(){
        return (control & 0x10) != 0;
    }


    /** Frame Count bit Valid
     * @param fcv true for 1, false for 0
     * @return this
     */
    public ControlField setFcv(boolean fcv){
        if( fcv ){
            control = (byte) (control | 0x10 );
        } else {
            control = (byte) (control & ~0x10 );
        }
        return this;
    }

    /** Primary message
     * 0 = message from secondary (responding) station
     * 1 = message from primary (initiating) station */
    public boolean isPrm(){
        return (control & 0x40) != 0;
    }

    public ControlField setPrm(boolean prm) {
        if( prm ){
            control = (byte) (control | 0x40);
        } else {
            control = (byte) (control & ~0x40);
        }
        return this;
    }

    public FunctionCode getFunctionCode(){
        return functionCode;
    }

    public ByteArray toByteArray(){
        return new ByteArray().add(control);
    }

    public String toString( ){
        return "ControlField[" + "" + toByteArray() + "]";
    }

}