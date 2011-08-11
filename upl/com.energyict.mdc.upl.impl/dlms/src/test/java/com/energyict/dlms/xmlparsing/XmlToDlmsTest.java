package com.energyict.dlms.xmlparsing;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 11-aug-2011
 * Time: 9:01:54
 */
public class XmlToDlmsTest {

    String xml1 = "<Scheme Name=\"Test AS300P\"> \n" +
            "\t<Compatibility> \n" +
            "\t\t<MeterTypes> \n" +
            "\t\t\t<Meter Type=\"AS300P\" Version=\"1\"/> \n" +
            "\t\t</MeterTypes> \n" +
            "\t\t<Transactions> \n" +
            "\t\t\t<SetRequest> \n" +
            "\t\t\t\t<SetRequestNormal> \n" +
            "\t\t\t\t\t<AttributeDescriptor> \n" +
            "\t\t\t\t\t\t<ClassId Value=\"0007\" /> \n" +
            "\t\t\t\t\t\t<InstanceId Value=\"0100630100FF\" /> \n" +
            "\t\t\t\t\t\t<AttributeId Value=\"03\" /> \n" +
            "\t\t\t\t\t</AttributeDescriptor> \n" +
            "\t\t\t\t\t<Value> \n" +
            "\t\t\t\t\t\t<Array Qty=\"1\"> \n" +
            "\t\t\t\t\t\t\t<Unsigned16 Value=\"0003\" /> \n" +
            "\t\t\t\t\t\t</Array> \n" +
            "\t\t\t\t\t</Value> \n" +
            "\t\t\t\t</SetRequestNormal> \n" +
            "\t\t\t</SetRequest> \n" +
            "\t\t</Transactions> \n" +
            "\t</Compatibility> \n" +
            "</Scheme>";

    String xml2 = "\n" +
            "<Scheme Name=\"Test AS300P\"> \n" +
            "\t<Compatibility> \n" +
            "\t\t<MeterTypes> \n" +
            "\t\t\t<Meter Type=\"AS300P\" Version=\"1\"/> \n" +
            "\t\t</MeterTypes> \n" +
            "\t\t<Transactions> \n" +
            "\t\t\t<SetRequest> \n" +
            "\t\t\t\t<SetRequestNormal> \n" +
            "\t\t\t\t\t<AttributeDescriptor> \n" +
            "\t\t\t\t\t\t<ClassId Value=\"0007\" /> \n" +
            "\t\t\t\t\t\t<InstanceId Value=\"0100630100FF\" /> \n" +
            "\t\t\t\t\t\t<AttributeId Value=\"03\" /> \n" +
            "\t\t\t\t\t</AttributeDescriptor> \n" +
            "\t\t\t\t\t<Value> \n" +
            "\t\t\t\t\t\t<Array Qty=\"2\"> \n" +
            "\t\t\t\t\t\t\t<Structure Qty=\"4\"> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0003\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"0100011900FF\" /> \n" +
            "\t\t\t\t\t\t\t\t<Integer8 Value=\"02\" /> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0000\" /> \n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t\t<Structure Qty=\"4\"> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0003\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"0100021900FF\" /> \n" +
            "\t\t\t\t\t\t\t\t<Integer8 Value=\"02\" /> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0000\" />'\n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t</Array> \n" +
            "\t\t\t\t\t</Value> \n" +
            "\t\t\t\t</SetRequestNormal> \n" +
            "\t\t\t</SetRequest> \n" +
            "\t\t</Transactions> \n" +
            "\t</Compatibility> \n" +
            "</Scheme>";

    String xml3 = "\n" +
            "<Scheme Name=\"Test AS300P\"> \n" +
            "\t<Compatibility> \n" +
            "\t\t<MeterTypes> \n" +
            "\t\t\t<Meter Type=\"AS300P\" Version=\"1\"/> \n" +
            "\t\t</MeterTypes> \n" +
            "\t\t<Transactions> \n" +
            "\t\t\t<SetRequest> \n" +
            "\t\t\t\t<SetRequestNormal> \n" +
            "\t\t\t\t\t<AttributeDescriptor> \n" +
            "\t\t\t\t\t\t<ClassId Value=\"0007\" /> \n" +
            "\t\t\t\t\t\t<InstanceId Value=\"0100630100FF\" /> \n" +
            "\t\t\t\t\t\t<AttributeId Value=\"03\" /> \n" +
            "\t\t\t\t\t</AttributeDescriptor> \n" +
            "\t\t\t\t\t<Value> \n" +
            "\t\t\t\t\t\t<Array Qty=\"2\"> \n" +
            "\t\t\t\t\t\t\t<Structure Qty=\"4\"> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0003\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"0100011900FF\" /> \n" +
            "\t\t\t\t\t\t\t\t<Integer8 Value=\"02\" /> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0000\" /> \n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t\t<Structure Qty=\"4\"> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0003\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"0100021900FF\" /> \n" +
            "\t\t\t\t\t\t\t\t<Integer8 Value=\"02\" /> \n" +
            "\t\t\t\t\t\t\t\t<Unsigned16 Value=\"0000\" />'\n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t</Array> \n" +
            "\t\t\t\t\t</Value> \n" +
            "\t\t\t\t</SetRequestNormal> \n" +
            "\t\t\t</SetRequest> \n" +
            "\t\t\t\n" +
            "\t\t\t<SetRequest> \n" +
            "\t\t\t\t<SetRequestNormal> \n" +
            "\t\t\t\t\t<AttributeDescriptor> \n" +
            "\t\t\t\t\t\t<ClassId Value=\"0014\" /> \n" +
            "\t\t\t\t\t\t<InstanceId Value=\"00000D0000FF\" /> \n" +
            "\t\t\t\t\t\t<AttributeId Value=\"02\" /> \n" +
            "\t\t\t\t\t</AttributeDescriptor> \n" +
            "\t\t\t\t\t<Value> \n" +
            "\t\t\t\t\t\t<Array Qty=\"2\"> \n" +
            "\t\t\t\t\t\t\t<Structure Qty=\"3\"> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"000102\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"07DB080BFF00000000800000\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"01\" />  \n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t\t\t<Structure Qty=\"3\"> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"030405\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"07DB0C0BFF00000000800000\" /> \n" +
            "\t\t\t\t\t\t\t\t<OctetString Value=\"02\" />  \n" +
            "\t\t\t\t\t\t\t</Structure> \n" +
            "\t\t\t\t\t\t</Array> \n" +
            "\t\t\t\t\t</Value> \n" +
            "\t\t\t\t</SetRequestNormal> \n" +
            "\t\t\t</SetRequest> \n" +
            "\t\t\t\n" +
            "\t\t</Transactions> \n" +
            "\t</Compatibility> \n" +
            "</Scheme>";

    @Test
    public void testParseSetRequests() throws Exception {

        byte[] expectedArray1 = new byte[]{1, 1, 18, 0, 3};
        byte[] expectedArray2 = new byte[]{1, 2, 2, 4, 18, 0, 3, 9, 6, 1, 0, 1, 25, 0, -1, 15, 2, 18, 0, 0, 2, 4, 18, 0, 3, 9, 6, 1, 0, 2, 25, 0, -1, 15, 2, 18, 0, 0};
        byte[] expectedArray3 = new byte[]{1, 2, 2, 3, 9, 3, 0, 1, 2, 9, 12, 7, (byte) 219, 8, 11, -1, 0, 0, 0, 0, (byte) 128, 0, 0, 9, 1, 1, 2, 3, 9, 3, 3, 4, 5, 9, 12, 7, (byte) 219, 12, 11, -1, 0, 0, 0, 0, (byte) 128, 0, 0, 9, 1, 2};
        DLMSConnection connection = new MockDLMSConnection();
        ProtocolLink protocol = new MockProtocolLink(connection);
        XmlToDlms x2d = new XmlToDlms(protocol);
        List<GenericDataToWrite> gdtw = x2d.parseSetRequests(xml1);
        assertNotNull(gdtw);
        assertEquals(1, gdtw.size());
        assertArrayEquals(expectedArray1, gdtw.get(0).getDataToWrite());
        assertEquals(7, gdtw.get(0).getGenericWrite().getObjectReference().getClassId());
        assertArrayEquals(new byte[]{1, 0, 99, 1, 0, -1}, gdtw.get(0).getGenericWrite().getObjectReference().getLn());

        x2d = new XmlToDlms(protocol);
        gdtw = x2d.parseSetRequests(xml2);
        assertNotNull(gdtw);
        assertEquals(1, gdtw.size());
        assertArrayEquals(expectedArray2, gdtw.get(0).getDataToWrite());

        x2d = new XmlToDlms(protocol);
        gdtw = x2d.parseSetRequests(xml3);
        assertNotNull(gdtw);
        assertEquals(2, gdtw.size());
        assertArrayEquals(expectedArray3, gdtw.get(1).getDataToWrite());
    }
}
