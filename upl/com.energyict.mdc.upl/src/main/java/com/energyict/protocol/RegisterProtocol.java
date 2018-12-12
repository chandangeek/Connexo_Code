/*
 * RegisterProtocol.java
 *
 * Created on 27 mei 2004, 16:12
 */

package com.energyict.protocol;

import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * RegisterProtocol interface must be implemented in the protocolclass if meter
 * register readings are needed. The collection software uses a universal
 * identification mechanism based on OBIS codes.
 * translateRegister(...) method is used at configuration time. The returned
 * RegisterInfo object informs the collection software if the meter protocol
 * supports a register for the given ObisCode.
 * readRegister(...) method is used by the collection software at runtime to
 * retrieve the meter's register value for the given ObisCode.
 *
 * @author Koen
 */
public interface RegisterProtocol {

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param obisCode the ObisCode to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws IOException Thrown in case of an exception
     */
    RegisterInfo translateRegister(ObisCode obisCode) throws IOException;

    /**
     * Request a RegisterValue object for an ObisCode. If the ObisCode is not
     * supported, NoSuchRegister is thrown.
     *
     * @param obisCode The ObisCode for which to request a RegisterValue
     * @return RegisterValue object for an ObisCode
     * @throws IOException Thrown in case of an exception
     */
    RegisterValue readRegister(ObisCode obisCode) throws IOException;

}