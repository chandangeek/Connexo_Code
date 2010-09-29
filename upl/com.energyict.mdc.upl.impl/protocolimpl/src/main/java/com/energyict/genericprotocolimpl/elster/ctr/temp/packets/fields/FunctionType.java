package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 16:03:26
 */
public enum FunctionType {

    ACK('K'),
    NACK('N'),
    QUERY_REQUEST('Q'),
    QUERY_RESPONSE('A'),
    QUERY_EVENTS_RESPONSE1('V'),
    QUERY_EVENTS_RESPONSE2('G'),
    IDENTIFICATION_REQUEST('I'),
    IDENTIFICATION_RESPONSE('J'),
    WRITE_REQUEST('W'),
    EXECUTE_REQUEST('F'),
    END_OF_SESSION_REQUEST('E');

    private final Integer functionTypeValue;

    FunctionType(int functionTypeValue) {
        this.functionTypeValue = new Integer(functionTypeValue);
    }

    public int getFunctionTypeValue() {
        return functionTypeValue;
    }

    public char getFunctionTypeValueAsChar() {
        return (char) (getFunctionTypeValue() & 0x07F);
    }

    public static FunctionType getFunctionType(int functionTypeValue) {
        for (FunctionType type : FunctionType.values()) {
            if (type.functionTypeValue.intValue() == (functionTypeValue & 0x07F)) {
                return type;
            }
        }
        throw new IllegalArgumentException("FunctionType with functionTypeValue '" + functionTypeValue + "' does not exist!");
    }

    public FunctionCode getFunctionCode() {
        return new FunctionCode(getFunctionTypeValue());
    }
}
