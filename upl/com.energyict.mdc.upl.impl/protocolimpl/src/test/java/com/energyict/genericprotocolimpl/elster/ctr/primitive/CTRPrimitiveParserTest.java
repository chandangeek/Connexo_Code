package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests parsing of all common objects (and converting them back to byte arrays)
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 17:04:20
 */
public class CTRPrimitiveParserTest extends TestCase {

    private static final int LENGTH = 128;

    private int sum(int[] valueLengths) {
        int sum = 0;
        for (int valueLength : valueLengths) {
            sum += valueLength;
        }
        return sum;
    }


    /**
     * Checks if the default lengths equal the value lengths
     *
     * @throws Exception
     */
    @Test
    public void testDefaultLengths() throws Exception {


        byte[] bytes = new byte[LENGTH];

        List<CTRObjectID> ids = new ArrayList<CTRObjectID>();
        CTRObjectFactory factory = new CTRObjectFactory();
        ids.add(new CTRObjectID("1.0.0"));
        ids.add(new CTRObjectID("1.1.0"));
        ids.add(new CTRObjectID("1.2.0"));
        ids.add(new CTRObjectID("1.3.0"));
        ids.add(new CTRObjectID("1.6.0"));
        ids.add(new CTRObjectID("1.7.0"));
        ids.add(new CTRObjectID("1.9.0"));
        ids.add(new CTRObjectID("1.A.0"));
        ids.add(new CTRObjectID("1.6.4"));
        ids.add(new CTRObjectID("1.9.4"));
        ids.add(new CTRObjectID("1.A.4"));
        ids.add(new CTRObjectID("1.A.5"));
        ids.add(new CTRObjectID("1.A.6"));
        ids.add(new CTRObjectID("1.7.4"));
        ids.add(new CTRObjectID("1.C.3"));
        ids.add(new CTRObjectID("1.C.2"));
        ids.add(new CTRObjectID("1.C.4"));
        ids.add(new CTRObjectID("1.D.3"));
        ids.add(new CTRObjectID("1.E.3"));
        ids.add(new CTRObjectID("1.F.2"));
        ids.add(new CTRObjectID("1.F.6"));

        ids.add(new CTRObjectID("2.0.0"));
        ids.add(new CTRObjectID("2.1.0"));
        ids.add(new CTRObjectID("2.3.0"));
        ids.add(new CTRObjectID("2.4.0"));
        ids.add(new CTRObjectID("2.5.0"));

        ids.add(new CTRObjectID("4.0.0"));
        ids.add(new CTRObjectID("4.1.0"));
        ids.add(new CTRObjectID("4.2.0"));
        ids.add(new CTRObjectID("4.2.6"));
        ids.add(new CTRObjectID("4.3.1"));
        ids.add(new CTRObjectID("4.4.1"));
        ids.add(new CTRObjectID("4.6.1"));
        ids.add(new CTRObjectID("4.7.1"));
        ids.add(new CTRObjectID("4.9.0"));
        ids.add(new CTRObjectID("4.9.1"));
        ids.add(new CTRObjectID("4.9.2"));
        ids.add(new CTRObjectID("4.9.3"));
        ids.add(new CTRObjectID("4.9.4"));
        ids.add(new CTRObjectID("4.9.5"));
        ids.add(new CTRObjectID("4.A.0"));
        ids.add(new CTRObjectID("4.A.1"));

        ids.add(new CTRObjectID("7.0.1"));
        ids.add(new CTRObjectID("7.3.1"));
        ids.add(new CTRObjectID("7.6.1"));
        ids.add(new CTRObjectID("7.9.0"));
        ids.add(new CTRObjectID("7.9.A"));
        ids.add(new CTRObjectID("7.B.0"));
        ids.add(new CTRObjectID("7.B.1"));
        ids.add(new CTRObjectID("7.B.2"));
        ids.add(new CTRObjectID("7.B.3"));
        ids.add(new CTRObjectID("7.B.4"));
        ids.add(new CTRObjectID("7.B.5"));
        ids.add(new CTRObjectID("7.B.6"));
        ids.add(new CTRObjectID("7.C.0"));
        
        ids.add(new CTRObjectID("8.0.0"));
        ids.add(new CTRObjectID("8.0.1"));
        ids.add(new CTRObjectID("8.0.2"));
        ids.add(new CTRObjectID("8.1.2"));
        ids.add(new CTRObjectID("8.1.3"));
        ids.add(new CTRObjectID("8.1.4"));
        ids.add(new CTRObjectID("8.2.0"));
        ids.add(new CTRObjectID("8.3.1"));
        ids.add(new CTRObjectID("8.4.1"));
        ids.add(new CTRObjectID("8.4.0"));
        ids.add(new CTRObjectID("8.5.0"));
        
        ids.add(new CTRObjectID("9.0.0"));
        ids.add(new CTRObjectID("9.0.1"));
        ids.add(new CTRObjectID("9.0.2"));
        ids.add(new CTRObjectID("9.0.3"));
        ids.add(new CTRObjectID("9.0.4"));
        ids.add(new CTRObjectID("9.0.5"));
        ids.add(new CTRObjectID("9.0.7"));
        ids.add(new CTRObjectID("9.0.9"));
        ids.add(new CTRObjectID("9.1.1"));
        ids.add(new CTRObjectID("9.1.2"));
        ids.add(new CTRObjectID("9.1.3"));
        ids.add(new CTRObjectID("9.2.0"));
        ids.add(new CTRObjectID("9.2.2"));
        ids.add(new CTRObjectID("9.2.3"));
        ids.add(new CTRObjectID("9.2.4"));
        ids.add(new CTRObjectID("9.2.5"));
        ids.add(new CTRObjectID("9.3.0"));
        ids.add(new CTRObjectID("9.3.1"));
        ids.add(new CTRObjectID("9.4.0"));
        ids.add(new CTRObjectID("9.4.1"));
        ids.add(new CTRObjectID("9.5.0"));
        
        ids.add(new CTRObjectID("A.0.1"));
        ids.add(new CTRObjectID("A.1.1"));
        ids.add(new CTRObjectID("A.2.1"));
        ids.add(new CTRObjectID("A.3.1"));
        ids.add(new CTRObjectID("A.1.7"));
        ids.add(new CTRObjectID("A.3.6"));
        ids.add(new CTRObjectID("A.1.6"));
        ids.add(new CTRObjectID("A.3.7"));
        ids.add(new CTRObjectID("A.4.5"));
        ids.add(new CTRObjectID("A.4.6"));
        ids.add(new CTRObjectID("A.4.7"));
        ids.add(new CTRObjectID("A.5.6"));
        ids.add(new CTRObjectID("A.5.1"));

        ids.add(new CTRObjectID("C.0.0"));
        ids.add(new CTRObjectID("C.0.1"));
        ids.add(new CTRObjectID("C.0.2"));
        ids.add(new CTRObjectID("C.0.3"));
        ids.add(new CTRObjectID("C.0.4"));
        ids.add(new CTRObjectID("C.0.5"));
        ids.add(new CTRObjectID("C.0.6"));
        ids.add(new CTRObjectID("C.0.7"));
        ids.add(new CTRObjectID("C.1.1"));
        ids.add(new CTRObjectID("C.1.2"));
        ids.add(new CTRObjectID("C.1.3"));
        ids.add(new CTRObjectID("C.1.4"));
        ids.add(new CTRObjectID("C.1.5"));
        ids.add(new CTRObjectID("C.1.6"));
        ids.add(new CTRObjectID("C.2.0"));
        ids.add(new CTRObjectID("C.2.1"));

        ids.add(new CTRObjectID("D.0.1"));
        ids.add(new CTRObjectID("D.1.1"));
        ids.add(new CTRObjectID("D.2.1"));
        ids.add(new CTRObjectID("D.3.1"));
        ids.add(new CTRObjectID("D.4.1"));
        ids.add(new CTRObjectID("D.6.3"));
        ids.add(new CTRObjectID("D.7.1"));
        ids.add(new CTRObjectID("D.8.1"));
        ids.add(new CTRObjectID("D.8.6"));
        ids.add(new CTRObjectID("D.8.A"));
        ids.add(new CTRObjectID("D.9.0"));
        ids.add(new CTRObjectID("D.A.0"));

        ids.add(new CTRObjectID("E.C.0"));
        ids.add(new CTRObjectID("E.E.0"));

        ids.add(new CTRObjectID("F.0.1"));
        ids.add(new CTRObjectID("F.1.1"));
        ids.add(new CTRObjectID("F.4.1"));
        ids.add(new CTRObjectID("F.5.0"));
        ids.add(new CTRObjectID("F.5.1"));
        ids.add(new CTRObjectID("F.5.2"));

        ids.add(new CTRObjectID("10.0.1"));
        ids.add(new CTRObjectID("10.0.2"));
        ids.add(new CTRObjectID("10.0.3"));
        ids.add(new CTRObjectID("10.1.0"));
        ids.add(new CTRObjectID("10.1.1"));
        ids.add(new CTRObjectID("10.1.2"));

        ids.add(new CTRObjectID("11.0.1"));
        ids.add(new CTRObjectID("11.0.8"));

        ids.add(new CTRObjectID("12.0.0"));
        ids.add(new CTRObjectID("12.1.0"));
        ids.add(new CTRObjectID("12.2.0"));
        ids.add(new CTRObjectID("12.4.1"));
        ids.add(new CTRObjectID("12.5.3"));

        ids.add(new CTRObjectID("13.6.2"));
        ids.add(new CTRObjectID("13.7.0"));
        ids.add(new CTRObjectID("13.7.1"));
        ids.add(new CTRObjectID("13.7.2"));
        ids.add(new CTRObjectID("13.7.3"));
        ids.add(new CTRObjectID("13.7.4"));
        ids.add(new CTRObjectID("13.7.5"));

        ids.add(new CTRObjectID("15.0.1"));
        ids.add(new CTRObjectID("15.1.1"));
        ids.add(new CTRObjectID("15.2.1"));

        ids.add(new CTRObjectID("17.0.1"));
        ids.add(new CTRObjectID("17.0.4"));

        ids.add(new CTRObjectID("18.0.1"));
        ids.add(new CTRObjectID("18.1.1"));
        ids.add(new CTRObjectID("18.2.1"));
        ids.add(new CTRObjectID("18.3.1"));
        ids.add(new CTRObjectID("18.4.1"));
        ids.add(new CTRObjectID("18.5.1"));
        ids.add(new CTRObjectID("18.6.1"));
        ids.add(new CTRObjectID("18.7.1"));
        ids.add(new CTRObjectID("18.8.1"));
        ids.add(new CTRObjectID("18.9.1"));

        AttributeType attributeType = new AttributeType(0xFF);
        attributeType.setHasIdentifier(false);
        List<AbstractCTRObject> objs = new ArrayList<AbstractCTRObject>();

        for (CTRObjectID id : ids) {
            AbstractCTRObject obj = factory.parse(bytes, 0, attributeType, id.toString());
            objs.add(obj);
        }

        for (AbstractCTRObject obj : objs) {
            if (obj.getDefault() != null) {
                assertEquals(obj.getValueLengths(obj.getId()).length, obj.getDefault().length);
            }
        }

        int index = 0;
        for (AbstractCTRObject obj : objs) {
            assertEquals(obj.getId().toString(), ids.get(index).toString());
            index++;
        }

        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();

        for (AbstractCTRObject obj1 : objs) {
            assertEquals(getValueBytesLength(obj1), converter.convertDefaults(obj1.getDefault(), obj1.getValueLengths(obj1.getId())).length);
        }

        for (AbstractCTRObject obj1 : objs) {
            if(obj1.getDefault() == null){
                assertArrayEquals(padData(obj1.getBytes()), bytes);
            } 
        }
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


    private int getValueBytesLength(AbstractCTRObject obj) {
        int[] lengths = obj.getValueLengths(obj.getId());
        int sum = 0;
        for (int i = 0; i < lengths.length; i++) {
            sum += obj.getValue(i).getBytes().length;
        }
        return sum;
    }
}
