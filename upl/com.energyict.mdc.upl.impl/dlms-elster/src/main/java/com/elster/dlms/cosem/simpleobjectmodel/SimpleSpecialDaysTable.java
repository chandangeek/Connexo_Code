/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleSpecialDaysTable.java $
 * Version:     
 * $Id: SimpleSpecialDaysTable.java 3643 2011-09-30 12:15:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.07.2011 09:49:14
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsDataArray;
import java.io.IOException;
import java.util.Collection;

/**
 * SimpleCosemObject for special days table.
 *
 * @author osse
 */
public class SimpleSpecialDaysTable extends SimpleCosemObject
{
  /*package private*/
  SimpleSpecialDaysTable(final SimpleCosemObjectDefinition definition, final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public SpecialDayEntry[] getEntries() throws IOException
  {
    try
    {
      final DlmsDataArray dlmsData = executeGet(2, DlmsDataArray.class, false);
      return SpecialDayEntry.fromDlmsDataArray(dlmsData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public void setEntries(final SpecialDayEntry[] entries) throws IOException
  {
    executeSet(2, new DlmsDataArray(entries));
  }

  public void setEntries(final Collection<SpecialDayEntry> entries) throws IOException
  {
    setEntries(entries.toArray(SpecialDayEntry.EMPTY_SPECIAL_DAY_ENTRIES));
  }

}
