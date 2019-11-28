/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.protocols;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ProtocolBridgeTest {

    public ProtocolBridgeTest() {
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
   * Test of getSideA method, of class ProtocolBridge.
   */
  @Test
  public void testDataTransfer() throws IOException
  {
    System.out.println("data transfer");
    ProtocolBridge instance = new ProtocolBridge();
    //A -> B
    instance.getSideA().getOutputStream().write(254);
    assertEquals(1,   instance.getSideB().getInputStream().available());
    assertEquals(254,   instance.getSideB().getInputStream().read());
    
    //B -> A
    instance.getSideB().getOutputStream().write(250);
    assertEquals(1,   instance.getSideA().getInputStream().available());
    assertEquals(250,   instance.getSideA().getInputStream().read());

  }


}