package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Function;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.StructureCode;
import com.energyict.genericprotocolimpl.elster.ctr.object.FlowAndVolumeCategory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 19-nov-2010
 * Time: 9:18:11
 */
public class GprsRequestFactoryTest extends TestCase {

    public void testGPRSRequestFactory() throws CTRParsingException {

        MTU155Properties mtu155Properties = new MTU155Properties();
        mtu155Properties.addProperty(MTU155Properties.KEYC, "32323232323232323232323232323232");
        mtu155Properties.addProperty(MTU155Properties.TIMEOUT, "1000");
        mtu155Properties.addProperty(MTU155Properties.RETRIES, "10");
        mtu155Properties.addProperty(MTU155Properties.DEBUG, "0");
        mtu155Properties.addProperty(MTU155Properties.ADDRESS, "0");
        mtu155Properties.addProperty(MTU155Properties.SECURITY_LEVEL, "1");

        GprsRequestFactory gprsRequestFactory = new GprsRequestFactory(null, null, null, mtu155Properties, null);

        GPRSFrame endOfSessionReq = gprsRequestFactory.getEndOfSessionRequest();
        GPRSFrame eventArrayReq = gprsRequestFactory.getEventArrayRequest(new Index_Q(3));
        GPRSFrame executeRequest = gprsRequestFactory.getExecuteRequest(new ReferenceDate().parse(new Date(), null), new WriteDataBlock(), new CTRObjectID(1,1,1), new byte[2]);
        GPRSFrame idRequest = gprsRequestFactory.getIdentificationRequest();
        FlowAndVolumeCategory obj = new FlowAndVolumeCategory(new CTRObjectID("1.1.1"));
        obj.parse(ProtocolTools.getBytesFromHexString("$01$11$01$01$01$01$01$01$01$01$01$01$01$01$01$01$01$01$01$01"),0, AttributeType.getQualifierAndValue());
        GPRSFrame reqWriteRequest = gprsRequestFactory.getRegisterWriteRequest(new ReferenceDate().parse(new Date(), null), new WriteDataBlock(), new P_Session(0x0F), new AttributeType(0), obj);
        GPRSFrame regRequest = gprsRequestFactory.getRegisterRequest(new AttributeType(0), new CTRObjectID[]{});
        GPRSFrame decfReq = gprsRequestFactory.getTableDECFRequest();
        GPRSFrame decReq = gprsRequestFactory.getTableDECRequest();
        GPRSFrame trace_CReq = gprsRequestFactory.getTrace_CRequest(new CTRObjectID(1,1,1), new PeriodTrace_C(1), new ReferenceDate().parse(new Date(), null));
        GPRSFrame traceReq = gprsRequestFactory.getTraceRequest(new CTRObjectID(1,1,1), new PeriodTrace(1), new StartDate(), new NumberOfElements(3));

        
        assertEquals(endOfSessionReq.getFunctionCode().getFunctionCode(), Function.END_OF_SESSION.getFunctionCode());
        assertEquals(executeRequest.getFunctionCode().getFunctionCode(), Function.EXECUTE.getFunctionCode());
        assertEquals(eventArrayReq.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());
        assertEquals(idRequest.getFunctionCode().getFunctionCode(), Function.IDENTIFICATION_REQUEST.getFunctionCode());
        assertEquals(reqWriteRequest.getFunctionCode().getFunctionCode(), Function.WRITE.getFunctionCode());
        assertEquals(regRequest.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());
        assertEquals(decfReq.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());
        assertEquals(decReq.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());
        assertEquals(trace_CReq.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());
        assertEquals(traceReq.getFunctionCode().getFunctionCode(), Function.QUERY.getFunctionCode());


        assertEquals(eventArrayReq.getStructureCode().getStructureCode(), StructureCode.EVENT_ARRAY);
        assertEquals(idRequest.getStructureCode().getStructureCode(), StructureCode.IDENTIFICATION);
        assertEquals(reqWriteRequest.getStructureCode().getStructureCode(), StructureCode.REGISTER);
        assertEquals(regRequest.getStructureCode().getStructureCode(), StructureCode.REGISTER);
        assertEquals(decfReq.getStructureCode().getStructureCode(), StructureCode.TABLE_DECF);
        assertEquals(decReq.getStructureCode().getStructureCode(), StructureCode.TABLE_DEC);
        assertEquals(trace_CReq.getStructureCode().getStructureCode(), StructureCode.TRACE_C);
        assertEquals(traceReq.getStructureCode().getStructureCode(), StructureCode.TRACE);






    }

}
