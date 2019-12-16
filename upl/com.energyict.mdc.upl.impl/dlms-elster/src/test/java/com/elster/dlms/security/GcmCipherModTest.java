/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.security;

import java.util.Arrays;
import com.elster.coding.CodingUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class GcmCipherModTest {

    public GcmCipherModTest() {
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
   * Test of setIvAndAad method, of class GcmCipherMod.
   */
  @Test
  public void testA() throws CipherException
  {
    byte[] eKey= CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] test= CodingUtils.string2ByteArray("10 D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF 4B35366956616759");
    byte[] iv= CodingUtils.string2ByteArray("4D4D4D0000BC614E01234567");

    GcmCipherMod instance= new GcmCipherMod(eKey, 12*8, true);
    instance.setIvAndAad(iv, test);

    byte[] out=new byte[12];
    instance.doFinal(out, 0);

    byte[] exp=CodingUtils.string2ByteArray("FE1466AFB3DBCD4F9389E2B7");
    assertArrayEquals(exp, out);
  }
  
  private void alterIv(byte[] iv)
  {
    for (int i= iv.length-1; i>=0; i--)
    {
      if ( (iv[i]&0xFF) == 0xFF)
      {
        iv[i]=0;
      }
      else
      {
        iv[i]++;
        break;
      }
    }
  }


  @Test
  public void testB() throws CipherException
  {
    byte[] eKey= CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] test= CodingUtils.string2ByteArray("10 D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF 503677524A323146");
    byte[] iv= CodingUtils.string2ByteArray("4D4D4D000000000100000000");
    byte[] exp=CodingUtils.string2ByteArray("1A52FE7DD3E72748973C1E28");

    GcmCipherMod instance= new GcmCipherMod(eKey, 12*8, true);

    while (true)
    {
      instance.setIvAndAad(iv, test);
      byte[] out=new byte[12];
      instance.doFinal(out, 0);
      if (Arrays.equals(exp, out))
      {
        System.out.println("found: "+CodingUtils.byteArrayToString(iv));
        return;
      }
      alterIv(iv);
    }
  }

}