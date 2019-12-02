/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/CommonDefs.java $
 * Version:
 * $Id: CommonDefs.java 3967 2012-01-26 08:51:02Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 4:23:08 PM
 */

package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.ObisCode;

/**
 * This class provides definitions for commonly used OBIS-Codes
 *
 * @author osse
 */
public class CommonDefs
{
  protected CommonDefs()
  {
    //no objects allowed
  }

  
  //--- Mandatory contents of a COSEM logical device (see BB ed.10 p. 32 &
  public static final ObisCode LOGICAL_DEVICE_NAME = new ObisCode( 0,0,42,0,0,255);
  public static final ObisCode CURRENT_ASSOCIATION = new ObisCode( 0,0,40,0,0,255);
  public static final ObisCode SAP_ASSIGNMENT = new ObisCode( 0,0,41,0,0,255); //Only required in the managment device
  
  
  public static final ObisCode DEVICE_TYPE = new ObisCode(7, 128, 0, 0, 2, 255);
  public static final ObisCode SOFTWARE_VERSION = new ObisCode(7,0,0,2,1,255);
  public static final ObisCode SERIAL_NUMBER = new ObisCode(0,0,96,1,0,255);
  public static final ObisCode CLOCK_OBJECT = new ObisCode(0,0,1,0,0,255);

  public static final ObisCode DEVICE_CONFIG_INFO_FOR_TOOL = new ObisCode(7,1,96,128,4,255);
  public static final ObisCode DEVICE_CONFIG_INFO_FOR_DCS = new ObisCode(7,2,96,128,4,255);
  
  public static final ObisCode IMAGE_TRANSFER = new ObisCode(0,0,44,0,0,255);
  
}
