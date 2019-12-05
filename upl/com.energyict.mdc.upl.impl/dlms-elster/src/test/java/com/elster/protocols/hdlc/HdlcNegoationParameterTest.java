/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.protocols.hdlc;

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
public class HdlcNegoationParameterTest {

    public HdlcNegoationParameterTest() {
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
   * Test of decode method, of class HdlcNegotiationParameters.
   */
  @Test
  public void testDecode() throws Exception
  {
    System.out.println("decode");
    InputStream inputStream = CodingUtils.string2InputStream("81 80 14 05 02 00 81  06 02 00 82  07 04 00 00 00 01  08 04 00 00 00 02  ");
    boolean skipHeader = false;
    HdlcNegotiationParameters instance = new HdlcNegotiationParameters();
    instance.decode(inputStream, skipHeader);

    assertEquals(0x81, instance.getMaxInformationFieldLengthTransmit());
    assertEquals(0x82, instance.getMaxInformationFieldLengthReceive());

    assertEquals(0x01, instance.getWindowSizeTransmit());
    assertEquals(0x02, instance.getWindowSizeReceive());

  }

  /**
   * Test of encode method, of class HdlcNegotiationParameters.
   */
  @Test
  public void testEncode() throws Exception
  {
    System.out.println("encode");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HdlcNegotiationParameters instance = new HdlcNegotiationParameters();

    instance.setMaxInformationFieldLengthTransmit(0x81);
    instance.setMaxInformationFieldLengthReceive(0x82);
    instance.setWindowSizeTransmit(0x01);
    instance.setWindowSizeReceive(0x02);
    instance.encode(outputStream);

    byte[] expectedResult= CodingUtils.string2ByteArray("81 80 14 05 02 00 81  06 02 00 82  07 04 00 00 00 01  08 04 00 00 00 02  ");
    byte[] result= outputStream.toByteArray();

    assertArrayEquals(expectedResult, result);
  }

}