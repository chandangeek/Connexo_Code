/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerCollection;
import com.elster.coding.CodingUtils;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class BerDecoderMappedCollectionTest
{
  @Test
  public void testDecode() throws Exception
  {
    
    System.out.println("decode");
    //BerId id = null;
    InputStream in =
            CodingUtils.string2InputStream(
            "60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 1D FF FF");
    BerDecoderCollection instance = new BerDecoderMappedCollection();
    //BerCollection expResult = null;
    BerCollection result = instance.decode(in);
    assertNotNull(result);//to remove the import warning.

    System.out.println(result.toString());

    // Test is incomplete
  }

}
