/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemAttributeInfo.java $
 * Version:     
 * $Id: CosemAttributeInfo.java 4437 2012-04-30 14:29:56Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  10.08.2010 15:12:32
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.Validator;
import com.elster.dlms.types.data.DlmsData;

/**
 * Information for an COSEM class attribute.
 *
 * @author osse
 */
public class CosemAttributeInfo
{
  public enum OctetStringType {OCTETS, DATE_TIME, DATE, TIME, ASCII_STRING}
    

  private final int id;
  private final String name;
  private final Validator validator;
  private final OctetStringType octetStringType;

  /**
   * Creates the an COSEM Attribute info.
   * 
   * @param id The number of the attribute
   * @param name The name of the attribute as declared in the BB.
   */
  public CosemAttributeInfo(final int id,final String name,final Validator validator, final OctetStringType octetStringType)
  {
    this.id = id;
    this.name = name;
    this.validator= validator;
    this.octetStringType= octetStringType;
  }

  /**
   * The name as declared in the BB.
   *
   * @return The name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * The id of the attribute.
   * 
   * @return The id
   */
  public int getId()
  {
    return id;
  }

  public OctetStringType getOctetStringType()
  {
    return octetStringType;
  }


  /**
   * Validates if the structure or type of {@code data} is valid for this attribute
   *
   * @param data The data to check
   * @throws com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption
   */
  public void validateData(final DlmsData data) throws ValidationExecption
  {
    if (validator!=null)
    {
      validator.validate(data);
    }
  }

  /**
   * Return the {@link Validator } for this attribute.
   *
   * @return The validator or {@code null} if no valildator is available.
   */
  public Validator getValidator()
  {
    return validator;
  }



}
