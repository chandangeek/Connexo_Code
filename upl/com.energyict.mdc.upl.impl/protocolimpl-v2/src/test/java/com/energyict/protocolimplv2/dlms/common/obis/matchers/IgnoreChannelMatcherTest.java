package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Test;

public class IgnoreChannelMatcherTest {

    @Test
    public void matchingChannelA() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.A);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("20.0.13.0.0.255")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.14.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.254")));
    }

    @Test
    public void matchingChannelB() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.B);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.99.13.0.0.255")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.12.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.100")));
    }

    @Test
    public void matchingChannelC() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.C);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.0.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.14.0.0.255")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.100")));
    }

    @Test
    public void matchingChannelD() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.D);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.100.0.255")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.0.13.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.100")));
        // not matching multiple fields
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.100")));
    }

    @Test
    public void matchingChannelE() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.E);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.2.255")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.12.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.100")));
        // not matching multiple fields
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.100")));
    }

    @Test
    public void matchingChannelF() {
        ObisCode obisCode = ObisCode.fromString("0.0.13.0.0.255");
        IgnoreChannelMatcher ignoreChannelMatcher = new IgnoreChannelMatcher(obisCode, ObisChannel.F);
        // matching obis
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.255")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.0")));
        Assert.assertTrue(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.0.1")));
        // not matching obis
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.1.13.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.12.0.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.1.0.255")));
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("0.0.13.0.1.255")));
        // not matching multiple fields
        Assert.assertFalse(ignoreChannelMatcher.matches(ObisCode.fromString("1.0.13.0.0.100")));
    }

}