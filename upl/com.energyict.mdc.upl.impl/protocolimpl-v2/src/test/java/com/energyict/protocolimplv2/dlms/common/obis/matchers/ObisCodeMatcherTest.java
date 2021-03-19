package com.energyict.protocolimplv2.dlms.common.obis.matchers;


import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Test;

public class ObisCodeMatcherTest {

    @Test
    public void matching(){
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(obisCode);
        Assert.assertTrue(obisCodeMatcher.matches(obisCode));
        Assert.assertTrue(obisCodeMatcher.matches(ObisCode.fromString("1.2.3.4.5.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("0.2.3.4.5.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("1.0.3.4.5.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("1.2.0.4.5.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("1.2.3.0.5.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("1.2.3.4.0.6")));
        Assert.assertFalse(obisCodeMatcher.matches(ObisCode.fromString("1.2.3.4.5.0")));
    }

    @Test
    public void mapping(){
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(obisCode);
        Assert.assertEquals(obisCode, obisCodeMatcher.map(obisCode));
        ObisCode o = ObisCode.fromString("0.1.2.3.4.5");
        Assert.assertEquals(o, obisCodeMatcher.map(o));
    }


}