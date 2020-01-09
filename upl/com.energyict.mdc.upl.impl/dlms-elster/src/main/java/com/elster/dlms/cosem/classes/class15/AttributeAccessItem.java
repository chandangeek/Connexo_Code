/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/AttributeAccessItem.java $
 * Version:     
 * $Id: AttributeAccessItem.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 11:46:30
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorChoice;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataNull;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Attribute access item.<P>
 * See "attribute_access_item" BB ed. 7 p. 58
 *
 * @author osse
 */
public final class AttributeAccessItem  implements IDlmsDataProvider
{
  private final int attributeId;
  private final AttributeAccessMode accessMode;
  private final int[] accessSelectors;
  public static final IDlmsDataValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.INTEGER),
          new ValidatorSimpleType(DlmsData.DataType.ENUM),
          new ValidatorChoice(
          new ValidatorSimpleType(DlmsData.DataType.NULL_DATA),
          new ValidatorArray(new ValidatorSimpleType(DlmsData.DataType.INTEGER))));

  public AttributeAccessItem(final int attributeId,final AttributeAccessMode accessMode,final int[] accessSelectors)
  {
    this.attributeId = attributeId;
    this.accessMode = accessMode;
    this.accessSelectors = accessSelectors.clone();
  }

  public AttributeAccessItem(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    DlmsDataStructure structure = (DlmsDataStructure)data;

    DlmsDataInteger dataAttributeId = (DlmsDataInteger)structure.get(0);
    DlmsDataEnum dataAccessMode = (DlmsDataEnum)structure.get(1);

    attributeId = dataAttributeId.getValue();

    switch (dataAccessMode.getValue())
    {
      case 0:
        accessMode = AttributeAccessMode.NO_ACCESS;
        break;
      case 1:
        accessMode = AttributeAccessMode.READ_ONLY;
        break;
      case 2:
        accessMode = AttributeAccessMode.WRITE_ONLY;
        break;
      case 3:
        accessMode = AttributeAccessMode.READ_AND_WRITE;
        break;
      case 4:
        accessMode = AttributeAccessMode.AUTHENTICATED_READ_ONLY;
        break;
      case 5:
        accessMode = AttributeAccessMode.AUTHENTICATED_WRITE_ONLY;
        break;
      case 6:
        accessMode = AttributeAccessMode.AUTHENTICATED_READ_AND_WRITE;
        break;
      default:
        throw new ValidationExecption("unknown access right " + dataAccessMode.getValue());
    }

    if (structure.get(2).getType() == DlmsData.DataType.NULL_DATA)
    {
      accessSelectors = null;
    }
    else if (structure.get(2).getType() == DlmsData.DataType.ARRAY)
    {
      DlmsDataArray accessSelectorArray = (DlmsDataArray)structure.get(2);
      accessSelectors = new int[accessSelectorArray.size()];

      for (int i = 0; i < accessSelectorArray.size(); i++)
      {
        accessSelectors[i] = ((DlmsDataInteger)accessSelectorArray.get(i)).getValue();
      }
    }
    else
    {
      //Severe because this else branch should be prevented by the valildator.
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unknown type for access selectors:{0}",
                                                 structure.get(2).getType());
      throw new ValidationExecption("Unknown type for access selectors:" + structure.get(2).getType());
    }
  }
  
  //@Override
  public DlmsData toDlmsData()
  {
    DlmsData accessSelectorsData;
    
    if (accessSelectors==null || accessSelectors.length==0)
    {
      accessSelectorsData= new DlmsDataNull();
    }
    else
    {
      DlmsData[] accessSelectorsDataArray= new DlmsData[accessSelectors.length];
      
      for (int i=0; i<accessSelectors.length; i++)
      {
        accessSelectorsDataArray[i]= new DlmsDataInteger(accessSelectors[i]);
      }
      accessSelectorsData=new DlmsDataArray(accessSelectorsDataArray);
    }
    
    return new DlmsDataStructure(
            new DlmsDataInteger(attributeId),
            new DlmsDataEnum(accessMode.ordinal()),
            accessSelectorsData
            );
  }
    

  public AttributeAccessMode getAccessMode()
  {
    return accessMode;
  }

  public int getAttributeId()
  {
    return attributeId;
  }

  public int[] getAccessSelectors()
  {
    return accessSelectors == null ? null : accessSelectors.clone();
  }

  @Override
  public String toString()
  {
    return "attr. id=" + attributeId + ", access=" + accessMode;
  }


}
