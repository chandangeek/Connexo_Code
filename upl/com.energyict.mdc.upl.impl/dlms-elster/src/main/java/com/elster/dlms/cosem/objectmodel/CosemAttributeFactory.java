/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemAttributeFactory.java $
 * Version:     
 * $Id: CosemAttributeFactory.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 9, 2010 9:47:46 AM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.classes.class15.CosemAttributeObjectList;
import com.elster.dlms.cosem.classes.class03.CosemAttributeScalerUnit;
import com.elster.dlms.cosem.classes.class06.CosemAttributeRegisterAssignment;
import com.elster.dlms.cosem.classes.class06.CosemAttributeRegisterMaskList;
import com.elster.dlms.cosem.classes.class07.CosemAttributeBuffer;
import com.elster.dlms.cosem.classes.class07.CosemAttributeCaptureObjects;
import com.elster.dlms.cosem.classes.class07.CosemAttributeSortObject;
import com.elster.dlms.cosem.classes.class07.SortMethodEnum;
import com.elster.dlms.cosem.classes.class08.ClockBaseEnum;
import com.elster.dlms.cosem.classes.class09.CosemAttributeScripts;
import com.elster.dlms.cosem.classes.class11.CosemAttributeSpecialDayEntries;
import com.elster.dlms.cosem.classes.class17.CosemAttributeSapAssignmentList;
import com.elster.dlms.cosem.classes.class18.CosemAttributeImageToActivateInfo;
import com.elster.dlms.cosem.classes.class18.ImageTransferStatusEnum;
import com.elster.dlms.cosem.classes.common.BaudrateEnum;
import com.elster.dlms.cosem.classes.class19.DefaultModeEnum;
import com.elster.dlms.cosem.classes.class19.ResponseTimeEnum;
import com.elster.dlms.cosem.classes.class20.CosemAttributeDayProfileTable;
import com.elster.dlms.cosem.classes.class20.CosemAttributeSeasonProfile;
import com.elster.dlms.cosem.classes.class20.CosemAttributeWeekProfileTable;
import com.elster.dlms.cosem.classes.class21.CosemAttributeActions;
import com.elster.dlms.cosem.classes.class28.AutoAnswerModeEnum;
import com.elster.dlms.cosem.classes.class29.AutoConnectModeEnum;
import com.elster.dlms.cosem.classes.class29.CosemAttributeTimeWindow;
import com.elster.dlms.cosem.classes.class8193.CosemAttributeParameterList;
import com.elster.dlms.cosem.classes.common.AttributeAccessMode;
import com.elster.dlms.cosem.classes.common.CosemAttributeDateTime;
import com.elster.dlms.cosem.classes.common.CosemAttributeEnum;
import com.elster.dlms.cosem.classes.common.CosemAttributeLogicalName;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo;
import com.elster.dlms.cosem.classes.info.CosemClassInfos;

/**
 * Factory for creating CosemAttributes
 *
 * @author osse
 */
public class CosemAttributeFactory
{
  private final CosemClassInfos classInfos;

  /**
   * Constructor.<P>
   *
   * @param classInfos Information for creating the attributes
   */
  public CosemAttributeFactory(CosemClassInfos classInfos)
  {
    this.classInfos = classInfos;
  }

  /**
   * Creates the specified attribute.<P>
   * If available, specialized attribute will be created.
   *
   * @param parent The object for this attribute.
   * @param attributeId Number of the attribute.
   * @param accessMode The access mode for this attribute.
   * @param accessSelectors An array of available access selectors or {@code null} if selective access is not available for this attribute.
   * @return The created attribute.
   */
  public CosemAttribute createAttribute(CosemObject parent, int attributeId, AttributeAccessMode accessMode,
                                        int[] accessSelectors)
  {

    CosemAttributeInfo attributeInfo =
            classInfos.getAttributeInfo(parent.getCosemClassId(), parent.getCosemClassVersion(), attributeId);

    CosemAttribute result = null;

    if (attributeId == 1)
    {
      result = new CosemAttributeLogicalName(parent, attributeId, accessMode, attributeInfo, accessSelectors);
    }

    switch (parent.getCosemClassId())
    {
      case CosemClassIds.REGISTER: //3
        switch (attributeId)
        {
          case 3:
            result = new CosemAttributeScalerUnit(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
            break;
        }
        break;
      case CosemClassIds.EXTENDED_REGISTER: //4
        switch (attributeId)
        {
          case 3:
            result = new CosemAttributeScalerUnit(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
            break;
        }
        break;
      case CosemClassIds.DEMAND_REGISTER: //5
        switch (attributeId)
        {
          case 4:
            result = new CosemAttributeScalerUnit(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
            break;
        }
        break;
      case CosemClassIds.REGISTER_ACTIVATION: //6
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeRegisterAssignment(parent, attributeId, accessMode, attributeInfo,
                                                          accessSelectors);
            break;
          case 3:
            result = new CosemAttributeRegisterMaskList(parent, attributeId, accessMode, attributeInfo,
                                                        accessSelectors);
            break;

        }
        break;

      case CosemClassIds.PROFILE_GENERIC: //7
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeBuffer(parent, attributeId, accessMode, attributeInfo,
                                              accessSelectors);
            break;
          case 3:
            result = new CosemAttributeCaptureObjects(parent, attributeId, accessMode, attributeInfo,
                                                      accessSelectors);
            break;
          case 5:
            result = new CosemAttributeEnum<SortMethodEnum>(parent, attributeId, accessMode,
                                                            attributeInfo, accessSelectors, SortMethodEnum.
                    getFactory());
            break;
          case 6:
            result = new CosemAttributeSortObject(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
            break;
        }
        break;
      case CosemClassIds.CLOCK: //8
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeDateTime(parent, attributeId, accessMode, attributeInfo,
                                                accessSelectors);
            break;
          case 5:
            result = new CosemAttributeDateTime(parent, attributeId, accessMode, attributeInfo,
                                                accessSelectors);
            break;
          case 6:
            result = new CosemAttributeDateTime(parent, attributeId, accessMode, attributeInfo,
                                                accessSelectors);
            break;
          case 9:
            result = new CosemAttributeEnum<ClockBaseEnum>(parent, attributeId, accessMode,
                                                           attributeInfo, accessSelectors, ClockBaseEnum.
                    getFactory());
            break;
        }
        break;

      case CosemClassIds.SCRIPT_TABLE: //9
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeScripts(parent, attributeId, accessMode, attributeInfo,
                                               accessSelectors);
            break;
        }
        break;

      case CosemClassIds.SPECIAL_DAYS_TABLE: //11
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeSpecialDayEntries(parent, attributeId, accessMode, attributeInfo,
                                                         accessSelectors);
            break;
        }
        break;

      case CosemClassIds.ASSOCIATION_LN: //15
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeObjectList(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
            break;
        }
        break;
      case CosemClassIds.SAP_ASSIGNMENT: //17
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeSapAssignmentList(parent, attributeId, accessMode, attributeInfo,
                                                         accessSelectors);
            break;
        }
        break;

      case CosemClassIds.IMAGE_TRANSFER: //18
        switch (attributeId)
        {
          case 6:
            result = new CosemAttributeEnum<ImageTransferStatusEnum>(parent, attributeId, accessMode,
                                                                     attributeInfo, accessSelectors,
                                                                     ImageTransferStatusEnum.getFactory());
            break;
          case 7:
            result = new CosemAttributeImageToActivateInfo(parent, attributeId, accessMode, attributeInfo,
                                                           accessSelectors);
            break;
        }
        break;
      case CosemClassIds.IEC_LOCAL_PORT_SETUP: //19
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeEnum<DefaultModeEnum>(parent, attributeId, accessMode,
                                                             attributeInfo, accessSelectors, DefaultModeEnum.
                    getFactory());
            break;
          case 3:
            result = new CosemAttributeEnum<BaudrateEnum>(parent, attributeId, accessMode,
                                                             attributeInfo, accessSelectors, BaudrateEnum.
                    getFactory());
            break;
          case 4:
            result = new CosemAttributeEnum<BaudrateEnum>(parent, attributeId, accessMode,
                                                          attributeInfo, accessSelectors, BaudrateEnum.
                    getFactory());
            break;
          case 5:
            result = new CosemAttributeEnum<ResponseTimeEnum>(parent, attributeId, accessMode,
                                                              attributeInfo, accessSelectors,
                                                              ResponseTimeEnum.getFactory());
            break;
        }
        break;
      case CosemClassIds.ACTIVITY_CALENDAR: //20
        switch (attributeId)
        {
          case 3:
            result = new CosemAttributeSeasonProfile(parent, attributeId, accessMode, attributeInfo,
                                                     accessSelectors);
            break;
          case 4:
            result = new CosemAttributeWeekProfileTable(parent, attributeId, accessMode, attributeInfo,
                                                        accessSelectors);
            break;
          case 5:
            result = new CosemAttributeDayProfileTable(parent, attributeId, accessMode, attributeInfo,
                                                       accessSelectors);
            break;
          case 7:
            result = new CosemAttributeSeasonProfile(parent, attributeId, accessMode, attributeInfo,
                                                     accessSelectors);
            break;
          case 8:
            result = new CosemAttributeWeekProfileTable(parent, attributeId, accessMode, attributeInfo,
                                                        accessSelectors);
            break;
          case 9:
            result = new CosemAttributeDayProfileTable(parent, attributeId, accessMode, attributeInfo,
                                                       accessSelectors);
            break;
        }
        break;
      case CosemClassIds.REGISTER_MONITOR: //21
        switch (attributeId)
        {
          case 4:
            result = new CosemAttributeActions(parent, attributeId, accessMode, attributeInfo,
                                               accessSelectors);
            break;
        }
        break;
      case CosemClassIds.IEC_HDLC_SETUP: //23
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeEnum<BaudrateEnum>(parent, attributeId, accessMode,
                                                           attributeInfo, accessSelectors, BaudrateEnum.
                    getFactory());
            break;

        }
        break;

      case CosemClassIds.AUTO_ANSWER: //28
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeEnum<AutoAnswerModeEnum>(parent, attributeId, accessMode,
                                                                attributeInfo, accessSelectors,
                                                                AutoAnswerModeEnum.getFactory());
            break;
          case 3:
            result = new CosemAttributeTimeWindow(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
        }
        break;
      case CosemClassIds.AUTO_CONNECT: //29
        switch (attributeId)
        {
          case 2:
            result = new CosemAttributeEnum<AutoConnectModeEnum>(parent, attributeId, accessMode,
                                                                 attributeInfo, accessSelectors,
                                                                 AutoConnectModeEnum.getFactory());
            break;
          case 5:
            result = new CosemAttributeTimeWindow(parent, attributeId, accessMode, attributeInfo,
                                                  accessSelectors);
        }
        break;
      case CosemClassIds.SENSOR_MANAGER: //67
        switch (attributeId)
        {
          case 8:
            result = new CosemAttributeScalerUnit(parent, attributeId, accessMode,
                                                  attributeInfo, accessSelectors);
            break;

          case 12:
            result = new CosemAttributeActions(parent, attributeId, accessMode, attributeInfo,
                                               accessSelectors);
            break;

          case 15:
            result = new CosemAttributeActions(parent, attributeId, accessMode, attributeInfo,
                                               accessSelectors);
            break;
        }
        break;
      case 8193: //Parameter list
        switch (attributeId)
        {
          case 3:
            result = new CosemAttributeParameterList(parent, attributeId, accessMode,
                                                     attributeInfo, accessSelectors);
            break;
        }
        break;

    }

    if (result == null)
    {
      result = new CosemAttribute(parent, attributeId, accessMode, attributeInfo, accessSelectors);
    }

    return result;
  }

}
