/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.cosem.classes.class3;

import com.elster.dlms.cosem.classes.class03.Unit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class UnitTest {


  /**
   * Test of findById method, of class Unit.
   */
  @Test
  public void testFindById()
  {

    for (Unit u: Unit.values())
    {
      assertEquals(u, Unit.findById(u.getId()));
    }

    assertNull(Unit.findById(0));
    assertNull(Unit.findById(66));
    assertNull(Unit.findById(69));
    assertNull(Unit.findById(71));
    assertNull(Unit.findById(252));
    assertNull(Unit.findById(256));
    assertNull(Unit.findById(953));
  }

}