/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemClassInfo.java $
 * Version:     
 * $Id: CosemClassInfo.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  04.08.2010 15:10:40
 */
package com.elster.dlms.cosem.classes.info;

import java.util.ArrayList;
import java.util.List;

/**
 * Information of an COSEM class.
 *
 * @author osse
 */
public class CosemClassInfo
{
  private final IdAndVersion idAndVersion;
  private final String className;
  private final List<CosemAttributeInfo> attributes = new ArrayList<CosemAttributeInfo>();
  private final List<CosemMethodInfo> methods = new ArrayList<CosemMethodInfo>();

  public CosemClassInfo(int id, int version, String className)
  {
    this.idAndVersion = new IdAndVersion(id, version);
    this.className = className;
  }

  /**
   * The class id (see BB)
   *
   * @return The class ID.
   */
  public int getClassId()
  {
    return idAndVersion.getId();
  }


  /**
   * The class name as declared in the BB.
   * 
   * @return The class name.
   */
  public String getClassName()
  {
    return className;
  }

  public int getClassVersion()
  {
    return idAndVersion.getVersion();
  }


  public List<CosemAttributeInfo> getAttributes()
  {
    return attributes;
  }

  public List<CosemMethodInfo> getMethods()
  {
    return methods;
  }
  
  public IdAndVersion getIdAndVersion()
  {
    return idAndVersion;
  }



  public static final class IdAndVersion
  {
    private final int id;
    private final int version;

    public IdAndVersion(int id, int version)
    {
      this.id = id;
      this.version = version;
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
      final IdAndVersion other = (IdAndVersion)obj;
      if (this.id != other.id)
      {
        return false;
      }
      if (this.version != other.version)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 37 * hash + this.id;
      hash = 37 * hash + this.version;
      return hash;
    }
    
    

    

    public int getId()
    {
      return id;
    }

    public int getVersion()
    {
      return version;
    }

  }

}
