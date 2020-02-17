/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import java.io.IOException;
import com.elster.coding.CodingUtils;
import java.io.ByteArrayOutputStream;
import com.elster.dlms.cosem.application.services.release.ReleaseRequest;
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
public class CoderReleaseRequestTest
{
  public CoderReleaseRequestTest()
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
   * Test of encodeObject method, of class CoderReleaseRequest.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    ReleaseRequest releaseRequest = new ReleaseRequest();
    releaseRequest.setReason(ReleaseRequest.Reason.NORMAL);
    releaseRequest.setUserInfo(CodingUtils.string2ByteArray("21 01 02 03"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderReleaseRequest instance = new CoderReleaseRequest();



    instance.encodeObject(releaseRequest, out);

    byte[] result = out.toByteArray();
    System.out.println(CodingUtils.byteArrayToString(result));
  }

  /**
   * Test of decodeObject method, of class CoderReleaseRequest.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray("62 03 80 01 00"));
    CoderReleaseRequest instance = new CoderReleaseRequest();
    ReleaseRequest result = instance.decodeObject(in);
    assertEquals(ReleaseRequest.Reason.NORMAL, result.getReason());
  }

  /**
   * Test of decodeObject method, of class CoderReleaseRequest.
   */
  @Test
  public void testDecodeObject2() throws Exception
  {
    System.out.println("decodeObject 2");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray("62 03 80 01 1E"));
    CoderReleaseRequest instance = new CoderReleaseRequest();
    ReleaseRequest result = instance.decodeObject(in);
    assertEquals(ReleaseRequest.Reason.USER_DEFINED, result.getReason());
  }

  /**
   * Test of decodeObject method, of class CoderReleaseRequest.
   */
  @Test
  public void testDecodeObject3() throws Exception
  {
    System.out.println("decodeObject 3");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray(
            "62 0B 80 01 1E BE 06 04 04 21 01 02 03"));
    CoderReleaseRequest instance = new CoderReleaseRequest();
    ReleaseRequest result = instance.decodeObject(in);
    assertEquals(ReleaseRequest.Reason.USER_DEFINED, result.getReason());
    assertArrayEquals(CodingUtils.string2ByteArray("21 01 02 03"), result.getUserInfo());
  }

  @Test(expected = IOException.class)
  public void testDecodeObject4() throws Exception
  {
    System.out.println("decodeObject 4");
    InputStream in = new ByteArrayInputStream(CodingUtils.string2ByteArray(
            "63 0B 80 01 1E BE 06 04 04 21 01 02 03"));
    CoderReleaseRequest instance = new CoderReleaseRequest();
    instance.decodeObject(in);

  }

}
