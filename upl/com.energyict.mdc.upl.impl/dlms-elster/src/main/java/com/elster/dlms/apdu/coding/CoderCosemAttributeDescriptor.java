/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderCosemAttributeDescriptor.java $
 * Version:     
 * $Id: CoderCosemAttributeDescriptor.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 10:56:16
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderOptionalValueWrapper;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import java.io.IOException;

/**
 * En-/decoder for the "COSEM attribute descriptor".
  * <P>
 * It directly decodes the access selection parameters.
 *
 * @author osse
 */
public class CoderCosemAttributeDescriptor extends AbstractAXdrCoder<CosemAttributeDescriptor>
{
  private final CoderCosemClassId coderClassId= new CoderCosemClassId();
  private final CoderCosemObjectInstanceId coderInstanceId= new CoderCosemObjectInstanceId();
  private final CoderCosemAttributeId coderAttributeId= new CoderCosemAttributeId();
  private final AXdrCoderOptionalValueWrapper<AccessSelectionParameters> coderAccessSelection = new AXdrCoderOptionalValueWrapper<AccessSelectionParameters>(new CoderSelectiveAccessSelector());


  @Override
  public void encodeObject(final CosemAttributeDescriptor object,final AXdrOutputStream out) throws IOException
  {
    coderClassId.encodeObject(object.getClassId(), out);
    coderInstanceId.encodeObject(object.getInstanceId(), out);
    coderAttributeId.encodeObject(object.getAttributeId(), out);
    coderAccessSelection.encodeObject(object.getAccessSelectionParameters(), out);
  }

  @Override
  public CosemAttributeDescriptor decodeObject(final AXdrInputStream in) throws IOException
  {
    final int classId=coderClassId.decodeObject(in);
    final ObisCode instanceId=coderInstanceId.decodeObject(in);
    final int attributeId= coderAttributeId.decodeObject(in);
    final AccessSelectionParameters accessSelectionParameters = coderAccessSelection.decodeObject(in);
    
    return new CosemAttributeDescriptor(instanceId,classId,attributeId,accessSelectionParameters);
  }


}
