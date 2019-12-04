/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.elster.coding.CodingUtils;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class FcsBuilderTest
{
  public FcsBuilderTest()
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

  @Test
  public void testFcsTable()
  {
    assertEquals(0, 0xFFFF & Fcs16Builder.getTableEntry(0));
    assertEquals(0x1189, 0xFFFF & Fcs16Builder.getTableEntry(1));
    assertEquals(0xFFCF, 0xFFFF & Fcs16Builder.getTableEntry(15 * 8));
    assertEquals(0x0318, 0xFFFF & Fcs16Builder.getTableEntry(6 * 8 + 3));
    assertEquals(0x0F78, 0xFFFF & Fcs16Builder.getTableEntry(255));
  }

  /**
   * Test of updateFcs16 method, of class Fcs16Builder.
   */
  @Test
  public void testUpdateFcs16_4args()
  {
    System.out.println("updateFcs16");
    byte[] data = CodingUtils.string2ByteArray("E0 E0 A0 47 00 02 60 4D 41 10 25 C7 E6 E6 00 60 36 A1 09 06 07 60 85 74 05 08 01 01 8A 02 07 80 8B 07 "
                                                        + "60 85 74 05 08 02 01 AC 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 00 00 "
                                                        + "1D FF FF 24 67");
    //byte[] dataShort = CodingUtils.string2ByteArray("A0 0A 00 02 60 4D 41 93 FF 98 ");
    int fcs = Fcs16Builder.INITIAL_VALUE;
    int offset = 2;
    int len = data.length-2;

    int expResult = Fcs16Builder.GOOD_CHECKSUM;

    int result = Fcs16Builder.updateFcs16(fcs, data, offset, len);
    assertEquals(expResult, result);
  }

  /**
   * Test of updateFcs16 method, of class Fcs16Builder.
   */
  @Test
  public void testUpdateFcs16_int_int()
  {
    System.out.println("updateFcs16");

    byte[] data = CodingUtils.string2ByteArray("A0 0A 00 02 60 4D 41 93 FF 98 ");
    int fcs = Fcs16Builder.INITIAL_VALUE;
    int len = data.length;
    int expResult = Fcs16Builder.GOOD_CHECKSUM;

    int result = fcs;
    for (int i=0 ; i<len; i++)
    {
      result = Fcs16Builder.updateFcs16(result, 0xFF & data[i]);
    }

    assertEquals(expResult, result);
  }
  
  
    /**
   * Test of updateFcs16 method, of class Fcs16Builder.
   */
  @Test
  public void testUpdateFcs16_int_int2()
  {
    System.out.println("updateFcs16");

    byte[] data = CodingUtils.string2ByteArray("A0 23 41 00 02 60 4D 73 4F B8 81 80 14 05 02 00 80 06 02 00 80 07 04 00 00 00 01 08 04 00 00 00 01 CE 6A");
    int fcs = Fcs16Builder.INITIAL_VALUE;
    int len = data.length;
    int expResult = Fcs16Builder.GOOD_CHECKSUM;

    int result = fcs;
    for (int i=0 ; i<len; i++)
    {
      result = Fcs16Builder.updateFcs16(result, 0xFF & data[i]);
    }

    assertEquals(expResult, result);
  }




}
