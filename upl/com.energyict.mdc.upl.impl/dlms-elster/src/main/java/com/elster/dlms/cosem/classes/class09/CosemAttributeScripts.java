/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class09/CosemAttributeScripts.java $
 * Version:     
 * $Id: CosemAttributeScripts.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class09;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;

/**
 * COSEM attribute for the entries of the special days table.
 *
 * @author osse
 */
public class CosemAttributeScripts extends TypedCosemAttribute<Script[]>
{
  public CosemAttributeScripts(final CosemObject parent, final int attributeId,
                                       final AttributeAccessMode accessMode,
                                       final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final Script[] value)
  {
    DlmsData[] entries = new DlmsData[value.length];
    for (int i = 0; i < value.length; i++)
    {
      entries[i] = value[i].toDlmsData();
    }
    return new DlmsDataArray(entries);
  }

  @Override
  protected Script[] dlmsData2Value(final DlmsData data) throws ValidationExecption
  {
    CosemAttributeValidators.ARRAY_VALIDATOR.validate(data);

    final DlmsDataArray array = (DlmsDataArray)data;
    Script[] result = new Script[array.size()];

    for (int i = 0; i < result.length; i++)
    {
      result[i] = new Script(array.get(i));
    }
    return result;
  }

}
