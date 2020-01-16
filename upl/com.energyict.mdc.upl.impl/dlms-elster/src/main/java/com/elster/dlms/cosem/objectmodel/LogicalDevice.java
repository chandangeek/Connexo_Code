/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/LogicalDevice.java $
 * Version:     
 * $Id: LogicalDevice.java 3931 2012-01-19 14:18:29Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Sep 22, 2010 11:19:42 AM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.classes.class15.CosemObjectListElement;
import com.elster.dlms.types.basic.ObisCode;
import java.util.Collections;
import java.util.List;

/**
 * DLMS Logical Device
 *
 * @author osse
 */
public class LogicalDevice extends CosemInnerDataNode
{
  private final int logicalDeviceId;
  private final CosemObjectList objects = new CosemObjectList();

  /**
   * Creates the {@code LogicalDevice} with the specified logical device id.
   *
   * @param logicalDeviceId
   */
  public LogicalDevice(final int logicalDeviceId)
  {
    super();
    this.logicalDeviceId = logicalDeviceId;
  }

  /**
   * Returns the logical device id.
   *
   * @return The logical device id.
   */
  public int getLogicalDeviceId()
  {
    return logicalDeviceId;
  }

  /**
   * Returns an array of objects belonging to this device.
   *
   * @return
   */
  public CosemObject[] getObjects()
  {
    return objects.toArray();
  }

  /**
   * Adds the specified COSEM object to this logical device.
   *
   * @param object The COSEM object to add.
   */
  public void addObject(final CosemObject object)
  {
    objects.add(object);
    super.getChildren().add(object);
  }

  /**
   * Returns an unmodifiable list of the objects as data nodes .<P> To add children use {@link #addObject(com.elster.dlms.cosem.objectmodel.CosemObject)
   * }
   *
   * @return An unmodifiable list of the objects as data nodes.
   */
  @Override
  public List<AbstractCosemDataNode> getChildren()
  {
    return Collections.unmodifiableList(super.getChildren());
  }

  /**
   * Creates objects from the list {@link CosemObjectListElement } and adds them to this logical device.<P>
   * The object list of this logical the must be empty before.
   *
   * @param objectFactory The object factory for creating the objects.
   * @param elements
   */
  public void buildCosemObjectList(final CosemObjectFactory objectFactory,
                                   final List<CosemObjectListElement> elements)
  {
    objects.clear();
    super.getChildren().clear();

    for (CosemObjectListElement element : elements)
    {
      addObject(objectFactory.createFromObjectListElement(this, element));
    }
  }

  public CosemObject findCosemObject(final ObisCode obisCode)
  {
    return objects.find(obisCode);
  }

  public CosemAttribute findCosemAttribute(final ObisCode obisCode, final int attributeId)
  {
    final CosemObject object = objects.find(obisCode);
    if (object == null)
    {
      return null;
    }
    else
    {
      return object.getAttribute(attributeId);
    }
  }

}
