/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleAutoAnswerObject.java $
 * Version:     
 * $Id: SimpleAutoAnswerObject.java 3643 2011-09-30 12:15:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.09.2011 11:45:08
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class28.AutoAnswerModeEnum;
import com.elster.dlms.cosem.classes.class28.AutoAnswerStatusEnum;
import com.elster.dlms.cosem.classes.class28.NumberOfRings;
import com.elster.dlms.cosem.classes.common.TimeWindow;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import java.io.IOException;
import java.util.Collection;

/**
 * Auto connect.<P>
 * COSEM class id 28, See BB. ed.10 p. 99
 *
 * @author osse
 */
public class SimpleAutoAnswerObject extends SimpleCosemObject
{

  /*package private*/
  SimpleAutoAnswerObject(final SimpleCosemObjectDefinition definition,
                         final SimpleCosemObjectManager objectManager)
  {
    super(definition, objectManager);

  }

  public AutoAnswerModeEnum getMode() throws IOException
  {
    final DlmsDataEnum data = executeGet(2, DlmsDataEnum.class, false);
    return AutoAnswerModeEnum.getFactory().findValue(data.getValue());
  }

  public void setMode(final AutoAnswerModeEnum mode) throws IOException
  {
    executeSet(2, new DlmsDataEnum(mode.getId()));
  }

  public TimeWindow[] getListeningWindow() throws IOException
  {
    try
    {
      final DlmsDataArray data = executeGet(3, DlmsDataArray.class, false);
      return TimeWindow.fromDlmsDataArray(data);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public void setListeningWindow(final TimeWindow[] windows) throws IOException
  {
    executeSet(3, new DlmsDataArray(windows));
  }

  public void setListeningWindow(final Collection<TimeWindow> windows) throws IOException
  {
    setListeningWindow(windows.toArray(TimeWindow.EMPTY_TIME_WINDOWS));
  }

  public AutoAnswerStatusEnum getStatus() throws IOException
  {
    final DlmsDataEnum data = executeGet(4, DlmsDataEnum.class, false);
    return AutoAnswerStatusEnum.getFactory().findValue(data.getValue());
  }

  public void setStatus(final AutoAnswerStatusEnum mode) throws IOException
  {
    getManager().executeSetData(getDefinition(), 4, new DlmsDataEnum(mode.getId()));
  }

  public int getNumberOfCalls() throws IOException
  {
    final DlmsDataUnsigned data = executeGet(5, DlmsDataUnsigned.class, false);
    return data.getValue();
  }

  public void setNumberOfCalls(final int numberOfCalls) throws IOException
  {
    executeSet(5, new DlmsDataUnsigned(numberOfCalls));
  }

  public NumberOfRings getNumberOfRings() throws IOException
  {
    final DlmsDataStructure data = executeGet(6, DlmsDataStructure.class, false);
    try
    {
      return new NumberOfRings(data);
    }
    catch (final ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public void setNumberOfRinges(final NumberOfRings numberOfRings) throws IOException
  {
    executeSet(6, numberOfRings.toDlmsData());
  }

}
