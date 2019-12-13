/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.types.basic;

import com.elster.dlms.types.basic.ServiceInvocationId.Priority;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class ServiceInvocationIdTest {


  /**
   * Test of getInvokeID method, of class ServiceInvocationId.
   */
  @Test
  public void testCreate1()
  {
    System.out.println("create 1");
    ServiceInvocationId instance = new ServiceInvocationId(178);
    assertEquals(11,instance.getInvokeID());
    assertEquals(ServiceClass.CONFIRMED,instance.getServiceClass());
    assertEquals(Priority.NORMAL ,instance.getPriority());
  }

   /**
   * Test of getInvokeID method, of class ServiceInvocationId.
   */
  @Test
  public void testCreate2()
  {
    System.out.println("create 2");
    ServiceInvocationId instance = new ServiceInvocationId(179);
    assertEquals(11,instance.getInvokeID());
    assertEquals(ServiceClass.CONFIRMED,instance.getServiceClass());
    assertEquals(Priority.HIGH ,instance.getPriority());
  }


   /**
   * Test of getInvokeID method, of class ServiceInvocationId.
   */
  @Test
  public void testCreate3()
  {
    System.out.println("create 3");
    ServiceInvocationId instance = new ServiceInvocationId(176);
    assertEquals(11,instance.getInvokeID());
    assertEquals(ServiceClass.UNCONFIRMED,instance.getServiceClass());
    assertEquals(Priority.NORMAL ,instance.getPriority());
  }


  /**
   * Test of getInvokeID method, of class ServiceInvocationId.
   */
  @Test
  public void testCreate4()
  {
    System.out.println("create 4");
    ServiceInvocationId instance = new ServiceInvocationId(66);
    assertEquals(4,instance.getInvokeID());
    assertEquals(ServiceClass.CONFIRMED,instance.getServiceClass());
    assertEquals(Priority.NORMAL ,instance.getPriority());
  }


   /**
   * Test of getInvokeID method, of class ServiceInvocationId.
   */
  @Test
  public void testCreate5()
  {
    System.out.println("create 5");
    ServiceInvocationId instance = new ServiceInvocationId(4,Priority.HIGH,ServiceClass.CONFIRMED);
    assertEquals(4,instance.getInvokeID());
    assertEquals(ServiceClass.CONFIRMED,instance.getServiceClass());
    assertEquals(Priority.HIGH ,instance.getPriority());
  }


  /**
  * Test of getInvokeID method, of class ServiceInvocationId.
  */
  @Test
  public void testCreate6()
  {
    System.out.println("create 6");
    ServiceInvocationId instance = new ServiceInvocationId(15,Priority.NORMAL,ServiceClass.UNCONFIRMED);
    assertEquals(15,instance.getInvokeID());
    assertEquals(ServiceClass.UNCONFIRMED,instance.getServiceClass());
    assertEquals(Priority.NORMAL ,instance.getPriority());
  }

    /**
  * Test of getInvokeID method, of class ServiceInvocationId.
  */
  @Test
  public void testCreate7()
  {
    System.out.println("create 7");
    ServiceInvocationId instance = new ServiceInvocationId(0,Priority.NORMAL,ServiceClass.UNCONFIRMED);
    assertEquals(0,instance.getInvokeID());
    assertEquals(ServiceClass.UNCONFIRMED,instance.getServiceClass());
    assertEquals(Priority.NORMAL ,instance.getPriority());
  }
  
  
      /**
  * Test of getInvokeID method, of class ServiceInvocationId.
  */
 // @Test
  public void disabledTestCreate1Adm2() //There was a change of the specification in the GB.
  {
    //-- Examples for the Invoke-Id-And-Priority byte:
    //-- Priority = High, Service-class = unconfirmed, Invoke-id = 1: value = 0x81
    //-- Priority = High, Service-class = confirmed, Invoke-id = 1: value = 0xC1
    //-- Priority = Normal, Service-class = confirmed, Invoke-id = 1: value = 0x41
    
    System.out.println("create 1 (acc. DLMS UA 1000-2 ed.7 Amd. 2)");
    ServiceInvocationId instance = new ServiceInvocationId(0x81);
    assertEquals(1,instance.getInvokeID());
    assertEquals(ServiceClass.UNCONFIRMED,instance.getServiceClass());
    assertEquals(Priority.HIGH ,instance.getPriority());
  }


 
}