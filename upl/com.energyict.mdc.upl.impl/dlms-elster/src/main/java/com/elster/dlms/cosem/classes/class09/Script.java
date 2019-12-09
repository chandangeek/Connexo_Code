/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class09/Script.java $
 * Version:     
 * $Id: Script.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.07.2011 10:09:04
 */
package com.elster.dlms.cosem.classes.class09;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataStructure;

/**
 * Script for script tables. (see BB ed.10 p.75)
 *
 * @author osse
 */
public class Script
{
  private final int scriptIdentifier;
  private final ActionSpecification[] actions;
  public static final AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          ActionSpecification.LIST_VALIDATOR);
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static Script[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    Script[] result = new Script[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new Script(array.get(i));
    }
    return result;
  }

  public Script(int scriptIdentifier, ActionSpecification[] actions)
  {
    this.scriptIdentifier = scriptIdentifier;
    this.actions = actions.clone();
  }

  public Script(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    DlmsDataStructure structure = (DlmsDataStructure)data;
    this.scriptIdentifier = ((DlmsDataLongUnsigned)structure.get(0)).getValue();
    this.actions = ActionSpecification.fromDlmsDataArray(structure.get(1));
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(scriptIdentifier),
            ActionSpecification.toDlmsDataArray(actions));
  }

  public ActionSpecification[] getActions()
  {
    return actions.clone();
  }

  public int getScriptIdentifier()
  {
    return scriptIdentifier;
  }

  @Override
  public String toString()
  {
    return "Script{" + "scriptIdentifier=" + scriptIdentifier + ", "+actions.length+" actions }";
  }
  
  

}
