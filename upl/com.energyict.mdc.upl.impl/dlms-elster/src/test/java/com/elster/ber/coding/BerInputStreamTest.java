/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import com.elster.coding.CodingUtils;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class BerInputStreamTest
{

  protected BerInputStream createStream(String data)
  {
    return new BerInputStream(CodingUtils.string2InputStream(data));
  }

  /**
   * Test of getPosition method, of class BerInputStream.
   */
  @Test
  public void testGetPosition() throws IOException
  {
    System.out.println("getPosition");
    BerInputStream instance =  createStream("07 60 85 74 05 08 01 01 07 60 85 74 05 08 01 01");
    int result = instance.getPosition();
    assertEquals(0, result);

    instance.readObjectIdentifer();
    result = instance.getPosition();

    assertEquals(8, result);

    instance.readObjectIdentifer();
    result = instance.getPosition();

    assertEquals(16, result);
  }

  /**
   * Test of readIdentifier method, of class BerInputStream.
   */
  @Test
  public void testReadIdentifier() throws Exception
  {
    System.out.println("readIdentifier");
    BerInputStream instance = createStream("02");
    BerId expResult = BerIds.ID_INT;
    BerId result = instance.readIdentifier();
    assertEquals(expResult, result);
  }

  /**
   * Test of readIdentifier method, of class BerInputStream.
   */
  @Test
  public void testReadIdentifier2() throws Exception
  {
    System.out.println("readIdentifier 2");
    BerInputStream instance = createStream("A1");
    BerId expResult = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 1);
    BerId result = instance.readIdentifier();
    assertEquals(expResult, result);
  }

  /**
   * Test of readLength method, of class BerInputStream.
   */
  @Test
  public void testReadLength() throws Exception
  {
    System.out.println("readLength");
    BerInputStream instance = createStream("81 F9" );
    int expResult = 0xF9;
    int result = instance.readLength();
    assertEquals(expResult, result);
  }

  /**
   * Test of readLength method, of class BerInputStream.
   */
  @Test
  public void testReadLength2() throws Exception
  {
    System.out.println("readLength 2");
    BerInputStream instance = createStream("82 FF C1");
    int expResult = 0xFFC1;
    int result = instance.readLength();
    assertEquals(expResult, result);
  }


  /**
   * Test of readLength method, of class BerInputStream.
   */
  @Test
  public void testReadLength3() throws Exception
  {
    System.out.println("readLength 2");
    BerInputStream instance = createStream("7F");
    int expResult = 0x7F;
    int result = instance.readLength();
    assertEquals(expResult, result);
  }


  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_0args() throws Exception
  {
    System.out.println("readInt");
    BerInputStream instance = createStream("02 CF C7");
    int expResult = -12345;
    int result = instance.readInt();
    assertEquals(expResult, result);
  }



  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_0args2() throws Exception
  {
    System.out.println("readInt");
    BerInputStream instance = createStream("01 00");
    int expResult = 0;
    int result = instance.readInt();
    assertEquals(expResult, result);
  }


  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_0args3() throws Exception
  {
    System.out.println("readInt");
    BerInputStream instance = createStream("03 F0 00 00");
    int expResult = -1048576 ;
    int result = instance.readInt();
    assertEquals(expResult, result);
  }

  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_0args4() throws Exception
  {
    System.out.println("readInt");
    BerInputStream instance = createStream("02 00 FF ");
    int expResult = 255 ;
    int result = instance.readInt();
    assertEquals(expResult, result);
  }

  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_0args5() throws Exception
  {
    System.out.println("readInt");
    BerInputStream instance = createStream("02 00 FE");
    int expResult = 254;
    int result = instance.readInt();
    assertEquals(expResult, result);
  }



  /**
   * Test of readInt method, of class BerInputStream.
   */
  @Test
  public void testReadInt_int() throws Exception
  {
    System.out.println("readInt");
    int contentLength = 3;
    BerInputStream instance =  createStream("F0 00 00");
    int expResult = -1048576 ;
    int result = instance.readInt(contentLength);
    assertEquals(expResult, result);
  }

  /**
   * Test of readBitString method, of class BerInputStream.
   */
  @Test
  public void testReadBitString_0args() throws Exception
  {
    System.out.println("readBitString");
    BerInputStream instance = createStream("07040A3B5F291CD0");
    BitString expResult = new BitString(44 , CodingUtils.string2ByteArray("0A3B5F291CD0"));
    BitString result = instance.readBitString();
    assertEquals(expResult, result);
  }

    /**
   * Test of readBitString method, of class BerInputStream.
   */
  @Test
  public void testReadBitString_0args2() throws Exception
  {
    System.out.println("readBitString");
    BerInputStream instance = createStream("02 05 03");
    BitString expResult = new BitString(3 , CodingUtils.string2ByteArray("03"));
    BitString result = instance.readBitString();

    assertEquals(expResult, result);
  }


  /**
   * Test of readBitString method, of class BerInputStream.
   */
  @Test
  public void testReadBitString_int() throws Exception
  {
    System.out.println("readBitString");
    int contentLength = 7;
    BerInputStream instance = createStream("040A3B5F291CD0");
    BitString expResult = new BitString(44 , CodingUtils.string2ByteArray("0A3B5F291CD0"));
    BitString result = instance.readBitString(contentLength);
    assertEquals(expResult, result);
  }
  
  
  
  /**
   * Test of readBitString method, of class BerInputStream.
   */
  @Test
  public void testReadBitString_int2() throws Exception
  {
    System.out.println("readBitString int 2");
    int contentLength = 1;
    BerInputStream instance = createStream("00");
    BitString expResult = new BitString(0 ,CodingUtils.string2ByteArray(""));
    BitString result = instance.readBitString(contentLength);
    assertEquals(expResult, result);
  }
  
  
    
  /**
   * Test of readBitString method, of class BerInputStream.
   */
  @Test(expected=IOException.class)
  public void testReadBitString_int3() throws Exception
  {
    System.out.println("readBitString int 2");
    int contentLength = 0;
    BerInputStream instance = createStream("");
    instance.readBitString(contentLength);
    fail("Exception expected");
  }
  
  /**
   * Test of readGraphicString method, of class BerInputStream.
   */
  @Test
  public void testReadGraphicString_0args() throws Exception
  {
    System.out.println("readGraphicString");
    BerInputStream instance = createStream("05 48 61 6C 6C 6F");
    String expResult = "Hallo";
    String result = instance.readGraphicString();
    assertEquals(expResult, result);
  }

  /**
   * Test of readGraphicString method, of class BerInputStream.
   */
  @Test
  public void testReadGraphicString_int() throws Exception
  {
    System.out.println("readGraphicString");
    int contentLength = 5;
    BerInputStream instance =  createStream("48 61 6C 6C 6F");
    String expResult = "Hallo";
    String result = instance.readGraphicString(contentLength);
    assertEquals(expResult, result);
  }

  /**
   * Test of readOctetString method, of class BerInputStream.
   */
  @Test
  public void testReadOctetString_0args() throws Exception
  {
    System.out.println("readOctetString");
    BerInputStream instance =  createStream("09 A1 B2 C3 00 02 03 F1 F2 FF");
    byte[] expResult = CodingUtils.string2ByteArray("A1 B2 C3 00 02 03 F1 F2 FF");
    byte[] result = instance.readOctetString();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of readOctetString method, of class BerInputStream.
   */
  @Test
  public void testReadOctetString_int() throws Exception
  {
    System.out.println("readOctetString");
    int contentLength = 9;
    BerInputStream instance =  createStream("A1 B2 C3 00 02 03 F1 F2 FF");
    byte[] expResult = CodingUtils.string2ByteArray("A1 B2 C3 00 02 03 F1 F2 FF");
    byte[] result = instance.readOctetString(contentLength);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of readObjectIdentifer method, of class BerInputStream.
   */
  @Test
  public void testReadObjectIdentifer_0args() throws Exception
  {
    System.out.println("readObjectIdentifer");
    BerInputStream instance = createStream("07 60 85 74 05 08 01 01");
    ObjectIdentifier expResult = new ObjectIdentifier(2,16,756,5,8,1,1);
    ObjectIdentifier result = instance.readObjectIdentifer();
    assertEquals(expResult, result);
  }

  /**
   * Test of readObjectIdentifer method, of class BerInputStream.
   */
  @Test
  public void testReadObjectIdentifer_int() throws Exception
  {
    System.out.println("readObjectIdentifer");
    int contentLength = 7;
    BerInputStream instance = createStream("60 85 74 05 08 01 01");
    ObjectIdentifier expResult = new ObjectIdentifier(2,16,756,5,8,1,1);
    ObjectIdentifier result = instance.readObjectIdentifer(contentLength);
    assertEquals(expResult, result);
  }

}
