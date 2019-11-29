/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemEnum.java $
 * Version:     
 * $Id: CosemEnum.java 2458 2010-12-10 17:09:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 2:46:33 PM
 */

package com.elster.dlms.cosem.classes.common;

/**
 * This is the base class for COSEM data enumeration elements.<P>
 *
 *
 * @author osse
 */
public class CosemEnum implements ICosemEnum
{
  private final int id;
  private final String name;

  public CosemEnum(final int id,final String name)
  {
    this.id = id;
    this.name = name;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
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
    final CosemEnum other = (CosemEnum)obj;
    if (this.id != other.id)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    return this.id;
  }

  @Override
  public String toString()
  {
    return name+ " ("+id + ")";
  }






}
