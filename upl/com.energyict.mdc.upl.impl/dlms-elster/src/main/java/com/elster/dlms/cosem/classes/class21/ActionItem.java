/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class21/ActionItem.java $
 * Version:     
 * $Id: ActionItem.java 3583 2011-09-28 15:44:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.08.2011 09:00:41
 */
package com.elster.dlms.cosem.classes.class21;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Action Item for the Register monitor (IC 21 Attr. 4)<P>
 * See BB ed.10 p.83
 *
 * @author osse
 */
public class ActionItem implements IDlmsDataProvider
{ 
  private final ObisCode scriptLogicalName;
  private final int scriptSelector;
  
  public final static IDlmsDataValidator VALIDATOR = CosemAttributeValidators.immutableValidator(
          new ValidatorStructure(
          new ValidatorOctetString(6, 6),
          new ValidatorSimpleType(DataType.LONG_UNSIGNED)
          ));

  public ActionItem(ObisCode logicalName, int scriptSelector)
  {
    this.scriptLogicalName = logicalName;
    this.scriptSelector = scriptSelector;
  }
  

  public ActionItem(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;
    final DlmsDataOctetString scriptLogicalNameData = (DlmsDataOctetString)structure.get(0);
    final DlmsDataLongUnsigned scriptSelectorData = (DlmsDataLongUnsigned)structure.get(1);

    this.scriptLogicalName = new ObisCode(scriptLogicalNameData.getValue());
    this.scriptSelector = scriptSelectorData.getValue();
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataOctetString(scriptLogicalName.toByteArray()),
            new DlmsDataLongUnsigned(scriptSelector));
  }

  public ObisCode getScriptLogicalName()
  {
    return scriptLogicalName;
  }

  public int getScriptSelector()
  {
    return scriptSelector;
  }

  @Override
  public String toString()
  {
    return "ActionItem{" + "scriptLogicalName=" + scriptLogicalName + ", scriptSelector=" + scriptSelector +
           '}';
  }
  
  

}
