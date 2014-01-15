package com.energyict.mdc.protocol.api;

import java.io.IOException;

/**
 * Interface to pass on bubble up data (caught by the RTU+Server) to a protocol.
 * Returns a BubbleUpObject (containing a list of Registers and ProfileData).
 * <p/>
 * Copyrights EnergyICT
 * Date: 31-mei-2011
 * Time: 9:09:48
 */
public interface BubbleUp {

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException;

}
