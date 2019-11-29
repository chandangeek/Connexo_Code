/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import com.elster.coding.CodingUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class BerOuputStreamTest
{
  public BerOuputStreamTest()
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
   * Test of writeIdentifier method, of class BerOutputStream.
   */
  @Test
  public void testWriteIdentifier() throws Exception
  {
    System.out.println("writeIdentifier");
    BerId identifier = BerIds.ID_INT;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("02");

    instance.writeIdentifier(identifier);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeIdentifier method, of class BerOutputStream.
   */
  @Test
  public void testWriteIdentifier2() throws Exception
  {
    System.out.println("writeIdentifier");
    BerId identifier = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 1);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("A1");

    instance.writeIdentifier(identifier);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeLength method, of class BerOutputStream.
   */
  @Test
  public void testWriteLength() throws Exception
  {
    System.out.println("writeLength");
    int length = 0xF9;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("81 F9");

    instance.writeLength(length);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeLength method, of class BerOutputStream.
   */
  @Test
  public void testWriteLength2() throws Exception
  {
    System.out.println("writeLength");
    int length = 0xFFC1;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("82 FF C1");

    instance.writeLength(length);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeLength method, of class BerOutputStream.
   */
  @Test
  public void testWriteLength3() throws Exception
  {
    System.out.println("writeLength");
    int length = 0x7F;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("7F");

    instance.writeLength(length);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt_Block() throws Exception
  {
    System.out.println("writeInt");
    for (int i = 1000; i > 0xFFF00000; i--)
    {

      int value = i;

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      BerOutputStream instance = new BerOutputStream(out);
      instance.writeInt(value);

      BerInputStream in = new BerInputStream(new ByteArrayInputStream(out.toByteArray()));

      int result = in.readInt();

      assertEquals(value, result);
    }
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  public void testWriteInt_TestAll() throws Exception
  {
    System.out.println("writeInt test all");
    PipedInputStream ip = new PipedInputStream();
    PipedOutputStream op = new PipedOutputStream(ip);
    final BerOutputStream instance = new BerOutputStream(op);
    BerInputStream in = new BerInputStream(ip);

    long t = System.currentTimeMillis();

    int i = Integer.MIN_VALUE;

    do
    {
      instance.writeInt(i);
      int result = in.readInt();
      assertEquals(i, result);
      if (i % 100000000 == 0)
      {
        long t2 = System.currentTimeMillis();
        System.out.println("Number: " + i + " Time:" + (t2 - t));
        t = System.currentTimeMillis();
      }
      i++;
    }
    while (i!=Integer.MIN_VALUE);
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt_TestMany() throws Exception
  {
    long seed = System.currentTimeMillis() ^ System.nanoTime();
    System.out.println("writeInt test many. Seed:" + seed);
    PipedInputStream ip = new PipedInputStream();
    PipedOutputStream op = new PipedOutputStream(ip);
    final BerOutputStream instance = new BerOutputStream(op);
    BerInputStream in = new BerInputStream(ip);

    Random random = new Random(seed);

    for (int i = 0; i <= 500000; i++)
    {
      int value = random.nextInt();
      instance.writeInt(value);
      int result = in.readInt();
      assertEquals(value, result);
    }
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt() throws Exception
  {
    System.out.println("writeInt");
    int value = -12345;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("02 CF C7");

    instance.writeInt(value);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt2() throws Exception
  {
    System.out.println("writeInt");
    int value = 0;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("01 00");

    instance.writeInt(value);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt3() throws Exception
  {
    System.out.println("writeInt");
    int value = -1048576;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("03 F0 00 00");

    instance.writeInt(value);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt4() throws Exception
  {
    System.out.println("writeInt");
    int value = 255;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("02 00 FF");

    instance.writeInt(value);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeInt method, of class BerOutputStream.
   */
  @Test
  public void testWriteInt5() throws Exception
  {
    System.out.println("writeInt");
    int value = 254;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("02 00 FE");

    instance.writeInt(value);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeBitString method, of class BerOutputStream.
   */
  @Test
  public void testWriteBitString() throws Exception
  {
    System.out.println("writeBitString");
    BitString bitString = new BitString(44, CodingUtils.string2ByteArray("0A3B5F291CD0"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("07040A3B5F291CD0");

    instance.writeBitString(bitString);

    assertArrayEquals(expectedResult, out.toByteArray());
  }


  /**
   * Test of writeBitString method, of class BerOutputStream.
   */
  @Test
  public void testWriteBitString2() throws Exception
  {
    System.out.println("writeBitString");
    BitString bitString = new BitString(3, CodingUtils.string2ByteArray("03"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("02 05 03");

    instance.writeBitString(bitString);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeGraphicString method, of class BerOutputStream.
   */
  @Test
  public void testWriteGraphicString() throws Exception
  {
    System.out.println("writeGraphicString");
    String graphicString = "Hallo";

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("05 48 61 6C 6C 6F");

    instance.writeGraphicString(graphicString);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeOctetString method, of class BerOutputStream.
   */
  @Test
  public void testWriteOctetString() throws Exception
  {
    System.out.println("writeOctetString");
    byte[] bytes = CodingUtils.string2ByteArray("A1 B2 C3 00 02 03 F1 F2 FF");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("09 A1 B2 C3 00 02 03 F1 F2 FF");
    instance.writeOctetString(bytes);

    assertArrayEquals(expectedResult, out.toByteArray());
  }

  /**
   * Test of writeObjectIdentifier method, of class BerOutputStream.
   */
  @Test
  public void testWriteObjectIdentifier() throws Exception
  {
    System.out.println("writeObjectIdentifier");
    ObjectIdentifier oid =  new ObjectIdentifier(2,16,756,5,8,1,1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BerOutputStream instance = new BerOutputStream(out);
    byte[] expectedResult = CodingUtils.string2ByteArray("07 60 85 74 05 08 01 01");

    instance.writeObjectIdentifier(oid);
    assertArrayEquals(expectedResult, out.toByteArray());
  }

}
