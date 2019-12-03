/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/info/CosemClassInfos.java $
 * Version:     
 * $Id: CosemClassInfos.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  10.08.2010 13:56:29
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.cosem.classes.info.CosemAttributeInfo.OctetStringType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.Validator;
import com.elster.dlms.cosem.classes.info.CosemClassInfo.IdAndVersion;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class provides additional information for COSEM classes.<P>
 * The information will be read from an (XML) information file.<P>
 * Use {@link #getInstance()} to get an instance.
 *
 * @author osse
 */
public final class CosemClassInfos
{
  private static final Logger LOGGER = Logger.getLogger(CosemClassInfos.class.getName());
  private static CosemClassInfos defaultInstance;

  /**
   * Returns an instance of this class.
   *
   * @return The instance.
   */
  public static synchronized CosemClassInfos getInstance()
  {
    if (defaultInstance == null)
    {
      defaultInstance = new CosemClassInfos();
    }
    return defaultInstance;
  }

  private CosemClassInfos()
  {
    try
    {
      parse();
    }
    catch (ParserConfigurationException ex)
    {
      Logger.getLogger(CosemClassInfos.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SAXException ex)
    {
      Logger.getLogger(CosemClassInfos.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(CosemClassInfos.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private Map<CosemClassInfo.IdAndVersion, CosemClassInfo> cosemClassMap;

  private void parse() throws ParserConfigurationException, SAXException, IOException
  {
    final CosemAttributeValidatorsParser validatorParser = new CosemAttributeValidatorsParser();

    final InputStream in = CosemClassInfos.class.getClassLoader().getResourceAsStream("cosemClassInfo.xml");
    try
    {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      final SaxHandler saxHandler = new SaxHandler(validatorParser.getValidatorMap());
      saxParser.parse(in, saxHandler);
      cosemClassMap = saxHandler.getCosemClassMap();
    }
    finally
    {
      in.close();
    }
  }

  // package private access to the map for unit tests.
  Map<IdAndVersion, CosemClassInfo> getCosemClassMap()
  {
    return cosemClassMap;
  }

  /**
   * Returns the {@link CosemClassInfo } for specified class id and class version.
   * 
   * @param classId The COSEM class id.
   * @param classVersion The COSEM class version.
   * @return The {@link CosemClassInfo } or <b>null</b> if no information is available.
   */
  public CosemClassInfo getInfo(final int classId, final int classVersion)
  {
    return getCosemClassMap().get(new CosemClassInfo.IdAndVersion(classId, classVersion));
  }

  /**
   * Returns the {@link CosemAttributeInfo }  for specified class id, class version and attribute id.
   *
   * @param classId The COSEM class id.
   * @param classVersion The COSEM class version.
   * @param attributeId The attribute id.
   * @return The {@link CosemAttributeInfo } or <b>null</b>  if no information was found.
   */
  public CosemAttributeInfo getAttributeInfo(final int classId, final int classVersion, final int attributeId)
  {
    final CosemClassInfo info = getCosemClassMap().get(new CosemClassInfo.IdAndVersion(classId, classVersion));

    if (info == null)
    {
      return null;
    }

    for (CosemAttributeInfo a : info.getAttributes())
    {
      if (a.getId() == attributeId)
      {
        return a;
      }
    }
    return null;
  }

  /**
   * Returns the {@link CosemMethodInfo } for specified class id, class version and method id.
   *
   * @param classId The COSEM class id.
   * @param classVersion The COSEM class version.
   * @param methodId The method id.
   * @return The {@link CosemAttributeInfo } or <b>null</b>  if no information was found.
   */
  public CosemMethodInfo getMethodInfo(final int classId, final int classVersion, final int methodId)
  {
    final CosemClassInfo info = getCosemClassMap().get(new CosemClassInfo.IdAndVersion(classId, classVersion));

    if (info == null)
    {
      return null;
    }

    for (CosemMethodInfo m : info.getMethods())
    {
      if (m.getNumber() == methodId)
      {
        return m;
      }
    }
    return null;
  }

  /**
   * Very simple (and not safe) SAX handler for the class information file.<P>
   * (The implementation can be simple, because the information file is an internal file)
   *
   */
  private static class SaxHandler extends DefaultHandler
  {
    private final static String E_COSEMCLASS = "cosemclass";
    private final static String A_COSEMCLASS_ID = "id";
    private final static String A_COSEMCLASS_NAME = "name";
    private final static String A_COSEMCLASS_VERSION = "version";
    private final static String E_ATTRIBUTE = "attribute";
    private final static String A_ATTRIBUTE_NUMBER = "number";
    private final static String A_ATTRIBUTE_NAME = "name";
    private final static String A_ATTRIBUTE_VALIDATOR = "validator";
    private final static String A_ATTRIBUTE_OCTETSTRING_TYPE = "octetStringType";
    private final static String E_METHOD = "method";
    private final static String A_METHOD_NUMBER = "number";
    private final static String A_METHOD_NAME = "name";
    private final Map<CosemClassInfo.IdAndVersion, CosemClassInfo> classMap =
            new HashMap<CosemClassInfo.IdAndVersion, CosemClassInfo>();
    private CosemClassInfo currentClass = null;
    private final Map<String, Validator> validators;

    public SaxHandler(Map<String, Validator> validators)
    {
      this.validators = validators;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws
            SAXException
    {
      LOGGER.log(Level.FINER, "uri={0}, localName={1}, qName={2}", new Object[]
              {
                uri, localName, qName
              });
      if (E_COSEMCLASS.equals(qName))
      {
        final String id = attributes.getValue(A_COSEMCLASS_ID);
        final String version = attributes.getValue(A_COSEMCLASS_VERSION);
        final String name = attributes.getValue(A_COSEMCLASS_NAME);

        currentClass = new CosemClassInfo(Integer.parseInt(id), Integer.parseInt(version),
                                          name);
        classMap.put(currentClass.getIdAndVersion(), currentClass);
      }
      else if (E_ATTRIBUTE.equals(qName))
      {
        final String no = attributes.getValue(A_ATTRIBUTE_NUMBER);
        final String name = attributes.getValue(A_ATTRIBUTE_NAME);
        final String validatorName = attributes.getValue(A_ATTRIBUTE_VALIDATOR);
        final String octetStringTypeName = attributes.getValue(A_ATTRIBUTE_OCTETSTRING_TYPE);


        Validator validator = null;
        if (validatorName != null)
        {
          validator = validators.get(validatorName);
          if (validator == null)
          {
            throw new IllegalStateException("Validator not found: " + validatorName);
          }
        }
        OctetStringType octetStringType = null;
        if (octetStringTypeName != null)
        {
          octetStringType =
                  OctetStringType.valueOf(octetStringTypeName);
        }
        else
        {
          octetStringType= OctetStringType.OCTETS;
        }


        final CosemAttributeInfo attributeInfo = new CosemAttributeInfo(Integer.parseInt(no), name, validator,
                                                                  octetStringType);
        currentClass.getAttributes().add(attributeInfo);
      }
      else if (E_METHOD.equals(qName))
      {
        String no = attributes.getValue(A_METHOD_NUMBER);
        String name = attributes.getValue(A_METHOD_NAME);
        CosemMethodInfo methodInfo = new CosemMethodInfo(Integer.parseInt(no), name);
        currentClass.getMethods().add(methodInfo);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
      if (E_COSEMCLASS.equals(qName))
      {
        currentClass = null;
      }
    }

    public Map<IdAndVersion, CosemClassInfo> getCosemClassMap()
    {
      return classMap;
    }

  }

}
