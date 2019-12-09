/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.objectmodel;

import java.util.List;
import com.elster.dlms.cosem.objectmodel.CosemObjectList.Listener;
import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Osse
 */
public class CosemObjectListTest
{
  private final LogicalDevice parent = new LogicalDevice(1);
  private final CosemObject testObject1C1 = new CosemObject(parent, new ObisCode(1, 2, 3, 4, 5, 1), 1, 1);
  private final CosemObject testObject1C2 = new CosemObject(parent, new ObisCode(1, 2, 3, 4, 5, 1), 2, 1);
  private final CosemObject testObject2C1 = new CosemObject(parent, new ObisCode(1, 2, 3, 4, 5, 2), 1, 1);
  //private final CosemObject testObject2C1b = new CosemObject(parent, new ObisCode(1, 2, 3, 4, 5, 2), 1, 1);

  public CosemObjectListTest()
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
   * Test of add method, of class CosemObjectList.
   */
  @Test
  public void testAdd()
  {
    System.out.println("add");
    CosemObjectList instance = new CosemObjectList();
    instance.add(testObject1C1);
    instance.add(testObject1C2);
    instance.add(testObject2C1);

    assertEquals(3, instance.size());

    assertSame(testObject1C1, instance.find(testObject1C1.getLogicalName(), testObject1C1.getCosemClassId()));
    assertSame(testObject1C2, instance.find(testObject1C2.getLogicalName(), testObject1C2.getCosemClassId()));
    assertSame(testObject2C1, instance.find(testObject2C1.getLogicalName(), testObject2C1.getCosemClassId()));
  }

  /**
   * Test of find method, of class CosemObjectList.
   */
  @Test
  public void testFind_ObisCode_int()
  {
    System.out.println("find");

    CosemObjectList instance = new CosemObjectList();

    instance.add(testObject1C1);
    instance.add(testObject1C2);
    instance.add(testObject2C1);

    assertEquals(3, instance.size());

    assertSame(testObject1C1, instance.find(testObject1C1.getLogicalName(), testObject1C1.getCosemClassId()));
    assertSame(testObject1C2, instance.find(testObject1C2.getLogicalName(), testObject1C2.getCosemClassId()));
    assertSame(testObject2C1, instance.find(testObject2C1.getLogicalName(), testObject2C1.getCosemClassId()));

    assertNull(instance.find(testObject1C1.getLogicalName(), 3));
    assertNull(instance.find(testObject2C1.getLogicalName(), 2));
  }

  /**
   * Test of find method, of class CosemObjectList.
   */
  @Test
  public void testFind_ObisCode()
  {
    System.out.println("find");

    CosemObjectList instance = new CosemObjectList();

    instance.add(testObject1C1);
    instance.add(testObject1C2);
    instance.add(testObject2C1);

    assertEquals(3, instance.size());

    assertSame("The object with the highest class id must be returned", testObject1C2,
               instance.find(testObject1C2.getLogicalName()));
    assertSame(testObject2C1, instance.find(testObject2C1.getLogicalName()));

    assertNull(instance.find(new ObisCode(7, 7, 7, 7, 7, 7)));
  }

  /**
   * Test of find method, of class CosemObjectList. (Changed add order)
   */
  @Test
  public void testFind_ObisCode2()
  {
    System.out.println("find");

    CosemObjectList instance = new CosemObjectList();

    instance.add(testObject1C2);
    instance.add(testObject1C1); //switched add order
    instance.add(testObject2C1);

    assertEquals(3, instance.size());

    assertSame("The object with the highest class id must be returned", testObject1C2,
               instance.find(testObject1C2.getLogicalName()));
    assertSame(testObject2C1, instance.find(testObject2C1.getLogicalName()));

    assertNull(instance.find(new ObisCode(7, 7, 7, 7, 7, 7)));
  }

  /**
   * Test of iterator method, of class CosemObjectList.
   */
  @Test
  public void testIterator()
  {
    System.out.println("iterator");


    CosemObjectList instance = new CosemObjectList();

    instance.add(testObject1C1);
    instance.add(testObject1C2);
    instance.add(testObject2C1);

    Iterator result = instance.iterator();
    assertSame(testObject1C1, result.next());
    assertSame(testObject1C2, result.next());
    assertSame(testObject2C1, result.next());
    assertFalse(result.hasNext());
  }

  /**
   * Test of addListener method, of class CosemObjectList.
   */
  @Test
  public void testAddListener()
  {
    System.out.println("addListener");

    final List<CosemObject> addedObjects = new ArrayList<CosemObject>();
    final List<CosemObject> removedObjects = new ArrayList<CosemObject>();



    Listener listener = new Listener()
    {
      //@Override
      public void objectAdded(CosemObjectList sender, CosemObject object)
      {
        addedObjects.add(object);
      }

      //@Override
      public void objectRemoved(CosemObjectList sender, CosemObject object)
      {
        removedObjects.add(object);
      }

    };

    CosemObjectList instance = new CosemObjectList();
    instance.addListener(listener);

    instance.add(testObject1C1);
    instance.add(testObject1C2);
    instance.add(testObject2C1);

    assertEquals(3, addedObjects.size());

    assertSame(testObject1C1, addedObjects.get(0));
    assertSame(testObject1C2, addedObjects.get(1));
    assertSame(testObject2C1, addedObjects.get(2));
  }

  /**
   * Test of removeListener method, of class CosemObjectList.
   */
  @Test
  public void testRemoveListener()
  {
    System.out.println("removeListener");

    final List<CosemObject> notifiedObjects = new ArrayList<CosemObject>();



    Listener listener = new Listener()
    {
      //@Override
      public void objectAdded(CosemObjectList sender, CosemObject object)
      {
        notifiedObjects.add(object);
      }

      //@Override
      public void objectRemoved(CosemObjectList sender, CosemObject object)
      {
        notifiedObjects.add(object);
      }

    };

    CosemObjectList instance = new CosemObjectList();
    instance.addListener(listener);

    instance.add(testObject1C1);

    instance.removeListener(listener);

    instance.add(testObject1C2);
    instance.add(testObject2C1);
    assertEquals(1, notifiedObjects.size());
    assertSame(testObject1C1, notifiedObjects.get(0));
  }

}
