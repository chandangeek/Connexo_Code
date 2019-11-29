/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemAttributeValidatorsParser.java $
 * Version:     
 * $Id: CosemAttributeValidatorsParser.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  10.08.2010 13:56:29
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.Validator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorAny;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorChoice;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.cosem.classes.info.CosemClassInfo.IdAndVersion;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class parses the cosemAttributeValidators.xml file.
 *
 * @author osse
 */
public final class CosemAttributeValidatorsParser
{
  private static final Logger LOGGER = Logger.getLogger(CosemAttributeValidatorsParser.class.getName());

  public CosemAttributeValidatorsParser()
  {
    try
    {
      parse();
    }
    catch (ParserConfigurationException ex)
    {
      Logger.getLogger(CosemAttributeValidatorsParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SAXException ex)
    {
      Logger.getLogger(CosemAttributeValidatorsParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(CosemAttributeValidatorsParser.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  Map<String, Validator> validatorMap;

  private void parse() throws ParserConfigurationException, SAXException, IOException
  {
    final InputStream in = CosemAttributeValidatorsParser.class.getClassLoader().getResourceAsStream("cosemAttributeValidators.xml");
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      SaxHandler saxHandler = new SaxHandler();
      saxParser.parse(in, saxHandler);

      validatorMap = saxHandler.getValidatorMap();
    }
    finally
    {
      in.close();
    }
  }

  public Map<String, Validator> getValidatorMap()
  {
    return validatorMap;
  }

  private static class SaxHandler extends DefaultHandler
  {
    private final static String E_VALIDATORS = "cosemattributevalidators";
    private final static String E_VALIDATOR = "validator";
    private final static String A_VALIDATOR_NAME = "name";
    private final static String A_LENGTH = "length";
    private final static String A_MAXLENGTH = "maxlength";
    private final static String A_MINLENGTH = "minlength";
    private final static String E_CHOICE = "CHOICE";
    private final static String E_ANY = "ANY";
    Map<CosemClassInfo.IdAndVersion, CosemClassInfo> classMap =
            new HashMap<CosemClassInfo.IdAndVersion, CosemClassInfo>();
    private Validator currentRootValidator = null;
    private final Stack<AbstractValidator> validatorStack = new Stack<AbstractValidator>();
    private final Map<String, Validator> validatorMap = new HashMap<String, Validator>();
    private final Map<String, AbstractValidator> xmlElementNameToValidatorMap =
            new HashMap<String, AbstractValidator>();

    public SaxHandler()
    {
      for (DlmsData.DataType dt : DlmsData.DataType.values())
      {
        xmlElementNameToValidatorMap.put(dt.getOrgName(), new ValidatorSimpleType(dt));
      }

    }

    public Map<String, Validator> getValidatorMap()
    {
      return validatorMap;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws
            SAXException
    {

      AbstractValidator newValidator = null;

      LOGGER.log(Level.FINER, "uri={0}, localName={1}, qName={2}", new Object[]
              {
                uri, localName, qName
              });
      if (E_VALIDATORS.equals(qName))
      {
      }
      else if (E_VALIDATOR.equals(qName))
      {
        String name = attributes.getValue(A_VALIDATOR_NAME);
        currentRootValidator = new Validator(name);
        newValidator = currentRootValidator;
      }
      else if (E_CHOICE.equals(qName))
      {
        newValidator = new ValidatorChoice();
      }
      else if (E_ANY.equals(qName))
      {
        newValidator = new ValidatorAny();
      }
      else if (DlmsData.DataType.ARRAY.getOrgName().equals(qName))
      {
        newValidator = new ValidatorArray();
      }
      else if (DlmsData.DataType.STRUCTURE.getOrgName().equals(qName))
      {
        newValidator = new ValidatorStructure();
      }
      else if (DlmsData.DataType.OCTET_STRING.getOrgName().equals(qName))
      {
        String strMaxLength = attributes.getValue(A_MAXLENGTH);
        String strMinLength = attributes.getValue(A_MINLENGTH);
        String strLength = attributes.getValue(A_LENGTH);

        int maxLength = -1;
        int minLength = -1;

        if (strLength != null)
        {
          int length = Integer.parseInt(strLength);
          maxLength = length;
          minLength = length;
        }

        if (strMinLength != null)
        {
          minLength = Integer.parseInt(strMinLength);
        }

        if (strMaxLength != null)
        {
          maxLength = Integer.parseInt(strMaxLength);
        }

        newValidator = new ValidatorOctetString(minLength, maxLength);
      }
      else
      {

        newValidator = xmlElementNameToValidatorMap.get(qName);
        if (newValidator == null)
        {
          throw new IllegalStateException("Unknown element: " + qName);
        }
      }

      if (newValidator != null)
      {
        if (!validatorStack.isEmpty())
        {
          validatorStack.peek().addChild(newValidator);
        }
        validatorStack.push(newValidator);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
      if (E_VALIDATOR.equals(qName))
      {
        if (currentRootValidator != validatorStack.pop())
        {
          throw new IllegalStateException("Unexpected element on stack");
        }

        validatorMap.put(currentRootValidator.getName(), currentRootValidator);
        currentRootValidator = null;
      }
      else if (!validatorStack.isEmpty())
      {
        validatorStack.pop();
      }
    }

    public Map<IdAndVersion, CosemClassInfo> getCosemClassMap()
    {
      return classMap;
    }

  }

}
