package com.energyict.protocolimplv2.elster.garnet.frame;

import com.energyict.protocolimplv2.elster.garnet.common.field.Field;
import com.energyict.protocolimplv2.elster.garnet.exception.CrcMismatchException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Crc;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Function;

/**
 * @author sva
 * @since 23/05/2014 - 9:35
 */
public interface Frame<T extends Frame> extends Field<T> {

    /**
     * Getter for the destination ID
     */
    public Address getDestinationAddress();

    /**
     * Getter for the function code
     */
    public Function getFunction();

    /**
     * Getter for the data of the frame
     */
    public Data getData();

    /**
     * Getter for the crc of the frame
     */
    public Crc getCrc();

    /**
     * Generate the CRC16 and apply it to the frame
     */
    public void generateAndSetCRC();

    /**
     * Validate the CRC16 of this frame
     * @throws CrcMismatchException in case the CRC16 is not valid
     */
    public void validateCRC() throws CrcMismatchException;
}
