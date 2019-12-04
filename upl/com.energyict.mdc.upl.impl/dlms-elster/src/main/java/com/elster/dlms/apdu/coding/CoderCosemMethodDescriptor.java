/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderCosemMethodDescriptor.java $
 * Version:     
 * $Id: CoderCosemMethodDescriptor.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 10:56:16
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import java.io.IOException;

/**
 * En-/decoder for the "COSEM method descriptor".
 *
 * @author osse
 */
public class CoderCosemMethodDescriptor extends AbstractAXdrCoder<CosemMethodDescriptor>
{
  private final CoderCosemClassId coderClassId = new CoderCosemClassId();
  private final CoderCosemObjectInstanceId coderInstanceId = new CoderCosemObjectInstanceId();
  private final CoderCosemMethodId coderMethodId = new CoderCosemMethodId();


  @Override
  public void encodeObject(final CosemMethodDescriptor object,final  AXdrOutputStream out) throws IOException
  {
    coderClassId.encodeObject(object.getClassId(), out);
    coderInstanceId.encodeObject(object.getInstanceId(), out);
    coderMethodId.encodeObject(object.getMethodId(), out);
  }

  @Override
  public CosemMethodDescriptor decodeObject(final AXdrInputStream in) throws IOException
  {
    final int classId=coderClassId.decodeObject(in);
    final ObisCode instanceId=coderInstanceId.decodeObject(in);
    final int methodId= coderMethodId.decodeObject(in);
    return new CosemMethodDescriptor(instanceId,classId,methodId);
  }

}
