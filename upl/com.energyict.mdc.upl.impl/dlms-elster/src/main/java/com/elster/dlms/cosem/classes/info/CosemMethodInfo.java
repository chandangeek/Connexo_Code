/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemMethodInfo.java $
 * Version:     
 * $Id: CosemMethodInfo.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  10.08.2010 15:14:16
 */

package com.elster.dlms.cosem.classes.info;

/**
 * Information for known COSEM methods 
 *
 * @author osse
 */
public class CosemMethodInfo
{
  private final int number;
  private final String name;
  //private final


  public CosemMethodInfo(int number, String name)
  {
    this.number = number;
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public int getNumber()
  {
    return number;
  }




}
