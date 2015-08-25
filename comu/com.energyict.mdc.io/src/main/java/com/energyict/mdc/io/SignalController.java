package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides functionality to manipulate the signals of the Serial Port
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 16:00
 */
@ProviderType
public interface SignalController {

    /**
     * Returns the state of serial port DSR (Data Set Ready) signal.
     */
    public boolean signalStateDSR();

    /**
     * Returns state of serial port CTS (Clear To Send) signal.
     */
    public boolean signalStateCTS();

    /**
     * Returns state of serial port CD (Carrier Detect) signal.
     */
    public boolean signalStateCD();

    /**
     * Returns true if the serial port for this object detected a Ring on the line since the last call to this method.
     */
    public boolean signalStateRing();

    /**
     * Set the DTR (Data Terminal Ready) signal of objects serial port.
     *
     * @param dtr The state that the objects DTR line will be set to.
     */
    public void setDTR(boolean dtr);

    /**
     * Set the RTS (Request To Send) signal of objects serial port.
     *
     * @param rts the state that the objects RTS line will be set to.
     */
    public void setRTS(boolean rts);

}