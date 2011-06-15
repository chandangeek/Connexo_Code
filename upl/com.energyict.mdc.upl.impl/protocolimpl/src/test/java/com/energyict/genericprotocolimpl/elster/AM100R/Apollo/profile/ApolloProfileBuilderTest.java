package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 15-jun-2011
 * Time: 14:01:52
 */
public class ApolloProfileBuilderTest {

    @Test
    public void getChannelMaskFromProfileConfiguration() throws IOException {
        ProfileConfiguration pc = new ProfileConfiguration("1.0.99.1.0.255", "", 86400);
        assertEquals(-1, ApolloProfileBuilder.getChannelMaskFromProfileConfig(pc));

        pc = new ProfileConfiguration("1.0.99.1.0.255", ":2-3-4", 86400);
        assertEquals(14, ApolloProfileBuilder.getChannelMaskFromProfileConfig(pc));

        pc = new ProfileConfiguration("1.0.99.1.0.255", ":3-5-7-9", 86400);
        assertEquals(340, ApolloProfileBuilder.getChannelMaskFromProfileConfig(pc));

        pc = new ProfileConfiguration("1.0.99.1.0.255", ":10", 86400);
        assertEquals(512, ApolloProfileBuilder.getChannelMaskFromProfileConfig(pc));
    }

}
