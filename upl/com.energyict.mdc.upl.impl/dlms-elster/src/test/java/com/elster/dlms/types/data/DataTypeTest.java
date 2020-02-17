/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class DataTypeTest
{
  /**
   */
  @Test
  public void testFindDataType()
  {

    //All types must be found
    for (DataType t : DataType.values())
    {
      assertEquals(t, DataType.findDataType(t.getTag()));
    }

    //Others must return "null"
    assertNull(DataType.findDataType(-1));
    assertNull(DataType.findDataType(8)); //Gap between FLOATING_POINT (7) and OCTET_STRING (9)
    assertNull(DataType.findDataType(28));
    assertNull(DataType.findDataType(254));
    assertNull(DataType.findDataType(256));
    assertNull(DataType.findDataType(10000));

  }

  /**
   */
  @Test
  public void testTagUniqueness()
  {
    final Set<Integer> tags = new HashSet<Integer>();


    for (DataType t : DataType.values())
    {
      assertFalse("Double tag found: " + t, tags.contains(t.getTag()));
      tags.add(t.getTag());
    }
    assertEquals(DataType.values().length, tags.size());
  }

  /**
   */
  @Test
  public void testTags()
  {
    assertEquals(18, DataType.LONG_UNSIGNED.getTag());
    assertEquals(20, DataType.LONG64.getTag());
  }

}
