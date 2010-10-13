package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 12-okt-2010
 * Time: 10:52:42
 */
public class StructureTest extends TestCase {

    private final int LENGTH = 128;

    @Test
    public void testStructures() throws CTRParsingException {
        byte[] bytes = padData(ProtocolTools.getBytesFromHexString("$51$52$53$54$55$56$01$00"));
        byte[] bytes2 = padData(ProtocolTools.getBytesFromHexString("$11$22$33$55$55$66$77$0F$01$00$00$00$00$01$00$03$00"));
        byte[] bytes3 = padData(ProtocolTools.getBytesFromHexString("$51$52$53$54$55$56$02$00$02$0A$0A$0A"));
        byte[] bytes4 = padData(ProtocolTools.getBytesFromHexString("$11$22$33$55$55$66$77$05$05$05$05$05$01$01$21$00$03$01$02$01$0A$0A$0A$1F$00$00$00$0A$00$10"));
        byte[] bytes5 = padData(ProtocolTools.getBytesFromHexString("$51$52$53$54$55$56$01$00$00$04$03"));
        byte[] bytes6 = padData(ProtocolTools.getBytesFromHexString("$01$01$03$00$04$00$00$0B$00$01"));
        byte[] bytes7 = padData(ProtocolTools.getBytesFromHexString("$51$52$53$54$55$56$03$03$02$00$02$01$02$02"));
        byte[] bytes8 = padData(ProtocolTools.getBytesFromHexString("$03$03$01$00$0F$00$00$01$01$01$0F$00$00$01$01$02$0F$00$00$01"));
        byte[] bytes9 = padData(ProtocolTools.getBytesFromHexString("$51$52$53$54$55$56$01$00$02$0A$0A$0A$00$03"));
        byte[] bytes10 = padData(ProtocolTools.getBytesFromHexString("$01$00$02$0A$0A$0A$00$03$00$00$00$01$00$00$00$02$00$00$00$03$00$00$00$04"));
        byte[] bytes11 = padData(ProtocolTools.getBytesFromHexString("$01$02$01$02$01$02$01$02$01$00$02$00"));

        ArrayEventsQueryRequestStructure aeqrs = new ArrayEventsQueryRequestStructure().parse(bytes, 0);
        ArrayEventsQueryResponseStructure aeqrspns = new ArrayEventsQueryResponseStructure().parse(bytes2, 0);

        Trace_CQueryRequestStructure trace_C_req = new Trace_CQueryRequestStructure().parse(bytes3, 0);
        Trace_CQueryResponseStructure trace_C_resp = new Trace_CQueryResponseStructure().parse(bytes4, 0);

        ArrayQueryRequestStructure arrqq = new ArrayQueryRequestStructure().parse(bytes5, 0);
        ArrayQueryResponseStructure arrsp = new ArrayQueryResponseStructure().parse(bytes6, 0);

        RegisterQueryRequestStructure regreq = new RegisterQueryRequestStructure().parse(bytes7, 0);
        RegisterQueryResponseStructure regrsp = new RegisterQueryResponseStructure().parse(bytes8, 0);

        TraceQueryRequestStructure trace_rqs = new TraceQueryRequestStructure().parse(bytes9, 0);
        TraceQueryResponseStructure trace_rsp = new TraceQueryResponseStructure().parse(bytes10, 0);

        IdentificationRequestStructure idreq = new IdentificationRequestStructure().parse(bytes11, 0);
        
        assertArrayEquals(bytes, aeqrs.getBytes());
        assertArrayEquals(bytes2, aeqrspns.getBytes());
        assertArrayEquals(bytes3, trace_C_req.getBytes());
        assertArrayEquals(bytes4, trace_C_resp.getBytes());
        assertArrayEquals(bytes5, arrqq.getBytes());
        assertArrayEquals(bytes6, arrsp.getBytes());
        assertArrayEquals(bytes7, regreq.getBytes());
        assertArrayEquals(bytes8, regrsp.getBytes());
        assertArrayEquals(bytes9, trace_rqs.getBytes());
        assertArrayEquals(bytes10, trace_rsp.getBytes());
        assertArrayEquals(bytes11, idreq.getBytes());
    }

    private byte[] padData(byte[] fieldData) {
        int paddingLength = LENGTH - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, LENGTH);
        }
        return fieldData;
    }
}