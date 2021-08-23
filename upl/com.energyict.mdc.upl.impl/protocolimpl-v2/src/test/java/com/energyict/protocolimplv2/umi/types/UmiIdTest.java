package com.energyict.protocolimplv2.umi.types;

import org.junit.Test;

import java.math.BigInteger;
import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class UmiIdTest {
    @Test
    public void createUmiId() {
        String id = new String("123");

        assertEquals(new UmiId(id).toString(), id);
        assertEquals(new UmiId(UmiId.MIN_UMI_ID.toString()).toString(), UmiId.MIN_UMI_ID.toString());
        assertEquals(new UmiId(UmiId.MAX_UMI_ID.toString()).toString(), UmiId.MAX_UMI_ID.toString());
    }

    @Test
    public void recreateUmiIdFromRaw() {
        String maxUmiId = new String(UmiId.MAX_UMI_ID.toString());
        UmiId umiId = new UmiId(maxUmiId);
        UmiId umiId1 = new UmiId(umiId.getRaw(), true);

        assertEquals(umiId1.toString(), maxUmiId);

        String minUmiId = new String(UmiId.MIN_UMI_ID.toString());
        UmiId umiId2 = new UmiId(minUmiId);
        UmiId umiId3 = new UmiId(umiId2.getRaw(), true);

        assertEquals(umiId3.toString(), minUmiId);

        String id = new String("123123");
        UmiId umiId4 = new UmiId(id);
        UmiId umiId5 = new UmiId(umiId4.getRaw(), true);

        String id2 = new String("9990162649972740");
        UmiId umiId6 = new UmiId(id);
        UmiId umiId7 = new UmiId(umiId6.getRaw(), true);

        assertEquals(umiId5.toString(), id);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnPassingIdAboveMax() {
        BigInteger bigInteger = new BigInteger("1");
        bigInteger = bigInteger.add(UmiId.MAX_UMI_ID);
        new UmiId(bigInteger.toString());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnPassingIdBelowMin() {
        BigInteger bigInteger = new BigInteger("1");
        bigInteger = UmiId.MIN_UMI_ID.subtract(bigInteger);
        new UmiId(bigInteger.toString());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnPassingNegativeId() {
        BigInteger bigInteger = new BigInteger("-123123");
        new UmiId(bigInteger.toString());
    }
}
