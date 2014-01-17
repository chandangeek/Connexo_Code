package com.energyict.mdc.channels.serial;

import com.energyict.mdc.engine.model.ComServer;
import org.junit.Test;

public class BuilderTest {

    @Test
    public void testBuilder() throws Exception {
        ComServer comServer=null;
        comServer.newOutboundComPort().name("test").numberOfSimultaneousConnections(12).add();

    }
}
