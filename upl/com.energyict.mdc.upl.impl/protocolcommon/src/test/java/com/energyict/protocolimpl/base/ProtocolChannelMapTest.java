package com.energyict.protocolimpl.base;

import com.energyict.protocol.InvalidPropertyException;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.*;

/**
 * This is a place holder with only a few basic tests on the ProtocolChannelMap object.
 * We should add more in depth tests in the future 
 *
 * Copyrights EnergyICT
 * Date: 23-jun-2010
 * Time: 9:21:38
 */
public class ProtocolChannelMapTest {

    private ProtocolChannelMap[] channelMap = null;

    public ProtocolChannelMap[] getChannelMap() throws InvalidPropertyException {
        if (channelMap == null) {
            channelMap = new ProtocolChannelMap[] {
                    new ProtocolChannelMap(""),
                    new ProtocolChannelMap("test"),
                    new ProtocolChannelMap("0"),
                    new ProtocolChannelMap("", true),
                    new ProtocolChannelMap("0", true),
                    new ProtocolChannelMap(new ArrayList())
            };
        }
        return channelMap;
    }

    @Test(expected=InvalidPropertyException.class)
    public void testWrongConstructorParameters() throws Exception {
        new ProtocolChannelMap("test.123.1", true);
    }

    @Test
    public void testToString() throws Exception {
        for (int i = 0; i < getChannelMap().length; i++) {
            ProtocolChannelMap protocolChannelMap = getChannelMap()[i];
            assertNotNull(protocolChannelMap);
            assertNotNull(protocolChannelMap.toString());
            assertNotSame("", protocolChannelMap.toString());
        }
    }

    @Test
    public void testGetChannelRegisterMap() throws Exception {
        for (int i = 0; i < getChannelMap().length; i++) {
            ProtocolChannelMap protocolChannelMap = getChannelMap()[i];
            assertNotNull(protocolChannelMap);
            assertNotNull(protocolChannelMap.getChannelRegisterMap());
        }
    }

    @Test
    public void testHasEqualRegisters() throws Exception {
    }

    @Test
    public void testGetProtocolChannels() throws Exception {
        for (int i = 0; i < getChannelMap().length; i++) {
            ProtocolChannelMap protocolChannelMap = getChannelMap()[i];
            assertNotNull(protocolChannelMap);
            assertNotNull(protocolChannelMap.getProtocolChannels());
        }
    }

    @Test
    public void testGetNrOfProtocolChannels() throws Exception {
        for (int i = 0; i < getChannelMap().length; i++) {
            ProtocolChannelMap protocolChannelMap = getChannelMap()[i];
            assertNotNull(protocolChannelMap);
            assertEquals(protocolChannelMap.getProtocolChannels().size(), protocolChannelMap.getNrOfProtocolChannels());
        }
    }

    @Test
    public void testGetNrOfUsedProtocolChannels() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testGetProtocolChannel() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testChannelExists() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testIsProtocolChannel() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testIsProtocolChannelEnabled() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testIsProtocolChannelZero() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testToChannelInfoList() throws Exception {
        // TODO: add test implementation
    }

    @Test
    public void testIsMappedChannels() throws Exception {
        // TODO: add test implementation
    }
}
