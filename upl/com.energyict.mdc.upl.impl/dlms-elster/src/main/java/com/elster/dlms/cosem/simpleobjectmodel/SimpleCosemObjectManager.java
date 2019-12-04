/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleCosemObjectManager.java $
 * Version:     
 * $Id: SimpleCosemObjectManager.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 1:48:22 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class15.AccessSelectorElsObjectIdList;
import com.elster.dlms.cosem.classes.class15.CosemObjectListElement;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemClassInfos;
import com.elster.dlms.cosem.objectmodel.CosemAccessSelector;
import com.elster.dlms.types.basic.*;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Manages the COSEM objects in the Simple COSEM Object Model.
 *
 * @author osse
 */
public class SimpleCosemObjectManager
{
  private final CosemApplicationLayer applicationLayer;
  private final Map<ObisCode, SimpleCosemObjectDefinition> definitions =
          new HashMap<ObisCode, SimpleCosemObjectDefinition>();
  private final Map<ObisCode, SimpleCosemObject> objects =
          new HashMap<ObisCode, SimpleCosemObject>();
  // private final AttributeObjectFactory attributeObjectFactory = new AttributeObjectFactory();
  private final CosemClassInfos cosemClassInfos = CosemClassInfos.getInstance();
  private final CosemAttributeCache cache = new CosemAttributeCache();
  private final static SimpleCosemObjectDefinition CURRENT_ASSOCIATION =
          new SimpleCosemObjectDefinition(CosemClassIds.ASSOCIATION_LN, 1, CommonDefs.CURRENT_ASSOCIATION);
  public static final Set<Integer> HANDLED_CLASS_IDS = createHandledClassIdsSet();

  /**
   * Creates the COSEM object manager.
   *
   * @param applicationLayer The application layer. All operation are done thru this application layer.
   * @param definitions The definitions for the Simple COSEM Object Model.
   */
  public SimpleCosemObjectManager(final CosemApplicationLayer applicationLayer,
                                  final SimpleCosemObjectDefinition[] definitions)
  {
    this.applicationLayer = applicationLayer;
    addDefinitions(definitions);
  }

  /**
   * Adds the specified definitions to the COSEM Object Model.
   *
   * @param defs The definitions to add.
   */
  public final void addDefinitions(final SimpleCosemObjectDefinition[] defs)
  {
    for (SimpleCosemObjectDefinition def : defs)
    {
      definitions.put(def.getLogicalName(), def);
    }
  }

  public void addDefinition(final SimpleCosemObjectDefinition def)
  {
    definitions.put(def.getLogicalName(), def);
  }

  /**
   * Returns the {@link SimpleCosemObjectDefinition } for the specified OBIS code.<P>
   * 
   * 
   * @param obisCode The OBIS code.
   * @return The definition for the specified ObisCode or null if the definition is not available.
   */
  public SimpleCosemObjectDefinition findDefinition(final ObisCode obisCode)
  {
    return definitions.get(obisCode);
  }

  /**
   * Returns the specified {@link SimpleCosemObject }.<P>
   *
   * @param logicalName The logical name (OBIS code) of the object to return.
   * @return The COSEM object.
   * @throws IOException
   */
  public SimpleCosemObject getSimpleCosemObject(final ObisCode logicalName) throws
          IOException
  {
    SimpleCosemObject simpleCosemObject = objects.get(logicalName);

    if (simpleCosemObject == null)
    {
      simpleCosemObject = createCosemObject(logicalName);
      objects.put(logicalName, simpleCosemObject);
    }

    return simpleCosemObject;
  }

  /**
   * Returns the specified {@link SimpleCosemObject }.<P>
   *
   * @param <T> The expected type (class) of the COSEM object. If the COSEM Object has an other type an exception will be thrown.
   * @param logicalName  The logical name (OBIS code) of the object to return.
   * @param type See {@code <T>}
   * @return The COSEM Object.
   * @throws IOException
   */
  public <T extends SimpleCosemObject> T getSimpleCosemObject(final ObisCode logicalName,
                                                              final Class<T> type) throws
          IOException
  {
    final SimpleCosemObject object = getSimpleCosemObject(logicalName);

    if (!type.isAssignableFrom(object.getClass()))
    {
      throw new IOException("Unexpected COSEM-class");
    }

    return type.cast(object);
  }

  /**
   * Returns the serial number of the device.<P>
   * The serial number will be read as necessary.<P>
   * Leading zeros will be removed.
   *
   * @return The serial number.
   * @throws IOException
   */
  public String getSerialNumber() throws IOException
  {
    final SimpleDataObject dataObject =
            getSimpleCosemObject(CommonDefs.SERIAL_NUMBER, SimpleDataObject.class);

    final String v = dataObject.getValueAsString();
    if (v == null)
    {
      throw new IOException("Unexpected data type for serial number."
                            + " Found:" + dataObject.getValue().getType().getOrgName());
    }
    return v;
  }

  /**
   * Returns the software version of the device as string.
   * The software version will be read as necessary.
   *
   * @return The software version
   * @throws IOException
   */
  public String getSoftwareVersion() throws IOException
  {
    final SimpleRegisterObject dataObject = getSimpleCosemObject(CommonDefs.SOFTWARE_VERSION,
                                                                 SimpleRegisterObject.class);
    final BigDecimal v = dataObject.getScaledValue();

    if (v == null)
    {
      throw new IOException("Unexpected data type for software version. Found:" + dataObject.getValue().
              getType().getOrgName());
    }
    return v.toString();
  }

  /**
   * Returns the date and time of the device.<P>
   * The value will always be read from the device.
   *
   * @return The date an the time.
   * @throws IOException
   */
  public DlmsDateTime getDateTime() throws IOException
  {
    final SimpleClockObject clockObject = getSimpleCosemObject(CommonDefs.CLOCK_OBJECT,
                                                               SimpleClockObject.class);
    return clockObject.getTime();
  }

  /**
   * Sets the new date time in the device.
   * 
   * @param dateTime The date time to set.
   * @throws IOException 
   */
  public void setDateTime(final DlmsDateTime dateTime) throws IOException
  {
    final SimpleClockObject clockObject = getSimpleCosemObject(CommonDefs.CLOCK_OBJECT,
                                                               SimpleClockObject.class);
    clockObject.setTime(dateTime);
  }

  private SimpleCosemObject createCosemObject(final ObisCode logicalName) throws IOException
  {
    SimpleCosemObjectDefinition definition = definitions.get(logicalName);

    if (definition == null)
    {
      definition = readAndAddDefinition(logicalName);
    }

    if (definition == null)
    {
      //TODO: change the type of the exeption.
      throw new IllegalArgumentException("No definition found for OBIS-Code: " + logicalName.toString());
    }

    switch (definition.getClassId())
    {
      case CosemClassIds.DATA:
        return new SimpleDataObject(definition, this);
      case CosemClassIds.REGISTER:
        return new SimpleRegisterObject(definition, this);
      case CosemClassIds.CLOCK:
        return new SimpleClockObject(definition, this);
      case CosemClassIds.EXTENDED_REGISTER:
        return new SimpleExtendedRegisterObject(definition, this);
      case CosemClassIds.ASSOCIATION_LN:
        return new SimpleAssociationLnObject(definition, this);
      case CosemClassIds.SPECIAL_DAYS_TABLE:
        return new SimpleSpecialDaysTable(definition, this);
      case CosemClassIds.ACTIVITY_CALENDAR:
        return new SimpleActivityCalendarObject(definition, this);
      case CosemClassIds.GPRS_MODEM_SETUP:
        return new SimpleGprsModemSetupObject(definition, this);
      case CosemClassIds.PROFILE_GENERIC:
        return new SimpleProfileObject(definition, this);
      case CosemClassIds.IMAGE_TRANSFER:
        return new SimpleImageTransferObject(definition, this, applicationLayer);
      case CosemClassIds.AUTO_CONNECT:
        return new SimpleAutoConnectObject(definition, this);
      case CosemClassIds.AUTO_ANSWER:
        return new SimpleAutoAnswerObject(definition, this);
      case CosemClassIds.SECURITY_SETUP:
        return new SimpleSecuritySetupObject(definition, this);
      default:
        return new SimpleCosemObject(definition, this);

    }
  }

  public SimpleProfileObject readProfile(final ObisCode logicalName, final Date from, final Date to) throws
          IOException
  {
    final SimpleProfileObject result = getSimpleCosemObject(logicalName, SimpleProfileObject.class);
    result.readProfileData(from, to);
    return result;
  }

  /*package private*/
  void executeSetData(final SimpleCosemObjectDefinition definition, final int attributeId,
                      final DlmsData data)
          throws
          IOException
  {
    cache.deleteAttribute(definition.getLogicalName(), attributeId);
    applicationLayer.setAttributeAndCheckResult(new CosemAttributeDescriptor(definition.getLogicalName(),
                                                                             definition.getClassId(),
                                                                             attributeId), data, null);
  }

  private void validateData(final SimpleCosemObjectDefinition definition,
                            CosemAttributeDescriptor attributeDescriptor,
                            final DlmsData data)
          throws UnexpectedDlmsDataTypeIOException
  {
    try
    {
      //The resulting data of class 15 attr. 2 can depend on the access selection parameters. 
      //TODO: repspect the different data types.
      boolean skipValidation = attributeDescriptor.getAccessSelectionParameters() != null
                               && definition.getClassId() == CosemClassIds.ASSOCIATION_LN
                               && attributeDescriptor.getAttributeId() == 2;
      if (!skipValidation)
      {
        final CosemAttributeInfo attributeInfo =
                cosemClassInfos.getAttributeInfo(definition.getClassId(), definition.getClassVersion(),
                                                 attributeDescriptor.getAttributeId());

        if (attributeInfo != null)
        {
          attributeInfo.validateData(data);
        }
      }
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  /**
   * Executes the COSEM GET service and validates the received data.<P>
   * The validation ensures that the received data has the required DLMS-data formats as defined by
   * the BB (ed.10)
   * 
   * package private
   */
  DlmsData executeGetData(final SimpleCosemObjectDefinition definition, final int attributeId,
                          final CosemAccessSelector accessSelector, final boolean forceUpdate) throws
          IOException
  {
    if (accessSelector == null)
    {
      throw new IllegalArgumentException("accessSelector must not be null");
    }

    final AccessSelectionParameters selectionParameters =
            new AccessSelectionParameters(accessSelector.getId(), accessSelector.toDlmsData());

    final CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(
            definition.getLogicalName(),
            definition.getClassId(),
            attributeId,
            selectionParameters);

    return executeGetData(definition, attributeDescriptor, forceUpdate);
  }

  /**
   * Executes the COSEM GET service and validates the received data.<P>
   * The validation ensures that the received data has the required DLMS-data formats as defined by
   * the BB (ed.). 
   * 
   * package private
   */
  DlmsData executeGetData(final SimpleCosemObjectDefinition definition, final int attributeId,
                          final boolean forceUpdate) throws
          IOException
  {

    final CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(definition.
            getLogicalName(),
                                                                                      definition.getClassId(),
                                                                                      attributeId);
    return executeGetData(definition, attributeDescriptor, forceUpdate);
  }

  private DlmsData executeGetData(final SimpleCosemObjectDefinition definition,
                                  final CosemAttributeDescriptor attributeDescriptor,
                                  final boolean forceUpdate) throws
          IOException
  {
    GetDataResult getDataResult = null;

    if (!forceUpdate)
    {
      getDataResult = cache.getDataResult(attributeDescriptor);
    }

    if (getDataResult == null)
    {
      getDataResult = applicationLayer.getAttribute(attributeDescriptor);
    }

    cache.putDataResult(attributeDescriptor, getDataResult);

    if (getDataResult.getAccessResult() != DataAccessResult.SUCCESS)
    {
      CosemApplicationLayer.checkGetDataResult(attributeDescriptor, getDataResult);
    }

    validateData(definition, attributeDescriptor, getDataResult.getData());
    return getDataResult.getData();
  }

  /**
   * Executes the COSEM GET service and validates the received data.<P>
   * The validation ensures that the received data has the required DLMS-data formats as defined by
   * the BB (ed.). 
   * 
   * package private
   */
  <T extends DlmsData> T executeGetData(final SimpleCosemObjectDefinition definition,
                                        final int attributeId,
                                        final Class<T> expectedDataType, final boolean forceUpdate) throws
          IOException
  {
    final DlmsData data = executeGetData(definition, attributeId, forceUpdate);

    if (expectedDataType.isAssignableFrom(data.getClass()))
    {
      return expectedDataType.cast(data);
    }
    else
    {
      throw new UnexpectedDlmsDataTypeIOException("Expected DLMS data type: "
                                                  + expectedDataType.getSimpleName()
                                                  + ", Received data type:" + data.getClass().getSimpleName());
    }
  }

  /*package private*/
  DlmsData executeMethod(final SimpleCosemObjectDefinition definition, final int methodID,
                         final DlmsData parameters) throws IOException
  {
    final DlmsData returnParameters = applicationLayer.executeActionAndCheckResponse(
            new CosemMethodDescriptor(definition.getLogicalName(), definition.getClassId(), methodID),
            parameters);
    return returnParameters;
  }

  /*package private*/
  void removeFromCache(final ObisCode obisCode)
  {
    cache.deleteAttributes(obisCode);
  }

  /*package private*/
  void removeFromCache(final ObisCode obisCode, final int attributeId)
  {
    cache.deleteAttribute(obisCode, attributeId);
  }

  private boolean allDefinitionsRead = false;

  public void readAllDefinitions() throws IOException
  {
    try
    {
      final DlmsDataArray data = executeGetData(CURRENT_ASSOCIATION, 2, DlmsDataArray.class, false);
      final CosemObjectListElement[] objectListArray = CosemObjectListElement.buildElements(data);

      for (CosemObjectListElement e : objectListArray)
      {
        SimpleCosemObjectDefinition definition =
                new SimpleCosemObjectDefinition(e.getClassId(), e.getVersion(), e.getLogicalName());
        addDefinition(definition);
      }
      allDefinitionsRead = true;
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }

  }

  SimpleCosemObjectDefinition readAndAddDefinition(ObisCode logicalName) throws IOException
  {
    if (allDefinitionsRead)
    {
      return null;
    }

    try
    {

      final AccessSelectorElsObjectIdList accessSelector = new AccessSelectorElsObjectIdList(logicalName);
      final DlmsData data = executeGetData(CURRENT_ASSOCIATION, 2, accessSelector, false);
      CosemObjectListElement[] elements =
              CosemObjectListElement.buildElements(data);

      if (elements.length == 0)
      {
        return null;
      }

      //Currently the EK280 will return 0 or 1 element.
      SimpleCosemObjectDefinition definition =
              new SimpleCosemObjectDefinition(elements[0].getClassId(), elements[0].getVersion(), elements[0].
              getLogicalName());
      addDefinition(definition);

      return definition;


    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  private static Set<Integer> createHandledClassIdsSet()
  {
    Set<Integer> classIds = new TreeSet<Integer>();
    classIds.add(CosemClassIds.DATA);
    classIds.add(CosemClassIds.REGISTER);
    classIds.add(CosemClassIds.CLOCK);
    classIds.add(CosemClassIds.EXTENDED_REGISTER);
    classIds.add(CosemClassIds.ASSOCIATION_LN);
    classIds.add(CosemClassIds.SPECIAL_DAYS_TABLE);
    classIds.add(CosemClassIds.ACTIVITY_CALENDAR);
    classIds.add(CosemClassIds.GPRS_MODEM_SETUP);
    classIds.add(CosemClassIds.PROFILE_GENERIC);
    classIds.add(CosemClassIds.IMAGE_TRANSFER);
    classIds.add(CosemClassIds.AUTO_CONNECT);
    classIds.add(CosemClassIds.AUTO_ANSWER);
    classIds.add(CosemClassIds.SECURITY_SETUP);
    return Collections.unmodifiableSet(classIds);
  }

}
