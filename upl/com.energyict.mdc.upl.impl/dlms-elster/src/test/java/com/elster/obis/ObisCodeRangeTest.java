/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.obis.ObisCodeRange.GroupRange;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class ObisCodeRangeTest
{

  /**
   */
  @Test
  public void testSingleRangeParse()
  {
    System.out.println("SingleRange.parse");
    final GroupRange instance1 = GroupRange.parse("3-17");
    assertFalse(GroupRange.valueOf(3, 18).equals(instance1));
    
    final GroupRange instance2 = GroupRange.parse("*");
    assertEquals(GroupRange.valueOf(0, 255), instance2);

    final GroupRange instance3 = GroupRange.parse("5");
    assertEquals(GroupRange.valueOf(5), instance3);
  }

  
  /**
   */
  @Test
  public void testRangeListParse()
  {
    System.out.println("RangeList.parse");
    final GroupRange instance1 = GroupRange.parse("[1,3-17,100-110]");
    assertEquals(
      GroupRange.valueOf(GroupRange.valueOf(1),GroupRange.valueOf(3,17),GroupRange.valueOf(100,110)), instance1);
  }
    
  /**
   */
  @Test
  public void testParse()
  {
    System.out.println("parse");
    final ObisCodeRange instance1 = ObisCodeRange.parse("1-7.[1,3-17,100-110].3.4.5-7.*");
    
    assertTrue(instance1.contains(new ObisCode(1,109,3,4,5,254)));
    assertFalse(instance1.contains(new ObisCode(1,2,3,4,5,254)));
    assertFalse(instance1.contains(new ObisCode(8,109,3,4,5,254)));
  }



  
}
