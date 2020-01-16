/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/CosemObjectListElement.java $
 * Version:     
 * $Id: CosemObjectListElement.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 10:08:56
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.IDlmsDataProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Object list element of COSEM class "Association LN" (ID 15) Attribute "object_list" (no. 2).<P>
 * See BB ed.10 p.58
 *
 * @author osse
 */
public final class CosemObjectListElement implements IDlmsDataProvider
{
  private final int classId;
  private final int version;
  private final ObisCode logicalName;
  private final List<AttributeAccessItem> accessRightsAttributes;
  private final List<MethodAccessItem> accessRightsMethods;
  
  public CosemObjectListElement(int classId, int version, ObisCode logicalName,
                                List<AttributeAccessItem> accessRightsAttributes,
                                List<MethodAccessItem> accessRightsMethods)
  {
    this.classId = classId;
    this.version = version;
    this.logicalName = logicalName;
    this.accessRightsAttributes = Collections.unmodifiableList(new ArrayList<AttributeAccessItem>(accessRightsAttributes));
    this.accessRightsMethods = Collections.unmodifiableList(new ArrayList<MethodAccessItem>(accessRightsMethods));
  }

  public CosemObjectListElement(DlmsData data) throws ValidationExecption
  {

    VALIDATOR.validate(data);
    /*
    class_id: long-unsigned,
    version: unsigned,
    logical_name: octet-string,
    access_rights: access_right
     */

    //Test the structure
    DlmsDataStructure structure = (DlmsDataStructure)data;

    DlmsDataLongUnsigned dataClassId = (DlmsDataLongUnsigned)structure.get(0);
    DlmsDataUnsigned dataVersion = (DlmsDataUnsigned)structure.get(1);
    DlmsDataOctetString dataLogicalName = (DlmsDataOctetString)structure.get(2);


    classId = dataClassId.getValue();
    version = dataVersion.getValue();
    logicalName = new ObisCode(dataLogicalName.getValue());


    DlmsDataStructure structureAccessRights = (DlmsDataStructure)structure.get(3);
    DlmsDataArray attributeAccessArray = (DlmsDataArray)structureAccessRights.get(0);

    List<AttributeAccessItem> tempAttributeAccessItems = new ArrayList<AttributeAccessItem>();
    for (DlmsData d : attributeAccessArray)
    {
      tempAttributeAccessItems.add(new AttributeAccessItem(d));
    }
    accessRightsAttributes = Collections.unmodifiableList(tempAttributeAccessItems);


    DlmsDataArray methodAccessArray = (DlmsDataArray)structureAccessRights.get(1);
    List<MethodAccessItem> tempAccessRightsMethods = new ArrayList<MethodAccessItem>();
    for (DlmsData d : methodAccessArray)
    {
      tempAccessRightsMethods.add(new MethodAccessItem(d));
    }
    accessRightsMethods = Collections.unmodifiableList(tempAccessRightsMethods);
  }

  

  public int getClassId()
  {
    return classId;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
  }

  public int getVersion()
  {
    return version;
  }

  public List<AttributeAccessItem> getAccessRightsAttributes()
  {
    return accessRightsAttributes;
  }

  public AttributeAccessItem findAttributeAccessItem(int id)
  {
    for (AttributeAccessItem i : accessRightsAttributes)
    {
      if (i.getAttributeId() == id)
      {
        return i;
      }
    }
    return null;
  }

  public List<MethodAccessItem> getAccessRightsMethods()
  {
    return accessRightsMethods;
  }

  public MethodAccessItem findMethodAccessItem(int id)
  {
    for (MethodAccessItem i : accessRightsMethods)
    {
      if (i.getMethodId() == id)
      {
        return i;
      }
    }
    return null;
  }
  
  private static final IDlmsDataValidator VALIDATOR_ACCESS_RIGHTS = new ValidatorStructure(
          new ValidatorArray(AttributeAccessItem.VALIDATOR),
          new ValidatorArray(MethodAccessItem.VALIDATOR));  

  public static final IDlmsDataValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.LONG_UNSIGNED),
          new ValidatorSimpleType(DlmsData.DataType.UNSIGNED),
          new ValidatorOctetString(6, 6),
          VALIDATOR_ACCESS_RIGHTS);
  
 //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(classId),
            new DlmsDataUnsigned(version),
            new DlmsDataOctetString(logicalName.toByteArray()),
            new DlmsDataStructure(
                    new DlmsDataArray(accessRightsAttributes.toArray(new AttributeAccessItem[accessRightsAttributes.size()])),
                    new DlmsDataArray(accessRightsMethods.toArray(new MethodAccessItem[accessRightsMethods.size()]))
                    ))
            ;
  }  

  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("class id=" + classId + ", version=" + version + ", logical name=" + logicalName);
    sb.append(", attributes={");
    for (AttributeAccessItem item : accessRightsAttributes)
    {
      sb.append("(" + item.toString() + ")");
    }
    sb.append("}");

    sb.append(", methods={");
    for (MethodAccessItem item : accessRightsMethods)
    {
      sb.append("(" + item.toString() + ")");
    }
    sb.append("}");

    return sb.toString();
  }

  public static CosemObjectListElement[] buildElements(DlmsData data) throws ValidationExecption
  {
    if (data.getType() != DlmsData.DataType.ARRAY)
    {
      throw new IllegalArgumentException("Array expected");
    }

    DlmsDataArray dlmsDataArray = (DlmsDataArray)data;

    CosemObjectListElement[] result = new CosemObjectListElement[dlmsDataArray.size()];

    for (int i = 0; i < dlmsDataArray.size(); i++)
    {
      result[i] = new CosemObjectListElement(dlmsDataArray.get(i));
    }

    return result;
  }



}
