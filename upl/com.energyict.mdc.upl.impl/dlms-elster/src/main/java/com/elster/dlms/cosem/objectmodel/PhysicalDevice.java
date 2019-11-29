/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/PhysicalDevice.java $
 * Version:     
 * $Id: PhysicalDevice.java 2528 2011-01-13 16:52:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Sep 22, 2010 11:18:57 AM
 */
package com.elster.dlms.cosem.objectmodel;

import java.util.Map;
import java.util.TreeMap;

/**
 * Physical DLMS device
 *
 * @author osse
 */
public class PhysicalDevice extends CosemInnerDataNode
{
  private final Map<Integer, LogicalDevice> logicalDevices = new TreeMap<Integer, LogicalDevice>();
  private static final LogicalDevice[] EMPTY_LOGICAL_DEVICES = new LogicalDevice[0];

  /**
   * Returns an array with the logical devices for this physical device.
   *
   * @return An array with the logical devices.
   */
  public LogicalDevice[] getLogicalDevices()
  {
    return logicalDevices.values().toArray(EMPTY_LOGICAL_DEVICES);
  }

  /**
   * Returns the logical device with the specified id.
   *
   * @param id The id. (In the HDLC communication profile this id is the lower HDLC-Address)
   * @return
   */
  public LogicalDevice getLogicalDevice(int id)
  {
    return logicalDevices.get(id);
  }

  /**
   * Add the specified logical device
   *
   * @param logicalDevice
   */
  public void addLogicalDevice(LogicalDevice logicalDevice)
  {
    logicalDevices.put(logicalDevice.getLogicalDeviceId(), logicalDevice);
    super.getChildren().add(logicalDevice);
  }

  /**
   * Removes
   *
   * @param id
   */
  public void removeLogicalDevice(int id)
  {
    LogicalDevice logicalDevice = logicalDevices.remove(id);
    if (logicalDevice != null)
    {
      super.getChildren().remove(logicalDevice);
    }
  }

}
