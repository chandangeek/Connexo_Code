package com.energyict.mdc.engine.offline.gui.actions;

import java.util.Arrays;
import java.util.Optional;

/**
 * Action is an enumaration of operations a {@link com.energyict.mdw.core.User User} can perform
 * on a {@link com.energyict.cpo.BusinessObject BusinessObject}. The list of actions a User can
 * execute is defined through the user's {@link com.energyict.mdw.core.Role Role}.
 *
 * @author Steven Willems.
 * @since Jun 2, 2009.
 */
public enum UserAction {
    CREATE("create"),
    BROWSE("browse"),
    UPDATE("updateProperties"),
    DELETE("delete"),
    MOVETOFOLDER("moveToFolder"),
    COPY("copy"),
    GOTO("goTo"),  // EISERVER-934 no more used, but keep it to avoid problems in case it is still in the database
    VIEW_REFERENCES("viewReferences"),
    VIEW_JOURNAL("viewJournal"),
    CREATETABLE("createTable"),
    CREATEGRAPH("createGraph"),
    CREATEQUERYSPEC("createQuerySpec"),
    CREATEEXCELREPORT("createExcelReport"),
    CREATEEISPECTOR("createEISpector"),
    CREATEFOLDER("createFolder"),
    BILLINGREPORT("billingReport"),
    EDIT_CALENDAR("editCalendar"),
    READVALUES("readValues"),
    EDITVALUES("editValues"),
    REPORT("report"),
    QUICKREPORT("quickReport"),
    REBUILD("rebuild"),
    EXECUTE("execute"),
    SCHEDULE("schedule"),
    START("start"),
    STOP("stop"),
    DEPENDENCY("dependencyTree"),
    VIEWSQL("viewSql"),
    CHANGEMETERTYPE("changeVirtualMeterType"),
    CHANGETOVERSIONED("changeToVersionedMeter"),
    CHANGERTUTYPE("changeDeviceConfiguration"), // GDE (18-jun-2013): translation key changed
    RTUEVENTS("rtuEvents"),
    RTUREGISTERS("rtuRegisters"), // EISERVER-934 no more used, but keep it to avoid problems in case it is still in the database
    DELETERTUEVENTS("deleteRtuEvents"),
    ADDCHANNEL("addChannel"),
    READNOW("triggerResumeCommunication"), // new translation (EISERVERSG-3936)
    VIEW("view"),
    EDIT("edit"),
    TESTEISPECTOR("eispectorTest"),
    RESTORE("restore"), // EISERVER-934 no more used, but keep it to avoid problems in case it is still in the database
    VALIDATE("validate"),
    EDITCHANNELDATA("editChannelValues"),
    EDITVALIDATIONRESULTS("editValidationResults"),
    SHOWPRIMEREGISTERREADINGS("primeRegisterReadings"),
    SHOWREGISTERREADINGS("registerReadings"),
    CREATERTU("createRtu"),
    CREATEVIRTUALMETER("createMeter"),
    CREATEVIRTUALMETERTYPE("createMeterType"),
    CREATEPARAMETER("createParameter"),
    CREATEMETERPORTFOLIO("createMeterPortfolio"),
    CREATEEXPORTSCHEDULE("createExportSchedule"),
    CREATEMETERREGISTER("createMeterRegister"),
    CREATECODE("createCode"),
    CREATEGROUP("createGroup"),
    ADDREGISTERDATA("addRegisterData"),
    EDITREGISTERDATA("editRegisterData"),
    CREATEFOLDERMAP("createMap"),
    CREATEUSERFILE("createUserFile"),
    CREATEGROUPREPORTSPEC("createGroupReportSpec"),
    IMPORT_GROUPREPORTSPEC("groupReportSpec"), // TODO: Is this action somewhere used? [gde]
    CHANGE_FOLDERTREEACCESSRIGHTS("changeFolderAccesRights"),
    ADDCONSUMPTIONDATA("addConsumptionData"),
    EDITCONSUMPTIONDATA("editConsumptionData"),
    CREATEREPORT("createReport"),
    CONSUMPTION_REPORT("consumptionReport"),
    ESTIMATED_CONSUMPTION_REPORT("estimatedConsumptionReport"),
    VALIDATIONOVERVIEW("validationOverview"),
    FOLDERJOURNAL("folderJournal"),
    CREATECOMPOSITEGRAPH("createCompositeGraph"),
    CREATEFOLDERALIAS("createFolderAlias"),
    CREATEEXCELTEMPLATE("createExcelTemplate"),
    VIEWTRANSFERS("viewTransfers"),
    VIEWSCORES("viewScores"),
    OPEN("open"),
    VIEWREADINGS("viewReadings"),
    EDITREADINGS("editReadings"),
    PUBLISH("publish"),
    PUBLISHTOALL("publishToAll"),
    ADDGRAPHTEMPLATE("addGraphTemplate"),
    FREETEXTFORMULAEDIT("freeTextFormulaEdit"),
    QUICKSEARCH("quickSearch"),
    PASSWORD_MANAGEMENT("passwordManagement"),
    CREATEKPI("createKpi"),
    CREATEDATACOLLECTIONMONITOR("createDataCollectionMonitor"),
    CREATEDATACOMMUNICATIONSET("createDataCommunicationSet"),
    UPSERT_FOLDERVERSION("upsertFolderVersion"), // new since 9.1.3 (and 8.11.37)
    DELETE_FOLDERVERSION("deleteFolderVersion"), // new since 9.1.3 (and 8.11.37)
    // 'MeteringWarehouse' actions

    SHOWCOMMUNICATIONSETTINGS("showCommunicationConnectionOverview"), // new translation (EISERVERSG-3936)
    SHOWSYSTEMPARAMETERS("showSystemParameters"),
    EDITSYSTEMPARAMETERS("editSystemParameters"),
    SHOWMASTERDATA("showMasterData"),
    SHOWSYSTEMLOGBOOK("showSystemLogbook"),
    SHOWIMPORTLOGGING("showImportLogging"),
    SHOWEXPORTLOGGING("showExportLogging"),
    SHOWEDILOGGING("showEdiLogging"),
    SHOWCONSUMPTIONREQUESTLOGGING("showConsumptionRequestLogging"),
    SHOWSERVICEREQUESTLOGGING("showServiceRequestLogging"),
    SHOWMESSAGESERVICELOGGING("showMessageServiceLogging"),
    SHOWOBJECTTYPES("showObjectTypes"),
    REBUILDMETERS("rebuildMeters"),
    SHOWPRICINGDATA("showInvoicingAndPricingData"),
    EXPORTEISERVERDATA("exportMasterData"),
    IMPORTEISERVERDATA("importMasterData"),
    SHOWSCRIPTS("showScripts"),
    SHOWMANUALMETERREADINGDATA("showManualMeterReadingData"),
    SHOWREMOTEMETERREADINGDATA("showRemoteMeterReadingData"),
    SHOWPROTOTYPES("showPrototypes"),
    SHOWAUDITINFO("showAuditTrail"),
    SHOWINVOICINGDATA("showInvoicing"),
    SHOWUSERMANAGEMENT("showUserManagement"),
    SHOWREPORTING("showReportManagement"),
    SHOWPROJECTMANAGEMENT("showProjectManagement"),
    SHOWPROJECTS("showProjects"),
    IMPORTFILE("importFile"),

    // Folder actions
    EXECUTEFOLDERACTION1("executeFolderActionLevel1"),
    EXECUTEFOLDERACTION2("executeFolderActionLevel2"),
    EXECUTEFOLDERACTION3("executeFolderActionLevel3"),
    EXECUTEFOLDERACTION4("executeFolderActionLevel4"),

    // Device actions
    EXECUTERTUACTION1("executeRtuActionLevel1"),
    EXECUTERTUACTION2("executeRtuActionLevel2"),
    EXECUTERTUACTION3("executeRtuActionLevel3"),
    EXECUTERTUACTION4("executeRtuActionLevel4"),

    // Virtual meter actions
    EXECUTEMPTACTION1("executeMptActionLevel1"),
    EXECUTEMPTACTION2("executeMptActionLevel2"),
    EXECUTEMPTACTION3("executeMptActionLevel3"),
    EXECUTEMPTACTION4("executeMptActionLevel4"),

    // Invoice actions
    APPROVE("approve"),
    REOPEN("reopen"),

    // BPM
    // START (cf higher)
    CANCEL("cancel"),
    COMPLETE("complete"),
    ARCHIVE("archive"),
    RESUME("resume"),
    ABORT("abort"),
    SHOWALARMMANAGEMENT("showAlarmManagement"),

    // RPT Module OfflineActions
    SHOWREPORTS("showReports"),
    CREATEREPORTSCHEDULE("createReportSchedule"),

    // DeviceMessage
    EXECUTEDEVICEMESSAGES1("executeDeviceMessagesLevel1"),
    EXECUTEDEVICEMESSAGES2("executeDeviceMessagesLevel2"),
    EXECUTEDEVICEMESSAGES3("executeDeviceMessagesLevel3"),
    EXECUTEDEVICEMESSAGES4("executeDeviceMessagesLevel4"),

    // Security Property related
    VIEWDEVICESECURITYPROPERTIES1("viewDeviceSecurityPropertiesLevel1"),
    VIEWDEVICESECURITYPROPERTIES2("viewDeviceSecurityPropertiesLevel2"),
    VIEWDEVICESECURITYPROPERTIES3("viewDeviceSecurityPropertiesLevel3"),
    VIEWDEVICESECURITYPROPERTIES4("viewDeviceSecurityPropertiesLevel4"),
    EDITDEVICESECURITYPROPERTIES1("editDeviceSecurityPropertiesLevel1"),
    EDITDEVICESECURITYPROPERTIES2("editDeviceSecurityPropertiesLevel2"),
    EDITDEVICESECURITYPROPERTIES3("editDeviceSecurityPropertiesLevel3"),
    EDITDEVICESECURITYPROPERTIES4("editDeviceSecurityPropertiesLevel4"),

    BROWSE_WORKPLACE("browseWorkplace"), // EISERVERSG-3936
    DEVICE_MESSAGES_OVERVIEW("deviceMessagesOverview"), // EISERVERSG-3936
    COMMUNICATION_SETUP("communicationSetup"), // EISERVERSG-3936

    // UsarAction for handling migration in which there are some actions in the database that do no exists in this enum
    // Should NOT be used by any factory !!!!
    UNKOWN_ACTION_IN_DATABASE_MIGRATION_NEEDS_TO_BE_RUN("unkownActionInDatabaseMigrationNeedsToBeRun"),

    // ComTask executable related
    EXECUTECOMTASK1("executeComTaskLevel1"),
    EXECUTECOMTASK2("executeComTaskLevel2"),
    EXECUTECOMTASK3("executeComTaskLevel3"),
    EXECUTECOMTASK4("executeComTaskLevel4"),

    USECOMSERVERMOBILE("useComServerMobile"),  // EISERVERSG-3755 no more used, but keep it to avoid problems in case it is still in the database

    // Message rules
    SHOW_MESSAGE_LIMITATION_RULES("showMessageLimitationRules"),

    // Dualcontrol related
    DUAL_CONTROL_REQUEST_APPROVAL("dualControlRequestApproval"),
    DUAL_CONTROL_APPROVE_CHANGES("dualControlApprove");

    private String key;

    private UserAction(String key) {
        this.key = key;
    }

    public String getDisplayNameKey() {
        return key == null ? toString() : key;
    }

    public static UserAction fromString(String key) {
        Optional<UserAction> userAction = Arrays.stream(UserAction.values()).filter(u -> u.getDisplayNameKey().equals(key)).findFirst();
        return userAction.isPresent() ? userAction.get() : null;
    }

    // We added a line to com.energyict.mdw.xml.MdwPersistentDelegates for this class
    // to be able to nicely serialize this one (audit)
}
