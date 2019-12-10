/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class28/NumberOfRings.java $
 * Version:     
 * $Id: NumberOfRings.java 3583 2011-09-28 15:44:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.08.2011 13:31:52
 */
package com.elster.dlms.cosem.classes.class28;


import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Number of rings structure<P>
 * IC 28 Attr.6 <P>
 * See BB ed.10 p.100
 *
 * @author osse
 */
public class NumberOfRings implements IDlmsDataProvider
{
  private final int nrRingsInWindow;
  private final int nrRingsOutOfWindow;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.UNSIGNED),
          new ValidatorSimpleType(DlmsData.DataType.UNSIGNED));

  public NumberOfRings(final int nrRingsInWindow,final int nrRingsOutOfWindow)
  {
    this.nrRingsInWindow = nrRingsInWindow;
    this.nrRingsOutOfWindow = nrRingsOutOfWindow;
  }

  public NumberOfRings(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;
    final DlmsDataUnsigned nrRingsInWindowData = (DlmsDataUnsigned)structure.get(0);
    final DlmsDataUnsigned nrRingsOutOfWindowData = (DlmsDataUnsigned)structure.get(1);

    nrRingsInWindow = nrRingsInWindowData.getValue();
    nrRingsOutOfWindow = nrRingsOutOfWindowData.getValue();
  }

  public int getNrRingsInWindow()
  {
    return nrRingsInWindow;
  }

  public int getNrRingsOutOfWindow()
  {
    return nrRingsOutOfWindow;
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataUnsigned(nrRingsInWindow),
            new DlmsDataUnsigned(nrRingsOutOfWindow));
  }

  @Override
  public String toString()
  {
    return "NumberOfRings{" + "nrRingsInWindow=" + nrRingsInWindow + ", nrRingsOutOfWindow="
           + nrRingsOutOfWindow + '}';
  }

}
