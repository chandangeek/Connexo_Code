/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/definitions/DlmsOids.java $
 * Version:     
 * $Id: DlmsOids.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  07.05.2010 13:47:32
 */

package com.elster.dlms.definitions;

import com.elster.dlms.types.basic.ObjectIdentifier;

/**
 * OIDs used by DLMS
 *
 * @author osse
 */
public final class DlmsOids
{

  private DlmsOids()
  {
    //no instances allowed.
  }
  
  
  public static final ObjectIdentifier DLMS_UA= new ObjectIdentifier(2,16,756,5,8);

  public static final ObjectIdentifier DLMS_UA_AC= new ObjectIdentifier(DLMS_UA,1);

  public static final ObjectIdentifier DLMS_UA_AC_LN= new ObjectIdentifier(DLMS_UA_AC,1);
  public static final ObjectIdentifier DLMS_UA_AC_SN= new ObjectIdentifier(DLMS_UA_AC,2);
  public static final ObjectIdentifier DLMS_UA_AC_LN_WC= new ObjectIdentifier(DLMS_UA_AC,3);
  public static final ObjectIdentifier DLMS_UA_AC_SN_WC= new ObjectIdentifier(DLMS_UA_AC,4);

  public static final ObjectIdentifier DLMS_UA_AUTHENTICATION_MECHANISM_NAME= new ObjectIdentifier(DLMS_UA,2);
  
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_LOWEST_LEVEL_SECURITY_MECHANISM_NAME= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,0);
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,1);
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,2);
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME_USING_MD5= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,3);
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME_USING_SHA_1= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,4);
  public static final ObjectIdentifier DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME_USING_GMAC= new ObjectIdentifier(DLMS_UA_AUTHENTICATION_MECHANISM_NAME,5);
}
