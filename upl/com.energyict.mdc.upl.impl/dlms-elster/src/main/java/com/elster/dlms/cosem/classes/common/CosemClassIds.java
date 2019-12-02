/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/CosemClassIds.java $
 * Version:
 * $Id: CosemClassIds.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:52:46 PM
 */
package com.elster.dlms.cosem.classes.common;

/**
 * Simple definitions of COSEM class IDs.
 *
 * @author osse
 */
public final class CosemClassIds
{

  private CosemClassIds()
  {
    //no instances allowed.
  }
  
  
  public static final int DATA = 1;
  public static final int REGISTER = 3;
  public static final int EXTENDED_REGISTER = 4;
  public static final int DEMAND_REGISTER = 5;
  public static final int REGISTER_ACTIVATION = 6;
  public static final int PROFILE_GENERIC = 7;
  public static final int CLOCK = 8;
  public static final int SCRIPT_TABLE = 9;
  public static final int SCHEDULE = 10;
  public static final int SPECIAL_DAYS_TABLE = 11;
  public static final int ASSOCIATION_SN = 12;
  public static final int ASSOCIATION_LN = 15;
  public static final int SAP_ASSIGNMENT = 17;
  public static final int IMAGE_TRANSFER = 18;
  public static final int IEC_LOCAL_PORT_SETUP = 19;
  public static final int ACTIVITY_CALENDAR = 20;
  public static final int REGISTER_MONITOR = 21;
  public static final int SINGLE_ACTION_SCHEDULE = 22;
  public static final int IEC_HDLC_SETUP = 23;
  public static final int MODEM_CONFIGURATION = 27;
  public static final int AUTO_ANSWER = 28;
  public static final int AUTO_CONNECT = 29;
  public static final int TCP_UDP_SETUP = 41;
  public static final int IPV4_SETUP = 42;
  public static final int ETHERNET_SETUP = 43;
  public static final int PPP_SETUP = 44;
  public static final int GPRS_MODEM_SETUP = 45;
  public static final int SECURITY_SETUP = 64;
  public static final int SENSOR_MANAGER = 67;
}
