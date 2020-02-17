/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/ObisCodeDefFactory.java $
 * Version:     
 * $Id: ObisCodeDefFactory.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 13:39:45
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.obis.IRangeMap.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for ObisCodes definitions.
 *
 * @author osse
 */
public class ObisCodeDefFactory
{
  private static final String[] FILE_NAMES =
  {
    "obisCodesAbstract", "obisCodesGas" //, "obisCodesElectricity"
  };

  private ObisCodeDefFactory()
  {
  }

  private static class DefListHolder
  {
    final static IRangeMap<ObisCodeDef> OBIS_CODE_DEF_LIST = buildDefList();

    private static IRangeMap<ObisCodeDef> buildDefList()
    {
      final List<Pair<ObisCodeDef>> defList = new ArrayList<Pair<ObisCodeDef>>();
      try
      {
        for (String fileName : FILE_NAMES)
        {
          final InputStream stream = ObisCodeDefFactory.class.getClassLoader().getResourceAsStream(fileName + ".txt");
          final ObisFileReader reader = new ObisFileReader(stream);
          defList.addAll(reader.read());
        }
      }
      catch (IOException ex)
      {
        final String message = "internal obis file could not be read";
        Logger.getLogger(ObisCodeDefFactory.class.getName()).log(Level.SEVERE, message, ex);
        throw new AssertionError(message + " " + ex.toString());
      }
      return new FlatRangeMap<ObisCodeDef>(defList);
    }
  }

  public static IRangeMap<ObisCodeDef> getObisCodeDefList()
  {
    return DefListHolder.OBIS_CODE_DEF_LIST;
  }

  private static class FinderHolder
  {
    final static KdTreeRangeMap<ObisCodeDef> TREE_NODE = new KdTreeRangeMap<ObisCodeDef>(getObisCodeDefList());
  }

  public static IRangeMap<ObisCodeDef> getFinder()
  {
    return FinderHolder.TREE_NODE;
  }

  public static String getDescription(final ObisCode obisCode)
  {
    final IRangeMap<ObisCodeDef> finder = getFinder();

    final ObisCodeDef def = finder.find(obisCode);

    if (def == null)
    {
      return "";
    }

    return def.describe(obisCode);
  }

}
