/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.cosem.application.services.open;

import com.elster.coding.CodingUtils;
import java.io.UnsupportedEncodingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class AuthenticationValueTest {

    public AuthenticationValueTest() {
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
   * Test of createCharstring method, of class AuthenticationValue.
   */
  @Test
  public void testCreateCharstring() throws UnsupportedEncodingException
  {
    System.out.println("createCharstring");
    
//    byte[] byteForm= CodingUtils.string2ByteArray("EA D4 26 08 3A A6 B4 54 4A ED DC FB 47 44 AA 8E AF 0A 61 58 A8 D2 06 B0 60 51 E6 55 3F C5 21 B3 F6 41 6E 39 3E EC A6 6F EF 35 6D AC 06 09 EC 30 B9 6C 5F 2B 83 2E 05 F7 B8 3B 8C FC 66 91 1A 7B");
    byte[] byteForm= CodingUtils.string2ByteArray("30 31 32");

    AuthenticationValue result = AuthenticationValue.createCharstring(new String(byteForm,"ASCII"));

    byte[] back= result.toBytes();

    assertArrayEquals(byteForm, back);
  }

 
}