/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class20/CosemAttributeWeekProfileTable.java $
 * Version:     
 * $Id: CosemAttributeWeekProfileTable.java 2901 2011-05-05 15:08:49Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class20;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * COSEM attribute for the week profile table
 *
 * @author osse
 */
public class CosemAttributeWeekProfileTable extends TypedCosemAttribute<WeekProfile[]>
{
  public CosemAttributeWeekProfileTable(final CosemObject parent, final int attributeId,
                                        final AttributeAccessMode accessMode,
                                        final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final WeekProfile[] value)
  {
    DlmsData[] entries = new DlmsData[value.length];
    for (int i = 0; i < value.length; i++)
    {
      entries[i] = value[i].toDlmsData();
    }
    return new DlmsDataArray(entries);
  }

  @Override
  protected WeekProfile[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    CosemAttributeValidators.ARRAY_VALIDATOR.validate(data);

    final DlmsDataArray array = (DlmsDataArray)data;
    WeekProfile[] result = new WeekProfile[array.size()];

    for (int i = 0; i < result.length; i++)
    {
      result[i] = new WeekProfile(array.get(i));
    }
    return result;
  }

}
