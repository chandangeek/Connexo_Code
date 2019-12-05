/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/CosemMethodDescriptor.java $
 * Version:     
 * $Id: CosemMethodDescriptor.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:11:27
 */
package com.elster.dlms.types.basic;

/**
 * This class holds the COSEM method descriptor. <P>
 * See GB ed. 7 p. 151-154. Valid values are described in the BB.
 *
 * @author osse
 */
public class CosemMethodDescriptor
{
  private final ObisCode instanceId;
  private final int classId;
  private final int methodId;


  public CosemMethodDescriptor(final ObisCode instanceId,final int classId,final int methodId)
  {
    this.classId = classId;
    this.instanceId = instanceId;
    this.methodId = methodId;
  }

  public int getMethodId()
  {
    return methodId;
  }

  public int getClassId()
  {
    return classId;
  }

  public ObisCode getInstanceId()
  {
    return instanceId;
  }

  @Override
  public String toString()
  {
    return "CosemMethodDescriptor{" + "instanceId=" + instanceId + ", classId=" + classId + ", methodId=" +
           methodId + '}';
  }

}
