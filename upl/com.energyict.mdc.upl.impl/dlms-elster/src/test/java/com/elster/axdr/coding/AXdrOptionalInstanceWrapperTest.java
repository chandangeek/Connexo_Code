/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.axdr.coding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class AXdrOptionalInstanceWrapperTest {

    public AXdrOptionalInstanceWrapperTest() {
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
   * Test of getWrapped method, of class AXdrCoderOptionalValueWrapper.
   */
  @Test
  public void testGetEncode() throws IOException
  {
    System.out.println("encode");
    AXdrCoderOptionalValueWrapper<Integer> instance = new AXdrCoderOptionalValueWrapper<Integer>(new AXdrCoderInteger8());

    ByteArrayOutputStream out= new ByteArrayOutputStream();
    instance.encodeObject(15, out);

    byte[] expts= new byte[]{(byte)1,(byte) 15};

    assertArrayEquals(expts, out.toByteArray());

  }

}