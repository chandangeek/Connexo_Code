package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 16-nov-2010
 * Time: 17:04:20
 */
public class CTRPrimitiveParserTest extends TestCase {

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


        byte[] bytes = new byte[128];
        CTRObjectFactory factory = new CTRObjectFactory();
        String id1 = "1.C.3";
        String id2 = "1.C.2";
        String id3 = "4.2.6";
        String id4 = "4.9.0";
        String id5 = "4.9.1";
        String id52 = "4.9.2";
        String id53 = "4.9.3";
        String id54 = "4.9.4";
        String id6 = "4.A.0";
        String id62 = "4.A.1";
        String id63 = "4.A.2";
        String id64 = "4.A.3";
        String id65 = "4.A.4";
        String id66 = "4.A.5";
        String id7 = "7.B.0";
        String id8 = "7.B.1";
        String id82 = "7.B.2";
        String id83 = "7.B.3";
        String id84 = "7.B.4";
        String id85 = "7.B.5";
        String id9 = "8.0.0";
        String id92 = "8.0.1";
        String id13 = "8.2.0";
        String id14 = "9.4.0";
        String id15 = "A.3.6";
        String id16 = "A.1.6";
        String id17 = "A.3.7";
        String id18 = "A.4.6";
        String id19 = "A.4.7";
        String id20 = "A.5.6";
        String id21 = "C.0.1";
        String id22 = "C.0.2";
        String id23 = "C.0.3";
        String id232 = "C.0.4";
        String id233 = "C.0.5";
        String id234 = "C.0.6";
        String id235 = "C.0.7";
        String id29 = "13.7.0";
        String id292 = "13.7.1";
        String id293 = "13.7.2";
        String id294 = "13.7.3";
        String id295 = "13.7.4";
        String id296 = "13.7.5";
        String id312 = "C.1.1";
        String id313 = "C.1.2";
        String id314 = "C.1.3";
        String id315 = "C.1.4";
        String id316 = "C.1.5";
        String id317 = "C.1.6";

        AttributeType attributeType = new AttributeType(0xFF);
        AbstractCTRObject obj1 = factory.parse(bytes, 0, attributeType, id1);
        AbstractCTRObject obj2 = factory.parse(bytes, 0, attributeType, id2);
        AbstractCTRObject obj3 = factory.parse(bytes, 0, attributeType, id3);
        AbstractCTRObject obj4 = factory.parse(bytes, 0, attributeType, id4);
        AbstractCTRObject obj5 = factory.parse(bytes, 0, attributeType, id5);
        AbstractCTRObject obj52 = factory.parse(bytes, 0, attributeType, id52);
        AbstractCTRObject obj53 = factory.parse(bytes, 0, attributeType, id53);
        AbstractCTRObject obj54 = factory.parse(bytes, 0, attributeType, id54);
        AbstractCTRObject obj6 = factory.parse(bytes, 0, attributeType, id6);
        AbstractCTRObject obj62 = factory.parse(bytes, 0, attributeType, id62);
        AbstractCTRObject obj63 = factory.parse(bytes, 0, attributeType, id63);
        AbstractCTRObject obj64 = factory.parse(bytes, 0, attributeType, id64);
        AbstractCTRObject obj65 = factory.parse(bytes, 0, attributeType, id65);
        AbstractCTRObject obj66 = factory.parse(bytes, 0, attributeType, id66);
        AbstractCTRObject obj7 = factory.parse(bytes, 0, attributeType, id7);
        AbstractCTRObject obj8 = factory.parse(bytes, 0, attributeType, id8);
        AbstractCTRObject obj82 = factory.parse(bytes, 0, attributeType, id82);
        AbstractCTRObject obj83 = factory.parse(bytes, 0, attributeType, id83);
        AbstractCTRObject obj84 = factory.parse(bytes, 0, attributeType, id84);
        AbstractCTRObject obj85 = factory.parse(bytes, 0, attributeType, id85);
        AbstractCTRObject obj9 = factory.parse(bytes, 0, attributeType, id9);
        AbstractCTRObject obj92 = factory.parse(bytes, 0, attributeType, id92);
        AbstractCTRObject obj13 = factory.parse(bytes, 0, attributeType, id13);
        AbstractCTRObject obj14 = factory.parse(bytes, 0, attributeType, id14);
        AbstractCTRObject obj15 = factory.parse(bytes, 0, attributeType, id15);
        AbstractCTRObject obj16 = factory.parse(bytes, 0, attributeType, id16);
        AbstractCTRObject obj17 = factory.parse(bytes, 0, attributeType, id17);
        AbstractCTRObject obj18 = factory.parse(bytes, 0, attributeType, id18);
        AbstractCTRObject obj19 = factory.parse(bytes, 0, attributeType, id19);
        AbstractCTRObject obj20 = factory.parse(bytes, 0, attributeType, id20);
        AbstractCTRObject obj21 = factory.parse(bytes, 0, attributeType, id21);
        AbstractCTRObject obj22 = factory.parse(bytes, 0, attributeType, id22);
        AbstractCTRObject obj23 = factory.parse(bytes, 0, attributeType, id23);
        AbstractCTRObject obj232 = factory.parse(bytes, 0, attributeType, id232);
        AbstractCTRObject obj233 = factory.parse(bytes, 0, attributeType, id233);
        AbstractCTRObject obj234 = factory.parse(bytes, 0, attributeType, id234);
        AbstractCTRObject obj235 = factory.parse(bytes, 0, attributeType, id235);
        AbstractCTRObject obj292 = factory.parse(bytes, 0, attributeType, id292);
        AbstractCTRObject obj293 = factory.parse(bytes, 0, attributeType, id293);
        AbstractCTRObject obj294 = factory.parse(bytes, 0, attributeType, id294);
        AbstractCTRObject obj295 = factory.parse(bytes, 0, attributeType, id295);
        AbstractCTRObject obj296 = factory.parse(bytes, 0, attributeType, id296);
        AbstractCTRObject obj312 = factory.parse(bytes, 0, attributeType, id312);
        AbstractCTRObject obj313 = factory.parse(bytes, 0, attributeType, id313);
        AbstractCTRObject obj314 = factory.parse(bytes, 0, attributeType, id314);
        AbstractCTRObject obj315 = factory.parse(bytes, 0, attributeType, id315);
        AbstractCTRObject obj316 = factory.parse(bytes, 0, attributeType, id316);
        AbstractCTRObject obj317 = factory.parse(bytes, 0, attributeType, id317);
        AbstractCTRObject obj29 = factory.parse(bytes, 0, attributeType, id29);


        assertEquals(obj1.getValueLengths(obj1.getId()).length, obj1.getDefault().length);
        assertEquals(obj2.getValueLengths(obj2.getId()).length, obj2.getDefault().length);
        assertEquals(obj3.getValueLengths(obj3.getId()).length, obj3.getDefault().length);
        assertEquals(obj4.getValueLengths(obj4.getId()).length, obj4.getDefault().length);
        assertEquals(obj5.getValueLengths(obj5.getId()).length, obj5.getDefault().length);
        assertEquals(obj52.getValueLengths(obj52.getId()).length, obj52.getDefault().length);
        assertEquals(obj53.getValueLengths(obj53.getId()).length, obj53.getDefault().length);
        assertEquals(obj54.getValueLengths(obj54.getId()).length, obj54.getDefault().length);
        assertEquals(obj6.getValueLengths(obj6.getId()).length, obj6.getDefault().length);
        assertEquals(obj62.getValueLengths(obj62.getId()).length, obj62.getDefault().length);
        assertEquals(obj63.getValueLengths(obj63.getId()).length, obj63.getDefault().length);
        assertEquals(obj64.getValueLengths(obj64.getId()).length, obj64.getDefault().length);
        assertEquals(obj65.getValueLengths(obj65.getId()).length, obj65.getDefault().length);
        assertEquals(obj66.getValueLengths(obj66.getId()).length, obj66.getDefault().length);
        assertEquals(obj7.getValueLengths(obj7.getId()).length, obj7.getDefault().length);
        assertEquals(obj8.getValueLengths(obj8.getId()).length, obj8.getDefault().length);
        assertEquals(obj82.getValueLengths(obj82.getId()).length, obj82.getDefault().length);
        assertEquals(obj83.getValueLengths(obj83.getId()).length, obj83.getDefault().length);
        assertEquals(obj84.getValueLengths(obj84.getId()).length, obj84.getDefault().length);
        assertEquals(obj85.getValueLengths(obj85.getId()).length, obj85.getDefault().length);
        assertEquals(obj9.getValueLengths(obj9.getId()).length, obj9.getDefault().length);
        assertEquals(obj92.getValueLengths(obj92.getId()).length, obj92.getDefault().length);
        assertEquals(obj13.getValueLengths(obj13.getId()).length, obj13.getDefault().length);
        assertEquals(obj14.getValueLengths(obj14.getId()).length, obj14.getDefault().length);
        assertEquals(obj15.getValueLengths(obj15.getId()).length, obj15.getDefault().length);
        assertEquals(obj16.getValueLengths(obj16.getId()).length, obj16.getDefault().length);
        assertEquals(obj17.getValueLengths(obj17.getId()).length, obj17.getDefault().length);
        assertEquals(obj18.getValueLengths(obj18.getId()).length, obj18.getDefault().length);
        assertEquals(obj19.getValueLengths(obj19.getId()).length, obj19.getDefault().length);
        assertEquals(obj20.getValueLengths(obj20.getId()).length, obj20.getDefault().length);
        assertEquals(obj21.getValueLengths(obj21.getId()).length, obj21.getDefault().length);
        assertEquals(obj22.getValueLengths(obj22.getId()).length, obj22.getDefault().length);
        assertEquals(obj23.getValueLengths(obj23.getId()).length, obj23.getDefault().length);
        assertEquals(obj232.getValueLengths(obj232.getId()).length, obj232.getDefault().length);
        assertEquals(obj233.getValueLengths(obj233.getId()).length, obj233.getDefault().length);
        assertEquals(obj234.getValueLengths(obj234.getId()).length, obj234.getDefault().length);
        assertEquals(obj235.getValueLengths(obj235.getId()).length, obj235.getDefault().length);
        assertEquals(obj292.getValueLengths(obj292.getId()).length, obj292.getDefault().length);
        assertEquals(obj293.getValueLengths(obj293.getId()).length, obj293.getDefault().length);
        assertEquals(obj294.getValueLengths(obj294.getId()).length, obj294.getDefault().length);
        assertEquals(obj295.getValueLengths(obj295.getId()).length, obj295.getDefault().length);
        assertEquals(obj296.getValueLengths(obj296.getId()).length, obj296.getDefault().length);
        assertEquals(obj312.getValueLengths(obj312.getId()).length, obj312.getDefault().length);
        assertEquals(obj313.getValueLengths(obj313.getId()).length, obj313.getDefault().length);
        assertEquals(obj314.getValueLengths(obj314.getId()).length, obj314.getDefault().length);
        assertEquals(obj315.getValueLengths(obj315.getId()).length, obj315.getDefault().length);
        assertEquals(obj316.getValueLengths(obj316.getId()).length, obj316.getDefault().length);
        assertEquals(obj317.getValueLengths(obj317.getId()).length, obj317.getDefault().length);
        assertEquals(obj29.getValueLengths(obj29.getId()).length, obj29.getDefault().length);

        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();

        assertEquals(getValueBytesLength(obj1), converter.convertDefaults(obj1.getDefault(), obj1.getValueLengths(obj1.getId())).length);
        assertEquals(getValueBytesLength(obj2), converter.convertDefaults(obj2.getDefault(), obj2.getValueLengths(obj2.getId())).length);
        assertEquals(getValueBytesLength(obj3), converter.convertDefaults(obj3.getDefault(), obj3.getValueLengths(obj3.getId())).length);
        assertEquals(getValueBytesLength(obj4), converter.convertDefaults(obj4.getDefault(), obj4.getValueLengths(obj4.getId())).length);
        assertEquals(getValueBytesLength(obj5), converter.convertDefaults(obj5.getDefault(), obj5.getValueLengths(obj5.getId())).length);
        assertEquals(getValueBytesLength(obj52), converter.convertDefaults(obj52.getDefault(), obj52.getValueLengths(obj52.getId())).length);
        assertEquals(getValueBytesLength(obj53), converter.convertDefaults(obj53.getDefault(), obj53.getValueLengths(obj53.getId())).length);
        assertEquals(getValueBytesLength(obj54), converter.convertDefaults(obj54.getDefault(), obj54.getValueLengths(obj54.getId())).length);
        assertEquals(getValueBytesLength(obj6), converter.convertDefaults(obj6.getDefault(), obj6.getValueLengths(obj6.getId())).length);
        assertEquals(getValueBytesLength(obj62), converter.convertDefaults(obj62.getDefault(), obj62.getValueLengths(obj62.getId())).length);
        assertEquals(getValueBytesLength(obj63), converter.convertDefaults(obj63.getDefault(), obj63.getValueLengths(obj63.getId())).length);
        assertEquals(getValueBytesLength(obj64), converter.convertDefaults(obj64.getDefault(), obj64.getValueLengths(obj64.getId())).length);
        assertEquals(getValueBytesLength(obj65), converter.convertDefaults(obj65.getDefault(), obj65.getValueLengths(obj65.getId())).length);
        assertEquals(getValueBytesLength(obj66), converter.convertDefaults(obj66.getDefault(), obj66.getValueLengths(obj66.getId())).length);
        assertEquals(getValueBytesLength(obj7), converter.convertDefaults(obj7.getDefault(), obj7.getValueLengths(obj7.getId())).length);
        assertEquals(getValueBytesLength(obj8), converter.convertDefaults(obj8.getDefault(), obj8.getValueLengths(obj8.getId())).length);
        assertEquals(getValueBytesLength(obj82), converter.convertDefaults(obj82.getDefault(), obj82.getValueLengths(obj82.getId())).length);
        assertEquals(getValueBytesLength(obj83), converter.convertDefaults(obj83.getDefault(), obj83.getValueLengths(obj83.getId())).length);
        assertEquals(getValueBytesLength(obj84), converter.convertDefaults(obj84.getDefault(), obj84.getValueLengths(obj84.getId())).length);
        assertEquals(getValueBytesLength(obj85), converter.convertDefaults(obj85.getDefault(), obj85.getValueLengths(obj85.getId())).length);
        assertEquals(getValueBytesLength(obj9), converter.convertDefaults(obj9.getDefault(), obj9.getValueLengths(obj9.getId())).length);
        assertEquals(getValueBytesLength(obj92), converter.convertDefaults(obj92.getDefault(), obj92.getValueLengths(obj92.getId())).length);
        assertEquals(getValueBytesLength(obj13), converter.convertDefaults(obj13.getDefault(), obj13.getValueLengths(obj13.getId())).length);
        assertEquals(getValueBytesLength(obj14), converter.convertDefaults(obj14.getDefault(), obj14.getValueLengths(obj14.getId())).length);
        assertEquals(getValueBytesLength(obj15), converter.convertDefaults(obj15.getDefault(), obj15.getValueLengths(obj15.getId())).length);
        assertEquals(getValueBytesLength(obj16), converter.convertDefaults(obj16.getDefault(), obj16.getValueLengths(obj16.getId())).length);
        assertEquals(getValueBytesLength(obj17), converter.convertDefaults(obj17.getDefault(), obj17.getValueLengths(obj17.getId())).length);
        assertEquals(getValueBytesLength(obj18), converter.convertDefaults(obj18.getDefault(), obj18.getValueLengths(obj18.getId())).length);
        assertEquals(getValueBytesLength(obj19), converter.convertDefaults(obj19.getDefault(), obj19.getValueLengths(obj19.getId())).length);
        assertEquals(getValueBytesLength(obj20), converter.convertDefaults(obj20.getDefault(), obj20.getValueLengths(obj20.getId())).length);
        assertEquals(getValueBytesLength(obj21), converter.convertDefaults(obj21.getDefault(), obj21.getValueLengths(obj21.getId())).length);
        assertEquals(getValueBytesLength(obj22), converter.convertDefaults(obj22.getDefault(), obj22.getValueLengths(obj22.getId())).length);
        assertEquals(getValueBytesLength(obj23), converter.convertDefaults(obj23.getDefault(), obj23.getValueLengths(obj23.getId())).length);
        assertEquals(getValueBytesLength(obj232), converter.convertDefaults(obj232.getDefault(), obj232.getValueLengths(obj232.getId())).length);
        assertEquals(getValueBytesLength(obj233), converter.convertDefaults(obj233.getDefault(), obj233.getValueLengths(obj233.getId())).length);
        assertEquals(getValueBytesLength(obj234), converter.convertDefaults(obj234.getDefault(), obj234.getValueLengths(obj234.getId())).length);
        assertEquals(getValueBytesLength(obj235), converter.convertDefaults(obj235.getDefault(), obj235.getValueLengths(obj235.getId())).length);
        assertEquals(getValueBytesLength(obj292), converter.convertDefaults(obj292.getDefault(), obj292.getValueLengths(obj292.getId())).length);
        assertEquals(getValueBytesLength(obj293), converter.convertDefaults(obj293.getDefault(), obj293.getValueLengths(obj293.getId())).length);
        assertEquals(getValueBytesLength(obj294), converter.convertDefaults(obj294.getDefault(), obj294.getValueLengths(obj294.getId())).length);
        assertEquals(getValueBytesLength(obj295), converter.convertDefaults(obj295.getDefault(), obj295.getValueLengths(obj295.getId())).length);
        assertEquals(getValueBytesLength(obj296), converter.convertDefaults(obj296.getDefault(), obj296.getValueLengths(obj296.getId())).length);
        assertEquals(getValueBytesLength(obj312), converter.convertDefaults(obj312.getDefault(), obj312.getValueLengths(obj312.getId())).length);
        assertEquals(getValueBytesLength(obj313), converter.convertDefaults(obj313.getDefault(), obj313.getValueLengths(obj313.getId())).length);
        assertEquals(getValueBytesLength(obj314), converter.convertDefaults(obj314.getDefault(), obj314.getValueLengths(obj314.getId())).length);
        assertEquals(getValueBytesLength(obj315), converter.convertDefaults(obj315.getDefault(), obj315.getValueLengths(obj315.getId())).length);
        assertEquals(getValueBytesLength(obj316), converter.convertDefaults(obj316.getDefault(), obj316.getValueLengths(obj316.getId())).length);
        assertEquals(getValueBytesLength(obj317), converter.convertDefaults(obj317.getDefault(), obj317.getValueLengths(obj317.getId())).length);
        assertEquals(getValueBytesLength(obj29), converter.convertDefaults(obj29.getDefault(), obj29.getValueLengths(obj29.getId())).length);

    }

    private int getValueBytesLength(AbstractCTRObject obj) {
        int[] lengths = obj.getValueLengths(obj.getId());
        int sum = 0;
        byte[] valueResult = null;
        for (int i = 0; i < lengths.length; i++) {
            sum += obj.getValue(i).getBytes().length;
        }
        return sum;
    }
}
