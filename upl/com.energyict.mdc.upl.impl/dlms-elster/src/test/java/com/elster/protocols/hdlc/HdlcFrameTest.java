/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.hdlc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.elster.coding.CodingUtils;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class HdlcFrameTest
{
  public HdlcFrameTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  /**
   * Test of decode method, of class HdlcFrame.
   */
  @Test
  public void testDecode() throws Exception
  {
    System.out.println("decode");

    InputStream inputStream = CodingUtils.string2InputStream(
            "7E A0 1C 00 02 60 4D 21 BA 6D AC E6 E6 00 C0 01 C1 00 07 07 00 63 63 00 FF 04 00 0E 77");

    int expLength = inputStream.available();

    HdlcFrame instance = new HdlcFrame();
    int length = instance.decode(inputStream);

    assertEquals(expLength, length);


    assertEquals(10, instance.getFormatType());
    assertEquals(false, instance.isSegmentationBit());
    assertEquals(28, instance.getFrameLength());

    HdlcAddress destAddress = instance.getDestAddress();

    assertEquals(0x0001, destAddress.getUpperHdlcAddress());
    assertEquals(0x1826, destAddress.getLowerHdlcAddress());
    assertEquals(4, destAddress.getAddressLength());

    HdlcAddress sourceAddress = instance.getSourceAddress();
    assertEquals(0x10, sourceAddress.getUpperHdlcAddress());
    assertEquals(0, sourceAddress.getLowerHdlcAddress());
    assertEquals(1, sourceAddress.getAddressLength());

    HdlcControlField controllField = instance.getControllField();
    assertEquals(HdlcControlField.CommandAndResponseType.I, controllField.getCommandAndResponseType());
    assertEquals(true, controllField.isPoolFinal());
    assertEquals(5, controllField.getReceiveSeqNo());
    assertEquals(5, controllField.getSendSeqNo());

    byte[] info = CodingUtils.string2ByteArray("E6 E6 00 C0 01 C1 00 07 07 00 63 63 00 FF 04 00");
    assertArrayEquals(info, instance.getInformationBytes());
  }

  /**
   * Test of encode method, of class HdlcFrame.
   */
  @Test
  public void testDecodeEncode() throws Exception
  {
    System.out.println("decode - encode");

    //simpler Test ob die Daten durch decodieren und encodieren und  unverändert bleiben.

    byte expected[] = CodingUtils.string2ByteArray(
            "A0 1C 00 02 60 4D 21 BA 6D AC E6 E6 00 C0 01 C1 00 07 07 00 63 63 00 FF 04 00 0E 77");
    InputStream inputStream = new ByteArrayInputStream(expected);

    HdlcFrame instance = new HdlcFrame();
    instance.decode(inputStream);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    instance.encode(outputStream);

    byte[] result = outputStream.toByteArray();

    assertArrayEquals(expected, result);
  }

  /**
   * Test of encode method, of class HdlcFrame.
   */
  @Test
  public void testEncodeDecode() throws Exception
  {
    System.out.println("encode - decode");

//    byte expected[] = CodingUtils.string2ByteArray(
//            "A0 1C 00 02 60 4D 21 BA 6D AC E6 E6 00 C0 01 C1 00 07 07 00 63 63 00 FF 04 00 0E 77");
//    InputStream inputStream = new ByteArrayInputStream(expected);

    HdlcFrame instance = new HdlcFrame();
    HdlcControlField controlField = new HdlcControlField();
    controlField.setPoolFinal(true);
    controlField.setSendSeqNo(4);
    controlField.setReceiveSeqNo(2);
    controlField.setCommandAndResponseType(HdlcControlField.CommandAndResponseType.I);

    instance.setControllField(controlField);
    instance.setDestAddress(new HdlcAddress(1));
    instance.setSourceAddress(new HdlcAddress(2));

    instance.setSegmentationBit(false);

    byte[] information = new byte[]
    {
      (byte)0xFF
    };

    instance.setInformation(information);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    instance.encode(out);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    HdlcFrame decodedFrame = new HdlcFrame();

    decodedFrame.decode(in);

    assertEquals(1, decodedFrame.getInformation().length);
    assertEquals((byte)0xFF, decodedFrame.getInformation()[0]);

    HdlcControlField decodedControlField = decodedFrame.getControllField();

    assertEquals(true, decodedControlField.isPoolFinal());
    assertEquals(4, controlField.getSendSeqNo());
    assertEquals(2, controlField.getReceiveSeqNo());
    assertEquals(HdlcControlField.CommandAndResponseType.I, controlField.getCommandAndResponseType());

    assertEquals( new HdlcAddress(1), decodedFrame.getDestAddress());
    assertEquals( new HdlcAddress(2), decodedFrame.getSourceAddress());

    assertEquals(false, decodedFrame.isSegmentationBit());
  }

}
