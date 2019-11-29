/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/ICosemAttributeDataAccess.java $
 * Version:
 * $Id: ICosemAttributeDataAccess.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.08.2011 14:20:16
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.types.data.DlmsData;
import java.beans.PropertyChangeListener;

/**
 * Encapsulates the pure new value part of the CosemAttribute.<P>
 * This makes it easier to create Adapters, which extract only a part of an CosemAttribute.
 * 
 *
 * @author osse
 */
public interface ICosemAttributeDataAccess
{
  String PROP_DATA = "data";

  /**
   * Returns the current data of this attribute.<P>
   *
   * @return The current data.
   */
  DlmsData getData();

  /**
   * Sets the current data.<P>
   * If the data differs from the data read from the device the changed flag will be set.<P>
   *
   * @param data
   */
  void setData(final DlmsData data);
  
  
  boolean isChanged();
  
  
   /**
   * Returns the access mode for this attribute.
   *
   * @return The access mode.
   */
  public AttributeAccessMode getAccessMode();

  /**
   * Returns the original COSEM attribute.
   * 
   * @return 
   */
  public CosemAttribute getCosemAttribute();
  
  
  
  /**
   * Add PropertyChangeListener.
   *
   * @param listener
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener);

  /**
   * Remove PropertyChangeListener.
   *
   * @param listener
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener);

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
