package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author sva
 * @since 10/08/2015 - 17:04
 */
public class UniversalObjectTest {

    @Test
    public void testIsCapturedObjectCumulative() throws Exception {
        UniversalObject col = new UniversalObject(Arrays.asList((long) 1, (long) 1, (long) 1, (long) 8, (long) 0, (long) 255), DLMSReference.LN.getReference());
        assertTrue(col.isCapturedObjectCumulative());
    }

    @Test
    public void testIsCapturedObjectCumulative_2() throws Exception {
        ObisCode obisCode = ObisCode.fromString("1.1.1.4.0.255");
        int classId = DLMSClassId.EXTENDED_REGISTER.getClassId();

        UniversalObject captureObject = UniversalObject.createCaptureObject(classId, obisCode);
        assertFalse(captureObject.isCapturedObjectCumulative());
    }

    @Test
    public void testIsCapturedObjectCumulative_3() throws Exception {
        ObisCode obisCode = ObisCode.fromString("1.1.1.8.0.255");
        int classId = DLMSClassId.EXTENDED_REGISTER.getClassId();

        UniversalObject captureObject = UniversalObject.createCaptureObject(classId, obisCode);
        assertTrue(captureObject.isCapturedObjectCumulative());
    }

    @Test
    public void testEquals_InstantiatedObjectListEntry() throws Exception {
        UniversalObject iol_1 = new UniversalObject(ObisCode.fromString("1.1.1.8.0.255"), DLMSClassId.REGISTER);
        UniversalObject iol_2 = new UniversalObject(ObisCode.fromString("1.1.1.8.0.255"), DLMSClassId.REGISTER);

        assertTrue(iol_1.equals(iol_2));
    }

    @Test
    public void testEquals_CapturedOjectListEntry() throws Exception {
        UniversalObject col_1 = new UniversalObject(Arrays.asList((long) 1, (long) 1, (long) 1, (long) 8, (long) 0, (long) 255), DLMSReference.LN.getReference());
        UniversalObject col_2 = new UniversalObject(Arrays.asList((long) 1, (long) 1, (long) 1, (long) 8, (long) 0, (long) 255), DLMSReference.LN.getReference());

        assertTrue(col_1.equals(col_2));
    }

    @Test
    public void testEquals_DifferentTypes() throws Exception {
        UniversalObject iol = new UniversalObject(ObisCode.fromString("1.1.1.8.0.255"), DLMSClassId.UNKNOWN);
        UniversalObject col = new UniversalObject(Arrays.asList((long) 1, (long) 1, (long) 1, (long) 8, (long) 0, (long) 255), DLMSReference.LN.getReference());

        assertTrue(iol.equals(col));
        assertTrue(col.equals(iol));
    }

    @Test
    public void testCreateCaptureObject() throws Exception {
        ObisCode obisCode = ObisCode.fromString("1.1.1.4.0.255");
        int classId = DLMSClassId.EXTENDED_REGISTER.getClassId();

        UniversalObject captureObject = UniversalObject.createCaptureObject(classId, obisCode);

        assertEquals(obisCode.getA(), captureObject.getLNAco());
        assertEquals(obisCode.getB(), captureObject.getLNBco());
        assertEquals(obisCode.getC(), captureObject.getLNCco());
        assertEquals(obisCode.getD(), captureObject.getLNDco());
        assertEquals(obisCode.getE(), captureObject.getLNEco());
        assertEquals(obisCode.getF(), captureObject.getLNFco());
        assertEquals(classId, captureObject.getClassIDco());
    }
}