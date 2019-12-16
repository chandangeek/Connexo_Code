/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class64/KeyData.java $
 * Version:     
 * $Id: KeyData.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 5, 2011 10:20:21 AM
 */
package com.elster.dlms.cosem.classes.class64;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Key data for the global key transfer<P>
 * See BB ed.10 p.72
 *
 * @author osse
 */
public class KeyData implements IDlmsDataProvider
{
  public static final KeyData[] EMPTY_KEY_DATA_ARRAY= new KeyData[0];
  
  public enum KeyId
  {
    GLOBAL_UNICAST_ENCRYPTION_KEY, GLOBAL_BROADCAST_ENCRYPTION_KEY, AUTHETICATION_KEY
  }

  private final KeyId keyId;
  private final byte[] keyWrapped;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.ENUM),
          new ValidatorSimpleType(DataType.OCTET_STRING));

  public KeyData(final KeyId keyId,final byte[] keyWrapped)
  {
    this.keyId = keyId;
    this.keyWrapped = keyWrapped.clone();
  }

  public KeyData(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;

    switch (((DlmsDataEnum)structure.get(0)).getValue())
    {
      case 0:
        this.keyId = KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY;
        break;
      case 1:
        this.keyId = KeyId.GLOBAL_BROADCAST_ENCRYPTION_KEY;
        break;
      case 2:
        this.keyId = KeyId.AUTHETICATION_KEY;
        break;
      default:
        throw new ValidationExecption("Unexepected key id");
    }
    this.keyWrapped = ((DlmsDataOctetString)structure.get(0)).getValue();
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataEnum(keyId.ordinal()),
            new DlmsDataOctetString(keyWrapped));
  }

  public KeyId getKeyId()
  {
    return keyId;
  }

  public byte[] getKeyWrapped()
  {
    return keyWrapped.clone();
  }

  
}
