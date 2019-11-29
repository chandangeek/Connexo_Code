/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/dataformat/Table7E1.java $
 * Version:     
 * $Id: Table7E1.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 09:51:32
 */

package com.elster.protocols.dataformat;

/**
 * This class provides an 7E1 parity table.
 *
 * @author osse
 */
public final class Table7E1
{

  private Table7E1()
  {
  }
  
  
  /**
   * An 128 byte table with TABLE_7E1[i]==byteWith7E1Parity(i)
   */
  private final static byte[] TABLE_7E1= buildTable();
  
  public static byte[] getTable()
  {
    return TABLE_7E1.clone();
  }
  
  private static byte[] buildTable()
  {
    final byte[] table = new byte[128];
    for (int i = 0; i < 128; i++)
    {
      if (Integer.bitCount(i)%2==0)
      {
        table[i]=(byte) i;
      }
      else
      {
        table[i]=(byte) (i | 0x80);
      }
    }
    return table;
  }

}
