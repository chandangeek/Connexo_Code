/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class11/CosemAttributeSpecialDayEntries.java $
 * Version:     
 * $Id: CosemAttributeSpecialDayEntries.java 3180 2011-07-08 13:10:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:30:33 AM
 */
package com.elster.dlms.cosem.classes.class11;

import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.objectmodel.CosemObject;
import com.elster.dlms.cosem.objectmodel.TypedCosemAttribute;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;

/**
 * COSEM attribute for the entries of the special days table.
 *
 * @author osse
 */
public class CosemAttributeSpecialDayEntries extends TypedCosemAttribute<SpecialDayEntry[]>
{
  public CosemAttributeSpecialDayEntries(final CosemObject parent, final int attributeId,
                                         final AttributeAccessMode accessMode,
                                         final CosemAttributeInfo attributeInfo, final int[] accessSelectors)
  {
    super(parent, attributeId, accessMode, attributeInfo, accessSelectors);
  }

  @Override
  protected DlmsData value2DlmsData(final SpecialDayEntry[] value)
  {
    DlmsData data[] = new DlmsData[value.length];

    for (int i = 0; i < value.length; i++)
    {
      data[i] = new DlmsDataStructure(
              new DlmsDataLongUnsigned(value[i].getIndex()),
              new DlmsDataOctetString(value[i].getDate().toBytes()),
              new DlmsDataUnsigned(value[i].getDayId()));
    }

    return new DlmsDataArray(data);
  }

  @Override
  protected SpecialDayEntry[] dlmsData2Value(final DlmsData data)
  {
    final DlmsDataArray array = (DlmsDataArray)data;
    final SpecialDayEntry[] result = new SpecialDayEntry[array.size()];

    for (int i = 0; i < result.length; i++)
    {
      final DlmsDataStructure structure = (DlmsDataStructure)array.get(i);
      final DlmsDataLongUnsigned index = (DlmsDataLongUnsigned)structure.get(0);
      final DlmsDataOctetString specialDayDate = (DlmsDataOctetString)structure.get(1);
      final DlmsDataUnsigned dayId = (DlmsDataUnsigned)structure.get(2);
      result[i] = new SpecialDayEntry(index.getValue(), new DlmsDate(specialDayDate.getValue()), dayId.
              getValue());
    }
    return result;
  }

}
