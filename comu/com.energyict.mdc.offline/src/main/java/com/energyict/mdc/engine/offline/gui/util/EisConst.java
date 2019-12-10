/*
 * EisConst.java
 *
 * Created on May 9, 2003, 8:21 AM
 */

package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import java.awt.*;

/**
 * @author gde
 */
public interface EisConst {

	// Preference Keys
	String PREFKEY_LOGON_USER = "logon.user";
	String PREFKEY_REPORTPERIOD_FROM = "report.period.from";
	String PREFKEY_REPORTPERIOD_TO = "report.period.to";
	String PREFKEY_SEARCH_COLUMNS_ALL = "search.columns.all";
	String PREFKEY_SEARCH_COLUMNS_FOLDER = "search.columns.folder";
	String PREFKEY_SEARCH_COLUMNS_RTU = "search.columns.rtu";
	String PREFKEY_SEARCH_COLUMNS_METER = "search.columns.meter";
	String PREFKEY_SEARCH_COLUMNS_CHANNEL = "search.columns.channel";
	String PREFKEY_SEARCH_COLUMNS_PARAMETER = "search.columns.parameter";
	String PREFKEY_SEARCH_COLUMNS_PORTFOLIO = "search.columns.portfolio";
	String PREFKEY_SEARCH_COLUMNS_PORTFOLIOITEM = "search.columns.portfolioitem";
	String PREFKEY_SEARCH_COLUMNS_EXPORTSCHEDULE = "search.columns.exportschedule";
	String PREFKEY_SEARCH_COLUMNS_METERTYPE = "search.columns.metertype";
	String PREFKEY_SEARCH_COLUMNS_CODE = "search.columns.code";
	String PREFKEY_SEARCH_COLUMNS_VIRTUALMETERFIELD = "search.columns.meterfield";
	String PREFKEY_SEARCH_COLUMNS_METERREGISTER = "search.columns.meterregister";
	String PREFKEY_SEARCH_COLUMNS_GROUP = "search.columns.group";
	String PREFKEY_SEARCH_COLUMNS_GROUPREPORTSPEC = "search.columns.groupreportspec";
	String PREFKEY_SEARCH_COLUMNS_EXCELREPORT = "search.columns.excelreport";
	String PREFKEY_SEARCH_COLUMNS_EXCELTEMPLATE = "search.columns.exceltemplate";
	String PREFKEY_SEARCH_COLUMNS_EISPECTOR = "search.columns.eispector";
	String PREFKEY_SEARCH_COLUMNS_REPORT = "search.columns.report";
	String PREFKEY_SEARCH_COLUMNS_COMPOSITE_GRAPH = "search.columns.compositegraph";
	String PREFKEY_SEARCH_COLUMNS_STRING_LOOKUP = "search.columns.stringlookup";
	String PREFKEY_SEARCH_COLUMNS_USERFILE = "search.columns.userfile";
	String PREFKEY_SEARCH_COLUMNS_CONSUMPTION_REQUEST = "search.columns.consumptionrequest";
	String PREFKEY_SEARCH_COLUMNS_SERVICE_REQUEST = "search.columns.servicerequest";
	String PREFKEY_MAINFRAME_MAXIMIZED = "mainframe.maximized";
	String PREFKEY_MAINFRAME_WIDTH = "mainframe.width";
	String PREFKEY_MAINFRAME_HEIGHT = "mainframe.height";
	String PREFKEY_MAINFRAME_LOCATIONX = "mainframe.location.x";
	String PREFKEY_MAINFRAME_LOCATIONY = "mainframe.location.y";
	String PREFKEY_WIDTH = ".width";
	String PREFKEY_HEIGHT = ".height";
	String PREFKEY_CHANNELCHART = "channelchart";
	String PREFKEY_CHANNELSUBSTITUTE_CHART = "channelsubstitute";
	String PREFKEY_USERFILE_PATH = "userfile.path";
	String PREFKEY_LOCATIONX = ".location.x";
	String PREFKEY_LOCATIONY = ".location.y";
	String PREFKEY_OPENMAPS = "mapmgr.openmapIds";
	String PREFKEY_EXPORT_PATH = "export.path";
	String PREFKEY_IMPORT_PATH = "import.path";
	String PREFKEY_SCROLLBARPOSITION_H = ".scrollbar.position.h";
	String PREFKEY_SCROLLBARPOSITION_V = ".scrollbar.position.v";
	String PREFKEY_AMRSETTINGS_COLUMNS = "amrsettingsoverview.columns";
	String PREFKEY_RTUREGISTERREADINGSPERIOD_FROM = "rturegisterreadings.period.from";
	String PREFKEY_RTUREGISTERREADINGSPERIOD_TO = "rturegisterreadings.period.to";

	String PREFKEY_EDILOGGING_COLUMNS = "edilogging.columns";
	String PREFKEY_USERSPANEL_COLUMNS = "userspanel.columns";
	String PREFKEY_RTUEVENTSREPORTPNL_COLUMNS = "rtueventsreport.columns";


	String PREFKEY_EXPORTLOGGING_COLUMNS = "exportlogging.columns";
	String PREFKEY_IMPORTLOGGING_COLUMNS = "importlogging.columns";
	String PREFKEY_CONSUMPTIONREQUESTLOGGING_COLUMNS = "consumptionrequestlogging.columns";
	String PREFKEY_AMRSETTINGS_AUTOREFRESH = "amrsettingsoverview.autorefresh";
	String PREFKEY_AMRSETTINGS_AUTOREFRESHRATE = "amrsettingsoverview.autorefreshrate";
	String PREFKEY_BILLINGREPORTPERIOD_FROM = "billingreport.period.from";
	String PREFKEY_BILLINGREPORTPERIOD_TO = "billingreport.period.to";
	String PREFKEY_REGISTERREPORT_COLUMNS = "registerreport.columns";
	String PREFKEY_CONSUMPTIONREPORT_COLUMNS = "consumptionreport.columns";
	String PREFKEY_EXPORTACTIONS_DATEFORMAT = "exportactions.dateformat";
	String PREFKEY_RTUREGISTERREADINGS_COLUMNS = "rturegisterreadings.columns";
	String PREFKEY_FONTSIZE = "fontsize";
	String PREFKEY_MAPS_AUTOREFRESH = "maps.autorefresh";
	String PREFKEY_MAPS_AUTOREFRESHRATE = "maps.autorefreshrate";
	String PREFKEY_SELECTTREEITEMPNL = "selecttreeitempnl";
	String PREFKEY_AUDITTRAIL_FROM = "audittrail.from";
	String PREFKEY_AUDITTRAIL_TO = "audittrail.to";
	String PREFKEY_EXPORTITEMSPNL = "exportitemspnl";
	String PREFKEY_METERFIELDFORMULAPNL = "meterfieldformulapnl";
	String PREFKEY_METERFIELDFORMULAPNL_DIVIDERLOCATION = "meterfieldformulapnl.dividerlocation";
	String PREFKEY_LICENSEIMPORT_PATH = "license.import.path";
	String PREFKEY_SAVEASGROUPPNL = "saveasgrouppnl";
	String PREFKEY_SAVEASPDF_PATH = "saveaspdf.path";
	String PREFKEY_TABLESAVEAS_PATH = "tablesaveas.path";
	String PREFKEY_TABLESAVEAS_FILTER = "tablesaveas.filter";
	String PREFKEY_CUSTOMREPORTSAVEAS_PATH = "customreportsaveas.path";
	String PREFKEY_CUSTOMREPORTSAVEAS_FILTER = "customreportsaveas.filter";
	String PREFKEY_INVOICES_COLUMNS = "invoices.columns";
	String PREFKEY_INVOICEPROPSPNL = "invoicepropspnl";
	String PREFKEY_SELECTTABLEITEMPNL = "selecttableitempnl";
	String PREFKEY_INVOICEPROPSPNL_COLUMNS = "invoicepropspnl.columns";
	String PREFKEY_INVOICESPNL_SELECT = "invoicespnl.select";
	String PREFKEY_SERVICEREQUESTLOGGING_COLUMNS = "servicerequestlogging.columns";
	String PREFKEY_RTUREGISTERREADINGSPNL = "rtu.register.readings.pnl";
	String PREFKEY_METERREGISTERREPORTPNL = "meter.register.report.pnl";
	String PREFKEY_SORTINVOICEITEMGROUPSBYFOLDER = "invoice.sort.itemgroups.by.folder";
	String PREFKEY_VALIDATIONOVERVIEWFILTER = "validation.overview.filter";
	String PREFKEY_SEARCH_COLUMNS_RTUREGISTER = "search.columns.rturegister";
	String PREFKEY_SEARCH_COLUMNS_RTUREGISTERMAPPING = "search.columns.rturegistermapping";
	String PREFKEY_REPORTS_COLUMNS = "reports.columns";
	String PREFKEY_REPORTS_SAVEAS_PATH = "reports.saveas.path";
	String PREFKEY_RTUMESSAGESOVERVIEW_COLUMNS = "rtumsgoverview.columns";
	String PREFKEY_RTUMESSAGESOVERVIEW_CONTENTS = "rtumsgoverview.contents";
	String PREFKEY_LOGGING_COLUMNS = "logging.columns";
	String PREFKEY_MESSAGESERVICELOGGING_COLUMNS = "messageservicelogging.columns";
	String PREFKEY_FOLDERJOURNAL_COLUMNS = "folderjournal.columns";
	String PREFKEY_TEST_REGRESSIONMODEL = "regressionmodel.test";
	String PREFKEY_DIVIDERLOCATION = ".dividerlocation";
	String PREFKEY_SEARCH_COLUMNS_LOADPROFILE = "search.columns.loadprofile";
	String PREFKEY_SEARCH_COLUMNS_KPI = "search.columns.kpi";
    String PREFKEY_SEARCH_COLUMNS_DATACOLLECTIONMONITOR = "search.columns.datacollectionmonitor";
    String PREFKEY_SEARCH_COLUMNS_DATACOMMUNICATIONSET = "search.columns.datacommunicationset";
    String PREFKEY_SEARCH_COLUMNS_LOGBOOK = "search.columns.logbook";
    String PREFKEY_SEARCH_COLUMNS_REGISTERGROUP = "search.columns.registergroup";
    String PREFKEY_SEARCH_COLUMS_COMTASK = "search.colums.comtask";
    String PREFKEY_SEARCH_COLUMS_COMTASKEXECUTION = "search.colums.comtaskexecution";
	String PREFKEY_LOADPROFILEREPORTPERIOD_FROM = "loadprofilereport.period.from";
	String PREFKEY_LOADPROFILEREPORTPERIOD_TO = "loadprofilereport.period.to";
    String PREFKEY_DEVICEMESSAGEPNL = "devicemessagepnl";
    // Additional User Preferences that can be edited via \Tools\UserPreferences

	// !! when adding new one: add the new value to
	// !! com.energyict.mdwswing.windows.EditableUserPreferencesPnl.PreferencesTableModel.initKeys() method
	// !! Do not forget to add the type in
	// !! com.energyict.mdwswing.windows.EditableUserPreferencesPnl.PreferencesTableModel.initTypes()

	// Node Name for additional userPreferences
	String PREFKEY_USERPREFS_NODENAME = "userprefs";
	// showtips: used for Excel Reports  where 'tips' are shown before creating or updating the template, or closing the template's property window
	// "0=do not show tips/1=show tips":
	String PREFKEY_USERPREFS_SHOWTIPS = "showtips";
	// browse.versions.tableview: when showing the (folder)versions in the browse panel, the user can choose
	// between the table view (=1) or the view as in the attributes tab of the folder properties panel (=0)
	String PREFKEY_USERPREFS_BROWSE_TABLEVIEW = "browse.versions.tableview";
	// browse.showall: when showing relations in the browse panel,
	// the user can choose to also see the obsolete items (=1) or not (=0)
	String PREFKEY_USERPREFS_BROWSE_SHOWALL = "browse.showall";
	// browse.showempty: when showing relations in the browse panel,
	// the user can choose to see relations panels for relationtype for which no relation on the object exists or not
	String PREFKEY_USERPREFS_BROWSE_SHOWEMPTY = "browse.showempty";
	// browse.viewdate: when showing relations in the browse panel,
	// the user can choose the view date
	String PREFKEY_USERPREFS_BROWSE_VIEWDATE = "browse.viewdate";
	// browse.limitresults: when showing relations in the browse panel,
	// the user can limit the number of relations shown
	String PREFKEY_USERPREFS_BROWSE_LIMITRESULTS = "browse.limitresults";
	// browse.maxresults: when showing relations in the browse panel,
	// the user can limit the number of relations shown to this number
	String PREFKEY_USERPREFS_BROWSE_MAXRESULTS = "browse.maxresults";
	// When a folder is successfully created using a prototype
	// EIServer can show a message informing about that successful creation
	// 1=show it / 0=don't show it
	String PREFKEY_USERPREFS_SHOW_MSG_FOLDER_CREATED = "show.msg.folder.created";
	// context variables of the workplace's "Historics" browse panel tab
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_FROMDATE = "browse.historics.fromdate";
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_TODATE = "browse.historics.todate";
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_ENABLED = "browse.historics.show.enabled";
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_CONSUMED = "browse.historics.show.consumed";
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_CANCELED = "browse.historics.show.canceled";
	String PREFKEY_USERPREFS_WORKPLACE_HISTORICS_ALLOCATED = "browse.historics.show.allocated";
	// context variables of the workplace's "Statistics" browse panel tab
	String PREFKEY_USERPREFS_WORKPLACE_STATISTICS_ENABLED = "browse.statistics.show.enabled";
	String PREFKEY_USERPREFS_WORKPLACE_STATISTICS_CONSUMED = "browse.statistics.show.consumed";
	String PREFKEY_USERPREFS_WORKPLACE_STATISTICS_CANCELED = "browse.statistics.show.canceled";
	String PREFKEY_USERPREFS_WORKPLACE_STATISTICS_ALLOCATED = "browse.statistics.show.allocated";

	String PREFKEY_USERPREFS_IMPORTFILE_IMPORTER = "importer";
	String PREFKEY_USERPREFS_IMPORTFILE_FILE = "file";
	String PREFKEY_USERPREFS_IMPORTFILE_WIDTH = "import.file.width";
	String PREFKEY_USERPREFS_IMPORTFILE_HEIGHT = "import.file.height";

	String PREFKEY_USERPREF_ITEMSPERPAGE = "items.per.page";
	String PREFKEY_COLUMNS_SDP_ESTIMATIONS = "columns.ServiceDeliveryPointEstimations";
	String PREFKEY_COLUMNS_SDP_WEIGHTS = "columns.ServiceDeliveryPointWeights";

	String PREFKEY_PRICING_VS_INVOICECHECKPNL = "pricingvsinvoicecheckpnl";

    String PREFKEY_SEARCH_COLUMNS_CONNECTIONTASK = "search.columns.connectiontask";
    String PREFKEY_SEARCH_COLUMNS_COMPORTPOOL = "search.columns.comportpool";

    String PREFKEY_COMTASKTABLE_COLUMNS = "comtasktable.columns";

    String PREFKEY_PROTOCOLTESTER_FILE_PATH = "protocoltester.file.path";

    String PREFKEY_EXPORTSCHEDULEITEMSPNL = "exportscheduleitemspnl";

	// Alignment
	int ALIGN_TOP = 1;
	int ALIGN_LEFT = 2;
	int ALIGN_BOTTOM = 3;
	int ALIGN_RIGHT = 4;
	int ALIGN_VCENTERED = 5;
	int ALIGN_HCENTERED = 6;
	// Arrangement (Z-order)
	int ARRANGE_TOFRONT = 1;
	int ARRANGE_TOBACK = 2;

	// Validation colors
	Color VALIDATIONCOLOR_NONE = new Color(255, 255, 255);
	Color VALIDATIONCOLOR_OK_TABLE = new Color(255, 255, 255);
	Color VALIDATIONCOLOR_OK_GRAPH = new Color(0, 150, 0);
	Color VALIDATIONCOLOR_SUSPECT = new Color(255, 75, 75);
	//    Color VALIDATIONCOLOR_SUSPECT = new Color(255, 225, 225);
	Color VALIDATIONCOLOR_MODIFIED = new Color(255, 255, 100);
	// Color VALIDATIONCOLOR_MODIFIED = new Color(255,255,190);
	Color VALIDATIONCOLOR_CONFIRMED = new Color(0, 255, 255);
	// Color VALIDATIONCOLOR_CONFIRMED = new Color(230, 255, 230);
	Color VALIDATIONCOLOR_LIMITS = Color.BLUE;
	Color VALIDATIONCOLOR_REFERENCE = new Color(200, 200, 200);
	Color VALIDATIONCOLOR_WARNING = new Color(255, 175, 0);

	// EIServer wide strings
	String NONE = "<< " +TranslatorProvider.instance.get().getTranslator().getTranslation("none", "None") + " >>";
	String UNCHANGED = "<< " + TranslatorProvider.instance.get().getTranslator().getTranslation("unchanged", "Unchanged") + " >>";
	String UNKNOWN = "<< " + TranslatorProvider.instance.get().getTranslator().getTranslation("unknown", "Unknown") + " >>";
	String ALL = "<< " + TranslatorProvider.instance.get().getTranslator().getTranslation("all", "All") + " >>";
}
