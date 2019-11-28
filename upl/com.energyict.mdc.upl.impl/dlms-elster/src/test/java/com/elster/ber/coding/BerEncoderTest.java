/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.ber.coding;

import org.junit.Test;
import com.elster.coding.CodingUtils;
import java.io.InputStream;
import com.elster.ber.types.BerValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class BerEncoderTest {

  /**
   * Test of encode method, of class BerEncoder. <P>
   * Simple encode-decode-equals test.
   */
  @Test
  public void testEncode_OutputStream_BerValue() throws Exception
  {

    System.out.println("encode - decode");

    byte[] bytes= CodingUtils.string2ByteArray(  "60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 1D FF FF");

    InputStream in = new ByteArrayInputStream(bytes);
    BerDecoderCollection decoder = new BerDecoderDefaultCollection();
    BerValue value = decoder.decode(in);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerEncoder instance = new BerEncoder();
    instance.encode(out, value);

    assertArrayEquals(bytes, out.toByteArray());
  }



}