/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class06/RegisterActivationMask.java $
 * Version:     
 * $Id: RegisterActivationMask.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 27, 2011 12:05:07 PM
 */
package com.elster.dlms.cosem.classes.class06;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;

/**
 * Register activation mask for COSEM class id 6 attribute 3 (Register activation - register_assignment)<P>
 * See BB ed.10 p.44f
 *
 * @author osse
 */
public class RegisterActivationMask
{
  private final String maskName;
  private final int[] indexList;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.OCTET_STRING),
          new ValidatorArray(new ValidatorSimpleType(DataType.UNSIGNED)));

  public RegisterActivationMask(final String maskName, final int[] indexList)
  {
    this.maskName = maskName;
    this.indexList = indexList.clone();
  }

  public static String buildMaskName(byte[] data)
  {
    return new String(data);
  }

  public RegisterActivationMask(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;

    byte[] value = ((DlmsDataOctetString)structure.get(0)).getValue();
    maskName = buildMaskName(value);

    final DlmsDataArray indexListArray = (DlmsDataArray)structure.get(1);
    indexList = new int[indexListArray.size()];
    for (int j = 0; j < indexList.length; j++)
    {
      indexList[j] = ((DlmsDataUnsigned)(indexListArray.get(j))).getValue();
    }
  }

  public DlmsData toDlmsData()
  {
    DlmsDataUnsigned indexListData[] = new DlmsDataUnsigned[indexList.length];
    for (int j = 0; j < indexList.length; j++)
    {
      indexListData[j] = new DlmsDataUnsigned(indexList[j]);
    }
    return new DlmsDataStructure(
            new DlmsDataOctetString(maskName.getBytes()),
            new DlmsDataArray(indexListData));
  }

  public int[] getIndexList()
  {
    return indexList.clone();
  }

  public String getMaskName()
  {
    return maskName;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("RegisterActivationMask{" + "maskName=");
    sb.append(maskName);
    sb.append(", indexList= {");
    boolean first = true;
    for (int i : indexList)
    {
      if (!first)
      {
        sb.append(", ");
      }
      sb.append(i);
      first = false;

    }
    sb.append("}}");
    return sb.toString();
  }

}
