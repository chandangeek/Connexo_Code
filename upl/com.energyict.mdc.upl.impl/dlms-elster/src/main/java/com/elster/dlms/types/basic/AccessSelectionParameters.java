/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/AccessSelectionParameters.java $
 * Version:     
 * $Id: AccessSelectionParameters.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:35:43
 */

package com.elster.dlms.types.basic;

import com.elster.dlms.types.data.DlmsData;

/**
 * This class holds the access selection parameters. <P>
 * See GB ed. 7 p. 146-149. Valid values are described in the BB.
 *
 * @author osse
 */
public class AccessSelectionParameters
{
  private final int selector;
  private final DlmsData data;

  public AccessSelectionParameters(int selector, DlmsData data)
  {
    this.selector = selector;
    this.data = data;
  }


  public DlmsData getAccessParameters()
  {
    return data;
  }


  public int getSelector()
  {
    return selector;
  }

  @Override
  public String toString()
  {
    return "AccessSelectionParameters{" + "selector=" + selector + ", data=" + data + '}';
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final AccessSelectionParameters other = (AccessSelectionParameters)obj;
    if (this.selector != other.selector)
    {
      return false;
    }
    if (this.data != other.data && (this.data == null || !this.data.equals(other.data)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 97 * hash + this.selector;
    hash = 97 * hash + (this.data != null ? this.data.hashCode() : 0);
    return hash;
  }
  
  



  

}
