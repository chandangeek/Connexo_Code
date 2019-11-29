/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemInnerDataNode.java $
 * Version:     
 * $Id: CosemInnerDataNode.java 3050 2011-06-07 12:50:30Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 4, 2010 2:29:56 PM
 */
package com.elster.dlms.cosem.objectmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Inner node of an COSEM data tree (/graph).<P>
 * The children will be monitored for status changes.
 *
 * @author osse
 */
public class CosemInnerDataNode extends CosemDataNode
{
  private final Children children = new Children();

  /**
   * Sole constructor
   */
  public CosemInnerDataNode()
  {
    super(ReadState.OK, WriteState.UNWRITTEN, false);
  }

  /**
   * Return the children of this data node.<P>
   * The children will be monitored for changes.
   * 
   * @return A modifiable list of children
   */
  public List<AbstractCosemDataNode> getChildren()
  {
    return children;
  }

  @Override
  public void collectCosemAttributes(final List<CosemAttribute> cosemAttributes)
  {
    for (AbstractCosemDataNode n : children)
    {
      n.collectCosemAttributes(cosemAttributes);
    }
  }

  private class Children extends AbstractList<AbstractCosemDataNode>
  {
    private final List<AbstractCosemDataNode> listImplementation = new ArrayList<AbstractCosemDataNode>();
    private final PropertyChangeListener childListener = new PropertyChangeListener()
    {
      //@Override
      public void propertyChange(final PropertyChangeEvent evt)
      {
        final String propertyName = evt.getPropertyName();
        if (CosemAttribute.PROP_CHANGED.equals(propertyName)
            || CosemAttribute.PROP_READSTATE.equals(propertyName)
            || CosemAttribute.PROP_WRITESTATE.equals(propertyName))
        {
          updateState();
        }
      }
    };


    @Override
    public AbstractCosemDataNode get(final int index)
    {
      return listImplementation.get(index);
    }

    @Override
    public int size()
    {
      return listImplementation.size();
    }

    @Override
    public AbstractCosemDataNode remove(final int index)
    {
      final AbstractCosemDataNode removed = listImplementation.remove(index);
      detachListener(removed);
      return removed;
    }

    @Override
    public boolean remove(final Object o)
    {
      if (!(o instanceof AbstractCosemDataNode))
      {
        throw new IllegalArgumentException("Object must be an instance of AbstractCosemDataNode");
      }

      final AbstractCosemDataNode n = (AbstractCosemDataNode)o;

      final boolean removed = listImplementation.remove(n);
      if (removed)
      {
        detachListener(n);
      }
      return removed;
    }

    @Override
    public void clear()
    {
      for (AbstractCosemDataNode n : listImplementation)
      {
        n.removePropertyChangeListener(childListener);
      }
      listImplementation.clear();

      setChanged(false);
      setReadState(ReadState.OK);
      setWriteState(WriteState.UNWRITTEN);
    }

    @Override
    public boolean add(AbstractCosemDataNode e)
    {
      final boolean r = listImplementation.add(e);
      attachListener(e);
      return r;
    }

    private void attachListener(AbstractCosemDataNode e)
    {
      e.addPropertyChangeListener(childListener);
      updateState();
    }

    private void detachListener(final AbstractCosemDataNode e)
    {
      e.removePropertyChangeListener(childListener);
      updateState();
    }

    private void updateState()
    {
      ReadState rs = null;
      WriteState ws = null;
      boolean c = false;


      for (AbstractCosemDataNode n : listImplementation)
      {
        if (rs == null || rs.ordinal() < n.getReadState().ordinal())
        {
          rs = n.getReadState();
        }

        if (ws == null || ws.ordinal() < n.getWriteState().ordinal())
        {
          ws = n.getWriteState();
        }

        if (n.isChanged())
        {
          c = true;
        }
      }

      if (rs == null)
      {
        rs = ReadState.OK;
      }

      if (ws == null)
      {
        ws = WriteState.UNWRITTEN;
      }

      setReadState(rs);
      setWriteState(ws);
      setChanged(c);
    }

  }

}
