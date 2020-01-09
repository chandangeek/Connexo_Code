/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.objectmodel;

import java.util.Date;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.classes.class07.EntryDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.objectmodel.AbstractCosemDataNode.ReadState;
import com.elster.dlms.cosem.objectmodel.AbstractCosemDataNode.WriteState;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsDataBoolean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CosemAttributeTest
{
  public CosemAttributeTest()
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
   * Test of setData method, of class CosemAttribute.
   */
  @Test
  public void testSetData()
  {
    System.out.println("setData");

    CosemAttribute instance = new CosemAttribute(new CosemObject(new LogicalDevice(1),new ObisCode(1,2,3,4,5,6),1,1), 1,
                                                 AttributeAccessMode.READ_AND_WRITE, null, null);

    instance.setStoredData(null,DataAccessResult.SUCCESS , ReadState.OK,WriteState.UNWRITTEN,null);
    assertEquals(false, instance.isChanged());
    instance.setStoredData(null,DataAccessResult.SUCCESS , ReadState.OK, WriteState.UNWRITTEN,null);
    assertEquals(false, instance.isChanged());

    instance.setStoredData(new DlmsDataBoolean(true),DataAccessResult.SUCCESS , ReadState.OK, WriteState.UNWRITTEN,new Date());
    assertEquals(false, instance.isChanged());

    instance.setData(new DlmsDataBoolean(true));
    assertEquals(false, instance.isChanged());

    instance.setData(new DlmsDataBoolean(false));
    assertEquals(true, instance.isChanged());

    instance.setData(new DlmsDataBoolean(true));
    assertEquals(false, instance.isChanged());
  }
  
  
  private boolean notified=false;

  /**
   * Test of setData method, of class CosemAttribute.
   */
  @Test
  public void testSetDataNotification()
  {
    System.out.println("setData");
    CosemAttribute instance = new CosemAttribute(new CosemObject(new LogicalDevice(1),new ObisCode(1,2,3,4,5,6),1,1), 1,
                                                 AttributeAccessMode.READ_AND_WRITE, null, null);

    instance.addPropertyChangeListener(new PropertyChangeListener() {

      //@Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        notified=true;
      }
    });

    instance.setStoredData(new DlmsDataBoolean(true),DataAccessResult.SUCCESS , ReadState.OK, WriteState.UNWRITTEN, new Date());
    assertTrue(notified);
    notified=false;
    instance.setStoredData(new DlmsDataBoolean(true),DataAccessResult.SUCCESS , ReadState.OK, WriteState.UNWRITTEN, new Date());
    assertFalse(notified);
    notified=false;
    instance.setStoredData(new DlmsDataBoolean(false),DataAccessResult.SUCCESS , ReadState.OK, WriteState.UNWRITTEN, new Date());
    assertTrue(notified);
    notified=false;
    instance.setStoredData(null,DataAccessResult.HARDWARE_FAULT , ReadState.OK, WriteState.UNWRITTEN, new Date());
    assertTrue(notified);
    notified=false;

  }


  /**
   * Test of getCosemAttributeDescriptor method, of class CosemAttribute.
   */
  @Test
  public void testGetCosemAttributeDescriptor()
  {
    System.out.println("getCosemAttributeDescriptor");
    CosemObject cosemObject =
            new CosemObject(new LogicalDevice(1),new ObisCode(1, 2, 3, 4, 5, 6),7,1);

    CosemAttribute instance = new CosemAttribute(cosemObject, 10,
                                                 AttributeAccessMode.AUTHENTICATED_READ_AND_WRITE, null,
                                                 new int[]
            {
              1, 2, 3
            });
    CosemAttributeDescriptor expResult = new CosemAttributeDescriptor(new ObisCode(1, 2, 3, 4, 5, 6), 7, 10);
    CosemAttributeDescriptor result = instance.getCosemAttributeDescriptor();
    assertEquals(expResult.getAccessSelectionParameters(), result.getAccessSelectionParameters());
    assertEquals(expResult.getAttributeId(), result.getAttributeId());
    assertEquals(expResult.getClassId(), result.getClassId());
    assertEquals(expResult.getInstanceId(), result.getInstanceId());
  }

  /**
   * Test of getCosemAttributeDescriptor method, of class CosemAttribute.
   */
  @Test
  public void testGetCosemAttributeDescriptor_withSelectiveAccess()
  {
    System.out.println("getCosemAttributeDescriptor");
    CosemObject cosemObject = new CosemObject(new LogicalDevice(1),new ObisCode(1, 2, 3, 4, 5, 6),7,1);

    CosemAttribute instance = new CosemAttribute(cosemObject, 10,
                                                 AttributeAccessMode.AUTHENTICATED_READ_AND_WRITE, null,
                                                 new int[]
            {
              1, 2, 3
            });
    EntryDescriptor entryDescriptor1 = new EntryDescriptor(0, 1, 2, 3);
    instance.putAccessSelector(entryDescriptor1);
    instance.setActiveAccessSelector(entryDescriptor1);

    EntryDescriptor entryDescriptor = new EntryDescriptor(0, 1, 2, 3);

    CosemAttributeDescriptor expResult = new CosemAttributeDescriptor(new ObisCode(1, 2, 3, 4, 5, 6), 7, 10, new AccessSelectionParameters(entryDescriptor.
            getId(), entryDescriptor.toDlmsData()));
    CosemAttributeDescriptor result = instance.getCosemAttributeDescriptor();

    assertNotNull(expResult.getAccessSelectionParameters());
    assertNotNull(result.getAccessSelectionParameters());
     
    
    assertEquals(expResult.getAccessSelectionParameters().getClass(), result.getAccessSelectionParameters().getClass());
    assertEquals(expResult.getAccessSelectionParameters().getSelector(), result.getAccessSelectionParameters().getSelector());
    assertEquals(expResult.getAccessSelectionParameters().getAccessParameters(), result.getAccessSelectionParameters().getAccessParameters());
    assertEquals(expResult.getAttributeId(), result.getAttributeId());
    assertEquals(expResult.getClassId(), result.getClassId());
    assertEquals(expResult.getInstanceId(), result.getInstanceId());
  }

}
