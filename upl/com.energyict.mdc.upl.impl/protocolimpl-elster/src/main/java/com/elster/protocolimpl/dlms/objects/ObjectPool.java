/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author heuckeg
 */
@SuppressWarnings("unused")
public abstract class ObjectPool
{
  private ArrayList<ObjectAccessor> poolObjects = new ArrayList<ObjectAccessor>();

  public ObjectPool()
  {
  }

  public ObjectPool(ArrayList<ObjectAccessor> initialList)
  {
    addAll(initialList);
  }

  public ObjectPool(ObjectAccessor[] initialList)
  {
    addAll(initialList);
  }

  final public void add(ObjectAccessor oa)
  {
    poolObjects.add(oa);
  }

  final public void addAll(ArrayList<ObjectAccessor> list)
  {
    poolObjects.addAll(list);
  }

  final public void addAll(ObjectAccessor[] list)
  {
    poolObjects.addAll(Arrays.asList(list));
  }

  public IReadWriteObject findByCode(final int version, final ObisCode obisCode)
  {
    for (ObjectAccessor oa : poolObjects)
    {
      if (oa.presentForVersion(version) && oa.hasObisCode(obisCode))
      {
        return oa.getRwObject();
      }
    }
    return null;
  }

  public IReadWriteObject findByFunction(final int version, final String function)
  {
    for (ObjectAccessor oa : poolObjects)
    {
      if (oa.presentForVersion(version) && oa.implementsFunction(function))
      {
        return oa.getRwObject();
      }
    }
    return null;
  }

  public static class ObjectAccessor
  {
    private final int fromVersion;
    private final int toVersion;
    private final String implementFunction;
    private final IReadWriteObject rwObject;

    public ObjectAccessor(int from, int to, String function, IReadWriteObject object)
    {
      fromVersion = from;
      toVersion = to;
      this.implementFunction = function;
      rwObject = object;
    }

    public IReadWriteObject getRwObject()
    {
      return rwObject;
    }

    boolean presentForVersion(int version)
    {
      return (version >= fromVersion) && (version < toVersion);
    }

    boolean implementsFunction(final String function)
    {
      return this.implementFunction.equalsIgnoreCase(function);
    }

    boolean hasObisCode(ObisCode obisCode)
    {
      return rwObject.getObisCode().equals(obisCode);
    }

  }

}
