package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Test;

public class ObisChannelTest {

    @Test
    public void channelValue(){
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Assert.assertEquals(1,ObisChannel.A.getValue(obisCode));
        Assert.assertEquals(2,ObisChannel.B.getValue(obisCode));
        Assert.assertEquals(3,ObisChannel.C.getValue(obisCode));
        Assert.assertEquals(4,ObisChannel.D.getValue(obisCode));
        Assert.assertEquals(5,ObisChannel.E.getValue(obisCode));
        Assert.assertEquals(6,ObisChannel.F.getValue(obisCode));
    }

    @Test
    public void deviceValue(){
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Assert.assertEquals(ObisCode.fromString("0.2.3.4.5.6"),ObisChannel.A.getDeviceValue(obisCode));
        Assert.assertEquals(ObisCode.fromString("1.0.3.4.5.6"),ObisChannel.B.getDeviceValue(obisCode));
        Assert.assertEquals(ObisCode.fromString("1.2.0.4.5.6"),ObisChannel.C.getDeviceValue(obisCode));
        Assert.assertEquals(ObisCode.fromString("1.2.3.0.5.6"),ObisChannel.D.getDeviceValue(obisCode));
        Assert.assertEquals(ObisCode.fromString("1.2.3.4.0.6"),ObisChannel.E.getDeviceValue(obisCode));
        Assert.assertEquals(ObisCode.fromString("1.2.3.4.5.0"),ObisChannel.F.getDeviceValue(obisCode));
    }

}