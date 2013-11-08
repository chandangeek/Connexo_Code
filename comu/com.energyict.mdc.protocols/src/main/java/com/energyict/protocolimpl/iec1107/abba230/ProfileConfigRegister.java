package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;
import java.util.Collection;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 3/01/12
 * Time: 15:45
 */
public interface ProfileConfigRegister {

    void loadConfig(ABBA230RegisterFactory rFactory, byte[] data) throws IOException;

    void loadConfig(ABBA230RegisterFactory rFactory, int data) throws IOException;

    int getNumberRegisters();

    Collection toChannelInfo() throws IOException;

    byte[] getAllChannelMask();

    public String toShortString();
}