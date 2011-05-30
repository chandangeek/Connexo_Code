package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 26-apr-2011
 * Time: 15:28:50
 */
public interface BubbleUp {

    public BubbleUpObject parseBubbleUpData(byte[] data) throws IOException;

}
