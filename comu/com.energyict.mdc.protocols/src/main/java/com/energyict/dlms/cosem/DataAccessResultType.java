package com.energyict.dlms.cosem;

import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 4-jan-2011
 * Time: 8:34:59
 */
public class DataAccessResultType extends AbstractDataType {

    private final DataAccessResultCode dataAccessResultCode;

    public DataAccessResultType(DataAccessResultCode code) {
        this.dataAccessResultCode = code;
    }

    public DataAccessResultCode getDataAccessResultCode() {
        return dataAccessResultCode;
    }

    @Override
    public int intValue() {
        return getDataAccessResultCode().getResultCode();
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        return new byte[0];
    }

    @Override
    protected int size() {
        return doGetBEREncodedByteArray().length;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(intValue());
    }

    @Override
    public long longValue() {
        return intValue();
    }
}
