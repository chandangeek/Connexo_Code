/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.types.data;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.BitString;
import java.text.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDataParserTest {

    public DlmsDataParserTest() {
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
   * Simple try to get an instance.
   */
  @Test
  public void testGetInstance()
  {
    System.out.println("getInstance");
    DlmsDataParser result = DlmsDataParser.getInstance();
    assertNotNull(result);
  }
  
  @Test
  public void testParseBitSting() throws ParseException
  {
    System.out.println("Parse bit string");
    DlmsDataParser instance= DlmsDataParser.getInstance();
    DlmsData result = instance.parse(DlmsData.DataType.BIT_STRING, "0000 0001 0");
    DlmsData expected= new DlmsDataBitString(new BitString(9, CodingUtils.string2ByteArray("01 00")));
    assertEquals(expected , result);
  }
  
  
  @Test
  public void testParseBitSting2() throws ParseException
  {
    System.out.println("Parse bit string 2");
    DlmsDataParser instance= DlmsDataParser.getInstance();
    DlmsData result = instance.parse(DlmsData.DataType.BIT_STRING, "0000 0001 1111 0000 1");
    DlmsData expected= new DlmsDataBitString(new BitString(17, CodingUtils.string2ByteArray("01 F0 80")));
    assertEquals(expected , result);
  }
  
  
  @Test
  public void testFormatBitSting() throws ParseException
  {
    System.out.println("Format bit string 2");
    DlmsDataParser instance= DlmsDataParser.getInstance();
    String result = instance.format( new DlmsDataBitString(new BitString(17, CodingUtils.string2ByteArray("01 F0 80"))));
    String expected =     "00000001 11110000 1";     
    assertEquals(expected , result);
  }
  
 
}