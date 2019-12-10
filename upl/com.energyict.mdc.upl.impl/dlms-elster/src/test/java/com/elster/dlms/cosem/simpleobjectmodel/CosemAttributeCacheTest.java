/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CosemAttributeCacheTest
{
  public CosemAttributeCacheTest()
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
  
  private static final CosemAttributeDescriptor AD1A2= new CosemAttributeDescriptor(new ObisCode("1.0.0.0.0.0"), 1, 2);
  private static final CosemAttributeDescriptor AD1A2_B= new CosemAttributeDescriptor(new ObisCode("1.0.0.0.0.0"), 1, 2);
  private static final CosemAttributeDescriptor AD1A3= new CosemAttributeDescriptor(new ObisCode("1.0.0.0.0.0"), 1, 3);
  private static final CosemAttributeDescriptor AD1A3_B= new CosemAttributeDescriptor(new ObisCode("1.0.0.0.0.0"), 1, 3);
  private static final CosemAttributeDescriptor AD2A2= new CosemAttributeDescriptor(new ObisCode("2.0.0.0.0.0"), 1, 2);
  private static final CosemAttributeDescriptor AD2A2_B= new CosemAttributeDescriptor(new ObisCode("2.0.0.0.0.0"), 1, 2);
  private static final CosemAttributeDescriptor AD2A3= new CosemAttributeDescriptor(new ObisCode("2.0.0.0.0.0"), 1, 3);
  private static final CosemAttributeDescriptor AD2A3_B= new CosemAttributeDescriptor(new ObisCode("2.0.0.0.0.0"), 1, 3);
  private static final CosemAttributeDescriptor AD3A2= new CosemAttributeDescriptor(new ObisCode("3.0.0.0.0.0"), 1, 2);
  
  private static final GetDataResult DATA_1= new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataInteger(1));
  private static final GetDataResult DATA_2= new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataInteger(2));
  private static final GetDataResult DATA_3= new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataInteger(3));
  private static final GetDataResult DATA_4= new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataInteger(4));
  private static final GetDataResult DATA_5= new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataInteger(5));
 
  /**
   * Test of getDataResult method, of class CosemAttributeCache.
   */
  @Test
  public void test1()
  {
    System.out.println("test put, get, delete");
    final CosemAttributeCache instance = new CosemAttributeCache();
    
    instance.putDataResult(AD1A2, DATA_1); //put
    instance.putDataResult(AD1A2_B, DATA_2); //put other data on the same attribute
    instance.putDataResult(AD1A3, DATA_3); //some more data
    instance.putDataResult(AD2A2, DATA_4); //some more data
    instance.putDataResult(AD2A3, DATA_5); //some more data
    
    assertEquals(DATA_2, instance.getDataResult(AD1A2));
    assertEquals(DATA_3, instance.getDataResult(AD1A3_B));
    assertEquals(DATA_4, instance.getDataResult(AD2A2_B));
    assertEquals(DATA_5, instance.getDataResult(AD2A3_B));
    assertNull( instance.getDataResult(AD3A2));
    
    instance.deleteAttributes(AD1A2.getInstanceId());
    
    assertNull(instance.getDataResult(AD1A2));
    assertNull(instance.getDataResult(AD1A3_B));
    assertEquals(DATA_4, instance.getDataResult(AD2A2_B));
    assertEquals(DATA_5, instance.getDataResult(AD2A3_B));
    assertNull( instance.getDataResult(AD3A2));
    
    instance.deleteAttribute(AD2A3.getInstanceId(),3);
    
    assertNull(instance.getDataResult(AD1A2));
    assertNull(instance.getDataResult(AD1A3_B));
    assertEquals(DATA_4, instance.getDataResult(AD2A2_B));
    assertNull(instance.getDataResult(AD2A3_B));
    assertNull(instance.getDataResult(AD3A2));
    
  }

}
