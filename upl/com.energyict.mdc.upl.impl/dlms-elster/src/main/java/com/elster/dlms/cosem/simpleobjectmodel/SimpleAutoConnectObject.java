/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleAutoConnectObject.java $
 * Version:     
 * $Id: SimpleAutoConnectObject.java 3643 2011-09-30 12:15:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.09.2011 11:45:08
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class29.AutoConnectModeEnum;
import com.elster.dlms.cosem.classes.common.TimeWindow;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import java.io.IOException;
import java.util.Collection;

/**
 * Auto connect.<P>
 * COSEM class id 29, See BB. ed.10 p. 101
 *
 * @author osse
 */
public class SimpleAutoConnectObject extends SimpleCosemObject
{

  /*package private*/
  SimpleAutoConnectObject(final SimpleCosemObjectDefinition definition,
                          final SimpleCosemObjectManager objectManager)
  {
    super(definition, objectManager);
  }

  public AutoConnectModeEnum getMode() throws IOException
  {
    final DlmsDataEnum data = executeGet(2, DlmsDataEnum.class, false);
    return AutoConnectModeEnum.getFactory().findValue(data.getValue());
  }

  public void setMode(final AutoConnectModeEnum mode) throws IOException
  {
    getManager().executeSetData(getDefinition(), 2, new DlmsDataEnum(mode.getId()));
  }

  public int getRepititions() throws IOException
  {
    final DlmsDataUnsigned data = executeGet(3, DlmsDataUnsigned.class, false);
    return data.getValue();
  }

  public void setRepititions(final int repetitions) throws IOException
  {
    getManager().executeSetData(getDefinition(), 3, new DlmsDataUnsigned(repetitions));
  }

  public int getRepititionDelay() throws IOException
  {
    final DlmsDataLongUnsigned data = executeGet(4, DlmsDataLongUnsigned.class, false);
    return data.getValue();
  }

  public void setRepititionDelay(final int repetitionDelay) throws IOException
  {
    executeSet( 4, new DlmsDataLongUnsigned(repetitionDelay));
  }

  public TimeWindow[] getCallingWindow() throws IOException
  {
    try
    {
      final DlmsDataArray data = executeGet(5, DlmsDataArray.class, false);
      return TimeWindow.fromDlmsDataArray(data);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public void setCallingWindow(final TimeWindow[] windows) throws IOException
  {
    executeSet(5, new DlmsDataArray(windows));
  }
  
  public void setCallingWindow(final Collection<TimeWindow> windows) throws IOException
  {
    setCallingWindow(windows.toArray(TimeWindow.EMPTY_TIME_WINDOWS));
  }

  public String[] getDestinationList() throws IOException
  {
    final DlmsDataArray data = executeGet(6, DlmsDataArray.class, false);
    String[] result = new String[data.size()];
    for (int i = 0; i < data.size(); i++)
    {
      result[i] = new String(((DlmsDataOctetString)data.get(i)).getValue());
    }
    return result;
  }

  public void setDestinationList(final String[] destinationList) throws IOException
  {
    DlmsDataOctetString[] octetStrings = new DlmsDataOctetString[destinationList.length];
    for (int i = 0; i < destinationList.length; i++)
    {
      octetStrings[i] = new DlmsDataOctetString(destinationList[i].getBytes());
    }
   executeSet(6, new DlmsDataArray(octetStrings));
  }

}
