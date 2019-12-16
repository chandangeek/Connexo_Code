/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemAttribute.java $
 * Version:     
 * $Id: CosemAttribute.java 6380 2013-03-28 14:46:45Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 13:33:36
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.applicationprocess.CosemApplicationProcess;
import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * COSEM attribute, used by the COSEM object.
 * See {@link CosemObject}
 *
 * @author osse
 */
public class CosemAttribute extends CosemDataNode implements ICosemAttributeDataAccess
{
  public static final String PROP_STORED_DATA = "storedData";
  public static final String PROP_UPDATE_REQUIRED = "updateRequired";
  public static final String PROP_READ_DATA_ACCESS_RESULT = "ReadDataAccessResult";
  public static final String PROP_ATTRIBUTE_ACCESS_MODE = "AttributeAccessMode";
  private final int attributeId;
  private final CosemObject parent;
  private final CosemAttributeInfo attributeInfo;
  private final int[] accessSelectorIds;
  private final Map<Integer, CosemAccessSelector> accessSelectors =
          new HashMap<Integer, CosemAccessSelector>();
  private CosemAccessSelector activeAccessSelector = null;
  private DlmsData changedData;
  private DlmsData storedData;
  private Date readTimestamp;
  private AttributeAccessMode accessMode;
  protected DataAccessResult readDataAccessResult;
  private boolean updateRequired = false;
  public static final Set<AttributeAccessMode> WRITEABLE_ACCESS_MODES =
          Collections.unmodifiableSet(
          EnumSet.of(AttributeAccessMode.WRITE_ONLY, AttributeAccessMode.READ_AND_WRITE));
  public static final Set<AttributeAccessMode> WRITEABLE_AUTHENTICATED_ACCESS_MODES =
          Collections.unmodifiableSet(
          EnumSet.of(AttributeAccessMode.WRITE_ONLY, AttributeAccessMode.READ_AND_WRITE,
                     AttributeAccessMode.AUTHENTICATED_WRITE_ONLY,
                     AttributeAccessMode.AUTHENTICATED_READ_AND_WRITE));

  /**
   * Get the value of readDataAccessResult
   *
   * @return the value of readDataAccessResult
   */
  public synchronized DataAccessResult getReadDataAccessResult()
  {
    return readDataAccessResult;
  }

  /**
   * Set the value of readDataAccessResult
   *
   * @param readDataAccessResult new value of readDataAccessResult
   */
//  public void setReadDataAccessResult(DataAccessResult readDataAccessResult)
//  {
//    DataAccessResult oldReadDataAccessResult = this.readDataAccessResult;
//    this.readDataAccessResult = readDataAccessResult;
//    propertyChangeSupport.firePropertyChange(PROP_READ_DATA_ACCESS_RESULT, oldReadDataAccessResult,
//                                             readDataAccessResult);
//  }
  /**
   * Creates the COSEM attribute.
   *
   * @param parent The parent of the attribute.
   * @param attributeId The attribute id.
   * @param accessMode The access mode.
   * @param accessSelectors The available access selectors for this attribute of {@code null} if no access selection is supported.
   * @param  attributeInfo The attribute info. (Can be <b>null</b>)
   */
  public CosemAttribute(final CosemObject parent, final int attributeId, final AttributeAccessMode accessMode,
                        final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super();
    this.attributeId = attributeId;
    this.parent = parent;
    this.accessMode = accessMode;
    this.attributeInfo = attributeInfo;
    if (accessSelectors == null)
    {
      this.accessSelectorIds = null;
    }
    else
    {
      this.accessSelectorIds = accessSelectors.clone();
    }
  }

  /**
   * Returns the access mode for this attribute.
   *
   * @return The access mode.
   */
  public synchronized AttributeAccessMode getAccessMode()
  {
    return accessMode;
  }

  /**
   * Changes the access mode of this attribute.
   * (Required for dynamic changes of access rights by the calibration lock.)
   * 
   * @param accessMode  The new access mode
   */
  public void setAccessMode(final AttributeAccessMode accessMode)
  {
    final AttributeAccessMode oldAccessMode;

    synchronized (this)
    {
      oldAccessMode = this.accessMode;
      this.accessMode = accessMode;
    }
    propertyChangeSupport.firePropertyChange(PROP_ATTRIBUTE_ACCESS_MODE, oldAccessMode, accessMode);
  }

  /**
   * Returns the available access selectors for this attribute or {@code null} if no access selectors available.
   *
   * @return
   */
  public int[] getAccessSelectors()
  {
    if (accessSelectorIds == null)
    {
      return null;
    }
    else
    {
      return accessSelectorIds.clone();
    }
  }

  public boolean isAccessSelectorAvailable(int id)
  {
    if (accessSelectorIds == null)
    {
      return false;
    }

    for (int availableId : accessSelectorIds)
    {
      if (availableId == id)
      {
        return true;
      }
    }
    return false;

  }

  /**
   * Returns the access selector for the specified id.<P>
   * To get the active access selector use {@link #getActiveAccessSelector()}<P>
   *
   * @param id The id.
   * @return The access selector.
   */
  public CosemAccessSelector getAccessSelector(final int id)
  {
    return accessSelectors.get(id);
  }

  /**
   * Stores the specified access selector.<P>
   * To get a stored access selector use {@link #getAccessSelector(int) }.<br>
   * An already stored access selector with the same id will be replaced by this access selector.<br>
   * This method does not influence the active access selector, even if the active access selector has the same id
   * as the specified access selector.<br>
   * This method and the method {@link #getAccessSelector(int)} simply enables the view to save currently unused access-
   * selectors.
   *
   * @param accessSelector The access selector to store.
   */
  public void putAccessSelector(final CosemAccessSelector accessSelector)
  {
    accessSelectors.put(accessSelector.getId(), accessSelector);
  }

  /**
   * Returns the active access selector.
   *
   * @return The active access selector.
   */
  public synchronized CosemAccessSelector getActiveAccessSelector()
  {
    return activeAccessSelector;
  }

  /**
   * Sets the active access selector.
   *
   * @param activeAccessSelector
   */
  public synchronized void setActiveAccessSelector(final CosemAccessSelector activeAccessSelector)
  {
    //TODO: add property change support.
    if (this.activeAccessSelector == activeAccessSelector)
    {
      return;
    }

    if (!safeEquals(this.activeAccessSelector, activeAccessSelector))
    {
      this.activeAccessSelector = activeAccessSelector;
      clear();
    }
  }

  /**
   * Returns the id of this attribute.
   *
   * @return The id.
   */
  public int getAttributeId()
  {
    return attributeId;
  }

  /**
   * Returns the current data of this attribute.<P>
   *
   * @return The current data.
   */
//  @Override
  public DlmsData getData()
  {
    synchronized (this)
    {
      return changedData;
    }
  }

  /**
   * Sets the current data.<P>
   * If the data differs from the data read from the device the changed flag will be set.<P>
   *
   * @param newData
   */
  //@Override
  public void setData(final DlmsData newData)
  {
    DlmsData oldData;
    boolean dataChanged = false;
    synchronized (this)
    {
      oldData = getData();
      dataChanged = !safeEquals(oldData, newData);
      if (dataChanged)
      {
        if (safeEquals(storedData, newData))
        {
          changedData = storedData; //Don't save a copy of the equal (immutable) new data.
        }
        else
        {
          changedData = newData;
        }
      }
    }

    if (dataChanged)
    {
      propertyChangeSupport.firePropertyChange(PROP_DATA, oldData, newData);
    }
    updateChangedState();
  }

  /**
   * Reverts changes.<P>
   * Technically it sets "data" to "stored data"
   * 
   */
  public void revert()
  {
    final DlmsData oldData;
    final DlmsData newData;
    synchronized (this)
    {
      oldData = changedData;
      newData = storedData;
      changedData = storedData;
    }

    if (!safeEquals(oldData, newData))
    {
      propertyChangeSupport.firePropertyChange(PROP_DATA, oldData, newData);
      updateChangedState();
    }
  }

  private synchronized void updateChangedState()
  {
    setChanged(!safeEquals(changedData, storedData));
  }

  /**
   * Returns the data stored in the device.
   *
   * @return The stored data in the device.
   */
  public synchronized DlmsData getStoredData()
  {
    return storedData;
  }

  /**
   * Set the stored data in the device.<P>
   * Normally this method is only used by {@link CosemApplicationProcess } to set the attribute after read
   * and write operations.
   *
   * @param newStoredData The stored data in the device
   * @param readState The new read state or null if the current read state should not be changed
   * @param writeState The new write state or null if the current write state should not be changed
   */
  public void setStoredData(final DlmsData newStoredData, final DataAccessResult dataAccessResult,
                            final ReadState readState, final WriteState writeState, final Date readTimestamp)
  {
    final DlmsData oldStoredData;
    final DlmsData oldChangedData;
    final DlmsData newChangedData;
    DataAccessResult oldDataAccessResult;

    synchronized (this)
    {
      final boolean dataWasChanged = !safeEquals(storedData, changedData);

      oldDataAccessResult = readDataAccessResult;
      oldStoredData = storedData;
      oldChangedData = changedData;

      readDataAccessResult = dataAccessResult;
      this.readTimestamp = readTimestamp == null ? null : (Date)readTimestamp.clone();

      //storedDataChanged = !safeEquals(this.storedData, newStoredData);

      if (!safeEquals(oldStoredData, newStoredData))
      {
        storedData = newStoredData;

        if (!dataWasChanged) //If changed keep the changed data.
        {
          this.changedData = newStoredData;
        }
      }

      newChangedData = this.changedData;
    }

    if (readState != null)
    {
      setReadState(readState);
    }

    if (writeState != null)
    {
      setWriteState(writeState);
    }

    setUpdateRequired(false);

    setChanged(!safeEquals(newStoredData, newChangedData));

    if (!safeEquals(oldChangedData, newChangedData))
    {
      propertyChangeSupport.firePropertyChange(PROP_DATA, oldChangedData, newChangedData);
    }

    if (!safeEquals(oldStoredData, newStoredData))
    {
      propertyChangeSupport.firePropertyChange(PROP_STORED_DATA, oldStoredData, newStoredData);
    }

    propertyChangeSupport.firePropertyChange(PROP_READ_DATA_ACCESS_RESULT, oldDataAccessResult,
                                             dataAccessResult);
  }

  /**
   * Returns the object for this attribute.
   *
   * @return The COSEM object.
   */
  public CosemObject getParent()
  {
    return parent;
  }

  /**
   * Changes the read state.<P>
   * Normally this method is only used by {@link CosemApplicationProcess }
   *
   * @param readState The new read state.
   */
  @Override
  public synchronized void setReadState(final ReadState readState)
  {
    super.setReadState(readState);
  }

  /**
   * Changes the write state.<P>
   * Normally this method is only used by {@link CosemApplicationProcess }
   *
   * @param newWriteState The new read state.
   */
  @Override
  public synchronized void setWriteState(final WriteState newWriteState)
  {
    super.setWriteState(newWriteState);
  }

  /**
   * Validates the specified data using {@link CosemAttributeInfo#validateData(com.elster.dlms.types.data.DlmsData)}
   * <P>
   * An ValidationExecption will thrown if the data is invalid.
   *
   * @param data The data to validate
   * @throws ValidationExecption
   */
  public void validate(DlmsData data) throws ValidationExecption
  {
    if (attributeInfo != null)
    {
      attributeInfo.validateData(data);
    }
  }

  /**
   * Validates the current data using {@link CosemAttributeInfo#validateData(com.elster.dlms.types.data.DlmsData)}
   * <P>
   * An ValidationExecption will thrown if the data is not <b>null</b> and invalid.
   *
   * @throws ValidationExecption
   */
  public void validate() throws ValidationExecption
  {
    if (attributeInfo != null && getData() != null)
    {
      attributeInfo.validateData(getData());
    }
  }

  /**
   * Returns the attribute info for this attribute.
   *
   * @return The attribute info.
   */
  public CosemAttributeInfo getAttributeInfo()
  {
    return attributeInfo;
  }

  /**
   * Creates and returns a {@link CosemAttributeDescriptor } for this attribute.
   *
   * @return The created {@link CosemAttributeDescriptor }
   */
  public CosemAttributeDescriptor getCosemAttributeDescriptor()
  {
    CosemAccessSelector accessSelector;
    synchronized (this)
    {
      accessSelector = activeAccessSelector;
    }

    if (accessSelector == null)
    {
      return new CosemAttributeDescriptor(parent.getLogicalName(), parent.getCosemClassId(), getAttributeId());
    }
    else
    {
      return new CosemAttributeDescriptor(parent.getLogicalName(), parent.getCosemClassId(), getAttributeId(),
                                          new AccessSelectionParameters(accessSelector.getId(),
                                                                        accessSelector.toDlmsData()));
    }
  }

  @Override
  public void collectCosemAttributes(final List<CosemAttribute> cosemAttributes)
  {
    cosemAttributes.add(this);
  }

  /**
   * Indicates that the device value should be updated.
   *
   * @return the value of updateRequired
   */
  public synchronized boolean isUpdateRequired()
  {
    return updateRequired;
  }

  /**
   * Set the value of updateRequired
   *
   * @param updateRequired new value of updateRequired
   */
  public synchronized void setUpdateRequired(final boolean updateRequired)
  {
    final boolean oldUpdateRequired = this.updateRequired;
    this.updateRequired = updateRequired;
    propertyChangeSupport.firePropertyChange(PROP_UPDATE_REQUIRED, oldUpdateRequired, updateRequired);
  }

  /**
   * Clear the read data.
   */
  public void clear()
  {
    DlmsData oldStored = null;
    DlmsData oldChangedData = null;

    synchronized (this)
    {
      oldChangedData = changedData;
      oldStored = storedData;

      storedData = null;
      changedData = null;

      switch (getReadState())
      {
        case READING:
          break;
        case UPDATING:
          setReadState(ReadState.READING);
          break;
        default:
          setReadState(ReadState.UNREAD);
      }

      switch (getWriteState())
      {
        case WRITING:
          break;
        default:
          setWriteState(WriteState.UNWRITTEN);
      }
    }
    propertyChangeSupport.firePropertyChange(PROP_DATA, oldChangedData, null);
    propertyChangeSupport.firePropertyChange(PROP_STORED_DATA, oldStored, null);

    updateChangedState();
  }

  public synchronized Date getReadTimestamp()
  {
    if (readTimestamp == null)
    {
      return null;
    }
    else
    {
      return (Date)readTimestamp.clone();
    }
  }

  protected DataAccessResult writeDataAccessResult;
  public static final String PROP_WRITE_DATA_ACCESS_RESULT = "writeDataAccessResult";

  /**
   * Get the value of writeDataAccessResult
   *
   * @return the value of writeDataAccessResult
   */
  public synchronized DataAccessResult getWriteDataAccessResult()
  {
    return writeDataAccessResult;
  }

  /**
   * Set the value of writeDataAccessResult
   *
   * @param writeDataAccessResult new value of writeDataAccessResult
   */
  public synchronized void setWriteDataAccessResult(DataAccessResult writeDataAccessResult)
  {
    DataAccessResult oldWriteDataAccessResult = this.writeDataAccessResult;
    this.writeDataAccessResult = writeDataAccessResult;
    propertyChangeSupport.firePropertyChange(PROP_WRITE_DATA_ACCESS_RESULT, oldWriteDataAccessResult,
                                             writeDataAccessResult);
  }

  @Override
  public String toString()
  {
    return "CosemAttribute{" + "attributeDescriptor=" + getCosemAttributeDescriptor() + '}';
  }

  public CosemAttribute getCosemAttribute()
  {
    return this;
  }

  private boolean safeEquals(final Object o1, final Object o2)
  {
    if (o1 == o2)
    {
      return true;
    }

    if (o1 == null)
    {
      return false;
    }

    return o1.equals(o2);
  }

  public boolean isWriteable(final boolean chiperedContext)
  {
    if (chiperedContext)
    {
      return CosemAttribute.WRITEABLE_AUTHENTICATED_ACCESS_MODES.contains(getAccessMode());
    }
    else
    {
      return CosemAttribute.WRITEABLE_ACCESS_MODES.contains(getAccessMode());
    }
  }

}
