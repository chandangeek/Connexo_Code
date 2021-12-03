/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceBulkCreateRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location.UtilitiesDeviceLocationBulkNotificationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location.UtilitiesDeviceLocationNotificationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod.PointOfDeliveryAssignedNotificationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod.PointOfDeliveryBulkAssignedNotificationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterBulkCreateRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation.StatusChangeRequestCancellationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateBulkEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultBulkCreateConfirmationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateConfirmationEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentBulkCancellationRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation.MeterReadingDocumentCancellationRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeRequestEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.search.SapAttributesDeviceSearchDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.sendmeterread.MeterReadingResultCreateEndpoint;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterPodNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterPodNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceLocationNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceLocationNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.PodNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.PodNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceLocationNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceLocationNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread.MasterMeterReadingResultCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread.MasterMeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread.MeterReadingResultCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread.MeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckConfirmationTimeoutHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckScheduledRequestHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckStatusChangeCancellationHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.SearchDataSourceHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.UpdateSapExportTaskHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.security.Privileges;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static java.time.temporal.ChronoUnit.DAYS;

@Singleton
@Component(
        name = "com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator",
        service = {WebServiceActivator.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + WebServiceActivator.COMPONENT_NAME},
        immediate = true)
public class WebServiceActivator implements MessageSeedProvider, TranslationKeyProvider {
    private static final Logger LOGGER = Logger.getLogger(WebServiceActivator.class.getName());
    private static final String DEVICE_TYPES_MAPPING = "com.elster.jupiter.sap.device.types.mapping";
    private static final String DEFAULT_DEVICE_TYPES_MAPPING = "MTREAHW0MV:A1860;MTREAHW0LV:AS3500";
    private static final String UUD_SUCCESSFUL_ERROR_CODES = "com.elster.jupiter.sap.uud.successfulerrorcodes";
    public static final String REGISTER_RECURRENCE_CODE = "com.elster.jupiter.sap.register.recurrencecode";
    public static final String REGISTER_DIVISION_CATEGORY_CODE = "com.elster.jupiter.sap.register.divisioncategorycode";

    public static final String COMPONENT_NAME = "SAP";
    public static final String APPLICATION_NAME = "MultiSense";
    public static final String DEFAULT_METERING_SYSTEM_ID = "HON";
    public static final String PROCESSING_ERROR_CATEGORY_CODE = "PRE";
    public static final String SUCCESSFUL_PROCESSING_TYPE_ID = "000";
    public static final String UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID = "001";
    public static final List<SAPMeterReadingDocumentReason> METER_READING_REASONS = new CopyOnWriteArrayList<>();
    public static final List<StatusChangeRequestCreateConfirmation> STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<StatusChangeRequestBulkCreateConfirmation> STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<StatusChangeRequestCancellationConfirmation> STATUS_CHANGE_REQUEST_CANCELLATION_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentRequestConfirmation> METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentBulkRequestConfirmation> METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentResult> METER_READING_DOCUMENT_RESULTS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentBulkResult> METER_READING_DOCUMENT_BULK_RESULTS = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceCreateConfirmation> UTILITIES_DEVICE_CREATE_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceBulkCreateConfirmation> UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceRegisterBulkCreateConfirmation> UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceRegisterCreateConfirmation> UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceRegisteredNotification> UTILITIES_DEVICE_REGISTERED_NOTIFICATION = new CopyOnWriteArrayList<>();
    public static final List<UtilitiesDeviceRegisteredBulkNotification> UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION = new CopyOnWriteArrayList<>();
    public static final List<MeasurementTaskAssignmentChangeConfirmation> MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentCancellationConfirmation> METER_READING_DOCUMENT_CANCELLATION_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingDocumentBulkCancellationConfirmation> METER_READING_DOCUMENT_BULK_CANCELLATION_CONFIRMATION = new CopyOnWriteArrayList<>();
    public static final List<MeterRegisterChangeConfirmation> METER_REGISTER_CHANGE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterReadingResultCreateConfirmation> METER_READING_RESULT_CREATE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final List<MeterRegisterBulkChangeConfirmation> METER_REGISTER_BULK_CHANGE_CONFIRMATIONS = new CopyOnWriteArrayList<>();
    public static final String EXPORT_TASK_NAME = "sap.soap.measurementtaskassignment.export.task";
    public static final String EXPORT_TASK_DEVICE_GROUP_NAME = "sap.soap.measurementtaskassignment.device.group";
    public static final String LIST_OF_ROLE_CODES = "sap.soap.measurementtaskassignment.role.codes";
    public static final String EXPORT_TASK_START_ON_DATE = "sap.soap.measurementtaskassignment.start.on";
    public static final String EXPORT_TASK_EXPORT_WINDOW = "sap.soap.measurementtaskassignment.export.window";
    public static final String EXPORT_TASK_UPDATE_WINDOW = "sap.soap.measurementtaskassignment.update.window";
    public static final String EXPORT_TASK_NEW_DATA_ENDPOINT = "sap.soap.measurementtaskassignment.new.data.endpoint";
    public static final String EXPORT_TASK_UPDATED_DATA_ENDPOINT = "sap.soap.measurementtaskassignment.updated.data.endpoint";

    private static final String METERING_SYSTEM_ID = "sap.soap.metering.system.id";
    private static final String DEFAULT_EXPORT_WINDOW = "Yesterday";
    private static final String DEFAULT_UPDATE_WINDOW = "Previous month";

    // Update SAP export task
    private static final String UPDATE_SAP_EXPORT_TASK_NAME = "Update SAP export group task";
    private static final int UPDATE_SAP_EXPORT_TASK_RETRY_DELAY = 60;

    // Search data sources by SAP id's
    private static final String OBJECT_SEARCH_INTERVAL_PROPERTY = "com.elster.jupiter.sap.objectsearchinterval";
    private static final String SEARCH_DATA_SOURCE_TASK_NAME = "SearchDataSourceTask";
    private static final String SEARCH_DATA_SOURCE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int SEARCH_DATA_SOURCE_TASK_RETRY_DELAY = 60;

    // Check SAP confirmation timeout
    private static final String CHECK_CONFIRMATION_TIMEOUT_FREQUENCY_PROPERTY = "com.elster.jupiter.sap.checkconfirmationtimeoutfrequency";
    private static final String CHECK_CONFIRMATION_TIMEOUT_TASK_NAME = "CheckConfirmationTimeoutTask";
    private static final String CHECK_CONFIRMATION_TIMEOUT_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int CHECK_CONFIRMATION_TIMEOUT_TASK_RETRY_DELAY = 60;

    // Check scheduled SAP requests
    private static final String CHECK_SCHEDULED_REQUEST_FREQUENCY_PROPERTY = "com.elster.jupiter.sap.checkscheduledrequestsfrequency";
    private static final String CHECK_SCHEDULED_REQUEST_TASK_NAME = "CheckScheduledRequestTask";
    private static final String CHECK_SCHEDULED_REQUEST_TASK_SCHEDULE = "0 0/60 * 1/1 * ? *";
    private static final int CHECK_SCHEDULED_REQUEST_TASK_RETRY_DELAY = 60;

    private static String exportTaskName;
    private static String exportTaskDeviceGroupName;
    private static List<String> listOfRoleCodes;
    private static Instant exportTaskStartOnDate;
    private static RelativePeriod exportTaskExportWindow;
    private static RelativePeriod exportTaskUpdateWindow;
    private static String exportTaskNewDataEndpointName;
    private static String exportTaskUpdatedDataEndpointName;

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile UserService userService;
    private volatile PropertySpecService propertySpecService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile LogBookService logBookService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile IssueService issueService;
    private volatile BatchService batchService;
    private volatile ServiceCallService serviceCallService;
    private volatile JsonService jsonService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile WebServicesService webServicesService;
    private volatile MessageService messageService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile TaskService taskService;
    private volatile BundleContext bundleContext;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile DataExportService dataExportService;
    private volatile TimeService timeService;
    private volatile SearchService searchService;
    private volatile MeasurementTaskAssignmentChangeProcessor measurementTaskAssignmentChangeProcessor;

    private final Map<AdditionalProperties, Integer> sapProperties = new HashMap<>();
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();
    private Map<String, String> deviceTypesMap;
    private Map<String, Pair<MacroPeriod, TimeAttribute>> recurrenceCodeMap;
    private Map<String, CIMPattern> divisionCategoryCodeMap;
    private String meteringSystemId;
    private List<String> uudSuccessfulErrorCodes = new ArrayList<>();
    private SearchDomainExtension sapAttributesSearchExtension;

    public static Optional<String> getExportTaskName() {
        return Optional.ofNullable(exportTaskName);
    }

    public static Optional<String> getExportTaskDeviceGroupName() {
        return Optional.ofNullable(exportTaskDeviceGroupName);
    }

    public static List<String> getListOfRoleCodes() {
        return listOfRoleCodes;
    }

    public static Instant getExportTaskStartOnDate() {
        return exportTaskStartOnDate;
    }

    public static RelativePeriod getExportTaskExportWindow() {
        return exportTaskExportWindow;
    }

    public static RelativePeriod getExportTaskUpdateWindow() {
        return exportTaskUpdateWindow;
    }

    public static Optional<String> getExportTaskNewDataEndpointName() {
        return Optional.ofNullable(exportTaskNewDataEndpointName);
    }

    public static Optional<String> getExportTaskUpdatedDataEndpointName() {
        return Optional.ofNullable(exportTaskUpdatedDataEndpointName);
    }

    public Map<String, String> getDeviceTypesMap() {
        return deviceTypesMap;
    }

    public Map<String, Pair<MacroPeriod, TimeAttribute>> getRecurrenceCodeMap() {
        return recurrenceCodeMap;
    }

    public Map<String, CIMPattern> getDivisionCategoryCodeMap() {
        return divisionCategoryCodeMap;
    }

    public Map<AdditionalProperties, Integer> getSapProperties() {
        return sapProperties;
    }

    public Integer getSapProperty(AdditionalProperties property) {
        return sapProperties.get(property);
    }

    public String getMeteringSystemId() {
        return meteringSystemId;
    }

    public List<String> getUudSuccessfulErrorCodes() {
        return uudSuccessfulErrorCodes;
    }

    public WebServiceActivator() {
        // for OSGI purposes
    }

    @Inject
    public WebServiceActivator(BundleContext bundleContext, Clock clock, ThreadPrincipalService threadPrincipalService,
                               TransactionService transactionService, MeteringService meteringService,
                               NlsService nlsService, UpgradeService upgradeService,
                               DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                               DeviceService deviceService, UserService userService, BatchService batchService,
                               PropertySpecService propertySpecService, PropertyValueInfoService propertyValueInfoService, LogBookService logBookService,
                               EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                               JsonService jsonService, CustomPropertySetService customPropertySetService,
                               WebServicesService webServicesService, MessageService messageService,
                               TaskService taskService, SAPCustomPropertySets sapCustomPropertySets, OrmService ormService,
                               MeteringGroupsService meteringGroupsService, DataExportService dataExportService,
                               TimeService timeService,
                               MeasurementTaskAssignmentChangeProcessor measurementTaskAssignmentChangeProcessor,
                               DeviceAlarmService deviceAlarmService,
                               IssueService issueService,
                               SearchService searchService) {
        this();
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        setMeteringService(meteringService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setDeviceLifeCycleService(deviceLifeCycleService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceService(deviceService);
        setUserService(userService);
        setBatchService(batchService);
        setPropertySpecService(propertySpecService);
        setPropertyValueInfoService(propertyValueInfoService);
        setLogBookService(logBookService);
        setEndPointConfigurationService(endPointConfigurationService);
        setServiceCallService(serviceCallService);
        setJsonService(jsonService);
        setCustomPropertySetService(customPropertySetService);
        setWebServicesService(webServicesService);
        setMessageService(messageService);
        setTaskService(taskService);
        setSAPCustomPropertySets(sapCustomPropertySets);
        setOrmService(ormService);
        seMeteringGroupsService(meteringGroupsService);
        setDataExportService(dataExportService);
        setTimeService(timeService);
        setMeasurementTaskAssignmentChangeProcessor(measurementTaskAssignmentChangeProcessor);
        setDeviceAlarmService(deviceAlarmService);
        setIssueService(issueService);
        setSearchService(searchService);
        activate(bundleContext);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DeviceLifeCycleService.class).toInstance(deviceLifeCycleService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(UserService.class).toInstance(userService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PropertyValueInfoService.class).toInstance(propertyValueInfoService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(DeviceAlarmService.class).toInstance(deviceAlarmService);
                bind(IssueService.class).toInstance(issueService);
                bind(BatchService.class).toInstance(batchService);
                bind(JsonService.class).toInstance(jsonService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(WebServicesService.class).toInstance(webServicesService);
                bind(MessageService.class).toInstance(messageService);
                bind(TaskService.class).toInstance(taskService);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(DataExportService.class).toInstance(dataExportService);
                bind(TimeService.class).toInstance(timeService);
                bind(MeasurementTaskAssignmentChangeProcessor.class).toInstance(measurementTaskAssignmentChangeProcessor);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(NlsService.class).toInstance(nlsService);
                bind(SearchService.class).toInstance(searchService);
                bind(WebServiceActivator.class).toInstance(WebServiceActivator.this);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        dataModel.register(getModule());

        loadProperties(bundleContext);
        getServiceCallCustomPropertySets().values().forEach(customPropertySetService::addCustomPropertySet);

        upgradeService.register(InstallIdentifier.identifier(APPLICATION_NAME, COMPONENT_NAME), dataModel, Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 7, 1), UpgraderV10_7_1.class)
                        .put(version(10, 7, 2), UpgraderV10_7_2.class)
                        .put(version(10, 7, 3), UpgraderV10_7_3.class)
                        .put(version(10, 8), UpgraderV10_8.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .put(version(10, 9, 20), UpgraderV10_9_20.class)
                        .build());

        registerServices(bundleContext);

        exportTaskName = getPropertyValue(bundleContext, EXPORT_TASK_NAME);
        exportTaskDeviceGroupName = getPropertyValue(bundleContext, EXPORT_TASK_DEVICE_GROUP_NAME);
        listOfRoleCodes = Collections.emptyList();
        Optional.ofNullable(getPropertyValue(bundleContext, LIST_OF_ROLE_CODES)).ifPresent(r -> listOfRoleCodes = Arrays.asList((r.split(","))));

        exportTaskStartOnDate = clock.instant().plus(1, DAYS);
        Optional<String> exportTaskStartOnDateParam = Optional.ofNullable(getPropertyValue(bundleContext, EXPORT_TASK_START_ON_DATE));
        if (exportTaskStartOnDateParam.isPresent()) {
            LocalDateTime startOnDate = LocalDateTime.parse(exportTaskStartOnDateParam.get(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH));
            exportTaskStartOnDate = startOnDate.atZone(ZoneId.systemDefault()).toInstant();
        }
        exportTaskExportWindow = findRelativePeriodOrThrowException(Optional.ofNullable(getPropertyValue(bundleContext, EXPORT_TASK_EXPORT_WINDOW))
                .orElse(DEFAULT_EXPORT_WINDOW));
        exportTaskUpdateWindow = findRelativePeriodOrThrowException(Optional.ofNullable(getPropertyValue(bundleContext, EXPORT_TASK_UPDATE_WINDOW))
                .orElse(DEFAULT_UPDATE_WINDOW));
        exportTaskNewDataEndpointName = getPropertyValue(bundleContext, EXPORT_TASK_NEW_DATA_ENDPOINT);
        exportTaskUpdatedDataEndpointName = getPropertyValue(bundleContext, EXPORT_TASK_UPDATED_DATA_ENDPOINT);

        meteringSystemId = Optional.ofNullable(getPropertyValue(bundleContext, METERING_SYSTEM_ID)).orElse(DEFAULT_METERING_SYSTEM_ID);

        sapAttributesSearchExtension = new SapAttributesDeviceSearchDomainExtension(dataModel, clock, thesaurus);
        searchService.register(sapAttributesSearchExtension);
        loadDeviceTypesMap();
        createOrUpdateUpdateSapExportTask();
        createOrUpdateSearchDataSourceTask();
        createOrUpdateCheckConfirmationTimeoutTask();
        createOrUpdateCheckScheduledRequestTask();
        createOrUpdateCheckStatusChangeCancellationTask();

        loadRecurrenceCodeMap();
        loadDivisionCategoryCodeMap();
        loadUudSuccessfulErrorCodes();

        failOngoingExportTaskServiceCalls();
    }

    private void loadUudSuccessfulErrorCodes() {
        String valueCodes = bundleContext.getProperty(UUD_SUCCESSFUL_ERROR_CODES);
        if (!Checks.is(valueCodes).emptyOrOnlyWhiteSpace()) {
            uudSuccessfulErrorCodes = Arrays.stream(valueCodes.split(","))
                    .map(String::trim).collect(Collectors.toList());
        }
    }

    private void loadDeviceTypesMap() {
        try {
            String strMap = Optional.ofNullable(getPropertyValue(bundleContext, DEVICE_TYPES_MAPPING)).orElse(DEFAULT_DEVICE_TYPES_MAPPING);
            deviceTypesMap = Arrays.stream(strMap.split(";"))
                    .map(String::trim)
                    .map(s -> s.split(":"))
                    .collect(Collectors.toMap(e -> e[0], e -> e[1]));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, MessageSeeds.ERROR_LOADING_PROPERTY.getDefaultFormat(DEVICE_TYPES_MAPPING, ex.getLocalizedMessage()));
            deviceTypesMap = Collections.emptyMap();
        }
    }

    private void loadRecurrenceCodeMap() {
        try {
            Optional<String> strMap = Optional.ofNullable(getPropertyValue(bundleContext, REGISTER_RECURRENCE_CODE));
            if (strMap.isPresent()) {
                recurrenceCodeMap = Arrays.stream(strMap.get().split(";"))
                        .map(String::trim)
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(e -> e[0].trim(), e -> {
                            String[] codes = e[1].split(",");
                            return Pair.of(MacroPeriod.get(Integer.parseInt(codes[0].trim())), TimeAttribute.get(Integer.parseInt(codes[1].trim())));
                        }));
            } else {
                recurrenceCodeMap = Collections.emptyMap();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, MessageSeeds.ERROR_LOADING_PROPERTY.getDefaultFormat(REGISTER_RECURRENCE_CODE, ex.getLocalizedMessage()));
            recurrenceCodeMap = Collections.emptyMap();
        }
    }

    private void loadDivisionCategoryCodeMap() {
        try {
            Optional<String> strMap = Optional.ofNullable(getPropertyValue(bundleContext, REGISTER_DIVISION_CATEGORY_CODE));
            if (strMap.isPresent()) {
                divisionCategoryCodeMap = Arrays.stream(strMap.get().split(";"))
                        .map(String::trim)
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(e -> e[0].trim(), e -> {
                            String[] codes = e[1].trim().split("\\.");
                            return CIMPattern.parseFromString(codes);
                        }));
            } else {
                divisionCategoryCodeMap = Collections.emptyMap();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, MessageSeeds.ERROR_LOADING_PROPERTY.getDefaultFormat(REGISTER_DIVISION_CATEGORY_CODE, ex.getLocalizedMessage()));
            divisionCategoryCodeMap = Collections.emptyMap();
        }
    }

    private RelativePeriod findRelativePeriodOrThrowException(String name) {
        return timeService.findRelativePeriodByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find relative period '" + name + "'."));
    }

    private void createOrUpdateUpdateSapExportTask() {
        Integer frequency = getSapProperty(AdditionalProperties.UPDATE_SAP_EXPORT_TASK_PROPERTY);

        createOrUpdateActionTask(UpdateSapExportTaskHandlerFactory.UPDATE_SAP_EXPORT_TASK_DESTINATION,
                UPDATE_SAP_EXPORT_TASK_RETRY_DELAY,
                TranslationKeys.UPDATE_SAP_EXPORT_TASK_SUBSCRIBER_NAME,
                UPDATE_SAP_EXPORT_TASK_NAME,
                PeriodicalScheduleExpression.every(frequency).days().at(0, 20, 0).build().encoded());
    }

    private void createOrUpdateCheckStatusChangeCancellationTask() {
        Integer frequency = getSapProperty(AdditionalProperties.CHECK_STATUS_CHANGE_FREQUENCY);

        createOrUpdateActionTask(CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_DESTINATION,
                CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_RETRY_DELAY,
                TranslationKeys.CHECK_STATUS_CHANGE_CANCELLATION_TASK_SUBSCRIBER_NAME,
                CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_DISPLAYNAME,
                PeriodicalScheduleExpression.every(frequency).minutes().at(0).build().encoded());
    }

    private void createOrUpdateSearchDataSourceTask() {
        String property = bundleContext.getProperty(OBJECT_SEARCH_INTERVAL_PROPERTY);
        createOrUpdateActionTask(SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_DESTINATION,
                SEARCH_DATA_SOURCE_TASK_RETRY_DELAY,
                TranslationKeys.SEARCH_DATA_SOURCE_SUBSCRIBER_NAME,
                SEARCH_DATA_SOURCE_TASK_NAME,
                property == null ? SEARCH_DATA_SOURCE_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createOrUpdateCheckConfirmationTimeoutTask() {
        String property = bundleContext.getProperty(CHECK_CONFIRMATION_TIMEOUT_FREQUENCY_PROPERTY);
        createOrUpdateActionTask(CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_DESTINATION,
                CHECK_CONFIRMATION_TIMEOUT_TASK_RETRY_DELAY,
                TranslationKeys.CHECK_CONFIRMATION_TIMEOUT_SUBSCRIBER_NAME,
                CHECK_CONFIRMATION_TIMEOUT_TASK_NAME,
                property == null ? CHECK_CONFIRMATION_TIMEOUT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createOrUpdateCheckScheduledRequestTask() {
        String property = bundleContext.getProperty(CHECK_SCHEDULED_REQUEST_FREQUENCY_PROPERTY);
        createOrUpdateActionTask(CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_DESTINATION,
                CHECK_SCHEDULED_REQUEST_TASK_RETRY_DELAY,
                TranslationKeys.CHECK_SCHEDULED_REQUEST_SUBSCRIBER_NAME,
                CHECK_SCHEDULED_REQUEST_TASK_NAME,
                property == null ? CHECK_SCHEDULED_REQUEST_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createOrUpdateActionTask(String destinationSpecName, int destinationSpecRetryDelay,
                                          TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        threadPrincipalService.set(() -> "Activator");
        try (TransactionContext context = transactionService.getContext()) {
            Optional<RecurrentTask> taskOptional = taskService.getRecurrentTask(taskName);
            if (taskOptional.isPresent()) {
                RecurrentTask task = taskOptional.get();
                task.setScheduleExpressionString(taskSchedule);
                task.save();
            } else {
                DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                        .get()
                        .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
                destination.activate();
                destination.subscribe(subscriberSpecName, WebServiceActivator.COMPONENT_NAME, Layer.DOMAIN);

                taskService.newBuilder()
                        .setApplication(WebServiceActivator.APPLICATION_NAME)
                        .setName(taskName)
                        .setScheduleExpressionString(taskSchedule)
                        .setDestination(destination)
                        .setPayLoad("payload")
                        .scheduleImmediately(true)
                        .build();
            }
            context.commit();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        getServiceCallCustomPropertySets().values().forEach(customPropertySetService::removeCustomPropertySet);
        searchService.unregister(sapAttributesSearchExtension);
    }

    public static Optional<SAPMeterReadingDocumentReason> findReadingReasonProvider(String readingReasonCode) {
        return WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getCodes().contains(readingReasonCode))
                .findFirst();
    }

    public List<UtilitiesDeviceRegisteredNotification> getUtilitiesDeviceRegisteredNotifications() {
        return Collections.unmodifiableList(UTILITIES_DEVICE_REGISTERED_NOTIFICATION);
    }

    private void loadProperties(BundleContext context) {
        EnumSet.allOf(AdditionalProperties.class)
                .forEach(key -> sapProperties.put(key, Optional.ofNullable(context.getProperty(key.getKey()))
                        .map(Integer::valueOf)
                        .orElse(key.getDefaultValue())));
    }

    private Map<String, CustomPropertySet> getServiceCallCustomPropertySets() {
        Map<String, CustomPropertySet> customPropertySetsMap = new HashMap<>();
        customPropertySetsMap.put(ConnectionStatusChangeDomainExtension.class.getName(),
                new ConnectionStatusChangeCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterReadingDocumentCreateRequestDomainExtension.class.getName(),
                new MasterMeterReadingDocumentCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterReadingDocumentCreateResultDomainExtension.class.getName(),
                new MasterMeterReadingDocumentCreateResultCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterReadingDocumentCreateRequestDomainExtension.class.getName(),
                new MeterReadingDocumentCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterReadingDocumentCreateResultDomainExtension.class.getName(),
                new MeterReadingDocumentCreateResultCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterUtilitiesDeviceCreateRequestDomainExtension.class.getName(),
                new MasterUtilitiesDeviceCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName(),
                new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName(),
                new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(UtilitiesDeviceCreateRequestDomainExtension.class.getName(),
                new UtilitiesDeviceCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(UtilitiesDeviceRegisterCreateRequestDomainExtension.class.getName(),
                new UtilitiesDeviceRegisterCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterRegisterChangeRequestDomainExtension.class.getName(),
                new MasterMeterRegisterChangeRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterRegisterChangeRequestDomainExtension.class.getName(),
                new MeterRegisterChangeRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterConnectionStatusChangeDomainExtension.class.getName(),
                new MasterConnectionStatusChangeCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(SubMasterMeterRegisterChangeRequestDomainExtension.class.getName(),
                new SubMasterMeterRegisterChangeRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterUtilitiesDeviceLocationNotificationDomainExtension.class.getName(),
                new MasterUtilitiesDeviceLocationNotificationCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(UtilitiesDeviceLocationNotificationDomainExtension.class.getName(),
                new UtilitiesDeviceLocationNotificationCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterPodNotificationDomainExtension.class.getName(),
                new MasterPodNotificationCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(PodNotificationDomainExtension.class.getName(),
                new PodNotificationCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MasterMeterReadingResultCreateRequestDomainExtension.class.getName(),
                new MasterMeterReadingResultCreateRequestCustomPropertySet(thesaurus, propertySpecService));
        customPropertySetsMap.put(MeterReadingResultCreateRequestDomainExtension.class.getName(),
                new MeterReadingResultCreateRequestCustomPropertySet(thesaurus, propertySpecService));

        return customPropertySetsMap;
    }

    private void registerServices(BundleContext bundleContext) {
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(StatusChangeRequestCreateEndpoint.class),
                InboundServices.SAP_STATUS_CHANGE_REQUEST_CREATE.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(StatusChangeRequestCancellationEndpoint.class),
                InboundServices.SAP_STATUS_CHANGE_REQUEST_CANCELLATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentCreateEndpoint.class),
                InboundServices.SAP_METER_READING_CREATE_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentCreateBulkEndpoint.class),
                InboundServices.SAP_METER_READING_CREATE_BULK_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentResultCreateConfirmationEndpoint.class),
                InboundServices.SAP_METER_READING_RESULT_CONFIRMATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentResultBulkCreateConfirmationEndpoint.class),
                InboundServices.SAP_METER_READING_BULK_RESULT_CONFIRMATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceCreateRequestEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_REQUEST_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceBulkCreateRequestEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_BULK_CREATE_REQUEST_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceRegisterCreateRequestEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_CREATE_REQUEST_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceRegisterBulkCreateRequestEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_BULK_CREATE_REQUEST_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceLocationNotificationEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_NOTIFICATION_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(UtilitiesDeviceLocationBulkNotificationEndpoint.class),
                InboundServices.SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_BULK_NOTIFICATION_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(PointOfDeliveryAssignedNotificationEndpoint.class),
                InboundServices.SAP_POINT_OF_DELIVERY_ASSIGNED_NOTIFICATION_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(PointOfDeliveryBulkAssignedNotificationEndpoint.class),
                InboundServices.SAP_POINT_OF_DELIVERY_BULK_ASSIGNED_NOTIFICATION_C_IN.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeasurementTaskAssignmentChangeRequestEndpoint.class),
                InboundServices.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentCancellationRequestEndpoint.class),
                InboundServices.SAP_SMART_METER_METER_READING_DOCUMENT_ERP_CANCELLATION_CONFIRMATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingDocumentBulkCancellationRequestEndpoint.class),
                InboundServices.SAP_SMART_METER_METER_READING_DOCUMENT_ERP_BULK_CANCELLATION_CONFIRMATION.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterRegisterChangeRequestEndpoint.class),
                InboundServices.SAP_METER_REGISTER_CHANGE_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterRegisterBulkChangeRequestEndpoint.class),
                InboundServices.SAP_METER_REGISTER_BULK_CHANGE_REQUEST.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(StatusChangeRequestBulkCreateEndpoint.class),
                InboundServices.SAP_STATUS_CHANGE_REQUEST_BULK_CREATE.getName());
        registerInboundSoapEndpoint(bundleContext,
                () -> dataModel.getInstance(MeterReadingResultCreateEndpoint.class),
                InboundServices.SAP_METER_READING_RESULT_CREATE_REQUEST.getName());
    }

    private <T extends InboundSoapEndPointProvider> void registerInboundSoapEndpoint(BundleContext bundleContext,
                                                                                     T provider, String serviceName) {
        Dictionary<String, Object> properties = new Hashtable<>(ImmutableMap.of("name", serviceName));
        serviceRegistrations
                .add(bundleContext.registerService(InboundSoapEndPointProvider.class, provider, properties));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSAPMeterReadingDocumentReason(SAPMeterReadingDocumentReason sapMeterReadingDocumentReason) {
        METER_READING_REASONS.add(sapMeterReadingDocumentReason);
    }

    public void removeSAPMeterReadingDocumentReason(SAPMeterReadingDocumentReason sapMeterReadingDocumentReason) {
        METER_READING_REASONS.remove(sapMeterReadingDocumentReason);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStatusChangeRequestCreateConfirmation(StatusChangeRequestCreateConfirmation statusChangeRequestCreateConfirmation) {
        STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.add(statusChangeRequestCreateConfirmation);
    }

    public void removeStatusChangeRequestCreateConfirmation(StatusChangeRequestCreateConfirmation statusChangeRequestCreateConfirmation) {
        STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.remove(statusChangeRequestCreateConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStatusChangeRequestBulkCreateConfirmation(StatusChangeRequestBulkCreateConfirmation statusChangeRequestBulkCreateConfirmation) {
        STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS.add(statusChangeRequestBulkCreateConfirmation);
    }

    public void removeStatusChangeRequestBulkCreateConfirmation(StatusChangeRequestBulkCreateConfirmation statusChangeRequestBulkCreateConfirmation) {
        STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS.remove(statusChangeRequestBulkCreateConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStatusChangeRequestCancellationConfirmation(StatusChangeRequestCancellationConfirmation statusChangeRequestCancellationConfirmation) {
        STATUS_CHANGE_REQUEST_CANCELLATION_CONFIRMATIONS.add(statusChangeRequestCancellationConfirmation);
    }

    public void removeStatusChangeRequestCancellationConfirmation(StatusChangeRequestCancellationConfirmation statusChangeRequestCancellationConfirmation) {
        STATUS_CHANGE_REQUEST_CANCELLATION_CONFIRMATIONS.remove(statusChangeRequestCancellationConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentRequestConfirmation(MeterReadingDocumentRequestConfirmation requestConfirmation) {
        METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS.add(requestConfirmation);
    }

    public void removeMeterReadingDocumentRequestConfirmation(MeterReadingDocumentRequestConfirmation requestConfirmation) {
        METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS.remove(requestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentBulkRequestConfirmation(MeterReadingDocumentBulkRequestConfirmation bulkRequestConfirmation) {
        METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS.add(bulkRequestConfirmation);
    }

    public void removeMeterReadingDocumentBulkRequestConfirmation(MeterReadingDocumentBulkRequestConfirmation bulkRequestConfirmation) {
        METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS.remove(bulkRequestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentResult(MeterReadingDocumentResult result) {
        METER_READING_DOCUMENT_RESULTS.add(result);
    }

    public void removeMeterReadingDocumentResult(MeterReadingDocumentResult result) {
        METER_READING_DOCUMENT_RESULTS.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentBulkResult(MeterReadingDocumentBulkResult bulkResult) {
        METER_READING_DOCUMENT_BULK_RESULTS.add(bulkResult);
    }

    public void removeMeterReadingDocumentBulkResult(MeterReadingDocumentBulkResult bulkResult) {
        METER_READING_DOCUMENT_BULK_RESULTS.remove(bulkResult);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceCreateConfirmation(UtilitiesDeviceCreateConfirmation result) {
        UTILITIES_DEVICE_CREATE_CONFIRMATION.add(result);
    }

    public void removeUtilitiesDeviceCreateConfirmation(UtilitiesDeviceCreateConfirmation result) {
        UTILITIES_DEVICE_CREATE_CONFIRMATION.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceBulkCreateConfirmation(UtilitiesDeviceBulkCreateConfirmation result) {
        UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION.add(result);
    }

    public void removeUtilitiesDeviceBulkCreateConfirmation(UtilitiesDeviceBulkCreateConfirmation result) {
        UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceRegisterCreateConfirmation(UtilitiesDeviceRegisterCreateConfirmation result) {
        UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION.add(result);
    }

    public void removeUtilitiesDeviceRegisterCreateConfirmation(UtilitiesDeviceRegisterCreateConfirmation result) {
        UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION.remove(result);
    }


    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceRegisterBulkCreateConfirmation(UtilitiesDeviceRegisterBulkCreateConfirmation result) {
        UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION.add(result);
    }

    public void removeUtilitiesDeviceRegisterBulkCreateConfirmation(UtilitiesDeviceRegisterBulkCreateConfirmation result) {
        UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceRegisteredNotification(UtilitiesDeviceRegisteredNotification result) {
        UTILITIES_DEVICE_REGISTERED_NOTIFICATION.add(result);
    }

    public void removeUtilitiesDeviceRegisteredNotification(UtilitiesDeviceRegisteredNotification result) {
        UTILITIES_DEVICE_REGISTERED_NOTIFICATION.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesDeviceRegisteredBulkNotification(UtilitiesDeviceRegisteredBulkNotification result) {
        UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION.add(result);
    }

    public void removeUtilitiesDeviceRegisteredBulkNotification(UtilitiesDeviceRegisteredBulkNotification result) {
        UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION.remove(result);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeasurementTaskAssignmentChangeRequestConfirmation(MeasurementTaskAssignmentChangeConfirmation measurementTaskAssignmentChangeRequestConfirmation) {
        MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS.add(measurementTaskAssignmentChangeRequestConfirmation);
    }

    public void removeMeasurementTaskAssignmentChangeRequestConfirmation(MeasurementTaskAssignmentChangeConfirmation measurementTaskAssignmentChangeRequestConfirmation) {
        MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS.remove(measurementTaskAssignmentChangeRequestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentCancellationConfirmation(MeterReadingDocumentCancellationConfirmation meterReadingDocumentCancellationConfirmation) {
        METER_READING_DOCUMENT_CANCELLATION_CONFIRMATION.add(meterReadingDocumentCancellationConfirmation);
    }

    public void removeMeterReadingDocumentCancellationConfirmation(MeterReadingDocumentCancellationConfirmation meterReadingDocumentCancellationConfirmation) {
        METER_READING_DOCUMENT_CANCELLATION_CONFIRMATION.remove(meterReadingDocumentCancellationConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingDocumentBulkCancellationConfirmation(MeterReadingDocumentBulkCancellationConfirmation meterReadingDocumentBulkCancellationConfirmation) {
        METER_READING_DOCUMENT_BULK_CANCELLATION_CONFIRMATION.add(meterReadingDocumentBulkCancellationConfirmation);
    }

    public void removeMeterReadingDocumentBulkCancellationConfirmation(MeterReadingDocumentBulkCancellationConfirmation meterReadingDocumentBulkCancellationConfirmation) {
        METER_READING_DOCUMENT_BULK_CANCELLATION_CONFIRMATION.remove(meterReadingDocumentBulkCancellationConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterRegisterChangeConfirmation(MeterRegisterChangeConfirmation requestConfirmation) {
        METER_REGISTER_CHANGE_CONFIRMATIONS.add(requestConfirmation);
    }

    public void removeMeterRegisterChangeConfirmation(MeterRegisterChangeConfirmation requestConfirmation) {
        METER_REGISTER_CHANGE_CONFIRMATIONS.remove(requestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingResultCreateConfirmation(MeterReadingResultCreateConfirmation requestConfirmation) {
        METER_READING_RESULT_CREATE_CONFIRMATIONS.add(requestConfirmation);
    }

    public void removeMeterReadingResultCreateConfirmation(MeterReadingResultCreateConfirmation requestConfirmation) {
        METER_READING_RESULT_CREATE_CONFIRMATIONS.remove(requestConfirmation);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterRegisterBulkChangeConfirmation(MeterRegisterBulkChangeConfirmation requestConfirmation) {
        METER_REGISTER_BULK_CHANGE_CONFIRMATIONS.add(requestConfirmation);
    }

    public void removeMeterRegisterBulkChangeConfirmation(MeterRegisterBulkChangeConfirmation requestConfirmation) {
        METER_REGISTER_BULK_CHANGE_CONFIRMATIONS.remove(requestConfirmation);
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer())
                .join(nlsService.getThesaurus(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "SAP");
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void seMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setMeasurementTaskAssignmentChangeProcessor(MeasurementTaskAssignmentChangeProcessor measurementTaskAssignmentChangeProcessor) {
        this.measurementTaskAssignmentChangeProcessor = measurementTaskAssignmentChangeProcessor;
    }

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(PropertyTranslationKeys.values()));
        keys.addAll(Arrays.asList(Privileges.values()));
        return keys;
    }

    // for test purposes
    DataModel getDataModel() {
        return dataModel;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    private String getPropertyValue(BundleContext context, String propertyName) {
        String value = context.getProperty(propertyName);
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            LOGGER.log(Level.WARNING, MessageSeeds.PROPERTY_IS_NOT_SET.getDefaultFormat(), propertyName);
        }
        return value;
    }

    private void failOngoingExportTaskServiceCalls() {
        List<ServiceCall> serviceCalls = dataExportService.getDataExportServiceCallType().findServiceCalls(EnumSet.of(DefaultState.ONGOING));
        serviceCalls.stream()
                .forEach(sC -> dataExportService.getDataExportServiceCallType()
                        .tryFailingServiceCall(sC, MessageSeeds.DATA_EXPORT_TASK_WAS_INTERRUPTED.getDefaultFormat()));
    }
}