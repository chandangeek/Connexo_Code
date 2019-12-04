/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemDataNode.java $
 * Version:     
 * $Id: CosemDataNode.java 3667 2011-10-05 16:36:13Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 4, 2010 4:48:05 PM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.objectmodel.AbstractCosemDataNode.ReadState;

/**
 * Base COSEM data node.<P>
 * Extends {@link AbstractCosemDataNode} with fields for the read, write and changed state.<P>
 *
 * @author osse
 */
public abstract class CosemDataNode extends AbstractCosemDataNode
{
  private ReadState readState;//= ReadState.UNREAD;
  private WriteState writeState;//= WriteState.UNWRITTEN;
  private boolean changed;//= false;

  /**
   * Sole constructor
   * 
   * 
   */
  public CosemDataNode()
  {
    this(ReadState.UNREAD, WriteState.UNWRITTEN, false);
  }

  /**
   * Constructor  with defined states.
   *
   * @param readState The read state.
   * @param writeState The write state.
   * @param changed The change state.
   */
  public CosemDataNode(final ReadState readState, final WriteState writeState, final boolean changed)
  {
    this.readState = readState;
    this.writeState = writeState;
    this.changed = changed;
  }

  @Override
  public synchronized ReadState getReadState()
  {
    return readState;
  }

  /**
   * Sets the read state. Fires the read state changed event as necessary.
   *
   * @param readState the new read state.
   */
  protected void setReadState(final ReadState readState)
  {
    final ReadState oldReadState;
    synchronized (this)
    {
      oldReadState = this.readState;
      this.readState = readState;
    }
    propertyChangeSupport.firePropertyChange(PROP_READSTATE, oldReadState, readState);
  }

  @Override
  public synchronized WriteState getWriteState()
  {
    return writeState;
  }

  /**
   * Sets the write state. Fires the write state changed event as necessary.
   *
   */
  protected void setWriteState(final WriteState writeState)
  {
    final WriteState oldWriteState;
    synchronized (this)
    {
      oldWriteState = this.writeState;
      this.writeState = writeState;
    }
    propertyChangeSupport.firePropertyChange(PROP_WRITESTATE, oldWriteState, writeState);
  }

  @Override
  public synchronized boolean isChanged()
  {
    return changed;
  }

  /**
   * Sets the changed state. Fires the change event as necessary.
   *
   * @param changed The new change state
   */
  protected void setChanged(final boolean changed)
  {
    final boolean oldChanged;
    synchronized (this)
    {
      oldChanged = this.changed;
      this.changed = changed;
    }
    propertyChangeSupport.firePropertyChange(PROP_CHANGED, oldChanged, changed);
  }

}
