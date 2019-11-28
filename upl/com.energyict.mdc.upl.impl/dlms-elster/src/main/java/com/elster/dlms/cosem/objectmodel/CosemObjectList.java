/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Special list for COSEM Objects.
 * <P> 
 * Thread safe, guarded by {@code this}).<br> Supports notifications and
 * fast searches.<br> Each COSEM object must have an unique pair of OBIS code and class id. (This is
 * requirement for logical devices -see GB ed.7 p.147)
 *
 * @author Osse
 */
public class CosemObjectList implements Iterable<CosemObject>
{
  private final static CosemObject[] EMPTY_OBJECTS = new CosemObject[0];
  private final List<CosemObject> objects = new ArrayList<CosemObject>();
  private List<CosemObject> objectsForIterator = null;
  private final Map<ObisCode, List<CosemObject>> objectMap = new HashMap<ObisCode, List<CosemObject>>();
  private static final Comparator<CosemObject> CLASS_ID_COMPARATOR = new Comparator<CosemObject>()
  {
    //@Override
    public int compare(final CosemObject o1, final CosemObject o2)
    {
      if (o1.getCosemClassId() < o2.getCosemClassId())
      {
        return 1;
      }
      if (o1.getCosemClassId() > o2.getCosemClassId())
      {
        return -1;
      }

      return 0;
    }

  };

  public void add(final CosemObject cosemObject)
  {
    synchronized (this)
    {
      final ObisCode logicalName = cosemObject.getLogicalName();


      if (find(logicalName, cosemObject.getCosemClassId()) != null)
      {
        throw new IllegalArgumentException("A cosem object with OBIS code " + logicalName + " and class id "
                                           + cosemObject.getCosemClassId() + " was already added before");
      }

      objects.add(cosemObject);
      objectsForIterator = null;
      List<CosemObject> listInMap = objectMap.get(logicalName);
      if (listInMap == null)
      {
        listInMap = new ArrayList<CosemObject>(1);
        objectMap.put(logicalName, listInMap);
        listInMap.add(cosemObject);
      }
      else
      {
        listInMap.add(cosemObject);
        Collections.sort(listInMap, CLASS_ID_COMPARATOR);
      }
    }

    notifyAdded(cosemObject);
  }

  public int size()
  {
    synchronized (this)
    {
      return objects.size();
    }
  }

  public void clear()
  {
    final List<CosemObject> removed;

    synchronized (this)
    {
      removed = new ArrayList<CosemObject>(objects);

      objects.clear();
      objectMap.clear();
      objectsForIterator = null;
    }

    for (CosemObject o : removed)
    {
      notifyRemoved(o);
    }
  }

  public CosemObject find(final ObisCode obisCode, final int classId)
  {
    synchronized (this)
    {
      final List<CosemObject> objectsForClassId = objectMap.get(obisCode);
      if (objectsForClassId == null)
      {
        return null;
      }

      for (CosemObject co : objectsForClassId)
      {
        if (co.getCosemClassId() == classId)
        {
          return co;
        }
      }
    }
    return null;
  }

  public CosemObject get(final int index)
  {
    synchronized (this)
    {
      return objects.get(index);
    }
  }

  /**
   * Return a CosemObject for the given OBIS code.<P> If more than one CosemObject for the OBIS code exists
   * the CosemObject with the highest COSEM class id will be returned.
   *
   * @param obisCode The OBIS for the Object to find.
   * @return The object or
   * <code>null</code> no object with the given OBIS code exists.
   */
  public CosemObject find(final ObisCode obisCode)
  {
    synchronized (this)
    {
      final List<CosemObject> objectsForObisCode = objectMap.get(obisCode);
      if (objectsForObisCode == null)
      {
        return null;
      }
      return objectsForObisCode.get(0);
    }
  }

  /**
   * Returns the objects of this list as array.
   *
   * @return
   */
  public CosemObject[] toArray()
  {
    synchronized (this)
    {
      return objects.toArray(EMPTY_OBJECTS);
    }
  }

  /**
   * Returns a read only iterator for the state then this method was called.<p> Objects witch
   *
   * @return
   */
  //@Override
  public Iterator<CosemObject> iterator()
  {
    synchronized (this)
    {
      if (objectsForIterator == null)
      {
        objectsForIterator = Collections.unmodifiableList(new ArrayList<CosemObject>(objects));
      }
      return objectsForIterator.iterator();
    }
  }

  private final CopyOnWriteArrayList<Listener> listenerList = new CopyOnWriteArrayList<Listener>();

  public void addListener(final Listener listener)
  {
    listenerList.add(listener);
  }

  public void removeListener(final Listener listener)
  {
    listenerList.remove(listener);
  }

  private void notifyAdded(final CosemObject cosemObject)
  {
    for (Listener l : listenerList)
    {
      l.objectAdded(this, cosemObject);
    }
  }

  private void notifyRemoved(CosemObject cosemObject)
  {
    for (Listener l : listenerList)
    {
      l.objectRemoved(this, cosemObject);
    }
  }

  public interface Listener
  {
    void objectAdded(CosemObjectList sender, CosemObject object);

    void objectRemoved(CosemObjectList sender, CosemObject object);

  }

}
