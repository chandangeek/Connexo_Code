/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/test/com/elster/dlms/apdu/coding/CoderDlmsDataTest.java $
 * Version:
 * $Id: CoderDlmsDataTest.java 6772 2013-06-14 15:12:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 */
package com.elster.dlms.apdu.coding;

import java.util.List;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataCompactArray;
import com.elster.dlms.types.data.DlmsDataLong;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.TypeDescription;
import com.elster.dlms.types.data.TypeDescriptionStructure;
import com.elster.coding.CodingUtils;
import com.elster.dlms.types.data.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderDlmsDataTest
{
  private final static String TEST_DATA_STRING = "01 17 02 04 12 00 08 11 00 09 06 00 00 01 00 00 FF 02 02 01 09 02 03 0F 01 16 01"
                                                 + "00 02 03 0F 02 16 03 00 02 03 0F 03 16 03 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16"
                                                 + "01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 03 00 02 03 0F 09 16 01 00 01 06 02 02 0F 01 03 00 02 02 0F"
                                                 + "02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 02 0F 05 03 00 02 02 0F 06 03 00 02 04 12 00 03 11 00"
                                                 + "09 06 07 00 61 61 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01"
                                                 + "01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 0D 02 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02"
                                                 + "03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 0D 00"
                                                 + "00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03"
                                                 + "00 02 04 12 00 03 11 00 09 06 07 00 2A 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00"
                                                 + "02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 29 00 00 FF 02 02 01 03"
                                                 + "02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03"
                                                 + "11 00 09 06 07 00 35 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01"
                                                 + "00 01 01 02 02 0F 01 03 00 02 04 12 00 0C 11 01 09 06 00 00 28 00 00 FF 02 02 01 02 02 03 0F 01 16 01"
                                                 + "00 02 03 0F 02 16 01 00 01 08 02 02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00"
                                                 + "02 02 0F 05 03 00 02 02 0F 06 03 00 02 02 0F 07 03 00 02 02 0F 08 03 00 02 04 12 00 01 11 00 09 06 00"
                                                 + "00 2A 00 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00 09 06"
                                                 + "07 00 00 08 01 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00 09"
                                                 + "06 00 00 60 01 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11 00"
                                                 + "09 06 07 01 00 00 01 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01 11"
                                                 + "00 09 06 07 00 00 02 02 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 01"
                                                 + "11 00 09 06 07 00 00 02 0B FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00"
                                                 + "01 11 00 09 06 07 00 00 02 0C FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12"
                                                 + "00 07 11 01 09 06 07 00 63 01 80 FF 02 02 01 08 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03"
                                                 + "16 01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F"
                                                 + "08 16 01 00 01 04 02 02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 04 12 00"
                                                 + "03 11 00 09 06 07 00 2B 00 00 FF 02 02 01 03 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16"
                                                 + "01 00 01 01 02 02 0F 01 03 00 02 04 12 00 03 11 00 09 06 07 00 2B 02 00 FF 02 02 01 03 02 03 0F 01 16"
                                                 + "01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00 0F 11 00 09 06 00"
                                                 + "00 28 00 00 FF 02 02 01 08 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 02 03 0F 04"
                                                 + "16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 01 00 01 04 02"
                                                 + "02 0F 01 03 00 02 02 0F 02 03 00 02 02 0F 03 03 00 02 02 0F 04 03 00 02 04 12 00 11 11 00 09 06 00 00"
                                                 + "29 00 00 FF 02 02 01 02 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 01 01 02 02 0F 01 03 00 02 04 12 00"
                                                 + "17 11 01 09 06 00 00 16 00 00 FF 02 02 01 09 02 03 0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16"
                                                 + "01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02 03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08"
                                                 + "16 01 00 02 03 0F 09 16 01 00 01 00 02 04 12 00 01 11 00 09 06 00 00 60 01 0A FF 02 02 01 02 02 03 0F"
                                                 + "01 16 01 00 02 03 0F 02 16 01 00 01 00 02 04 12 00 07 11 01 09 06 07 00 63 63 00 FF 02 02 01 08 02 03"
                                                 + "0F 01 16 01 00 02 03 0F 02 16 01 00 02 03 0F 03 16 01 00 02 03 0F 04 16 01 00 02 03 0F 05 16 01 00 02"
                                                 + "03 0F 06 16 01 00 02 03 0F 07 16 01 00 02 03 0F 08 16 01 00 01 04 02 02 0F 01 03 00 02 02 0F 02 03 00"
                                                 + "02 02 0F 03 03 00 02 02 0F 04 03 00";

  /**
   * Test of decodeObject method, of class CoderDlmsData.<P> Simple test if an (big) object list can be
   * decoded without an exception.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    byte[] testBytes = CodingUtils.string2ByteArray(TEST_DATA_STRING);

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);
    assertNotNull(result);
    // System.out.println(result.toString());
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.<P> Simple test if an (big) object list can be
   * decoded without an exception.
   */
  @Test
  public void testDecodeBitString() throws Exception
  {
    System.out.println("decodeObject");
    byte[] testBytes = CodingUtils.string2ByteArray("040400002019");

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);
    assertNotNull(result);
  }

  byte[] readTestFile(String name) throws IOException
  {
    InputStream in =
            CoderDlmsDataTest.class.getResourceAsStream("/com/elster/testfiles/" + name);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String line = reader.readLine();



      while (line != null)
      {
        out.write(CodingUtils.string2ByteArray(line));
        line = reader.readLine();
      }
    }
    finally
    {
      in.close();
    }

    return out.toByteArray();


  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   * <P>
   * Test if a faulty buffer (Archive) leads to an IOException.
   */
  @Test(expected = IOException.class)
  public void testDecodeInvalidBuffer() throws Exception
  {
    System.out.println("decode invalid buffer");
    byte[] testBytes = readTestFile("invalidDlmsDataArchive.txt");
    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    instance.decodeObject(in);
    fail("Exception expected");
  }

  //241608
  @Test(expected = IOException.class)
  public void testDecodeRandom241608() throws Exception
  {
    randomDecode(241608,new CoderDlmsData());
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   * <P>
   * Test if a faulty buffer (Archive) leads to an IOException.
   */
  @Test
  public void testDecodeRandom() throws Exception
  {
    System.out.println("decode random");

    int ioExceptionCount = 0;
    CoderDlmsData instance = new CoderDlmsData();
    final Random random = new Random();
    for (int i = 0; i < 10000; i++)
    {
      final long seed = 568647L + i;
      random.setSeed(seed);

      final int length = random.nextInt(100000);
      byte[] testBytes = new byte[length];
      random.nextBytes(testBytes);
      
      long startTime2  = System.currentTimeMillis();
      try
      {
       // System.out.print("s(" + seed + ") ");
        final InputStream in = new ByteArrayInputStream(testBytes);
        instance.decodeObject(in);
      }
      catch (IOException ignore)
      {
        //IOExceptions are fine      
        ioExceptionCount++;
        if (ioExceptionCount < 20)
        {
          System.out.println("IOException seed: " + seed + " Exception: " + ignore.toString());
        }
        if (ioExceptionCount == 20)
        {
          System.out.println("...");
          System.out.println("");
        }
      }
      catch (Exception ex)
      {
        System.out.println("Failed seed: " + seed);
        throw ex;
      }
      
      long duration= System.currentTimeMillis()-startTime2;
      if (duration > 500)
      {
        System.out.println("Slow seed: " + seed+", "+duration+"ms");
      }
    }
  }

  private void randomDecode(final long seed,final CoderDlmsData instance ) throws IOException
  {
    final Random random = new Random(seed);

    final int length = random.nextInt(100000);
    byte[] testBytes = new byte[length];
    random.nextBytes(testBytes);
    final InputStream in = new ByteArrayInputStream(testBytes);
    instance.decodeObject(in);


  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.<P> Test if encoding of an decoded object list leads
   * to the same byte array as used for decoding.
   */
  @Test
  public void testDecodeEncodeObject() throws Exception
  {
    System.out.println("decode encode Object");
    byte[] testBytes = CodingUtils.string2ByteArray(TEST_DATA_STRING);

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.encodeObject(result, out);
    assertArrayEquals(testBytes, out.toByteArray());
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   */
  @Test
  public void testDecodeObject_CompactArray() throws Exception
  {
    System.out.println("decodeObject with compactArray");
    byte[] testBytes = CodingUtils.string2ByteArray("13 12 0A 11 11 22 22 33 33 44 44 55 55");

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);
    System.out.println(result.toString());

    assertEquals(DlmsData.DataType.COMPACT_ARRAY, result.getType());

    DlmsDataCompactArray compactArray = (DlmsDataCompactArray)result;

    assertNotNull(compactArray.getTypeDescription());

    assertEquals(DlmsData.DataType.LONG_UNSIGNED, compactArray.getTypeDescription().getType());

    assertEquals(5, compactArray.size());

    for (int i = 0; i < compactArray.size(); i++)
    {
      assertEquals(DlmsData.DataType.LONG_UNSIGNED, compactArray.get(i).getType());
      assertEquals(Integer.valueOf((i + 1) * 0x1111), compactArray.get(i).getValue());
    }
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   */
  @Test
  public void testDecodeObject_CompactArray2() throws Exception
  {
    System.out.println("decodeObject with compactArray");
    byte[] testBytes =
            CodingUtils.string2ByteArray(
            "13 0203 09 11 11 3C  0C07D00101FF000000FF800000 00 00  0C07D00101FF000000FF800000 00 00  0C07D00101FF000000FF800000 00 00  0C07D00101FF000000FF800000 00 00");

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);
    System.out.println(result.toString());

    assertEquals(DlmsData.DataType.COMPACT_ARRAY, result.getType());

    DlmsDataCompactArray compactArray = (DlmsDataCompactArray)result;

    assertNotNull(compactArray.getTypeDescription());

    assertEquals(DlmsData.DataType.STRUCTURE, compactArray.getTypeDescription().getType());

    TypeDescriptionStructure typeDescriptionStructure = (TypeDescriptionStructure)compactArray.
            getTypeDescription();

    assertEquals(3, typeDescriptionStructure.getElements().size());
    assertEquals(DlmsData.DataType.OCTET_STRING, typeDescriptionStructure.getElements().get(0).getType());
    assertEquals(DlmsData.DataType.UNSIGNED, typeDescriptionStructure.getElements().get(1).getType());
    assertEquals(DlmsData.DataType.UNSIGNED, typeDescriptionStructure.getElements().get(2).getType());

    assertEquals(4, compactArray.size());

    for (int i = 0; i < compactArray.size(); i++)
    {
      assertEquals(DlmsData.DataType.STRUCTURE, compactArray.get(i).getType());

      DlmsDataStructure structure = (DlmsDataStructure)compactArray.get(i);
      assertEquals(3, structure.size());

      assertEquals(DlmsData.DataType.OCTET_STRING, structure.get(0).getType());
      assertArrayEquals(CodingUtils.string2ByteArray("07D00101FF000000FF800000"), (byte[])structure.get(0).
              getValue());

      assertEquals(DlmsData.DataType.UNSIGNED, structure.get(1).getType());
      assertEquals(Integer.valueOf(0), structure.get(1).getValue());

      assertEquals(DlmsData.DataType.UNSIGNED, structure.get(2).getType());
      assertEquals(Integer.valueOf(0), structure.get(2).getValue());
    }

    System.out.println(result.toString());
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   */
  @Test
  public void testDecodeObject_CompactArray3() throws Exception
  {
    System.out.println("decodeObject with compactArray");
    byte[] testBytes =
            CodingUtils.string2ByteArray(
            "13 02 04 09 11 06 06 34  0C "
            + "07D00101FF000000FF800000 80 11111111 22222222 "
            + "00 00 33333333 44444444 "
            + "00 00 55555555 66666666 "
            + "00 00 77777777 88888888 ");

    InputStream in = new ByteArrayInputStream(testBytes);
    CoderDlmsData instance = new CoderDlmsData();
    DlmsData result = instance.decodeObject(in);

    System.out.println(result.toString());

    assertEquals(DlmsData.DataType.COMPACT_ARRAY, result.getType());

    DlmsDataCompactArray compactArray = (DlmsDataCompactArray)result;

    assertNotNull(compactArray.getTypeDescription());

    assertEquals(DlmsData.DataType.STRUCTURE, compactArray.getTypeDescription().getType());

    TypeDescriptionStructure typeDescriptionStructure = (TypeDescriptionStructure)compactArray.
            getTypeDescription();

    assertEquals(4, typeDescriptionStructure.getElements().size());
    assertEquals(DlmsData.DataType.OCTET_STRING, typeDescriptionStructure.getElements().get(0).getType());
    assertEquals(DlmsData.DataType.UNSIGNED, typeDescriptionStructure.getElements().get(1).getType());
    assertEquals(DlmsData.DataType.DOUBLE_LONG_UNSIGNED,
                 typeDescriptionStructure.getElements().get(2).getType());
    assertEquals(DlmsData.DataType.DOUBLE_LONG_UNSIGNED,
                 typeDescriptionStructure.getElements().get(3).getType());

    assertEquals(4, compactArray.size());

    for (int i = 0; i < compactArray.size(); i++)
    {
      assertEquals(DlmsData.DataType.STRUCTURE, compactArray.get(i).getType());

      DlmsDataStructure structure = (DlmsDataStructure)compactArray.get(i);
      assertEquals(4, structure.size());

      assertEquals(DlmsData.DataType.OCTET_STRING, structure.get(0).getType());
      assertEquals(DlmsData.DataType.UNSIGNED, structure.get(1).getType());
      assertEquals(DlmsData.DataType.DOUBLE_LONG_UNSIGNED, structure.get(2).getType());
      assertEquals(DlmsData.DataType.DOUBLE_LONG_UNSIGNED, structure.get(3).getType());
    }

    DlmsDataStructure entry0 = new DlmsDataStructure(
            new DlmsDataOctetString(CodingUtils.string2ByteArray("07D00101FF000000FF800000")),
            new DlmsDataUnsigned(0x80),
            new DlmsDataDoubleLongUnsigned(0x11111111),
            new DlmsDataDoubleLongUnsigned(0x22222222));
    DlmsDataStructure entry1 = new DlmsDataStructure(
            new DlmsDataOctetString(CodingUtils.string2ByteArray("")),
            new DlmsDataUnsigned(0x00),
            new DlmsDataDoubleLongUnsigned(0x33333333),
            new DlmsDataDoubleLongUnsigned(0x44444444));
    DlmsDataStructure entry2 = new DlmsDataStructure(
            new DlmsDataOctetString(CodingUtils.string2ByteArray("")),
            new DlmsDataUnsigned(0x00),
            new DlmsDataDoubleLongUnsigned(0x55555555),
            new DlmsDataDoubleLongUnsigned(0x66666666));
    DlmsDataStructure entry3 = new DlmsDataStructure(
            new DlmsDataOctetString(CodingUtils.string2ByteArray("")),
            new DlmsDataUnsigned(0x00),
            new DlmsDataDoubleLongUnsigned(0x77777777L),
            new DlmsDataDoubleLongUnsigned(0x88888888L));

    assertEquals(entry0, compactArray.get(0));
    assertEquals(entry1, compactArray.get(1));
    assertEquals(entry2, compactArray.get(2));
    assertEquals(entry3, compactArray.get(3));

    System.out.println(result.toString());
  }

  /**
   * Test of decodeObject method, of class CoderDlmsData.
   */
  @Test
  public void testEncodeObject_CompactArray() throws Exception
  {
    System.out.println("decodeObject with compactArray");
    byte[] testBytes = CodingUtils.string2ByteArray("13 12 0A 11 11 22 22 33 33 44 44 55 55");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderDlmsData instance = new CoderDlmsData();

    DlmsData[] elements = new DlmsData[5];

    for (int i = 0; i < 5; i++)
    {
      elements[i] = new DlmsDataLongUnsigned((i + 1) * 0x1111);
    }
    DlmsDataCompactArray compactArray = new DlmsDataCompactArray(elements);

    instance.encodeObject(compactArray, out);

    assertArrayEquals(testBytes, out.toByteArray());
  }

  /**
   * Test of encodeObject method, of class CoderDlmsData. <P> With type description.
   */
  @Test
  public void testEncodeObject_CompactArray2() throws Exception
  {
    System.out.println("encodeObject with compactArray");
    byte[] testBytes = CodingUtils.string2ByteArray("13 12 0A 11 11 22 22 33 33 44 44 55 55");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderDlmsData instance = new CoderDlmsData();

    DlmsData[] elements = new DlmsData[5];
    for (int i = 0; i < 5; i++)
    {
      elements[i] = new DlmsDataLongUnsigned((i + 1) * 0x1111);
    }

    DlmsDataCompactArray compactArray = new DlmsDataCompactArray(new TypeDescription(
            DlmsData.DataType.LONG_UNSIGNED), elements);


    instance.encodeObject(compactArray, out);

    assertArrayEquals(testBytes, out.toByteArray());
  }

  /**
   * Test of encodeObject method, of class CoderDlmsData. <P> with type description, without elements.
   */
  @Test
  public void testEncodeObject_CompactArray3() throws Exception
  {
    System.out.println("encodeObject with compactArray");
    byte[] testBytes = CodingUtils.string2ByteArray("13 12 00");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderDlmsData instance = new CoderDlmsData();


    DlmsDataCompactArray compactArray = new DlmsDataCompactArray(new TypeDescription(
            DlmsData.DataType.LONG_UNSIGNED), new DlmsData[0]);

    instance.encodeObject(compactArray, out);

    assertArrayEquals(testBytes, out.toByteArray());
  }

  /**
   * Test of encodeObject method, of class CoderDlmsData.
   *
   * Different types must lead to an IOException
   */
  @Test(expected = IOException.class)
  public void testEncodeObject_CompactArray4() throws Exception
  {
    System.out.println("encodeObject with compactArray");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderDlmsData instance = new CoderDlmsData();



    List<DlmsData> elements = new ArrayList<DlmsData>();


    for (int i = 0; i < 5; i++)
    {
      elements.add(new DlmsDataLongUnsigned((i + 1) * 0x1111));
    }
    for (int i = 0; i < 5; i++)
    {
      elements.add(new DlmsDataLong((i + 1) * 0x1111));
    }

    DlmsDataCompactArray compactArray = new DlmsDataCompactArray(elements.toArray(
            DlmsDataCompactArray.EMPTY_DLMS_DATA_ARRAY));

    instance.encodeObject(compactArray, out);
  }

  /**
   * Test of encodeObject method, of class CoderDlmsData.<P> With type description and wrong elements.
   */
  @Test(expected = IOException.class)
  public void testEncodeObject_CompactArray5() throws Exception
  {
    System.out.println("encodeObject with compactArray");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderDlmsData instance = new CoderDlmsData();

    DlmsData[] elements = new DlmsData[5];
    for (int i = 0; i < 5; i++)
    {
      elements[i] = new DlmsDataLong((i + 1) * 0x1111);
    }

    DlmsDataCompactArray compactArray = new DlmsDataCompactArray(new TypeDescription(
            DlmsData.DataType.LONG_UNSIGNED), elements);
    instance.encodeObject(compactArray, out);
  }

  @Test
  public void testFloat32Stability() throws Exception
  {
    System.out.println("");

    byte[] org = CodingUtils.string2ByteArray("17 3F 7F A0 51");


    CoderDlmsData instance = new CoderDlmsData();
    DlmsData data = instance.decodeObjectFromBytes(org);

    assertEquals(DlmsData.DataType.FLOAT32, data.getType());
    assertEquals(0.99854F, data.getValue());

    byte[] encoded = instance.encodeObjectToBytes(data);
    assertArrayEquals(org, encoded);
  }

  @Test
  public void testFloatingPointStability() throws Exception
  {
    System.out.println("");

    byte[] org = CodingUtils.string2ByteArray("07 3F 7F A0 51");


    CoderDlmsData instance = new CoderDlmsData();
    DlmsData data = instance.decodeObjectFromBytes(org);

    assertEquals(DlmsData.DataType.FLOATING_POINT, data.getType());
    assertEquals(0.99854F, data.getValue());

    byte[] encoded = instance.encodeObjectToBytes(data);
    assertArrayEquals(org, encoded);
  }

  @Test
  public void testUInt16() throws Exception
  {
    System.out.println("");

    byte[] org = CodingUtils.string2ByteArray("12 F0 26"); //Example from A-XDR Exceprt "DLMS UA 1001-2 ed.1" p. 10


    CoderDlmsData instance = new CoderDlmsData();
    DlmsData data = instance.decodeObjectFromBytes(org);

    assertEquals(DlmsData.DataType.LONG_UNSIGNED, data.getType());
    assertEquals(61478, data.getValue());

    byte[] encoded = instance.encodeObjectToBytes(data);
    assertArrayEquals(org, encoded);
  }

  @Test
  public void testSmsContent1() throws Exception
  {
    System.out.println("");

    final byte[] smsContent = CodingUtils.string2ByteArray(
            "01 01 02 09 11 06 06 07 DC 05 1E 06 00 00 0E 10 12 FF 00 0F 00 16 12 06 3A DE 68 B1 09 06 FF FF FF FF FF FF"
            + "13 12 30"
            + "00 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 09 00 0A 00 0B 00 0C"
            + "10 01 10 02 10 03 10 04 10 05 10 06 10 07 10 08 10 09 10 0A 10 0B 10 0C");


    final CoderDlmsData instance = new CoderDlmsData();
    final DlmsData data = instance.decodeObjectFromBytes(smsContent);

    assertEquals(DlmsData.DataType.ARRAY, data.getType());
    final DlmsDataArray array = (DlmsDataArray)data;
    assertEquals(1, array.size());
    assertEquals(DlmsData.DataType.STRUCTURE, array.get(0).getType());
    final DlmsDataStructure structure = (DlmsDataStructure)array.get(0);
    assertEquals(9, structure.size());


  }

}
