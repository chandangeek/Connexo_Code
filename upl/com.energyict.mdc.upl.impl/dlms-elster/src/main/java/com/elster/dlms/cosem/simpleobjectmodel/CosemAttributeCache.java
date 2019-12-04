/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/CosemAttributeCache.java $
 * Version:     
 * $Id: CosemAttributeCache.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.09.2011 10:44:59
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class to cache COSEM Attributes from the device
 *
 * @author osse
 */
class CosemAttributeCache
{
  private final Map<CosemAttributeDescriptor, GetDataResult> map =
          new HashMap<CosemAttributeDescriptor, GetDataResult>();
  private final Map<ObisCode, List<CosemAttributeDescriptor>> cachedAttributes =
          new HashMap<ObisCode, List<CosemAttributeDescriptor>>();

  public CosemAttributeCache()
  {
    //nothing to do
  }

  public GetDataResult getDataResult(final CosemAttributeDescriptor attributeDescriptor)
  {
    return map.get(attributeDescriptor);
  }

  public void putDataResult(final CosemAttributeDescriptor attributeDescriptor, final GetDataResult dataResult)
  {
    if (!map.containsKey(attributeDescriptor))
    {
      getCachedAttributeList(attributeDescriptor.getInstanceId()).add(attributeDescriptor);
    }
    map.put(attributeDescriptor, dataResult);
  }

  private List<CosemAttributeDescriptor> getCachedAttributeList(final ObisCode obisCode)
  {
    List<CosemAttributeDescriptor> list = cachedAttributes.get(obisCode);
    if (list == null)
    {
      list = new ArrayList<CosemAttributeDescriptor>();
      cachedAttributes.put(obisCode, list);
    }
    return list;
  }

  public void deleteAttribute(final ObisCode obisCode, final int attributeId)
  {
    if (cachedAttributes.containsKey(obisCode))
    {
      final List<CosemAttributeDescriptor> attributeList = cachedAttributes.get(obisCode);

      final Iterator<CosemAttributeDescriptor> iterator = attributeList.iterator();

      while (iterator.hasNext())
      {
        final CosemAttributeDescriptor next = iterator.next();
        if (next.getAttributeId() == attributeId)
        {
          map.remove(next);
          iterator.remove();
        }
      }

      if (attributeList.isEmpty())
      {
        cachedAttributes.remove(obisCode);
      }
    }
  }

  public void deleteAttributes(final ObisCode obisCode)
  {
    if (cachedAttributes.containsKey(obisCode))
    {
      final List<CosemAttributeDescriptor> attributeList = cachedAttributes.get(obisCode);

      for (CosemAttributeDescriptor d : attributeList)
      {
        map.remove(d);
      }
      cachedAttributes.remove(obisCode);
    }
  }

  public void clear()
  {
    cachedAttributes.clear();
    map.clear();
  }

}
