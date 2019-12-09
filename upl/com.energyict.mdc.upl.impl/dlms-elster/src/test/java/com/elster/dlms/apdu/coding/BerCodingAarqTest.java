/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.types.BerCollection;
import com.elster.coding.CodingUtils;
import java.io.IOException;
import java.io.InputStream;
import com.elster.ber.coding.BerDecoderCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class BerCodingAarqTest
{
  public BerCodingAarqTest()
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
   * Test of buildDecoderAarq method, of class BerCodingAarq.
   */
  @Test
  public void testBuildCoderAarq() throws IOException
  {
    System.out.println("buildCoderAarq");
    BerDecoderCollection result = BerCodingAarq.buildDecoderAarq();

    InputStream in =
            CodingUtils.string2InputStream(
            "60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 1D FF FF");

    BerCollection collection = result.decode(in);

    System.out.println(collection.toString());
  }

}
