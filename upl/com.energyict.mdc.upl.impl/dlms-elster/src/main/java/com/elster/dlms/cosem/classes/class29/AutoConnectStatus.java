/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class29/AutoConnectStatus.java $
 * Version:     
 * $Id: AutoConnectStatus.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  13.01.2012 16:44:17
 */
package com.elster.dlms.cosem.classes.class29;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Type class for the elster specific auto connect status.
 *
 * @author osse
 */
public class AutoConnectStatus implements IDlmsDataProvider
{
  private final int channelState;
  private final int[] destinationStates;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.ENUM),
          new ValidatorArray(new ValidatorSimpleType(DlmsData.DataType.ENUM)));
  
  
  public AutoConnectStatus(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    
    final DlmsDataStructure structure= (DlmsDataStructure)data;
    
    final DlmsDataEnum channelStateData= (DlmsDataEnum)structure.get(0);
    this.channelState= channelStateData.getValue();
    
    final DlmsDataArray array= (DlmsDataArray)structure.get(1);
    
    destinationStates= new int[array.size()];
    
    for (int i=0; i<destinationStates.length; i++)
    {
      destinationStates[i]= ((DlmsDataEnum) array.get(i)).getValue();
    }
  }

  public AutoConnectStatus(final int channelState,final int[] destinationStates)
  {
    this.channelState = channelState;
    this.destinationStates = destinationStates.clone();
  }

  public int getChannelState()
  {
    return channelState;
  }

  public int[] getDestinatinoState()
  {
    return destinationStates.clone();
  }

  //@Override
  public DlmsData toDlmsData()
  {
    DlmsDataEnum[] destinationStatesData = new DlmsDataEnum[destinationStates.length];

    for (int i = 0; i < destinationStatesData.length; i++)
    {
      destinationStatesData[i] = new DlmsDataEnum(destinationStates[i]);
    }
    return new DlmsDataStructure(new DlmsDataEnum(channelState),new DlmsDataArray(destinationStatesData));
  }

}
