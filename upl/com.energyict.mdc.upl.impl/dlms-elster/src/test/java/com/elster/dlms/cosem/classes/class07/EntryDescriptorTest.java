/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class EntryDescriptorTest {

    public EntryDescriptorTest() {
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
   * Test of filterActiveObjectDefinitions method, of class EntryDescriptor.
   */
  @Test
  public void testFilterActiveObjectDefinitions()
  {
    System.out.println("filterActiveObjectDefinitions");
    final List<CaptureObjectDefinition> all = new ArrayList<CaptureObjectDefinition>();

    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,1), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,2), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,3), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,4), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,5), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,6), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,7), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,8), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,9), 2, 0));

    final EntryDescriptor instance = new EntryDescriptor(1, 100, 2, 5);
    final List<CaptureObjectDefinition> result = instance.filterActiveObjectDefinitions(all);

    assertEquals(4, result.size());

    assertEquals(2, result.get(0).getLogicalName().getF());
    assertEquals(3, result.get(1).getLogicalName().getF());
    assertEquals(4, result.get(2).getLogicalName().getF());
    assertEquals(5, result.get(3).getLogicalName().getF());
  }


    /**
   * Test of filterActiveObjectDefinitions method, of class EntryDescriptor.
   */
  @Test
  public void testFilterActiveObjectDefinitions2()
  {
    System.out.println("filterActiveObjectDefinitions 2");
    final List<CaptureObjectDefinition> all = new ArrayList<CaptureObjectDefinition>();

    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,1), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,2), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,3), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,4), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,5), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,6), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,7), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,8), 2, 0));
    all.add(new CaptureObjectDefinition(3, new ObisCode(1,0,0,0,0,9), 2, 0));


    EntryDescriptor instance = new EntryDescriptor(1, 100, 2, 0);
    List<CaptureObjectDefinition> result = instance.filterActiveObjectDefinitions(all);

    assertEquals(8, result.size());

    assertEquals(2, result.get(0).getLogicalName().getF());
    assertEquals(3, result.get(1).getLogicalName().getF());
    assertEquals(4, result.get(2).getLogicalName().getF());
    assertEquals(5, result.get(3).getLogicalName().getF());
    assertEquals(6, result.get(4).getLogicalName().getF());
    assertEquals(7, result.get(5).getLogicalName().getF());
    assertEquals(8, result.get(6).getLogicalName().getF());
    assertEquals(9, result.get(7).getLogicalName().getF());


  }


}