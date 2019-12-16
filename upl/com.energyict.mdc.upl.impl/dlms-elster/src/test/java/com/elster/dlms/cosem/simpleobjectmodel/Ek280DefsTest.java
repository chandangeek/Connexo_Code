/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import java.util.Set;
import com.elster.dlms.types.basic.ObisCode;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class Ek280DefsTest
{
  public Ek280DefsTest()
  {
    //--
  }

  @Test
  public void testUniquenessOfDefs()
  {

    final Set<ObisCode> codes = new HashSet<ObisCode>();

    for (SimpleCosemObjectDefinition def : Ek280Defs.DEFINITIONS)
    {
      if (codes.contains(def.getLogicalName()))
      {
        fail("Double definition for "+def.getLogicalName());
      }
      else
      {
        codes.add(def.getLogicalName());
      }
    }



  }

}
