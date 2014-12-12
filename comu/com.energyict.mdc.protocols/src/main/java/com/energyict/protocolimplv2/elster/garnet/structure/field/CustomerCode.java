package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class CustomerCode extends AbstractField<CustomerCode> {

    public static final int LENGTH = 4;

    private String customerCode;

    public CustomerCode() {
        this.customerCode = new String();
    }

    public CustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromHexString(customerCode, LENGTH);
    }

    @Override
    public CustomerCode parse(byte[] rawData, int offset) throws ParsingException {
        customerCode = getHexStringFromBCD(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }
}