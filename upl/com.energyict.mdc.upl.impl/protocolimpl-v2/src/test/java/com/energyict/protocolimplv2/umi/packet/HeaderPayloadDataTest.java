package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.IData;
import org.junit.Test;


import java.security.InvalidParameterException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HeaderPayloadDataTest {
    static public HeaderPayloadData createTestHeaderPayloadData(IData payload) {
        AppPacketType type = AppPacketType.EVENT_PUBLISH;
        SecurityScheme signatureScheme = SecurityScheme.NO_SECURITY;
        SecurityScheme respSignatureSchemeRequest = SecurityScheme.NO_SECURITY;
        short transactionNumber = 9;

        HeaderPayloadData headerPayloadData = new HeaderPayloadData.Builder()
                .packetType(type)
                .signatureScheme(signatureScheme)
                .respSignatureSchemeRequest(respSignatureSchemeRequest)
                .transactionNumber(transactionNumber)
                .payloadLength(payload)
                .build();
        return headerPayloadData;
    }

    @Test
    public void createHeaderPayloadAndCheckFields() {
        AppPacketType type = AppPacketType.EVENT_PUBLISH;
        SecurityScheme signatureScheme = SecurityScheme.SYMMETRIC;
        SecurityScheme respSignatureSchemeRequest = SecurityScheme.NO_SECURITY;
        short transactionNumber = 9;
        int payloadLength = 543;

        HeaderPayloadData headerPayloadData = new HeaderPayloadData.Builder()
                .packetType(type)
                .signatureScheme(signatureScheme)
                .respSignatureSchemeRequest(respSignatureSchemeRequest)
                .transactionNumber(transactionNumber)
                .payloadLength(new LittleEndianData(new byte[payloadLength]))
                .build();
        assertEquals(type, headerPayloadData.getType());
        assertEquals(signatureScheme, headerPayloadData.getSignatureScheme());
        assertEquals(true, headerPayloadData.getRespSignatureSchemeRequest().isPresent());
        assertEquals(false, headerPayloadData.getResultCode().isPresent());
        assertEquals(respSignatureSchemeRequest, headerPayloadData.getRespSignatureSchemeRequest().get());
        assertEquals(transactionNumber, headerPayloadData.getTransactionNumber());
        assertEquals(payloadLength, headerPayloadData.getPayloadLength());
    }

    @Test
    public void recreateAADFromRawData() {
        int payloadLength = 543;
        HeaderPayloadData headerPayloadData = createTestHeaderPayloadData(new LittleEndianData(new byte[payloadLength]));
        HeaderPayloadData headerPayloadData2 = new HeaderPayloadData(headerPayloadData.getRaw());

        headerPayloadData.equals(headerPayloadData2);
        assertEquals(headerPayloadData, headerPayloadData2);
        assertArrayEquals(headerPayloadData.getRaw(), headerPayloadData2.getRaw());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new HeaderPayloadData(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new HeaderPayloadData(new byte[HeaderPayloadData.SIZE + 1]);
    }
}
