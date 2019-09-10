/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.property.controller.Registry
 * @singleton
 *
 * Properties registry.
 * Use it to add custom properties to the property map.
 *
 * Example:
 *
 * // add properties by one
 * var registry = Uni.property.controller.Registry;
 * registry.addProperty('CUSTOM_PROPERTY', 'App.view.property.Custom');
 *IssuesAndAlarmsLayerBuilder.java:
 * // or provide a config
 * registry.addProperties({
 *      'CUSTOM_1': 'App.view.property.Custom1',
 *      'CUSTOM_2': 'App.view.property.Custom2'
 * })
 *
 */
Ext.define('Uni.property.controller.Registry', {
    extend: 'Ext.app.Controller',
    singleton: true,
    requires: [
        'Uni.property.view.property.Text',
        'Uni.property.view.property.Combobox',
        'Uni.property.view.property.DeviceGroupCombobox',
        'Uni.property.view.property.Textarea',
        'Uni.property.view.property.Password',
        'Uni.property.view.property.Hexstring',
        'Uni.property.view.property.Boolean',
        'Uni.property.view.property.Number',
        'Uni.property.view.property.DecimalNumber',
        'Uni.property.view.property.NullableBoolean',
        'Uni.property.view.property.Date',
        'Uni.property.view.property.DateTime',
        'Uni.property.view.property.Period',
        'Uni.property.view.property.Time',
        'Uni.property.view.property.CodeTable',
        'Uni.property.view.property.Reference',
        'Uni.property.view.property.Multiselect',
        'Uni.property.view.property.RelativePeriod',
        'Uni.property.view.property.AdvanceReadingsSettings',
        'Uni.property.view.property.AdvanceReadingsSettingsWithoutNone',
        'Uni.property.view.property.deviceconfigurations.DeviceConfigurations',
        'Uni.property.view.property.ObisCode',
        'Uni.property.view.property.ObisCodeCombo',
        'Uni.property.view.property.Quantity',
        'Uni.property.view.property.ReadingQualities',
        'Uni.property.view.property.Assign',
        'Uni.property.view.property.EndDeviceEventTypeList',
        'Uni.property.view.property.EventTypeWindow',
        'Uni.property.view.property.LifeCycleStatusInDeviceType',
        'Uni.property.view.property.RaisedEventProps',
        'Uni.property.view.property.RecurrenceSelectionProps',
        'Uni.property.view.property.RadioGroup',
        'Uni.property.view.property.EventTime',
        'Uni.property.view.property.StartAlarmProcess',
        'Uni.property.view.property.CalendarWithEventCode',
        'Uni.property.view.property.MaximumAbsoluteDifference',
        'Uni.property.view.property.NoneOrBigDecimal',
        'Uni.property.view.property.ReadingType',
        'Uni.property.view.property.NoneOrTimeDuration',
        'Uni.property.view.property.UsagePoint',
        'Uni.property.view.property.metrologyconfigurations.MetrologyConfigurations',
        'Uni.property.view.property.devicelifecycletransitions.DeviceLifecycleTransitions',
        'Uni.property.view.property.Recurrence',
        'Uni.property.view.property.Task',
        'Uni.property.view.property.ServiceCallIssueType',
        'Uni.property.view.property.ServiceCallIssueState',
        'Uni.property.view.property.CustomEventTypeReference',
        'Uni.property.view.property.comtasks.ComTasks',
        'Uni.property.view.property.webservices.WebServiceIssue',
    ],

    /**
     * Default properties registered
     */
    propertiesMap: {
        ADVANCEREADINGSSETTINGS: 'Uni.property.view.property.AdvanceReadingsSettings',
        ADVANCEREADINGSSETTINGSWITHOUTNONE: 'Uni.property.view.property.AdvanceReadingsSettingsWithoutNone',
        ASSIGN: 'Uni.property.view.property.Assign',
        BOOLEAN: 'Uni.property.view.property.Boolean',
        BPM_PROCESS: 'Uni.property.view.property.StartAlarmProcess',
        CLOCK: 'Uni.property.view.property.DateTime',
        CODETABLE: 'Uni.property.view.property.CodeTable',
        COMBOBOX: 'Uni.property.view.property.Combobox',
        DATE: 'Uni.property.view.property.Date',
        DEVICECONFIGURATIONLIST: 'Uni.property.view.property.deviceconfigurations.DeviceConfigurations',
        DEVICEGROUPTYPE: 'Uni.property.view.property.DeviceGroupCombobox',
        METROLOGYCONFIGURATIONLIST: 'Uni.property.view.property.metrologyconfigurations.MetrologyConfigurations',
        LIFECYCLETRANSITION:'Uni.property.view.property.devicelifecycletransitions.DeviceLifecycleTransitions',
        DURATION: 'Uni.property.view.property.Period',
        EAN13: 'Uni.property.view.property.Text',
        EAN18: 'Uni.property.view.property.Text',
        ENCRYPTED_STRING: 'Uni.property.view.property.Password',
        ENDDEVICEEVENTTYPE: 'Uni.property.view.property.EndDeviceEventTypeList',
        FIRMWAREVERSION: 'Uni.property.view.property.Reference',
        HEXSTRING: 'Uni.property.view.property.Hexstring',
        IDWITHNAME: 'Uni.property.view.property.Reference',
        LIFECYCLESTATUSINDEVICETYPE: 'Uni.property.view.property.LifeCycleStatusInDeviceType',
        LISTREADINGQUALITY: 'Uni.property.view.property.ReadingQualities',
        LISTVALUE: 'Uni.property.view.property.Multiselect',
        LOADPROFILE: 'Uni.property.view.property.Reference',
        LOADPROFILETYPE: 'Uni.property.view.property.Reference',
        LOGBOOK: 'Uni.property.view.property.Reference',
        LONG: 'Uni.property.view.property.Number',
        NONE_OR_TIMEDURATION: 'Uni.property.view.property.NoneOrTimeDuration',
        NULLABLE_BOOLEAN: 'Uni.property.view.property.NullableBoolean',
        NUMBER: 'Uni.property.view.property.DecimalNumber',
        OBISCODE: 'Uni.property.view.property.ObisCode',
        PASSWORD: 'Uni.property.view.property.Password',
        QUANTITY: 'Uni.property.view.property.Quantity',
        RAISEEVENTPROPS: 'Uni.property.view.property.RaisedEventProps',
        RECURRENCE_SELECTION_PROPS: 'Uni.property.view.property.RecurrenceSelectionProps',
        RADIO_GROUP: 'Uni.property.view.property.RadioGroup',
        READINGTYPE: 'Uni.property.view.property.Reference',
        RECURRENCE: 'Uni.property.view.property.Recurrence',
        REFERENCE: 'Uni.property.view.property.Reference',
        REGISTER: 'Uni.property.view.property.Reference',
        RELATIVEPERIOD: 'Uni.property.view.property.RelativePeriod',
        RELATIVEPERIODWITHCOUNT: 'Uni.property.view.property.EventTime',
        SELECTIONGRID: 'Uni.property.view.property.SelectionGrid',
        TASK: 'Uni.property.view.property.Task',
        TEXT: 'Uni.property.view.property.Text',
        TEXTAREA: 'Uni.property.view.property.Textarea',
        TEMPORALAMOUNT: 'Uni.property.view.property.Period',
        TIMEDURATION: 'Uni.property.view.property.Period',
        TIMEOFDAY: 'Uni.property.view.property.Time',
        TIMESTAMP: 'Uni.property.view.property.DateTime',
        USAGE_POINT: 'Uni.property.view.property.UsagePoint',
        UNKNOWN: 'Uni.property.view.property.Text',
        CALENDARWITHEVENTCODE: 'Uni.property.view.property.CalendarWithEventCode',
        TWO_VALUES_DIFFERENCE: 'Uni.property.view.property.MaximumAbsoluteDifference',
        NONE_OR_BIGDECIMAL: 'Uni.property.view.property.NoneOrBigDecimal',
        IRREGULAR_READINGTYPE: 'Uni.property.view.property.ReadingType',
        REGULAR_READINGTYPE: 'Uni.property.view.property.ReadingType',
        ANY_READINGTYPE: 'Uni.property.view.property.ReadingType',
        INTEGER: 'Uni.property.view.property.Number',
        WEB_SERVICES_ENDPOINT: 'Uni.property.view.property.Reference',
        SERVICE_CALL: 'Uni.property.view.property.ServiceCallIssueType',
        SERVICE_CALL_STATE: 'Uni.property.view.property.ServiceCallIssueState',
        CUSTOM_EVENT_TYPE: 'Uni.property.view.property.CustomEventTypeReference',
        EXCLUDED_COM_TASKS: 'Uni.property.view.property.comtasks.ComTasks',
        ENDPOINT_CONFIGURATION_LIST: 'Uni.property.view.property.webservices.WebServiceIssue',
    },

// store must be registered on some ctrl (not in the responsibility of this class: move later?)
    stores: [
        'Uni.property.store.TimeUnits',
        'Uni.property.store.RelativePeriods',
        'Uni.property.store.PropertyReadingTypes',
        'Uni.property.store.PropertyDeviceConfigurations',
        'Uni.property.store.DeviceTypes',
        'Uni.property.store.DeviceDomains',
        'Uni.property.store.DeviceSubDomains',
        'Uni.property.store.DeviceEventOrActions',
        'Uni.property.store.RelativePeriodsWithCount',
        'Uni.property.store.ReadingTypes',
        'Uni.property.store.PropertyDeviceLifecycleTransition',
        'Uni.property.store.PropertyCommunicationTasks',
        'Uni.property.store.PropertyCommunicationTasksCurrentValue',
        'Uni.property.store.PropertyWebServices',
    ],

    /**
     * Register new property
     *
     * @param {string} key
     * @param {string} model
     */
    addProperty: function (key, model) {
        if (!Ext.isString(key)) {
            throw '!Ext.isString(key)'
        }

        if (!this.getProperty(key)) {
            this.propertiesMap[key] = model;
        }
    },

    /**
     * Register properties config
     *
     * @param {Object} properties
     */
    addProperties: function (properties) {
        Ext.apply(this.propertiesMap, properties)
    },

    /**
     * Retrieve property widget
     * @see Uni.property.view.property.Base
     *
     * @param {string} key
     * @returns {string|null}
     */
    getProperty: function (key) {
        return this.propertiesMap[key] || null;
    }
})
;
