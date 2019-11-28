/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.release.ReleaseResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderReleaseResponseTest
{
  public CoderReleaseResponseTest()
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
   * Test of encodeObject method, of class CoderReleaseResponse.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    ReleaseResponse response = new ReleaseResponse();
    response.setReason(ReleaseResponse.Reason.NORMAL);
    response.setUserInfo(CodingUtils.string2ByteArray("21 01 02 03"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderReleaseResponse instance = new CoderReleaseResponse();
    instance.encodeObject(response, out);
    byte[] result = out.toByteArray();

    System.out.println(CodingUtils.byteArrayToString(result));
    
    assertTrue(result.length > 2);
    assertEquals(result.length - 2, result[1] & 0xFF);
    assertEquals(0x63, result[0]);
  }

  /**
   * Test of decodeObject method, of class CoderReleaseResponse.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray("63 0B 80 01 01 BE 06 04 04 21 01 02 03"));
    CoderReleaseResponse instance = new CoderReleaseResponse();
    ReleaseResponse result = instance.decodeObject(in);
    assertEquals(ReleaseResponse.Reason.NOT_FINISHED, result.getReason());
    assertArrayEquals(CodingUtils.string2ByteArray("21 01 02 03"), result.getUserInfo());
  }
  
  
   /**
   * Test of decodeObject method, of class CoderReleaseResponse.
   */
  @Test(expected= IOException.class)
  public void testDecodeObject2() throws Exception
  {
    System.out.println("decodeObject 2");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray("62 0B 80 01 01 BE 06 04 04 21 01 02 03"));
    CoderReleaseResponse instance = new CoderReleaseResponse();
    instance.decodeObject(in);
  }

}
