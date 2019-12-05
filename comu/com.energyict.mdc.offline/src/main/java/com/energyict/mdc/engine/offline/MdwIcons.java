/*
 * MdwIcons.java
 *
 * Created on 1 oktober 2003, 16:58
 */

package com.energyict.mdc.engine.offline;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * fbl: added functionality for the webclient.  The weblcient needs to be
 * able to know the URL of the images to load them from the jar file.
 * Added getIconId().
 *
 * @author Geert
 */
public class MdwIcons {

    public static final Icon NO_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/no_icon.png"));

    public static final String MISSINGOBJECT_ICON_SRC = "/mdw/images/missingobject.gif";
    public static final Icon MISSINGOBJECT_ICON =
            new ImageIcon(MdwIcons.class.getResource(MISSINGOBJECT_ICON_SRC));

    public static final String ROOT_ICON_SRC = "/mdw/images/root.gif";
    public static final Icon ROOT_ICON = new ImageIcon(MdwIcons.class.getResource(ROOT_ICON_SRC));

    public static final String FOLDER_ICON_SRC = "/mdw/images/folder.gif";
    public static final Icon FOLDER_ICON = new ImageIcon(MdwIcons.class.getResource(FOLDER_ICON_SRC));

    public static final String TYPEDFOLDER_ICON_SRC = "/mdw/images/typedfolder.gif";
    public static final Icon TYPEDFOLDER_ICON = new ImageIcon(MdwIcons.class.getResource(TYPEDFOLDER_ICON_SRC));

    public static final String RTU_ICON_SRC = "/mdw/images/rtu.gif";
    public static final Icon RTU_ICON = new ImageIcon(MdwIcons.class.getResource(RTU_ICON_SRC));

    public static final String METER_ICON_SRC = "/mdw/icons/meter004.gif";
    public static final Icon METER_ICON = new ImageIcon(MdwIcons.class.getResource(METER_ICON_SRC));

    public static final String RTU_TYPE_ICON_SRC = "/mdw/images/rtuType.gif";
    public static final Icon RTU_TYPE_ICON = new ImageIcon(MdwIcons.class.getResource(RTU_TYPE_ICON_SRC));

    public static final String VIRTUALMETERTYPE_ICON_SRC = "/mdw/images/metertype.gif";
    public static final Icon VIRTUALMETERTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(VIRTUALMETERTYPE_ICON_SRC));

    public static final String AGGREGATORTYPE_ICON_SRC = "/mdw/images/aggregatortype.gif";
    public static final Icon AGGREGATORTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(AGGREGATORTYPE_ICON_SRC));

    public static final String DISAGGREGATORTYPE_ICON_SRC = "/mdw/images/disaggregatortype.gif";
    public static final Icon DISAGGREGATORTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(DISAGGREGATORTYPE_ICON_SRC));

    public static final String SUMMATORTYPE_ICON_SRC = "/mdw/images/summatortype.gif";
    public static final Icon SUMMATORTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(SUMMATORTYPE_ICON_SRC));

    public static final String VIRTUALMETER_ICON_SRC = "/mdw/images/meter.gif";
    public static final Icon VIRTUALMETER_ICON = new ImageIcon(MdwIcons.class.getResource(VIRTUALMETER_ICON_SRC));

    public static final String AGGREGATOR_ICON_SRC = "/mdw/images/aggregator.gif";
    public static final Icon AGGREGATOR_ICON = new ImageIcon(MdwIcons.class.getResource(AGGREGATOR_ICON_SRC));

    public static final String DISAGGREGATOR_ICON_SRC = "/mdw/images/disaggregator.gif";
    public static final Icon DISAGGREGATOR_ICON = new ImageIcon(MdwIcons.class.getResource(DISAGGREGATOR_ICON_SRC));

    public static final String SUMMATOR_ICON_SRC = "/mdw/images/summator.gif";
    public static final Icon SUMMATOR_ICON = new ImageIcon(MdwIcons.class.getResource(SUMMATOR_ICON_SRC));

    public static final String CHANNEL_ICON_SRC = "/mdw/images/channel.gif";
    public static final Icon CHANNEL_ICON = new ImageIcon(MdwIcons.class.getResource(CHANNEL_ICON_SRC));

    public static final String CHANNEL_RAW_VALUE_ICON_SRC = "/mdw/images/channel_rawvalue.gif";
    public static final Icon CHANNEL_RAW_VALUE_ICON = new ImageIcon(MdwIcons.class.getResource(CHANNEL_RAW_VALUE_ICON_SRC));

    public static final String STOPPEDMETER_ICON_SRC = "/mdw/images/stoppedmeter.gif";
    public static final Icon STOPPEDMETER_ICON = new ImageIcon(MdwIcons.class.getResource(STOPPEDMETER_ICON_SRC));

    public static final String FIELD_ICON_SRC = "/mdw/images/meterField.gif";
    public static final Icon FIELD_ICON = new ImageIcon(MdwIcons.class.getResource(FIELD_ICON_SRC));

    public static final String PARAMETER_ICON_SRC = "/mdw/images/parameter.gif";
    public static final Icon PARAMETER_ICON = new ImageIcon(MdwIcons.class.getResource(PARAMETER_ICON_SRC));

    public static final String METERPORTFOLIO_ICON_SRC = "/mdw/images/meterportfolio.gif";
    public static final Icon METERPORTFOLIO_ICON = new ImageIcon(MdwIcons.class.getResource(METERPORTFOLIO_ICON_SRC));

    public static final String EXPORTSCHEDULE_ICON_SRC = "/mdw/images/expsched.gif";
    public static final Icon EXPORTSCHEDULE_ICON = new ImageIcon(MdwIcons.class.getResource(EXPORTSCHEDULE_ICON_SRC));

    public static final Icon EXPORTSCHEDULE_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) EXPORTSCHEDULE_ICON).getImage()));

    public static final String CODE_ICON_SRC = "/mdw/images/code.gif";
    public static final Icon CODE_ICON = new ImageIcon(MdwIcons.class.getResource(CODE_ICON_SRC));

    public static final String COMPORT_ICON_SRC = "/mdw/images/comport.gif";
    public static final Icon COMPORT_ICON = new ImageIcon(MdwIcons.class.getResource(COMPORT_ICON_SRC));

    // !
    public static final Icon COMPORT_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) COMPORT_ICON).getImage()));

    public static final String COMSERVER_ICON_SRC = "/mdw/images/comserver.gif";
    public static final Icon COMSERVER_ICON = new ImageIcon(MdwIcons.class.getResource(COMSERVER_ICON_SRC));

    // !
    public static final Icon COMSERVER_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) COMSERVER_ICON).getImage()));

    public static final String MODEMPOOL_ICON_SRC = "/mdw/images/phone.gif";
    public static final Icon MODEMPOOL_ICON = new ImageIcon(MdwIcons.class.getResource(MODEMPOOL_ICON_SRC));

    // !
    public static final Icon MODEMPOOL_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) MODEMPOOL_ICON).getImage()));

    public static final String PROTOCOL_ICON_SRC = "/mdw/images/protocol.gif";
    public static final Icon PROTOCOL_ICON = new ImageIcon(MdwIcons.class.getResource(PROTOCOL_ICON_SRC));

    public static final String INFOTYPE_ICON_SRC = "/mdw/images/note.gif";
    public static final Icon INFOTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(INFOTYPE_ICON_SRC));

    public static final String JOBSERVER_ICON_SRC = "/mdw/images/jobserver.gif";
    public static final Icon JOBSERVER_ICON = new ImageIcon(MdwIcons.class.getResource(JOBSERVER_ICON_SRC));

    // !
    public static final Icon JOBSERVER_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) JOBSERVER_ICON).getImage()));

    public static final String COMPROFILE_ICON_SRC = "/mdw/images/comprofile.gif";
    public static final Icon COMPROFILE_ICON = new ImageIcon(MdwIcons.class.getResource(COMPROFILE_ICON_SRC));

    public static final String COMPROFILE_ADHOC_ICON_SRC = "/mdw/images/comprofileadhoc.gif";
    public static final Icon COMPROFILE_ADHOC_ICON = new ImageIcon(MdwIcons.class.getResource(COMPROFILE_ADHOC_ICON_SRC));

    public static final String PLUGGABLE_ICON_SRC = "/mdw/images/pluggable.png";
    public static final Icon PLUGGABLE_ICON = new ImageIcon(MdwIcons.class.getResource(PLUGGABLE_ICON_SRC));

    public static final String PLUGGABLE_ICON_INVALID_SRC = "/mdw/images/pluggable_invalid.png";
    public static final Icon PLUGGABLE_INVALID_ICON = new ImageIcon(MdwIcons.class.getResource(PLUGGABLE_ICON_INVALID_SRC));

    public static final String PHENOMENON_ICON_SRC = "/mdw/images/phenomenon.gif";
    public static final Icon PHENOMENON_ICON = new ImageIcon(MdwIcons.class.getResource(PHENOMENON_ICON_SRC));

    public static final String IMPORTSCHEDULE_ICON_SRC = "/mdw/images/impsched.gif";
    public static final Icon IMPORTSCHEDULE_ICON = new ImageIcon(MdwIcons.class.getResource(IMPORTSCHEDULE_ICON_SRC));

    // !
    public static final Icon IMPORTSCHEDULE_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) IMPORTSCHEDULE_ICON).getImage()));

    public static final String IMPORTMAPPING_ICON_SRC = "/mdw/images/importmapping.gif";
    public static final Icon IMPORTMAPPING_ICON = new ImageIcon(MdwIcons.class.getResource(IMPORTMAPPING_ICON_SRC));

    public static final String PERIOD_ICON_SRC = "/mdw/images/period.gif";
    public static final Icon PERIOD_ICON = new ImageIcon(MdwIcons.class.getResource(PERIOD_ICON_SRC));

    public static final String USER_ICON_SRC = "/mdw/images/user.png";
    public static final Icon USER_ICON = new ImageIcon(MdwIcons.class.getResource(USER_ICON_SRC));

    public static final String USERGROUP_ICON_SRC = "/mdw/images/usergroup.png";
    public static final Icon USERGROUP_ICON = new ImageIcon(MdwIcons.class.getResource(USERGROUP_ICON_SRC));

    public static final String ATTRIBUTEVALUETYPE_ICON_SRC = "/mdw/images/attribute_valuetype.png";
    public static final Icon ATTRIBUTEVALUETYPE_ICON = new ImageIcon(MdwIcons.class.getResource(ATTRIBUTEVALUETYPE_ICON_SRC));

    public static final String QUERYSPEC_ICON_SRC = "/mdw/images/queryspec.gif";
    public static final Icon QUERYSPEC_ICON = new ImageIcon(MdwIcons.class.getResource(QUERYSPEC_ICON_SRC));

    public static final String TIMEOFUSE_ICON_SRC = "/mdw/images/timeofuse.gif";
    public static final Icon TIMEOFUSE_ICON = new ImageIcon(MdwIcons.class.getResource(TIMEOFUSE_ICON_SRC));

    public static final String PRODUCTSPEC_ICON_SRC = "/mdw/images/productspec.gif";
    public static final Icon PRODUCTSPEC_ICON = new ImageIcon(MdwIcons.class.getResource(PRODUCTSPEC_ICON_SRC));

    public static final String METERREGISTER_ICON_SRC = "/mdw/images/meterregister.gif";
    public static final Icon METERREGISTER_ICON = new ImageIcon(MdwIcons.class.getResource(METERREGISTER_ICON_SRC));

    public static final String SYSTEMPARAMETER_ICON_SRC = "/mdw/images/systemparameter.gif";
    public static final Icon SYSTEMPARAMETER_ICON = new ImageIcon(MdwIcons.class.getResource(SYSTEMPARAMETER_ICON_SRC));

    public static final String TABLE_ICON_SRC = "/mdw/images/table.gif";
    public static final Icon TABLE_ICON = new ImageIcon(MdwIcons.class.getResource(TABLE_ICON_SRC));

    public static final String GRAPH_ICON_SRC = "/mdw/images/graph.gif";
    public static final Icon GRAPH_ICON = new ImageIcon(MdwIcons.class.getResource(GRAPH_ICON_SRC));

    public static final String COMBINEDGRAPH_ICON_SRC = "/mdw/images/combinedgraph.gif";
    public static final Icon COMBINEDGRAPH_ICON = new ImageIcon(MdwIcons.class.getResource(COMBINEDGRAPH_ICON_SRC));

    public static final String VALIDATIONUSERSTATE_ICON_SRC = "/mdw/images/validationuserstate.gif";
    public static final Icon VALIDATIONUSERSTATE_ICON = new ImageIcon(MdwIcons.class.getResource(VALIDATIONUSERSTATE_ICON_SRC));

    public static final String GROUP_ICON_SRC = "/mdw/images/group.gif";
    public static final Icon GROUP_ICON = new ImageIcon(MdwIcons.class.getResource(GROUP_ICON_SRC));

    public static final String READINGREASON_ICON_SRC = "/mdw/images/readingreason.gif";
    public static final Icon READINGREASON_ICON = new ImageIcon(MdwIcons.class.getResource(READINGREASON_ICON_SRC));

    public static final String READINGQUALITY_ICON_SRC = "/mdw/images/readingquality.gif";
    public static final Icon READINGQUALITY_ICON = new ImageIcon(MdwIcons.class.getResource(READINGQUALITY_ICON_SRC));

    public static final String FOLDERMAP_ICON_SRC = "/mdw/images/foldermap.gif";
    public static final Icon FOLDERMAP_ICON = new ImageIcon(MdwIcons.class.getResource(FOLDERMAP_ICON_SRC));

    public static final String USERFILE_ICON_SRC = "/mdw/images/userfile.gif";
    public static final Icon USERFILE_ICON = new ImageIcon(MdwIcons.class.getResource(USERFILE_ICON_SRC));

    public static final String USERFILEPIC_ICON_SRC = "/mdw/images/userfilepic.gif";
    public static final Icon USERFILEPIC_ICON = new ImageIcon(MdwIcons.class.getResource(USERFILEPIC_ICON_SRC));

    public static final String GRAPHTEMPLATE_ICON_SRC = "/mdw/images/graphtemplate.gif";
    public static final Icon GRAPHTEMPLATE_ICON = new ImageIcon(MdwIcons.class.getResource(GRAPHTEMPLATE_ICON_SRC));

    public static final String ACCOUNT_ICON_SRC = "/mdw/images/account.gif";
    public static final Icon ACCOUNT_ICON = new ImageIcon(MdwIcons.class.getResource(ACCOUNT_ICON_SRC));

    public static final String EISPECTOR_ICON_SRC = "/mdw/images/eispector.gif";
    public static final Icon EISPECTOR_ICON = new ImageIcon(MdwIcons.class.getResource(EISPECTOR_ICON_SRC));

    public static final String RTUREGISTERMAPPING_ICON_SRC = "/mdw/images/rturegmapping.gif";
    public static final Icon RTUREGISTERMAPPING_ICON = new ImageIcon(MdwIcons.class.getResource(RTUREGISTERMAPPING_ICON_SRC));

    public static final String CHANNELVAULT_ICON_SRC = "/mdw/images/channelvault.gif";
    public static final Icon CHANNELVAULT_ICON = new ImageIcon(MdwIcons.class.getResource(CHANNELVAULT_ICON_SRC));

    // !
    public static final Icon CHANNELVAULT_INACTIVE_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) CHANNELVAULT_ICON).getImage()));

    public static final String ACCOUNTINGPERIOD_ICON_SRC = "/mdw/images/accountingperiod.gif";
    public static final Icon ACCOUNTINGPERIOD_ICON = new ImageIcon(MdwIcons.class.getResource(ACCOUNTINGPERIOD_ICON_SRC));

    public static final String FOLDERACTION_ICON_SRC = "/mdw/images/folderaction.gif";
    public static final Icon FOLDERACTION_ICON = new ImageIcon(MdwIcons.class.getResource(FOLDERACTION_ICON_SRC));

    public static final String VALIDATIONMETHOD_ICON_SRC = "/mdw/images/validationmethod.gif";
    public static final Icon VALIDATIONMETHOD_ICON = new ImageIcon(MdwIcons.class.getResource(VALIDATIONMETHOD_ICON_SRC));

    public static final Icon NON_ACTIVE_EISPECTOR_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) EISPECTOR_ICON).getImage()));

    public static final String EXCELREPORT_ICON_SRC = "/mdw/images/excelreport.gif";
    public static final Icon EXCELREPORT_ICON = new ImageIcon(MdwIcons.class.getResource(EXCELREPORT_ICON_SRC));

    public static final String ESTIMATIONMETHOD_ICON_SRC = "/mdw/images/estimationmethod.gif";
    public static final Icon ESTIMATIONMETHOD_ICON = new ImageIcon(MdwIcons.class.getResource(ESTIMATIONMETHOD_ICON_SRC));

    public static final String ESTIMATIONPROCEDURE_ICON_SRC = "/mdw/images/estimationprocedure.gif";
    public static final Icon ESTIMATIONPROCEDURE_ICON = new ImageIcon(MdwIcons.class.getResource(ESTIMATIONPROCEDURE_ICON_SRC));

    public static final String GROUPREPORTSPEC_ICON_SRC = "/mdw/images/groupreport.gif";
    public static final Icon GROUPREPORTSPEC_ICON = new ImageIcon(MdwIcons.class.getResource(GROUPREPORTSPEC_ICON_SRC));

    public static final String LOOKUP_ICON_SRC = "/mdw/images/lookup.gif";
    public static final Icon LOOKUP_ICON = new ImageIcon(MdwIcons.class.getResource(LOOKUP_ICON_SRC));

    public static final String BILLABLEITEMSPEC_ICON_SRC = "/mdw/images/billableitem.gif";
    public static final Icon BILLABLEITEMSPEC_ICON = new ImageIcon(MdwIcons.class.getResource(BILLABLEITEMSPEC_ICON_SRC));

    public static final String USEDCURRENCY_ICON_SRC = "/mdw/images/usedcurrency.gif";
    public static final Icon USEDCURRENCY_ICON = new ImageIcon(MdwIcons.class.getResource(USEDCURRENCY_ICON_SRC));

    public static final String PRICESTRUCTURE_ICON_SRC = "/mdw/images/pricestructure.gif";
    public static final Icon PRICESTRUCTURE_ICON = new ImageIcon(MdwIcons.class.getResource(PRICESTRUCTURE_ICON_SRC));

    public static final Icon PRICESTRUCTURE_UNRELEASED_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) PRICESTRUCTURE_ICON).getImage()));

    public static final String ALLOWANCESPEC_ICON_SRC = "/mdw/images/allowance.gif";
    public static final Icon ALLOWANCESPEC_ICON = new ImageIcon(MdwIcons.class.getResource(ALLOWANCESPEC_ICON_SRC));

    public static final String REPORT_ICON_SRC = "/mdw/images/report.gif";
    public static final Icon REPORT_ICON = new ImageIcon(MdwIcons.class.getResource(REPORT_ICON_SRC));
    public static final Icon REPORT_UNRELEASED_ICON =
            new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) REPORT_ICON).getImage()));


    public static final String PRICESPECIFICATIONRESOLVER_ICON_SRC = "/mdw/images/pricestructresolver.gif";
    public static final Icon PRICESPECIFICATIONRESOLVER_ICON =
            new ImageIcon(MdwIcons.class.getResource(PRICESPECIFICATIONRESOLVER_ICON_SRC));

    public static final String PRICESPECIFICATION_ICON_SRC = "/mdw/images/pricespec.gif";
    public static final Icon PRICESPECIFICATION_ICON = new ImageIcon(MdwIcons.class.getResource(PRICESPECIFICATION_ICON_SRC));

    public static final String RTUREGISTERGROUP_ICON_SRC = "/mdw/images/rturegistergroup.gif";
    public static final Icon RTUREGISTERGROUP_ICON = new ImageIcon(MdwIcons.class.getResource(RTUREGISTERGROUP_ICON_SRC));

    public static final String SCRIPT_ICON_SRC = "/mdw/images/script.gif";
    public static final Icon SCRIPT_ICON = new ImageIcon(MdwIcons.class.getResource(SCRIPT_ICON_SRC));

    public static final String NUMBERLOOKUP_ICON_SRC = "/mdw/images/lookup09.gif";
    public static final Icon NUMBERLOOKUP_ICON = new ImageIcon(MdwIcons.class.getResource(NUMBERLOOKUP_ICON_SRC));

    public static final String STRINGLOOKUP_ICON_SRC = "/mdw/images/lookupAZ.gif";
    public static final Icon STRINGLOOKUP_ICON = new ImageIcon(MdwIcons.class.getResource(STRINGLOOKUP_ICON_SRC));

    public static final String MANUALMETERREADER_ICON_SRC = "/mdw/images/pda.gif";
    public static final Icon MANUALMETERREADER_ICON = new ImageIcon(MdwIcons.class.getResource(MANUALMETERREADER_ICON_SRC));

    public static final String METERREADERTOUR_ICON_SRC = "/mdw/images/mmrtour.gif";
    public static final Icon METERREADERTOUR_ICON = new ImageIcon(MdwIcons.class.getResource(METERREADERTOUR_ICON_SRC));

    public static final String METERREADERTASK_ICON_SRC = "/mdw/images/mmrtask.gif";
    public static final Icon METERREADERTASK_ICON = new ImageIcon(MdwIcons.class.getResource(METERREADERTASK_ICON_SRC));

    public static final String MAPITEM_WHITE_ICON_SRC = "/mdw/images/mapitemWhite.gif";
    public static final Icon MAPITEM_WHITE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_WHITE_ICON_SRC));

    public static final String MAPITEM_BLUE_ICON_SRC = "/mdw/images/mapitemBlue.gif";
    public static final Icon MAPITEM_BLUE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_BLUE_ICON_SRC));

    public static final String MAPITEM_RED_ICON_SRC = "/mdw/images/mapitemRed.gif";
    public static final Icon MAPITEM_RED_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_RED_ICON_SRC));

    public static final String MAPITEM_BROWN_ICON_SRC = "/mdw/images/mapitemBrown.gif";
    public static final Icon MAPITEM_BROWN_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_BROWN_ICON_SRC));

    public static final String MAPITEM_ORANGE_ICON_SRC = "/mdw/images/mapitemOrange.gif";
    public static final Icon MAPITEM_ORANGE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_ORANGE_ICON_SRC));

    public static final String MAPITEM_OLIVE_ICON_SRC = "/mdw/images/mapitemOlive.gif";
    public static final Icon MAPITEM_OLIVE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_OLIVE_ICON_SRC));

    public static final String MAPITEM_GREEN_ICON_SRC = "/mdw/images/mapitemGreen.gif";
    public static final Icon MAPITEM_GREEN_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEM_GREEN_ICON_SRC));

    public static final String MAPITEMMAP_WHITE_ICON_SRC = "/mdw/images/mapitemMapWhite.gif";
    public static final Icon MAPITEMMAP_WHITE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_WHITE_ICON_SRC));

    public static final String MAPITEMMAP_BLUE_ICON_SRC = "/mdw/images/mapitemMapBlue.gif";
    public static final Icon MAPITEMMAP_BLUE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_BLUE_ICON_SRC));

    public static final String MAPITEMMAP_RED_ICON_SRC = "/mdw/images/mapitemMapRed.gif";
    public static final Icon MAPITEMMAP_RED_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_RED_ICON_SRC));

    public static final String MAPITEMMAP_BROWN_ICON_SRC = "/mdw/images/mapitemMapBrown.gif";
    public static final Icon MAPITEMMAP_BROWN_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_BROWN_ICON_SRC));

    public static final String MAPITEMMAP_ORANGE_ICON_SRC = "/mdw/images/mapitemMapOrange.gif";
    public static final Icon MAPITEMMAP_ORANGE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_ORANGE_ICON_SRC));

    public static final String MAPITEMMAP_OLIVE_ICON_SRC = "/mdw/images/mapitemMapOlive.gif";
    public static final Icon MAPITEMMAP_OLIVE_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_OLIVE_ICON_SRC));

    public static final String MAPITEMMAP_GREEN_ICON_SRC = "/mdw/images/mapitemMapGreen.gif";
    public static final Icon MAPITEMMAP_GREEN_ICON = new ImageIcon(MdwIcons.class.getResource(MAPITEMMAP_GREEN_ICON_SRC));

    public static final String DIALCALENDAR_ICON_SRC = "/mdw/images/dialcalendar.png";
    public static final Icon DIALCALENDAR_ICON = new ImageIcon(MdwIcons.class.getResource(DIALCALENDAR_ICON_SRC));

    public static final String PROTOTYPE_ICON_SRC = "/mdw/images/prototype.gif";
    public static final Icon PROTOTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(PROTOTYPE_ICON_SRC));

    public static final String PROTOTYPEPARAMETER_ICON_SRC = "/mdw/images/prototypeparameter.gif";
    public static final Icon PROTOTYPEPARAMETER_ICON = new ImageIcon(MdwIcons.class.getResource(PROTOTYPEPARAMETER_ICON_SRC));

    public static final String INSERTACTION_ICON_SRC = "/mdw/images/insertaction.gif";
    public static final Icon INSERTACTION_ICON = new ImageIcon(MdwIcons.class.getResource(INSERTACTION_ICON_SRC));

    public static final String UPDATEACTION_ICON_SRC = "/mdw/images/updateaction.gif";
    public static final Icon UPDATEACTION_ICON = new ImageIcon(MdwIcons.class.getResource(UPDATEACTION_ICON_SRC));

    public static final String DELETEACTION_ICON_SRC = "/mdw/images/deleteaction.gif";
    public static final Icon DELETEACTION_ICON = new ImageIcon(MdwIcons.class.getResource(DELETEACTION_ICON_SRC));

    public static final String FOLDERATTRIBUTETYPE_ICON_SRC = "/mdw/images/attribute_type.png";
    public static final Icon FOLDERATTRIBUTETYPE_ICON = new ImageIcon(MdwIcons.class.getResource(FOLDERATTRIBUTETYPE_ICON_SRC));

    public static final String RTUREGISTER_ICON_SRC = "/mdw/images/rturegister.gif";
    public static final Icon RTUREGISTER_ICON = new ImageIcon(MdwIcons.class.getResource(RTUREGISTER_ICON_SRC));

    public static final String COMPONENT_ICON_SRC = "/mdw/images/component.gif";
    public static final Icon COMPONENT_ICON = new ImageIcon(MdwIcons.class.getResource(COMPONENT_ICON_SRC));

    public static final String EXCELTEMPLATE_ICON_SRC = "/mdw/images/exceltemplate.gif";
    public static final Icon EXCELTEMPLATE_ICON = new ImageIcon(MdwIcons.class.getResource(EXCELTEMPLATE_ICON_SRC));

    public static final String TIMEZONE_ICON_SRC = "/mdw/images/timezone.gif";
    public static final Icon TIMEZONE_ICON = new ImageIcon(MdwIcons.class.getResource(TIMEZONE_ICON_SRC));

    public static final String INVOICE_ICON_SRC = "/mdw/images/invoice.png";
    public static final Icon INVOICE_ICON = new ImageIcon(MdwIcons.class.getResource(INVOICE_ICON_SRC));

    public static final String SEARCH_ICON_SRC = "/mdw/images/search.png";
    public static final Icon SEARCH_ICON = new ImageIcon(MdwIcons.class.getResource(SEARCH_ICON_SRC));

    public static final String OPENFILE_ICON_SRC = "/mdw/images/openfile.gif";
    public static final Icon OPENFILE_ICON = new ImageIcon(MdwIcons.class.getResource(OPENFILE_ICON_SRC));

    public static final String IDGENERATOR_ICON_SRC = "/mdw/images/idgenerator.gif";
    public static final Icon IDGENERATOR_ICON = new ImageIcon(MdwIcons.class.getResource(IDGENERATOR_ICON_SRC));

    public static final String INVOICENUMBERFORMAT_ICON_SRC = "/mdw/images/invoicenrformat.gif";
    public static final Icon INVOICENUMBERFORMAT_ICON = new ImageIcon(MdwIcons.class.getResource(INVOICENUMBERFORMAT_ICON_SRC));

    public static final String RELATIONTYPE_ICON_SRC = "/mdw/images/relationtype.gif";
    public static final Icon RELATIONTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(RELATIONTYPE_ICON_SRC));

    public static final String CONSTRAINT_ICON_SRC = "/mdw/images/constraint.png";
    public static final Icon CONSTRAINT_ICON = new ImageIcon(MdwIcons.class.getResource(CONSTRAINT_ICON_SRC));

    public static final String ALL_ICON_SRC = "/mdw/images/all.gif";
    public static final Icon ALL_ICON = new ImageIcon(MdwIcons.class.getResource(ALL_ICON_SRC));

    public static final String GLOBALISATIONCODE_ICON_SRC = "/mdw/images/globalcode.gif";
    public static final Icon GLOBALISATIONCODE_ICON = new ImageIcon(MdwIcons.class.getResource(GLOBALISATIONCODE_ICON_SRC));

    public static final String EVENTHANDLER_ICON_SRC = "/mdw/images/eventhandling.png";
    public static final Icon EVENTHANDLER_ICON = new ImageIcon(MdwIcons.class.getResource(EVENTHANDLER_ICON_SRC));

    public static final String SHOW_ICON_SRC = "/mdw/images/eye.png";
    public static final Icon SHOW_ICON = new ImageIcon(MdwIcons.class.getResource(SHOW_ICON_SRC));

    public static final String JOBQUEUE_ICON_SRC = "/mdw/images/jobqueue.gif";
    public static final Icon JOBQUEUE_ICON = new ImageIcon(MdwIcons.class.getResource(JOBQUEUE_ICON_SRC));

    public static final String LOOKUP_LINK_ICON_SRC = "/mdw/images/lookuplink.gif";
    public static final Icon LOOKUP_LINK_ICON = new ImageIcon(MdwIcons.class.getResource(LOOKUP_LINK_ICON_SRC));

    public static final String PROCESSCASE_ICON_SRC = "/mdw/images/case.png";
    public static final Icon PROCESSCASE_ICON = new ImageIcon(MdwIcons.class.getResource(PROCESSCASE_ICON_SRC));

    public static final String PROCESSCASETYPE_ICON_SRC = "/mdw/images/casetype.png";
    public static final Icon PROCESSCASETYPE_ICON = new ImageIcon(MdwIcons.class.getResource(PROCESSCASETYPE_ICON_SRC));

    public static final Icon CONSUMPTION_REQUEST_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/consumptionrequest.png"));

    public static final String SERVICEREQUEST_ICON_SRC = "/mdw/images/servicerequest.png";
    public static final Icon SERVICEREQUEST_ICON = new ImageIcon(MdwIcons.class.getResource(SERVICEREQUEST_ICON_SRC));

    public static final String SERVICEREQUESTTYPE_ICON_SRC = "/mdw/images/servicerequesttype.png";
    public static final Icon SERVICEREQUESTTYPE_ICON = new ImageIcon(MdwIcons.class.getResource(SERVICEREQUESTTYPE_ICON_SRC));

    public static final String WORKGROUP_ICON_SRC = "/mdw/images/workgroup.png";
    public static final Icon WORKGROUP_ICON = new ImageIcon(MdwIcons.class.getResource(WORKGROUP_ICON_SRC));

    public static final String WORKPLACE_ICON_SRC = "/mdw/images/workplace.png";
    public static final Icon WORKPLACE_ICON = new ImageIcon(MdwIcons.class.getResource(WORKPLACE_ICON_SRC));

    public static final String WORKITEM_ICON_SRC = "/mdw/images/workitem.png";
    public static final Icon WORKITEM_ICON = new ImageIcon(MdwIcons.class.getResource(WORKITEM_ICON_SRC));

    public static final String PROCESS_ICON_SRC = "/mdw/images/process.png";
    public static final Icon PROCESS_ICON = new ImageIcon(MdwIcons.class.getResource(PROCESS_ICON_SRC));

    public static final String PROCESSVERSION_ICON_SRC = "/mdw/images/processversion.png";
    public static final Icon PROCESSVERSION_ICON = new ImageIcon(MdwIcons.class.getResource(PROCESSVERSION_ICON_SRC));

    public static final String SEARCHWORKPLACE_ICON_SRC = "/mdw/images/searchworkplace.png";
    public static final Icon SEARCHWORKPLACE_ICON = new ImageIcon(MdwIcons.class.getResource(SEARCHWORKPLACE_ICON_SRC));

    public static final String STARTPROCESSCASE_ICON_SRC = "/mdw/images/startprocesscase.png";
    public static final Icon STARTPROCESSCASE_ICON = new ImageIcon(MdwIcons.class.getResource(STARTPROCESSCASE_ICON_SRC));

    public static Icon PLACEANDTOKEN_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/placeandtoken.png"));

    public static final String WORKDAYSCALENDAR_ICON_SRC = "/mdw/images/workdayscalendar.png";
    public static Icon WORKDAYSCALENDAR_ICON = new ImageIcon(MdwIcons.class.getResource(WORKDAYSCALENDAR_ICON_SRC));

    public static final String RTUREGISTERSFOLDER_ICON_SRC = "/mdw/images/rturegistersfolder.gif";
    public static Icon RTUREGISTERSFOLDER_ICON = new ImageIcon(MdwIcons.class.getResource(RTUREGISTERSFOLDER_ICON_SRC));

    public static final String SEASONSET_ICON_SRC = "/mdw/images/seasonset.png";
    public static Icon SEASONSET_ICON = new ImageIcon(MdwIcons.class.getResource(SEASONSET_ICON_SRC));

    public static final String MESSAGESERVICE_ICON_SRC = "/mdw/images/messageservice.gif";
    public static Icon MESSAGESERVICE_ICON = new ImageIcon(MdwIcons.class.getResource(MESSAGESERVICE_ICON_SRC));

    public static final String ROLE_ICON_SRC = "/mdw/images/role.png";
    public static final Icon ROLE_ICON = new ImageIcon(MdwIcons.class.getResource(ROLE_ICON_SRC));

    public static final String RTUREGISTERVALIDATIONMETHOD_ICON_SRC = "/mdw/images/registervalidationmethod.png";
    public static final Icon RTUREGISTERVALIDATIONMETHOD_ICON =
            new ImageIcon(MdwIcons.class.getResource(RTUREGISTERVALIDATIONMETHOD_ICON_SRC));

    public static final Icon LOADPROFILETYPE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofiletype.png"));
    public static final Icon LOADPROFILE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofile.png"));
    public static final Icon LOADPROFILESFOLDER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofilesfolder.png"));

    public static final Icon LOGBOOKTYPE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbooktype.png"));
    public static final Icon LOGBOOK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbook.png"));
    public static final Icon LOGBOOKFOLDER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbookfolder.png"));

    public static final Icon REFRESH_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/updateaction.gif"));
    public static final Icon CUT_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/scissors-blue.png"));
    public static final Icon COPY_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/document-copy.png"));
    public static final Icon PASTE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/clipboard-paste-image.png"));

    public static final Icon ORGANISATION_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/organisation.png"));
    public static final Icon SPINNING_WHEEL_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/wait30trans.gif"));
    public static final Icon LIGHT_BULB_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/LightBulb.gif"));

    public static final Icon ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/added.png"));
    public static final Icon DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/deleted.png"));
    public static final Icon MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/moved.png"));

    public static final Icon CHANNELADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/channeladded.png"));
    public static final Icon CHANNELDELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/channeldeleted.png"));
    public static final Icon CHANNELMAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/channelmoved.png"));

    public static final Icon LOADPROFILEADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofileadded.png"));
    public static final Icon LOADPROFILEDELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofiledeleted.png"));
    public static final Icon LOADPROFILEMAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/loadprofilemoved.png"));

    public static final Icon RTUREGISTERADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/rturegisteradded.png"));
    public static final Icon RTUREGISTERDELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/rturegisterdeleted.png"));
    public static final Icon RTUREGISTERMAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/rturegistermoved.png"));

    public static final Icon LOGBOOKADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbookadded.png"));
    public static final Icon LOGBOOKDELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbookdeleted.png"));
    public static final Icon LOGBOOKMAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/logbookmoved.png"));

    public static final Icon SERVICELOCATION_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/servicelocation.png"));
    public static final Icon SERVICELOCATIONTYPE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/servicelocationtype.png"));
    public static final Icon KPI_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/kpi.png"));
    public static final String KPITYPE_ICON_SRC =  "/mdw/images/kpitype.png";
    public static final Icon KPITYPE_ICON = new ImageIcon(MdwIcons.class.getResource(KPITYPE_ICON_SRC));
    public static final Icon PURPOSE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/purpose.png"));
    public static final Icon GRIDSEGMENT_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/Smallgrid.gif"));
    public static final Icon SDPTYPE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/servicedeliverypointtype.png"));
    public static final Icon SDP_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/servicedeliverypoint.png"));
    public static final Icon ALARMRULE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/bell.png"));
    public static final Icon ALARM_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/bell.png"));

    public static final Icon DATACOLLECTIONMONITOR_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/datacollmonitor.png"));
    public static final Icon DATACOMMUNICATIONSET_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/datacommunicationset.png"));

    public static final Icon SORTING_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/sorting.png"));
    public static Icon INFORMATION_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/information.png"));

    public static final Icon DEVICE_CONFIG_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/deviceconfig.png"));
    public static final Icon DISCOVERY_PROTOCOL_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/discoveryprotocol.png"));
    public static final Icon CONNECTION_TYPE_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/connectiontype.png"));

    public static final Icon ADD_ICON = new ImageIcon(MdwIcons.class.getResource("/bpm/images/add.png"));
    public static final Icon DELETE_ICON = new ImageIcon(MdwIcons.class.getResource("/bpm/images/delete.gif"));
    public static final Icon RESET_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/reset.png"));

    public static final Icon SECURITY_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/security.png"));
    public static final Icon SECURITYSET_ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/securitysetadded.png"));
    public static final Icon SECURITYSET_MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/securitysetmapped.png"));
    public static final Icon SECURITYSET_DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/securitysetdeleted.png"));
    public static final Icon PROTOCOLPROPS_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/protocolprops.png"));
    public static final Icon PROTOCOLPROPS_ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/protocolpropsadded.png"));
    public static final Icon PROTOCOLPROPS_MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/protocolpropsmapped.png"));
    public static final Icon PROTOCOLPROPS_DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/protocolpropsdeleted.png"));

    public static final Icon DEVICEACTION_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/deviceaction.png"));

    // ----------------------------------------
    // MDC icons (that are also needed in mdw)
    // ----------------------------------------
    public static final Icon OFFLINE_COMSERVER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/offline_comserver.png"));
    public static final Icon ONLINE_COMSERVER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/online_comserver.png"));
    public static final Icon REMOTE_COMSERVER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/remote_comserver.png"));
    public static final Icon INBOUND_COMPORT_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inbound_comport.png"));
    public static final Icon OUTBOUND_COMPORT_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outbound_comport.png"));
    public static final Icon COMPORTPOOL_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/comportpool.png"));
    public static final Icon INBOUND_COMPORTPOOL_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inbound_comportpool.png"));
    public static final Icon OUTBOUND_COMPORTPOOL_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outbound_comportpool.png"));
    public static final Icon COMTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/comtask.png"));
    public static final Icon COMTASK_ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/comtaskadded.png"));
    public static final Icon COMTASK_MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/comtaskmapped.png"));
    public static final Icon COMTASK_DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/comtaskdeleted.png"));
    public static final Icon INBOUND_COMTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundcomtask.png"));
    public static final Icon OUTBOUND_COMTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundcomtask.png"));
//    public static final Icon INBOUND_SCHEDULED_COMTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundscheduledcomtask.png"));
//    public static final Icon OUTBOUND_SCHEDULED_COMTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundscheduledcomtask.png"));
    public static final Icon CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/connectiontask.png"));
    public static final Icon INBOUND_CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundconnectiontask.png"));
    public static final Icon INBOUND_CONNECTIONTASKS_FOLDER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundconnectiontasksfolder.png"));
    public static final Icon INBOUND_CONNECTIONTASK_ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundconnectiontaskadded.png"));
    public static final Icon INBOUND_CONNECTIONTASK_DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundconnectiontaskdeleted.png"));
    public static final Icon INBOUND_CONNECTIONTASK_MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/inboundconnectiontaskmoved.png"));
    public static final Icon OUTBOUND_CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundconnectiontask.png"));
    public static final Icon OUTBOUND_CONNECTIONTASKS_FOLDER_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundconnectiontasksfolder.png"));
    public static final Icon OUTBOUND_CONNECTIONTASK_ADDED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundconnectiontaskadded.png"));
    public static final Icon OUTBOUND_CONNECTIONTASK_DELETED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundconnectiontaskdeleted.png"));
    public static final Icon OUTBOUND_CONNECTIONTASK_MAPPED_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/outboundconnectiontaskmoved.png"));
    public static final Icon PARTIAL_INBOUND_CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/partialinboundconnectiontask.png"));
    public static final Icon PARTIAL_OUTBOUND_CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/partialoutboundconnectiontask.png"));
    public static final Icon PARTIAL_CONNECTIONTASK_ICON = new ImageIcon(MdwIcons.class.getResource("/mdc/images/partialconnectiontask.png"));
    public static final Icon WARNING_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/icons/warning.png"));

    // ----------------------------------------
    // MOP icons (that are also needed in mdw)
    // ----------------------------------------
    public static final Icon ALARMSTATE_OPEN_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_state_open.png"));
    public static final Icon ALARMSTATE_ESCALATED_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_state_escalated.png"));
    public static final Icon ALARMSTATE_CLOSED_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_state_closed.png"));

    public static final Icon ALARMSEVERITY_CRITICAL_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_severity_critical.png"));
    public static final Icon ALARMSEVERITY_MAJOR_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_severity_major.png"));
    public static final Icon ALARMSEVERITY_MINOR_ICON = new ImageIcon(MdwIcons.class.getResource("/mop/alarm_severity_minor.png"));

    // ----------------------------------------
    // ComServer Mobile icons
    // ----------------------------------------
    public static final Icon ONLINE_ICON_24 = new ImageIcon(MdwIcons.class.getResource("/mdw/images/online_24.png"));
    public static final Icon OFFLINE_ICON_24 = new ImageIcon(MdwIcons.class.getResource("/mdw/images/offline_24.png"));
    public static final Icon SPACER_1x32_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/spacer_1x32.png"));
    public static final Icon MANUAL_METER_READ_ICON_24 = new ImageIcon(MdwIcons.class.getResource("/mdw/images/M_Meter_Read_24.png"));
    public static final Icon MANUAL_METER_READ_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/M_Meter_Read.png"));
    public static final Icon MANUAL_METER_READ_ICON_DISABLED = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) MANUAL_METER_READ_ICON).getImage()));
    public static final Icon REMOTE_METER_READ_ICON_24 = new ImageIcon(MdwIcons.class.getResource("/mdw/images/R_Meter_Read_24.png"));
    public static final Icon REMOTE_METER_READ_ICON = new ImageIcon(MdwIcons.class.getResource("/mdw/images/R_Meter_Read.png"));
    public static final Icon REMOTE_METER_READ_ICON_DISABLED = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) REMOTE_METER_READ_ICON).getImage()));

    // map containing custom icons
    private static final Map<String, Icon> customIcons = new HashMap<String, Icon>() {
        {
            put(MISSINGOBJECT_ICON_SRC, MISSINGOBJECT_ICON);
            put(ROOT_ICON_SRC, ROOT_ICON);
            put(FOLDER_ICON_SRC, FOLDER_ICON);
            put(TYPEDFOLDER_ICON_SRC, TYPEDFOLDER_ICON);
            put(RTU_ICON_SRC, RTU_ICON);
            put(METER_ICON_SRC, METER_ICON);
            put(VIRTUALMETERTYPE_ICON_SRC, VIRTUALMETERTYPE_ICON);
            put(AGGREGATORTYPE_ICON_SRC, AGGREGATORTYPE_ICON);
            put(DISAGGREGATORTYPE_ICON_SRC, DISAGGREGATORTYPE_ICON);
            put(SUMMATORTYPE_ICON_SRC, SUMMATORTYPE_ICON);
            put(VIRTUALMETER_ICON_SRC, VIRTUALMETER_ICON);
            put(AGGREGATOR_ICON_SRC, AGGREGATOR_ICON);
            put(DISAGGREGATOR_ICON_SRC, DISAGGREGATOR_ICON);
            put(SUMMATOR_ICON_SRC, SUMMATOR_ICON);
            put(CHANNEL_ICON_SRC, CHANNEL_ICON);
            put(STOPPEDMETER_ICON_SRC, STOPPEDMETER_ICON);
            put(FIELD_ICON_SRC, FIELD_ICON);
            put(PARAMETER_ICON_SRC, PARAMETER_ICON);
            put(METERPORTFOLIO_ICON_SRC, METERPORTFOLIO_ICON);
            put(EXPORTSCHEDULE_ICON_SRC, EXPORTSCHEDULE_ICON);
            put(CODE_ICON_SRC, CODE_ICON);
            put(COMPORT_ICON_SRC, COMPORT_ICON);
            put(COMSERVER_ICON_SRC, COMSERVER_ICON);
            put(MODEMPOOL_ICON_SRC, MODEMPOOL_ICON);
            put(PROTOCOL_ICON_SRC, PROTOCOL_ICON);
            put(INFOTYPE_ICON_SRC, INFOTYPE_ICON);
            put(JOBSERVER_ICON_SRC, JOBSERVER_ICON);
            put(COMPROFILE_ICON_SRC, COMPROFILE_ICON);
            put(PLUGGABLE_ICON_SRC, PLUGGABLE_ICON);
            put(PHENOMENON_ICON_SRC, PHENOMENON_ICON);
            put(IMPORTSCHEDULE_ICON_SRC, IMPORTSCHEDULE_ICON);
            put(IMPORTMAPPING_ICON_SRC, IMPORTMAPPING_ICON);
            put(PERIOD_ICON_SRC, PERIOD_ICON);
            put(USER_ICON_SRC, USER_ICON);
            put(USERGROUP_ICON_SRC, USERGROUP_ICON);
            put(ATTRIBUTEVALUETYPE_ICON_SRC, ATTRIBUTEVALUETYPE_ICON);
            put(QUERYSPEC_ICON_SRC, QUERYSPEC_ICON);
            put(TIMEOFUSE_ICON_SRC, TIMEOFUSE_ICON);
            put(PRODUCTSPEC_ICON_SRC, PRODUCTSPEC_ICON);
            put(METERREGISTER_ICON_SRC, METERREGISTER_ICON);
            put(SYSTEMPARAMETER_ICON_SRC, SYSTEMPARAMETER_ICON);
            put(TABLE_ICON_SRC, TABLE_ICON);
            put(GRAPH_ICON_SRC, GRAPH_ICON);
            put(COMBINEDGRAPH_ICON_SRC, COMBINEDGRAPH_ICON);
            put(VALIDATIONUSERSTATE_ICON_SRC, VALIDATIONUSERSTATE_ICON);
            put(GROUP_ICON_SRC, GROUP_ICON);
            put(READINGREASON_ICON_SRC, READINGREASON_ICON);
            put(READINGQUALITY_ICON_SRC, READINGQUALITY_ICON);
            put(FOLDERMAP_ICON_SRC, FOLDERMAP_ICON);
            put(USERFILE_ICON_SRC, USERFILE_ICON);
            put(USERFILEPIC_ICON_SRC, USERFILEPIC_ICON);
            put(GRAPHTEMPLATE_ICON_SRC, GRAPHTEMPLATE_ICON);
            put(ACCOUNT_ICON_SRC, ACCOUNT_ICON);
            put(EISPECTOR_ICON_SRC, EISPECTOR_ICON);
            put(RTUREGISTERMAPPING_ICON_SRC, RTUREGISTERMAPPING_ICON);
            put(CHANNELVAULT_ICON_SRC, CHANNELVAULT_ICON);
            put(ACCOUNTINGPERIOD_ICON_SRC, ACCOUNTINGPERIOD_ICON);
            put(FOLDERACTION_ICON_SRC, FOLDERACTION_ICON);
            put(VALIDATIONMETHOD_ICON_SRC, VALIDATIONMETHOD_ICON);
            put(EXCELREPORT_ICON_SRC, EXCELREPORT_ICON);
            put(ESTIMATIONMETHOD_ICON_SRC, ESTIMATIONMETHOD_ICON);
            put(ESTIMATIONPROCEDURE_ICON_SRC, ESTIMATIONPROCEDURE_ICON);
            put(GROUPREPORTSPEC_ICON_SRC, GROUPREPORTSPEC_ICON);
            put(LOOKUP_ICON_SRC, LOOKUP_ICON);
            put(BILLABLEITEMSPEC_ICON_SRC, BILLABLEITEMSPEC_ICON);
            put(USEDCURRENCY_ICON_SRC, USEDCURRENCY_ICON);
            put(PRICESTRUCTURE_ICON_SRC, PRICESTRUCTURE_ICON);
            put(ALLOWANCESPEC_ICON_SRC, ALLOWANCESPEC_ICON);
            put(REPORT_ICON_SRC, REPORT_ICON);
            put(PRICESPECIFICATIONRESOLVER_ICON_SRC, PRICESPECIFICATIONRESOLVER_ICON);
            put(PRICESPECIFICATION_ICON_SRC, PRICESPECIFICATION_ICON);
            put(RTUREGISTERGROUP_ICON_SRC, RTUREGISTERGROUP_ICON);
            put(SCRIPT_ICON_SRC, SCRIPT_ICON);
            put(NUMBERLOOKUP_ICON_SRC, NUMBERLOOKUP_ICON);
            put(STRINGLOOKUP_ICON_SRC, STRINGLOOKUP_ICON);
            put(MANUALMETERREADER_ICON_SRC, MANUALMETERREADER_ICON);
            put(METERREADERTOUR_ICON_SRC, METERREADERTOUR_ICON);
            put(MAPITEM_WHITE_ICON_SRC, MAPITEM_WHITE_ICON);
            put(MAPITEM_BLUE_ICON_SRC, MAPITEM_BLUE_ICON);
            put(MAPITEM_RED_ICON_SRC, MAPITEM_RED_ICON);
            put(MAPITEM_BROWN_ICON_SRC, MAPITEM_BROWN_ICON);
            put(MAPITEM_ORANGE_ICON_SRC, MAPITEM_ORANGE_ICON);
            put(MAPITEM_OLIVE_ICON_SRC, MAPITEM_OLIVE_ICON);
            put(MAPITEM_GREEN_ICON_SRC, MAPITEM_GREEN_ICON);
            put(MAPITEMMAP_WHITE_ICON_SRC, MAPITEMMAP_WHITE_ICON);
            put(MAPITEMMAP_BLUE_ICON_SRC, MAPITEMMAP_BLUE_ICON);
            put(MAPITEMMAP_RED_ICON_SRC, MAPITEMMAP_RED_ICON);
            put(MAPITEMMAP_BROWN_ICON_SRC, MAPITEMMAP_BROWN_ICON);
            put(MAPITEMMAP_ORANGE_ICON_SRC, MAPITEMMAP_ORANGE_ICON);
            put(MAPITEMMAP_OLIVE_ICON_SRC, MAPITEMMAP_OLIVE_ICON);
            put(MAPITEMMAP_GREEN_ICON_SRC, MAPITEMMAP_GREEN_ICON);
            put(DIALCALENDAR_ICON_SRC, DIALCALENDAR_ICON);
            put(PROTOTYPE_ICON_SRC, PROTOTYPE_ICON);
            put(PROTOTYPEPARAMETER_ICON_SRC, PROTOTYPEPARAMETER_ICON);
            put(INSERTACTION_ICON_SRC, INSERTACTION_ICON);
            put(UPDATEACTION_ICON_SRC, UPDATEACTION_ICON);
            put(DELETEACTION_ICON_SRC, DELETEACTION_ICON);
            put(FOLDERATTRIBUTETYPE_ICON_SRC, FOLDERATTRIBUTETYPE_ICON);
            put(RTUREGISTER_ICON_SRC, RTUREGISTER_ICON);
            put(COMPONENT_ICON_SRC, COMPONENT_ICON);
            put(EXCELTEMPLATE_ICON_SRC, EXCELTEMPLATE_ICON);
            put(METERREADERTASK_ICON_SRC, METERREADERTASK_ICON);
            put(TIMEZONE_ICON_SRC, TIMEZONE_ICON);
            put(INVOICE_ICON_SRC, INVOICE_ICON);
            put(SEARCH_ICON_SRC, SEARCH_ICON);
            put(OPENFILE_ICON_SRC, OPENFILE_ICON);
            put(IDGENERATOR_ICON_SRC, IDGENERATOR_ICON);
            put(INVOICENUMBERFORMAT_ICON_SRC, INVOICENUMBERFORMAT_ICON);
            put(RELATIONTYPE_ICON_SRC, RELATIONTYPE_ICON);
            put(CONSTRAINT_ICON_SRC, CONSTRAINT_ICON);
            put(GLOBALISATIONCODE_ICON_SRC, GLOBALISATIONCODE_ICON);
            put(LOOKUP_LINK_ICON_SRC, LOOKUP_LINK_ICON);
            put(ALL_ICON_SRC, ALL_ICON);
            put(PROCESSCASE_ICON_SRC, PROCESSCASE_ICON);
            put(PROCESSCASETYPE_ICON_SRC, PROCESSCASETYPE_ICON);
            put(SERVICEREQUEST_ICON_SRC, SERVICEREQUEST_ICON);
            put(SERVICEREQUESTTYPE_ICON_SRC, SERVICEREQUESTTYPE_ICON);
            put(WORKGROUP_ICON_SRC, WORKGROUP_ICON);
            put(WORKPLACE_ICON_SRC, WORKPLACE_ICON);
            put(WORKITEM_ICON_SRC, WORKITEM_ICON);
            put(PROCESS_ICON_SRC, PROCESS_ICON);
            put(PROCESSVERSION_ICON_SRC, PROCESSVERSION_ICON);
            put(SEARCHWORKPLACE_ICON_SRC, SEARCHWORKPLACE_ICON);
            put(COMPROFILE_ADHOC_ICON_SRC, COMPROFILE_ADHOC_ICON);
        }
    };

    public static synchronized Icon getIcon(String iconId) {
        return customIcons.get(iconId);
    }

    /**
     * Given an Icon, find the filename/url of the icon.
     *
     * @param icon for wich to find url
     * @return url of the icon
     */
    public static synchronized String getIconId(Icon icon) {
        Iterator<String> keys = customIcons.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Icon customIcon = customIcons.get(key);
            if (icon == customIcon) {
                return key;
            }
        }
        return null;
    }

    public static synchronized Icon getCustomIcon(String strIconId) {
        ImageIcon icon = (ImageIcon) customIcons.get(strIconId);
        if (icon == null) {
            URL url = MdwIcons.class.getResource(strIconId);
            if (url != null) {
                icon = new ImageIcon(url);
                customIcons.put(strIconId, icon);
            }
        }
        return icon;
    }

    public static Icon createGrayedOut(Icon icon) {
        if (icon instanceof ImageIcon) {
            Image normalImage = ((ImageIcon)icon).getImage();
            Image grayImage = GrayFilter.createDisabledImage(normalImage);
            return new ImageIcon(grayImage);
        }
        return icon;
    }
}