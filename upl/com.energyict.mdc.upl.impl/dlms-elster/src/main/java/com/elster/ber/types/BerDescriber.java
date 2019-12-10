/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerDescriber.java $
 * Version:     
 * $Id: BerDescriber.java 1822 2010-08-05 10:27:58Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 14:50:35
 */
package com.elster.ber.types;

/**
 * Utility class to describe BER structures.
 *
 * @author osse
 */
public class BerDescriber
{
  private static final String NL = "\r\n";
  private static final String INDENT = "  ";
  int level = 0;
  StringBuilder sb = new StringBuilder();

  public void writeLn(String line)
  {
    for (int i = 0; i < level; i++)
    {
      sb.append(INDENT);
    }

    sb.append(line);
    sb.append(NL);
  }

  public void incLevel()
  {
    level++;
  }

  public void decLevel()
  {
    level--;
  }

  public void resetLevel()
  {
    level = 0;
  }

  @Override
  public String toString()
  {
    return sb.toString();
  }

}
