/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleProfileObject.java $
 * Version:     
 * $Id: SimpleProfileObject.java 5777 2013-01-02 13:44:48Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 17, 2011 3:06:48 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.cosem.classes.class07.*;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *  One profile. 
 *
 * @author osse
 */
public class SimpleProfileObject extends SimpleCosemObject
{
  private DlmsDataArray buffer = null;

  SimpleProfileObject(final SimpleCosemObjectDefinition definition, final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public CaptureObjectDefinition getCaptureObjectDefinition(final int col) throws IOException
  {
    return getCaptureObjects()[col];
  }

  public CaptureObjectDefinition[] getCaptureObjects() throws IOException
  {
    try
    {
      final DlmsDataArray dlmsData = executeGet(3, DlmsDataArray.class, false);
      return CaptureObjectDefinition.fromDlmsDataArray(dlmsData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  /**
   * Returns the related object for the specified column.
   * 
   * If no concrete implementation for the class id exists, the base class (SimpleCosemObject) will be returned.
   *
   * @param col The column
   * @return The related object .
   */
  public SimpleCosemObject getRelatedObject(final int col) throws IOException
  {
    final CaptureObjectDefinition[] captureObjects = getCaptureObjects();
    final CaptureObjectDefinition capturedObject = captureObjects[col];
    final SimpleCosemObject simpleCosemObject = getManager().getSimpleCosemObject(capturedObject.
            getLogicalName());

    if (simpleCosemObject.getDefinition().getClassId() != capturedObject.getClassId())
    {
      throw new IOException("Unexpected COSEM-class ID"); //Should not happen for Elster devices.
    }
    return simpleCosemObject;
  }

  /** 
   * searches the captured object list for a given object,
   * 
   * @param code - logical name of object to find
   * 
   * @return index of object if found, otherwise -1
   */
  public int indexOfCapturedObject(final ObisCode code) throws IOException
  {
    return indexOfCapturedObject(code, 2);
  }

  /** 
   * searches the captured object list for a given object,
   * 
   * @param code - logical name of object to find
   * 
   * @return index of object if found, otherwise -1
   */
  public int indexOfCapturedObject(final ObisCode code, final int attribute) throws IOException
  {
    CaptureObjectDefinition[] defs = getCaptureObjects();
    for (int i = 0; i < defs.length; i++)
    {
      if (defs[i].getLogicalName().equals(code) && (defs[i].getAttributeIndex() == attribute))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the unit for the specified column.
   *
   * @param col The index of the column
   * @return The unit or {@code null} if no unit is available
   */
  public Unit getUnit(final int col) throws IOException
  {
    final SimpleCosemObject relatedObject = getRelatedObject(col);
    if (relatedObject instanceof SimpleRegisterObject)
    {
      return ((SimpleRegisterObject)relatedObject).getScalerUnit().getUnit();
    }
    return null;
  }

  public Object getValue(final int row, final int col) throws IOException
  {

    final DlmsData value = getRawValue(row, col);
    if (value == null)
    {
      return null;
    }

    Object result = value.getValue();

    final SimpleCosemObject relatedObject = getRelatedObject(col);

    final CaptureObjectDefinition def = getCaptureObjectDefinition(col);

    if ((def.getAttributeIndex() == 2 && def.getClassId() == CosemClassIds.CLOCK)
        || (def.getAttributeIndex() == 5 && def.getClassId() == CosemClassIds.EXTENDED_REGISTER))
    {
      result = new DlmsDateTime(((DlmsDataOctetString)value).getValue());
    }
    else
    {
      if (relatedObject instanceof SimpleRegisterObject)
      {
        final ScalerUnit scalerUnit = ((SimpleRegisterObject)relatedObject).getScalerUnit();
        final BigDecimal bigDecimal = scalerUnit.scale(value);
        if (bigDecimal != null)
        {
          result = bigDecimal;
        }
      }
    }
    return result;
  }

  public DlmsData getRawValue(int row, final int col) throws IOException
  {
    if (buffer != null)
    {
      if (getSortMethod() == SortMethodEnum.SORT_METHOD_LIFO)
      {
        row = (getRowCount() - 1) - row;
      }
      final DlmsDataStructure line = (DlmsDataStructure)buffer.get(row);
      return line.get(col);
    }
    else
    {
      return null;
    }
  }

  public int getRowCount() throws IOException
  {
    return buffer.size();
  }

  public int getColumnCount() throws IOException
  {
    return getCaptureObjects().length;
  }

  /**
   * @return the capturePeriod
   */
  public long getCapturePeriod() throws IOException
  {
    return executeGet(4, DlmsDataDoubleLongUnsigned.class, false).getValue();
  }

  /**
   * @return the entriesInUse
   */
  public long getEntriesInUse() throws IOException
  {
    return executeGet(7, DlmsDataDoubleLongUnsigned.class, false).getValue();
  }

  /**
   * Reads and returns the maximum number of entries in the profile.
   * <P>
   * (see Attribute 8 of profile class)
   * 
   * @return The maximum number of entries in the profile
   */
  public long getProfileEntries() throws IOException
  {
    return executeGet(8, DlmsDataDoubleLongUnsigned.class, false).getValue();
  }

  /**
   * 
   * Reads and returns the sort method.
   * <P>
   * (see Attribute 5 of profile class)
   * 
   * @return The sort method.
   * @throws IOException 
   */
  public SortMethodEnum getSortMethod() throws IOException
  {
    final int value = executeGet(5, DlmsDataEnum.class, false).getValue();
    return SortMethodEnum.getFactory().findValue(value);
  }

  /**
   * Reads the complete profile.
   * 
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfile() throws IOException
  {
    return readProfileData(null);
  }

  /**
   * Reads the complete profile.
   * 
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfile( final boolean forceRead,
                              final boolean cacheResult) throws IOException
  {
    return readProfileData(null,forceRead,cacheResult);
  }

  /**
   * Reads the profile.
   * 
   * @param from The lower bound of the range to readout.
   * @param to The upper bound of the range to readout.
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final Date from, final Date to) throws IOException
  {
    final DlmsDateTime dlmsDateTimeFrom = from == null ? null : new DlmsDateTime(from);
    final DlmsDateTime dlmsDateTimeTo = to == null ? null : new DlmsDateTime(to);

    return readProfileData(dlmsDateTimeFrom, dlmsDateTimeTo);
  }

  
  /**
   * Reads the profile.
   * 
   * @param from The lower bound of the range to readout.
   * @param to The upper bound of the range to readout.
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final Date from, final Date to,final boolean forceRead,
                              final boolean cacheResult) throws IOException
  {
    final DlmsDateTime dlmsDateTimeFrom = from == null ? null : new DlmsDateTime(from);
    final DlmsDateTime dlmsDateTimeTo = to == null ? null : new DlmsDateTime(to);

    return readProfileData(dlmsDateTimeFrom, dlmsDateTimeTo, forceRead, cacheResult);
  }

  /**
   * Reads the profile.
   * 
   * @param from The lower bound of the range to readout.
   * @param to The upper bound of the range to readout.
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final DlmsDateTime from, final DlmsDateTime to) throws IOException
  {
    return readProfileData(buildRangeDescriptor(from, to));
  }

  /**
   * Reads the profile.
   * 
   * @param from The lower bound of the range to readout.
   * @param to The upper bound of the range to readout.
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final DlmsDateTime from, final DlmsDateTime to,final boolean forceRead,
                              final boolean cacheResult) throws IOException
  {
    return readProfileData(buildRangeDescriptor(from, to), forceRead, cacheResult);
  }
  /**
   * Reads the profile using the specified access selector.
   * 
   * @param accessSelector The access selector to use
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final AbstractBufferAccessSelector accessSelector) throws IOException
  {
    return readProfileData(accessSelector, true, false);
  }

  /**
   * Reads the profile using the specified access selector.
   * 
   * @param accessSelector The access selector to use
   * @param forceRead If true the value will be read from the device, if false a cached value will be used if possible 
   * @param cacheResult If true the value will be saved in the cache after reading.
   *
   * @return The numbers of lines read.
   * @throws IOException 
   * 
   */
  public long readProfileData(final AbstractBufferAccessSelector accessSelector, final boolean forceRead,
                              final boolean cacheResult) throws IOException
  {
    if (accessSelector == null)
    {
      buffer = executeGet(2, DlmsDataArray.class, forceRead);
    }
    else
    {
      buffer = (DlmsDataArray)getManager().executeGetData(getDefinition(), 2, accessSelector, true);
    }

    if (!cacheResult)
    {
      getManager().removeFromCache(getDefinition().getLogicalName(), 2);//don't cache the buffer 
    }

    return getRowCount();
  }

  

  /**
   * Read the specified newest entries of profile.
   * 
   * @param newestEntries The numbers of newest entries to read.
   * @return The numbers of lines read.
   * @throws IOException 
   */
  public long readProfile(final int newestEntries) throws IOException
  {
    return readProfile(newestEntries,true,false);
  }

  /**
   * Read the specified newest entries of profile.
   * 
   * @param newestEntries The numbers of newest entries to read.
   * @return The numbers of lines read.
   * @throws IOException 
   */
  public long readProfile(final int newestEntries,final boolean forceRead,
                              final boolean cacheResult) throws IOException
  {
    EntryDescriptor entryDescriptor;

    if (getSortMethod() == SortMethodEnum.SORT_METHOD_FIFO)
    {
      final long entriesInUse = getEntriesInUse();
      final long from = entriesInUse <= newestEntries ? 1 : entriesInUse - (newestEntries - 1);
      final long to = entriesInUse;
      entryDescriptor = new EntryDescriptor(from, to, 1, getColumnCount());
    }
    else
    {
      entryDescriptor = new EntryDescriptor(1, newestEntries, 1, getColumnCount());
    }
    return readProfileData(entryDescriptor,forceRead, cacheResult);
  }

  private RangeDescriptor buildRangeDescriptor(final DlmsDateTime from, final DlmsDateTime to) throws
          IOException
  {

    RangeDescriptor rangeDescriptor = null;
    if (from != null || to != null)
    {
      int dateTimeCol = -1;
      final CaptureObjectDefinition[] defs = getCaptureObjects();

      for (int i = 0; i < defs.length; i++)
      {
        if (defs[i].getClassId() == CosemClassIds.CLOCK && defs[i].getAttributeIndex() == 2)
        {
          dateTimeCol = i;
          break;
        }
      }

      if (dateTimeCol < 0)
      {
        throw new IOException("No date time column found");
      }

      final DlmsDataDateTime fromData = from == null ? null : new DlmsDataDateTime(from);
      final DlmsDataDateTime toData = to == null ? null : new DlmsDataDateTime(to);
      rangeDescriptor = new RangeDescriptor(defs[dateTimeCol], fromData, toData, null);
    }
    return rangeDescriptor;
  }

}
