package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:51)
 */
@ProviderType
public interface ModemComponent {

    public void connect(String name, SerialComChannel comChannel);

    public void disconnect(SerialComChannel comChannel);

    /**
     * Initialize the modem so it is ready for dialing/receival of a call.
     * During this initialization, several steps are performed:<br>
     * <ul>
     * <li>If present, the current connection of the modem is hung up</li>
     * <li>The default profile of the modem is restored.</li>
     * <li>All initialization strings are send out to the modem</li>
     * </ul>
     *
     * @param name
     * @param comChannel
     */
    public void initializeModem(String name, SerialComChannel comChannel);

    public void initializeAfterConnect(ComChannel comChannel);

    public void write(ComChannel comChannel, String dataToWrite);

    public boolean readAndVerify(ComChannel comChannel, String expectedAnswer, long timeOutInMillis);

}