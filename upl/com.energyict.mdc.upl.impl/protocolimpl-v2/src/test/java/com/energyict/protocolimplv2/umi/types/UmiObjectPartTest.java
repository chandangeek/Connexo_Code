package com.energyict.protocolimplv2.umi.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UmiObjectPartTest {
    @Test
    public void createUmiObjectPart() {
        String objectPartString = new String("umi.1.0.42.1[3]");
        UmiObjectPart objectPart = new UmiObjectPart(objectPartString);
        assertEquals("umi.1.0.42.1", objectPart.getUmiCode().toString());
        assertEquals(3, objectPart.getStartElement());
        assertEquals(3, objectPart.getEndElement());
        assertEquals(0xFF, objectPart.getMember());
        assertEquals(objectPartString, objectPart.toString());

        String objectPartString1 = new String("umi.1.0.43.1[3:5]");
        UmiObjectPart objectPart1 = new UmiObjectPart(objectPartString1);
        assertEquals("umi.1.0.43.1", objectPart1.getUmiCode().toString());
        assertEquals(3, objectPart1.getStartElement());
        assertEquals(5, objectPart1.getEndElement());
        assertEquals(0xFF, objectPart1.getMember());
        assertEquals(objectPartString1, objectPart1.toString());

        String objectPartString2 = new String("umi.255.0.42.1[:]");
        UmiObjectPart objectPart2 = new UmiObjectPart(objectPartString2);
        assertEquals("umi.255.0.42.1", objectPart2.getUmiCode().toString());
        assertEquals(0, objectPart2.getStartElement());
        assertEquals(0xFFFF, objectPart2.getEndElement());
        assertEquals(0xFF, objectPart2.getMember());
        assertEquals(objectPartString2, objectPart2.toString());

        String objectPartString3 = new String("umi.1.0.42.1[3:]");
        UmiObjectPart objectPart3 = new UmiObjectPart(objectPartString3);
        assertEquals("umi.1.0.42.1", objectPart3.getUmiCode().toString());
        assertEquals(3, objectPart3.getStartElement());
        assertEquals(0xFFFF, objectPart3.getEndElement());
        assertEquals(0xFF, objectPart3.getMember());
        assertEquals(objectPartString3, objectPart3.toString());

        String objectPartString4 = new String("umi.1.0.42.1[:4]");
        UmiObjectPart objectPart4 = new UmiObjectPart(objectPartString4);
        assertEquals("umi.1.0.42.1", objectPart4.getUmiCode().toString());
        assertEquals(0, objectPart4.getStartElement());
        assertEquals(4, objectPart4.getEndElement());
        assertEquals(0xFF, objectPart4.getMember());
        assertEquals(objectPartString4, objectPart4.toString());

        String objectPartString5 = new String("umi.1.0.42.2/4");
        UmiObjectPart objectPart5 = new UmiObjectPart(objectPartString5);
        assertEquals("umi.1.0.42.2", objectPart5.getUmiCode().toString());
        assertEquals(0, objectPart5.getStartElement());
        assertEquals(0, objectPart5.getEndElement());
        assertEquals(4, objectPart5.getMember());
        assertEquals(objectPartString5, objectPart5.toString());

        String objectPartString6 = new String("umi.1.0.42.3[2]/3");
        UmiObjectPart objectPart6 = new UmiObjectPart(objectPartString6);
        assertEquals("umi.1.0.42.3", objectPart6.getUmiCode().toString());
        assertEquals(2, objectPart6.getStartElement());
        assertEquals(2, objectPart6.getEndElement());
        assertEquals(3, objectPart6.getMember());
        assertEquals(objectPartString6, objectPart6.toString());

        String objectPartString7 = new String("umi.1.0.42.3[2:4]/3");
        UmiObjectPart objectPart7 = new UmiObjectPart(objectPartString7);
        assertEquals("umi.1.0.42.3", objectPart7.getUmiCode().toString());
        assertEquals(2, objectPart7.getStartElement());
        assertEquals(4, objectPart7.getEndElement());
        assertEquals(3, objectPart7.getMember());
        assertEquals(objectPartString7, objectPart7.toString());

        String objectPartString8 = new String("umi.1.0.76.25/0");
        UmiObjectPart objectPart8 = new UmiObjectPart(objectPartString8);
        assertEquals("umi.1.0.76.25", objectPart8.getUmiCode().toString());
        assertEquals(0, objectPart8.getStartElement());
        assertEquals(0, objectPart8.getEndElement());
        assertEquals(0, objectPart8.getMember());
        assertEquals(objectPartString8, objectPart8.toString());

        String objectPartString9 = new String("umi.1.0.76.25");
        UmiObjectPart objectPart9 = new UmiObjectPart(objectPartString9);
        assertEquals("umi.1.0.76.25", objectPart9.getUmiCode().toString());
        assertEquals(0, objectPart9.getStartElement());
        assertEquals(0, objectPart9.getEndElement());
        assertEquals(0xFF, objectPart9.getMember());
        assertEquals(objectPartString9, objectPart9.toString());

        String objectPartString10 = new String("umi.1.0.76.43[2:]/1");
        UmiObjectPart objectPart10 = new UmiObjectPart(objectPartString10);
        assertEquals("umi.1.0.76.43", objectPart10.getUmiCode().toString());
        assertEquals(2, objectPart10.getStartElement());
        assertEquals(0xFFFF, objectPart10.getEndElement());
        assertEquals(1, objectPart10.getMember());
        assertEquals(objectPartString10, objectPart10.toString());
    }
}
