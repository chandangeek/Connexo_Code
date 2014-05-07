package com.energyict.protocols.messaging;


/**
 * This enum represents all possible datatypes for Cosem attributes (Integer, Decimal, String, Date and Other)
 *
 * @author Isabelle
 */
public enum CosemDataType {

    INTEGER(1),
    DECIMAL(2),
    STRING(3),
    DATE(4),
    OTHER(256);

    private int type;

    CosemDataType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
