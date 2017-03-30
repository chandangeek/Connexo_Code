/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;
import java.util.Collection;

public interface ProfileConfigRegister {

    void loadConfig(ABBA230RegisterFactory rFactory, byte[] data) throws IOException;

    void loadConfig(ABBA230RegisterFactory rFactory, int data) throws IOException;

    int getNumberRegisters();

    Collection toChannelInfo() throws IOException;

    byte[] getAllChannelMask();

    public String toShortString();
}