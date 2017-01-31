package com.energyict.protocol;

import java.io.IOException;
import java.util.List;

/**
 * BulkRegisterProtocol interface must be implemented in the protocolclass if meter
 * register readings are needed. The collection software used an universal
 * identification mechanism based on OBIS codes.
 * translateRegisters(...) method is used at configuration time. The returned
 * BulkRegisterInfo object informs the collection software if the meter protocol
 * supports a register for the given ObisCode.
 * readRegisters(...) method is used by the collection software at runtime to
 * retrieve the meter's register value for the given List of ObisCodes.
 * <p/>
 * Copyrights EnergyICT
 * Date: 4-feb-2011
 * Time: 9:54:07
 */
public interface BulkRegisterProtocol {

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws IOException Thrown in case of an exception
     */
    RegisterInfo translateRegister(Register register) throws IOException;

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    List<RegisterValue> readRegisters(List<Register> registers) throws IOException;

}
