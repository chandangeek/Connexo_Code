/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemObject.java $
 * Version:     
 * $Id: CosemObject.java 3891 2012-01-09 11:03:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 13:33:05
 */
package com.elster.dlms.cosem.objectmodel;


import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents one COSEM object.<P>
 *
 * @author osse
 */
public class CosemObject extends CosemInnerDataNode
{
  private final LogicalDevice parent;
  private final ObisCode logicalName;
  private final int classId;
  private final int version;
  
  private final List<CosemAttribute> attributes = new ArrayList<CosemAttribute>();
  private final List<CosemMethod> methods = new ArrayList<CosemMethod>();

  private static final CosemAttribute[] EMPTY_ATTRIBUTES = new CosemAttribute[0];

  public CosemObject(final LogicalDevice parent,final ObisCode logicalName,final int classId,final int version)
  {
    super();
    this.parent = parent;
    this.logicalName = logicalName;
    this.classId = classId;
    this.version = version;
  }
  

  /**
   * Returns the class ID for this object.
   *
   * @return
   */
  //@Override
  public int getCosemClassId()
  {
    return classId;
  }



  /**
   * Returns the logical name (OBIS-Code) for this object.
   *
   * @return The logical name.
   */
  //@Override
  public ObisCode getLogicalName()
  {
    return logicalName;
  }


  /**
   * Returns the class version of this object.
   *
   * @return The class version.
   */
  //@Override
  public int getCosemClassVersion()
  {
    return version;
  }


  /**
   * Returns all attributes of this object.
   *
   * @return The attributes as array.
   */
  public CosemAttribute[] getAttributes()
  {
    return attributes.toArray(EMPTY_ATTRIBUTES);
  }

  /**
   * Returns the attribute with the specified attribute id or null if the attribute was not found.
   *
   * @param attributeId The attribute id.
   * @return The attribute or null
   */
  public CosemAttribute getAttribute(int attributeId)
  {
    //shortcut
    if (attributeId <= attributes.size() && attributeId>0 && attributes.get(attributeId - 1).getAttributeId() == attributeId)
    {
      return attributes.get(attributeId - 1);
    }

    //search
    for (CosemAttribute a : attributes)
    {
      if (a.getAttributeId() == attributeId)
      {
        return a;
      }
    }

    return null;
  }

  /**
   * Adds an attribute to this object.
   *
   * @param attribute The new attribute.
   */
  public void addAttribute(final CosemAttribute attribute)
  {
    attributes.add(attribute);
    super.getChildren().add(attribute);
  }

  /**
   * Returns an unmodifiable list of the attributes as data nodes .<P>
   * To add children use {@link #addAttribute(com.elster.dlms.cosem.objectmodel.CosemAttribute) }
   *
   * @return An unmodifiable list of the attributes as data nodes.
   */
  @Override
  public List<AbstractCosemDataNode> getChildren()
  {
    return Collections.unmodifiableList(super.getChildren());
  }

  /**
   * Returns a (modifiable) list of methods.
   *
   * @return
   */
  public List<CosemMethod> getMethods()
  {
    return methods;
  }

  /**
   * Returns the method with the specified method id or null if the attribute was not found.
   * 
   * @param methodId The method id.
   * @return The method or null
   */
  public CosemMethod getMethod(int methodId)
  {
    //shortcut
    if ((methodId>0) && methodId <= methods.size() && methods.get(methodId - 1).getId() == methodId)
    {
      return methods.get(methodId - 1);
    }

    //search
    for (CosemMethod m : methods)
    {
      if (m.getId() == methodId)
      {
        return m;
      }
    }

    return null;
  }

  /*
   * Returns the name of this class.
   *
   */
  //@Override
  public String getCosemClassName()
  {
    return "Class " + getCosemClassId();
  }

  public LogicalDevice getParent()
  {
    return parent;
  }

  

}
