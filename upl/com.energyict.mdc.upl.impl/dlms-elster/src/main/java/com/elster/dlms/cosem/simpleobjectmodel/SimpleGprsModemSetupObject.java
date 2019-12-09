/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleGprsModemSetupObject.java $
 * Version:     
 * $Id: SimpleGprsModemSetupObject.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  25.07.2011 12:16:59
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import java.io.IOException;

/**
 * Simple Object for IC 45 "GPRS modem setup".<P>
 * See BB ed. 10 p. 116<P>
 * Attribute 4 (quality_of_service) will not be considered.<br>
 * Attribute -5 will be interpreted as APN user name
 * Attribute -6 will be interpreted as APN password
 *
 * @author osse
 */
public class SimpleGprsModemSetupObject extends SimpleCosemObject
{
  /*package private*/
  SimpleGprsModemSetupObject(final SimpleCosemObjectDefinition definition,
                             final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public String getApn() throws IOException
  {
    return new String(executeGet(2, DlmsDataOctetString.class, false).getValue());
  }

  public void setApn(final String apn) throws IOException
  {
    executeSet(2, new DlmsDataOctetString(apn.getBytes()));
  }

  public String getApnPassword() throws IOException
  {
    return new String(executeGet(-6, DlmsDataOctetString.class, false).getValue());
  }

  public void setApnPassword(final String apnPassword) throws IOException
  {
    executeSet(-6, new DlmsDataOctetString(apnPassword.getBytes()));
  }

  public String getApnUser() throws IOException
  {
    return new String(executeGet(-5, DlmsDataOctetString.class, false).getValue());
  }

  public void setApnUser(final String apnUser) throws IOException
  {
    executeSet(-5, new DlmsDataOctetString(apnUser.getBytes()));    
  }

  public int getPincode() throws IOException
  {
    return executeGet(3, DlmsDataLongUnsigned.class, false).getValue();
  }

  public void setPincode(final int pincode) throws IOException
  {
    executeSet(3, new DlmsDataLongUnsigned(pincode));    
  }

}
