package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Test;

public class DlmsClassIdMatcherTest {

    @Test
    public void matching(){
        DlmsClassIdMatcher dlmsClassIdMatcher = new DlmsClassIdMatcher(DLMSClassId.REGISTER);
        Assert.assertTrue(dlmsClassIdMatcher.matches(DLMSClassId.REGISTER));
        Assert.assertFalse(dlmsClassIdMatcher.matches(DLMSClassId.DATA));
    }

    @Test
    public void mapping(){
        DlmsClassIdMatcher dlmsClassIdMatcher = new DlmsClassIdMatcher(DLMSClassId.REGISTER);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Assert.assertEquals(obisCode, dlmsClassIdMatcher.map(obisCode));
    }

}