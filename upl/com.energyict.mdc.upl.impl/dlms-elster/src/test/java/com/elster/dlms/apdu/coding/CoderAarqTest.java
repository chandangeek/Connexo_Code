/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import java.io.ByteArrayInputStream;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.definitions.DlmsOids;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.open.OpenRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderAarqTest
{
  public CoderAarqTest()
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
   * Test of decodeObject method, of class CoderAarq.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");

    InputStream in =
            CodingUtils.string2InputStream(
            "60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 1D FF FF");
    CoderAarq instance = new CoderAarq();
    OpenRequest result = instance.decodeObject(in);

    assertEquals(DlmsOids.DLMS_UA_AC_LN, result.getApplicationContextName());
    assertEquals(DlmsOids.DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME,
                 result.getSecurityMechanismName());
    assertEquals(AuthenticationValue.createCharstring("55555555"), result.getCallingAuthenticationValue());
    assertNotNull(result.getProposedXDlmsContext());
  }

  /**
   * Test of decodeObject method, of class CoderAarq.
   */
  @Test
  public void testDecodeEncodeObject() throws Exception
  {
    System.out.println("decodeObject");

    byte[] bytes =
            CodingUtils.string2ByteArray(
            "60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 1D FF FF");

    //--- encode ---
    InputStream in =new ByteArrayInputStream(bytes);
    CoderAarq instance = new CoderAarq();

    OpenRequest result = instance.decodeObject(in);

    //--- decode ---
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    instance.encodeObject(result, out);

    assertArrayEquals(bytes, out.toByteArray());
  }

}
