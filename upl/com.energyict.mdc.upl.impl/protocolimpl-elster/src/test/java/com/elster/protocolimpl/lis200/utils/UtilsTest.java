package com.elster.protocolimpl.lis200.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testSplitLineWithFourValuesExpectAStringArrayWithFourValues() {
        String line = "(100)(200)(300)(400)";
        String[] result = Utils.splitLine(line);
        Assert.assertEquals("100", result[0]);
        Assert.assertEquals("200", result[1]);
        Assert.assertEquals("300", result[2]);
        Assert.assertEquals("400", result[3]);
    }

    @Test
    public void testSplitLineWithFourValuesTwoHaveSomeGarbageInBetweenExpectAStringArrayWithFourValues() throws DecoderException {
        String garbage = new String(Hex.decodeHex("FF".toCharArray()));
        String line = "(100)(200)" +garbage +"(300)"+ garbage + "(400)";
        String[] result = Utils.splitLine(line);
        Assert.assertEquals("100", result[0]);
        Assert.assertEquals("200", result[1]);
        Assert.assertEquals("300", result[2]);
        Assert.assertEquals("400", result[3]);
    }

}
