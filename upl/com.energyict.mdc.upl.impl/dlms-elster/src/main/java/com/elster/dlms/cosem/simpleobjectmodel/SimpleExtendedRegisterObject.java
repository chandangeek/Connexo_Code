/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleExtendedRegisterObject.java $
 * Version:     
 * $Id: SimpleExtendedRegisterObject.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:55:47 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import java.io.IOException;

/**
 * Class for Objects of COSEM class "Register".
 *
 * @author osse
 */
public class SimpleExtendedRegisterObject extends SimpleRegisterObject
{
  SimpleExtendedRegisterObject(final SimpleCosemObjectDefinition definition,
                               final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public DlmsDateTime getCaptureTime() throws IOException
  {
    final DlmsDataOctetString rawDateTime = executeGet(5, DlmsDataOctetString.class, true);
    return new DlmsDateTime(rawDateTime.getValue());
  }

  public DlmsDateTime getCaptureTime(boolean forceRead) throws IOException
  {
    final DlmsDataOctetString rawDateTime = executeGet(5, DlmsDataOctetString.class, forceRead);
    return new DlmsDateTime(rawDateTime.getValue());
  }

  public DlmsData getStatus() throws IOException
  {
    return executeGet(4, DlmsData.class, true);
  }

  public DlmsData getStatus(boolean forceRead) throws IOException
  {
    return executeGet(4, DlmsData.class, forceRead);
  }

}
