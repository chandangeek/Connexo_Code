/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class21/ActionSet.java $
 * Version:     
 * $Id: ActionSet.java 3583 2011-09-28 15:44:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.08.2011 09:00:41
 */
package com.elster.dlms.cosem.classes.class21;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Action Set for the Register monitor (IC 21 Attr. 4)<P>
 * See BB ed.10 p.83
 *
 * @author osse
 */
public class ActionSet implements IDlmsDataProvider
{ 
  private final ActionItem actionUp;
  private final ActionItem actionDown;
  
  public final static IDlmsDataValidator VALIDATOR = CosemAttributeValidators.immutableValidator(
          new ValidatorStructure(
          ActionItem.VALIDATOR,
          ActionItem.VALIDATOR
          ));
  
  public final static IDlmsDataValidator LIST_VALIDATOR
          = CosemAttributeValidators.immutableValidator(new ValidatorArray(VALIDATOR));

  public static ActionSet[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    ActionSet[] result = new ActionSet[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new ActionSet(array.get(i));
    }
    return result;
  }

  public ActionSet(ActionItem actionUp, ActionItem actionDown)
  {
    this.actionUp = actionUp;
    this.actionDown = actionDown;
  }

  public ActionSet(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;
   
    this.actionUp= new ActionItem(structure.get(0));
    this.actionDown= new ActionItem(structure.get(1));
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            actionUp.toDlmsData(),
            actionDown.toDlmsData());
  }

  public ActionItem getActionDown()
  {
    return actionDown;
  }

  public ActionItem getActionUp()
  {
    return actionUp;
  }

  @Override
  public String toString()
  {
    return "ActionSet{" + "actionUp=" + actionUp + ", actionDown=" + actionDown + '}';
  }
  
  
  
  


}
