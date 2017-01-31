/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HalfDuplexController.java
 *
 * Created on 17 maart 2004, 15:41
 */

package com.energyict.mdc.protocol.api.legacy;

import java.io.IOException;

/**
 * RS485 Halfduplex interface
 *
 * @author Koen
 *
 * @deprecated Create your own <i>custom</i> ConnectionType based on the AbstractSerialConnectionType(?). Wrap the Input- and OutputStreams
 * so the halfDuplex can be controlled by the connectionType.
 */
public interface HalfDuplexController {

    /**
     * Set the RTS and wait for the CTS before continue.
     * Fix timeout of 2000 ms to wait for CTS to follow RTS
     *
     * @param nrOfBytes
     */
    void request2Send(int nrOfBytes);

    /**
     * Clear the RTS and wait for the CTS before continue...
     * Fix timeout of 2000 ms to wait for CTS to follow RTS
     *
     * @param nrOfBytes delay calculated using nrOfBytes and baudrate
     */
    void request2Receive(int nrOfBytes);

    /**
     * Wait for CD L, set the RTS H and wait for the CTS H before continue
     * KV 26062006 Modem V25/V25bis Halfduplex interface
     *
     * @param nrOfBytes
     */
    void request2SendV25(int nrOfBytes);

    /**
     * Clear the RTS and wait for the CD H before continue.
     *
     * @param nrOfBytes delay calculated using nrOfBytes and baudrate
     */
    void request2ReceiveV25(int nrOfBytes);

    /**
     * request2SendRS485
     */
    void request2SendRS485();

    /**
     * @param nrOfBytes
     */
    void request2ReceiveRS485(int nrOfBytes);

    /**
     * Extra delay to add after transmit
     *
     * @param halfDuplexTXDelay extra delay to add after transmit before dropping RTS
     */
    void setDelay(long halfDuplexTXDelay);

    /**
     * @return
     * @throws IOException
     */
    boolean sigCD() throws IOException;

    /**
     * @return
     * @throws IOException
     */
    boolean sigCTS() throws IOException;

    /**
     * @param dtr
     * @throws IOException
     */
    void setDTR(boolean dtr) throws IOException;

    /**
     * @param rts
     * @throws IOException
     */
    void setRTS(boolean rts) throws IOException;
}
