/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.connection.UmiConnection;
import com.energyict.protocolimplv2.umi.types.UmiId;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;

public class UmiLinkSessionTest {

    @Mock
    UmiConnection connection;

    @Test
    public void getDestinationIdTest() {
        UmiLinkSession umiLinkSession = new UmiLinkSession(connection);

        UmiId testUmiId0 = new UmiId("9991253571665920");
        umiLinkSession.setDestinationId(testUmiId0);
        assertEquals(testUmiId0.toString(), umiLinkSession.getDestinationId().toString());

        UmiId testUmiId1 = new UmiId("9991253571665921");
        umiLinkSession.setDestinationId(testUmiId1);
        assertEquals(testUmiId1.toString(), umiLinkSession.getDestinationId().toString());

        UmiId testUmiId9 = new UmiId("9991253571665929");
        umiLinkSession.setDestinationId(testUmiId9);
        assertEquals("9991253571665921", umiLinkSession.getDestinationId().toString());
    }
}
