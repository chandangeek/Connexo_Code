/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/AbstractCosemDataNode.java $
 * Version:     
 * $Id: AbstractCosemDataNode.java 3896 2012-01-10 15:39:39Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 4, 2010 2:22:01 PM
 */
package com.elster.dlms.cosem.objectmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for COSEM data nodes.<P>
 *
 * @author osse
 */
public abstract class AbstractCosemDataNode
{
  public static final String PROP_WRITESTATE = "writeState";
  public static final String PROP_READSTATE = "readState";
  public static final String PROP_CHANGED = "changed";

  protected final PropertyChangeSupport propertyChangeSupport= new PropertyChangeSupport(this);

  /**
   * The read state of a data node.
   *
   */
  public enum ReadState
  {
    /**
     * The node was successfully read.
     */
    OK,
    /**
     * An error occurred during reading the node.
     */
    ERROR,
    /**
     * The node was not read.
     */
    UNREAD,
    /**
     * The node was successfully read and is read at this moment again.
     */
    UPDATING,
    /**
     * The node is read at this moment.
     */
    READING
  };

  /**
   * The write state of this node.
   *
   */
  public enum WriteState
  {
    /**
     * The node was successfully written.
     */
    OK,
    /**
     * An error occurred during writing the node
     */
    ERROR,
    /**
     * The node was not written.
     */
    UNWRITTEN,
    /**
     * The node is written at the moment.
     */
    WRITING
  };

  /**
   * Returns the read state of this node.
   *
   * @return The read state.
   */
  public abstract ReadState getReadState();

  /**
   * Returns the write state of this node.
   *
   * @return The write state.
   */
  public abstract WriteState getWriteState();

  /*
   * Return true if the data of this node was changed.<P>
   * (That means data has to be written)
   */
  public abstract boolean isChanged();

  /**
   * Collects all attributes belonging to this node.<P>
   * (If the specialized class is an attribute it a adds only itself to the list.)
   * 
   * @param cosemAttributes The list to add the attributes to.
   */
  public abstract void collectCosemAttributes(List<CosemAttribute> cosemAttributes);

  private static final CosemAttribute[] EMPTY_ATTRIBUTES = new CosemAttribute[0];

  /**
   * Returns an array of {@link CosemAttribute} belonging to this node.<P>
   * Internally it calls {@link #collectCosemAttributes(java.util.List) }
   *
   * @return An array of {@link CosemAttribute}
   */
  public CosemAttribute[] getCosemAttributes()
  {
    final List<CosemAttribute> attributes = new ArrayList<CosemAttribute>();
    collectCosemAttributes(attributes);

    return attributes.toArray(EMPTY_ATTRIBUTES);
  }


  /**
   * Add PropertyChangeListener.
   *
   * @param listener
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove PropertyChangeListener.
   *
   * @param listener
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(final String propertyName,final PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public void addPropertyChangeListener(final String propertyName,final PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

}
