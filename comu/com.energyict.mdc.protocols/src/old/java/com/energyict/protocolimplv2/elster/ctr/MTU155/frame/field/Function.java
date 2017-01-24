package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

/**
 * Enum with all common function objects
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 17:53:49
 */
public enum Function {

    ACK(0x2B, "APlication ACK"),
    NACK(0x2D, "Apllication NACK"),
    IDENTIFICATION_REQUEST(0x28, "Identification request"),
    IDENTIFICATION_REPLY(0x29, "Identification reply"),
    QUERY(0x3F, "Query"),
    ANSWER(0x21, "Answer"),
    VOLUNTARY(0x3B, "Voluntary"),
    EXECUTE(0x26, "Execute function"),
    WRITE(0x2F, "Write function"),
    END_OF_SESSION(0x25, "End of session"),
    SECRET(0x23, "Secret"),
    DOWNLOAD(0x24, "Download"),
    INVALID_FUNCTION(0xFF, "Invalid structure code");

    private final int functionCode;
    private final String description;

    private Function(int structureCode, String description) {
        this.functionCode = structureCode;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public static Function fromFunctionCode(int code) {
        for (Function function : Function.values()) {
            if (function.getFunctionCode() == (code & 0x3F)) {
                return function;
            }
        }
        return INVALID_FUNCTION;
    }


}
