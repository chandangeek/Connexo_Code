/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemObjectFactory.java $
 * Version:     
 * $Id: CosemObjectFactory.java 3891 2012-01-09 11:03:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 1:04:16 PM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.classes.class15.AttributeAccessItem;
import com.elster.dlms.cosem.classes.class15.CosemObjectListElement;
import com.elster.dlms.cosem.classes.class15.MethodAccessItem;
import com.elster.dlms.cosem.classes.class18.CosemImageTransferObject;
import com.elster.dlms.cosem.classes.info.CosemClassInfos;
import com.elster.dlms.types.basic.ObisCode;

/**
 * COSEM Object factory.<P>
 * Provides methods for creating (specialized) COSEM objects.
 *
 *
 * 
 * @author osse
 */
public class CosemObjectFactory
{
  //TODO: check if the class is needed.

  private final CosemAttributeFactory attributeFactory;
  private final CosemClassInfos classInfos;

  /**
   * Constructor<P>
   *
   * @param classInfos {@link CosemClassInfos} for providing additional information for the created objects.
   */
  public CosemObjectFactory(CosemClassInfos classInfos)
  {
    this.classInfos = classInfos;
    this.attributeFactory = new CosemAttributeFactory(classInfos);
  }

  public final CosemObject createFromObjectListElement(LogicalDevice parent, CosemObjectListElement listElement)
  {
    CosemObject result = createObject(parent, listElement.getLogicalName(), listElement.getClassId(), listElement.getVersion(), false, false);

    for (AttributeAccessItem aai : listElement.getAccessRightsAttributes())
    {
      CosemAttribute cosemAttribute = attributeFactory.createAttribute(result, aai.getAttributeId(), aai.getAccessMode(), aai.getAccessSelectors());
      result.addAttribute(cosemAttribute);
    }

    for (MethodAccessItem mai : listElement.getAccessRightsMethods())
    {
      CosemMethod cosemMethod = new CosemMethod(result, mai.getMethodId(), mai.getAccessMode());
      result.getMethods().add(cosemMethod);
    }

    return result;
  }

  public CosemObject createObject(LogicalDevice parent, ObisCode instanceId, int classId, int classVersion, boolean addAttributes,
          boolean addMethods)
  {
    if (addAttributes || addMethods)
    {
      throw new UnsupportedOperationException("Not supported yet");
    }

    CosemObject result;

    switch (classId)
    {
      case 18:
        result = new CosemImageTransferObject(parent, instanceId, classId, classVersion);
        break;
      default:
        result = new CosemObject(parent, instanceId, classId, classVersion);
    }

    return result;
  }

  public CosemAttributeFactory getAttributeFactory()
  {
    return attributeFactory;
  }

  public CosemClassInfos getClassInfos()
  {
    return classInfos;
  }
}
