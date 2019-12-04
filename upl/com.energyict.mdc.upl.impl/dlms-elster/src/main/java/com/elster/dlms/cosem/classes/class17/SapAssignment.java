/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class17/SapAssignment.java $
 * Version:     
 * $Id: SapAssignment.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 27, 2011 10:33:40 AM
 */
package com.elster.dlms.cosem.classes.class17;

import com.elster.coding.CodingUtils;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import java.util.Arrays;

/**
 * Object definition for COSEM class id 17 attribute 2 (SAP assignment - SAP_assignment_list)<P>
 * See BB ed.10 p. 62
 *
 * @author osse
 */
public class SapAssignment
{
  private final int sap;
  private final byte[] logicalDeviceName;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorSimpleType(DataType.OCTET_STRING));
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);
  
  
  public static SapAssignment[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array= (DlmsDataArray)data;
    SapAssignment[] result= new SapAssignment[array.size()];
    
    for (int i=0; i<array.size(); i++)
    {
      result[i]= new SapAssignment(array.get(i));
    }
    return result;
  }

  public SapAssignment(int sap, byte[] logicalDeviceName)
  {
    this.sap = sap;
    this.logicalDeviceName = logicalDeviceName.clone();
  }

  public SapAssignment(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.sap = ((DlmsDataLongUnsigned)structure.get(0)).getValue();
    this.logicalDeviceName = ((DlmsDataOctetString)structure.get(1)).getValue();
  }

  DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(sap),
            new DlmsDataOctetString(logicalDeviceName));
  }

  public byte[] getLogicalDeviceName()
  {
    return logicalDeviceName.clone();
  }

  public int getSap()
  {
    return sap;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final SapAssignment other = (SapAssignment)obj;
    if (this.sap != other.sap)
    {
      return false;
    }
    if (!Arrays.equals(this.logicalDeviceName, other.logicalDeviceName))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 19 * hash + this.sap;
    hash = 19 * hash + Arrays.hashCode(this.logicalDeviceName);
    return hash;
  }

  @Override
  public String toString()
  {
    return "SapAssignment{" + "sap=" + sap + ", logicalDeviceName=" + CodingUtils.byteArrayToString(
            logicalDeviceName) + '}';
  }

}
