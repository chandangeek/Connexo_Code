/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemAttributeValidators.java $
 * Version:     
 * $Id: CosemAttributeValidators.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  10.08.2010 13:56:29
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Classes for DLMS data structure validation
 *
 * @author osse
 */
public class CosemAttributeValidators
{
  /**
   * Helper to validate if an DlsmData object is an array (without checking the array contents).
   */
  public static final AbstractValidator ARRAY_VALIDATOR= new ValidatorSimpleType(DataType.ARRAY);

  private CosemAttributeValidators()
  {
  }
  
  public static interface IDlmsDataValidator
  {
    public void validate(DlmsData data) throws ValidationExecption;
    public boolean isValid(DlmsData data);
  }
  
  public static IDlmsDataValidator immutableValidator(IDlmsDataValidator child)
  {
    return new ImmutableValidator(child);
  }
  
  
  public static class ImmutableValidator implements IDlmsDataValidator
  {
    private final IDlmsDataValidator child;

    public ImmutableValidator(IDlmsDataValidator child)
    {
      this.child = child;
    }

    //@Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      child.validate(data);
    }

    //@Override
    public boolean isValid(final DlmsData data)
    {
      return child.isValid(data);
    }
  }

  public static class ValidationExecption extends Exception
  {
    public ValidationExecption(Throwable cause)
    {
      super(cause);
    }

    public ValidationExecption(String message, Throwable cause)
    {
      super(message, cause);
    }

    public ValidationExecption(String message)
    {
      super(message);
    }

    public ValidationExecption()
    {
      super();
    }

  }

  /**
   * Base class for validators
   */
  public static abstract class AbstractValidator implements IDlmsDataValidator
  {
    public abstract void validate(DlmsData data) throws ValidationExecption;

    public abstract boolean isValid(DlmsData data);

    public abstract void addChild(IDlmsDataValidator validator);

  }

  /**
   * Root for validators.
   */
  public static final class Validator extends AbstractValidator
  {
    private final String name;
    private IDlmsDataValidator root;

    public Validator(String name)
    {
      this.name = name;
    }

    @Override
    public void validate(DlmsData data) throws ValidationExecption
    {
      if (root != null)
      {
        root.validate(data);
      }
    }

    public IDlmsDataValidator getRoot()
    {
      return root;
    }

    public void setRoot(AbstractValidator root)
    {
      this.root = root;
    }

    public String getName()
    {
      return name;
    }

    @Override
    public void addChild(final IDlmsDataValidator validator)
    {
      if (root != null)
      {
        throw new IllegalStateException("Only one child is supported");
      }
      root = validator;
    }

    @Override
    public boolean isValid(final DlmsData data)
    {
      if (root == null)
      {
        return true;
      }
      else
      {
        return root.isValid(data);
      }
    }

  }

  /**
   * Validator that simply checks the type of an DLMS data object.
   * 
   */
  public static class ValidatorSimpleType extends AbstractValidator
  {
    private final DlmsData.DataType requiredType;

    public ValidatorSimpleType(final DataType requiredType)
    {
      this.requiredType = requiredType;
    }

    @Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      if (data.getType() != requiredType)
      {
        throw new ValidationExecption("Unexpected type: " + data.getType() + " expected:"
                                      + requiredType);
      }
    }

    @Override
    public void addChild(final IDlmsDataValidator validator)
    {
      throw new IllegalStateException("Children not supported");
    }

    @Override
    public boolean isValid(final DlmsData data)
    {
      return data.getType() == requiredType;
    }

  }

  /**
   * Validator for arrays.
   * 
   */
  public static class ValidatorArray extends ValidatorSimpleType
  {
    IDlmsDataValidator child;

    public ValidatorArray()
    {
      super(DlmsData.DataType.ARRAY);
    }

    public ValidatorArray(final IDlmsDataValidator child)
    {
      super(DlmsData.DataType.ARRAY);
      addChild(child);
    }

    @Override
    public final void addChild(final IDlmsDataValidator validator)
    {
      if (child != null)
      {
        throw new IllegalStateException("Only one child is supported");
      }
      child = validator;
    }

    @Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      super.validate(data);

      if (child == null)
      {
        return;
      }

      DlmsDataArray array = (DlmsDataArray)data;

      for (DlmsData d : array)
      {
        child.validate(d);
      }
    }

    @Override
    public boolean isValid(final DlmsData data)
    {
      if (!super.isValid(data))
      {
        return false;
      }

      if (child == null)
      {
        return true;
      }

      final DlmsDataArray array = (DlmsDataArray)data;

      for (DlmsData d : array)
      {
        if (!child.isValid(d))
        {
          return false;
        }
      }
      return true;
    }

  }

  /**
   * Validator for structures.
   * 
   */
  public static class ValidatorStructure extends ValidatorSimpleType
  {
    private final List<IDlmsDataValidator> children;

    public ValidatorStructure()
    {
      super(DlmsData.DataType.STRUCTURE);
      this.children = new ArrayList<IDlmsDataValidator>();
    }

    public ValidatorStructure(final IDlmsDataValidator... children)
    {
      super(DlmsData.DataType.STRUCTURE);
      final List<IDlmsDataValidator> temp = new ArrayList<IDlmsDataValidator>(Arrays.asList(children));
      this.children = Collections.unmodifiableList(temp);
    }

    @Override
    public void addChild(final IDlmsDataValidator validator)
    {
      children.add(validator);
    }

    @Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      super.validate(data);
      final DlmsDataStructure structure = (DlmsDataStructure)data;

      if (structure.size() != children.size())
      {
        throw new ValidationExecption("Wrong element count: " + structure.size() + " ,expected:" + children.
                size());
      }

      for (int i = 0; i < children.size(); i++)
      {
        children.get(i).validate(structure.get(i));
      }
    }

    @Override
    public boolean isValid(DlmsData data)
    {
      if (!super.isValid(data))
      {
        return false;
      }

      final DlmsDataStructure structure = (DlmsDataStructure)data;

      if (structure.size() != children.size())
      {
        return false;
      }

      for (int i = 0; i < children.size(); i++)
      {
        if (!children.get(i).isValid(structure.get(i)))
        {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Validator that allows to have multiple choices of DLMS data objects, structures and arrays.<P>
   * Every child is one choice. 
   * 
   */
  public static class ValidatorChoice extends AbstractValidator
  {
    private final List<IDlmsDataValidator> children;

    public ValidatorChoice()
    {
      super();
      children = new ArrayList<IDlmsDataValidator>();
    }
    
    public ValidatorChoice(final IDlmsDataValidator... children)
    {
      super();
      final List<IDlmsDataValidator> temp = new ArrayList<IDlmsDataValidator>(Arrays.asList(children));
      this.children = Collections.unmodifiableList(temp);
    }
    

    @Override
    public void addChild(final IDlmsDataValidator validator)
    {
      children.add(validator);
    }

    @Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      if (!isValid(data))
      {
        throw new ValidationExecption("No choice found");
      }
    }

    @Override
    public boolean isValid(final DlmsData data)
    {
      for (int i = 0; i < children.size(); i++)
      {
        if (children.get(i).isValid(data))
        {
          return true;
        }
      }
      return false;
    }
  }
  
    /**
   * Validator that allows all kind of data.<P>
   * 
   */
  public static class ValidatorAny extends AbstractValidator
  {

    public ValidatorAny()
    {
      super();
    }

    @Override
    public void addChild(final IDlmsDataValidator validator)
    {
      throw new IllegalStateException("Childs not supported");
    }

    @Override
    public void validate(final DlmsData data) throws ValidationExecption
    {
      //nothing to do
    }

    @Override
    public boolean isValid(final DlmsData data)
    {
      return true;
    }
  }

  

  /**
   * Validator with length check for octet strings.
   */
  public static class ValidatorOctetString extends ValidatorSimpleType
  {
    private final int minLength;
    private final int maxLength;

    /**
     * Validator for octet strings.
     *
     * @param minLength Minimum length for the octet string. (-1: the check will not be performed)
     * @param maxLength Maximum length for the octet string. (-1: the check will not be performed)
     */
    public ValidatorOctetString(final int minLength, final int maxLength)
    {
      super(DlmsData.DataType.OCTET_STRING);
      this.minLength = minLength;
      this.maxLength = maxLength;
    }

    public ValidatorOctetString()
    {
      super(DlmsData.DataType.OCTET_STRING);
      this.minLength = -1;
      this.maxLength = -1;
    }

    @Override
    public void validate(DlmsData data) throws ValidationExecption
    {

      super.validate(data);

      DlmsDataOctetString octetString = (DlmsDataOctetString)data;


      if (minLength >= 0 && octetString.size() < minLength)
      {
        throw new ValidationExecption("Octet string to short: " + octetString.size() + " minimum length:"
                                      + minLength);
      }

      if (maxLength >= 0 && octetString.size() > maxLength)
      {
        throw new ValidationExecption("Octet string to long: " + octetString.size() + " maximum length:"
                                      + maxLength);
      }
    }

    @Override
    public boolean isValid(DlmsData data)
    {
      if (!super.isValid(data))
      {
        return false;
      }
      DlmsDataOctetString octetString = (DlmsDataOctetString)data;


      if (minLength >= 0 && octetString.size() < minLength)
      {
        return false;
      }

      if (maxLength >= 0 && octetString.size() > maxLength)
      {
        return false;
      }
      return true;
    }

  }

}
