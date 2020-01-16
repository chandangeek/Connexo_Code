/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.protocols.dataformat;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class Table7E1Test {

  

  //Tests all bytes of the table (using the old long implementation.)
  @Test
  public void testTable()
  {
    byte[] table= Table7E1.getTable();
    
    
    for (int i = 0; i < 128; i++)
    {
      boolean isEven=true;

      for (int j = 0; j < 7; j++)
      {
        if (0 != ((i >> j) & 1))
        {
          isEven = !isEven;
        }
      }

      if (isEven)
      {
        assertEquals(i,table[i]);
      }
      else
      {
        assertEquals((byte) (i | 0x80),table[i]);
      }
    }
  }

}