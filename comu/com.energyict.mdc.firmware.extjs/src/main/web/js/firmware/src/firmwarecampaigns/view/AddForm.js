/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.AddForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.form.field.TimeInHoursAndMinutes',
        'Fwc.firmwarecampaigns.view.DynamicRadioGroup',
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.firmwarecampaigns.store.FirmwareTypes',
        'Fwc.firmwarecampaigns.model.FirmwareManagementOption',
        'Fwc.firmwarecampaigns.store.DaysWeeksMonths',
        'Fwc.firmwarecampaigns.store.ComTasksForValidate',
        'Fwc.firmwarecampaigns.store.ComTasksForSendCalendar'

    ],
    alias: 'widget.firmware-campaigns-add-form',
    returnLink: null,
    action: null,
    skipLoadingIndication: false,
    campaignRecordBeingEdited: null,

    defaults: {
        labelWidth: 260,
        width: 600,
        msgTarget: 'under'
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 10 0',
                hidden: true
            },
            {
                xtype: 'textfield',
                itemId: 'firmware-campaign-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                required: true,
                allowBlank: false
            },
            {
                xtype: 'combobox',
                itemId: 'firmware-campaign-device-type',
                name: 'deviceType',
                fieldLabel: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                required: true,
                allowBlank: false,
                store: 'Fwc.store.DeviceTypes',
                forceSelection: true,
                queryMode: 'local',
                displayField: 'localizedValue',
                valueField: 'id',
                listeners: {
                    change: {
                        fn: Ext.bind(me.onDeviceTypeChange, me)
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'FWC', 'Device group'),
                itemId: 'firmware-campaign-device-group-field-container',
                required: true,
                allowBlank: false,
                layout: 'hbox',
                width: 650,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'firmware-campaign-device-group',
                        name: 'deviceGroup',
                        store: 'Fwc.store.DeviceGroups',
                        forceSelection: true,
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'name',
                        width: 325
                    },
                    {
                        xtype: 'displayfield',
                        margin: '0 0 0 10',
                        htmlEncode: false,
                        value: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;" data-qwidth="400" data-qtitle="' +
                            Uni.I18n.translate('firmware.campaigns.deviceGroupTooltip.header', 'FWC', 'Help - About device group')+'" data-qtip="'
                            + Ext.htmlEncode(Uni.I18n.translate('firmware.campaigns.deviceGroupTooltip.title', 'FWC', 'Only devices that meet the following criteria will be included in the firmware campaign')
                                + ':<ul class="ul#uni-panel-no-items-found"><li>'
                                + Uni.I18n.translate('firmware.campaigns.deviceGroupTooltip.reason1', 'FWC', 'Devices with as device type the selected device type')
                                + '</li><li>'
                                + Uni.I18n.translate('firmware.campaigns.deviceGroupTooltip.reason2', 'FWC', 'Devices that are member of the selected device group at the moment the firmware campaign is added'))
                            + '</li></ul>"></span>'
                    }
                ]
            },
            {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: Uni.I18n.translate('general.timeBoundaryStart', 'FWC', 'Time boundary start'),
                name: 'timeBoundaryStartTimeInSec',
                itemId: 'timeBoundaryStart',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: Uni.I18n.translate('general.timeBoundaryEnd', 'FWC', 'Time boundary end'),
                name: 'timeBoundaryEndTimeInSec',
                itemId: 'timeBoundaryEnd',
                required: true,
                allowBlank: false,

            },
            {
                xtype: 'dynamic-radiogroup',
                itemId: 'firmware-management-option',
                name: 'managementOption',
                fieldLabel: Uni.I18n.translate('firmware.campaigns.firmwareManagementOption', 'FWC', 'Firmware management option'),
                required: true,
                allowBlank: false,
                hidden: true,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onManagementOptionChange, me)
                    }
                },
                width: 1000
            },
            {
                xtype: 'dynamic-radiogroup',
                itemId: 'firmware-type',
                name: 'firmwareType',
                fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
                required: true,
                allowBlank: false,
                hidden: true,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onFirmwareTypeChange, me)
                    }
                }
            },
            {
                itemId: 'period-values',
                xtype: 'fieldcontainer',
                name: 'recurrenceValues',
                fieldLabel: Uni.I18n.translate('general.firmwareTimeout', 'FWC', 'Timeout before validation'),
                margin: '30 0 10 0',
                hidden: true,
                required: true,
                layout: 'hbox',
                items: [
                    {
                        itemId: 'period-number',
                        xtype: 'numberfield',
                        name: 'recurrenceNumber',
                        allowDecimals: false,
                        minValue: 1,
                        value: 1,
                        width: 65,
                        margin: '0 10 0 0'
                    },
                    {
                        itemId: 'period-combo',
                        xtype: 'combobox',
                        store: Ext.create('Fwc.firmwarecampaigns.store.DaysWeeksMonths'),
                        displayField: 'displayValue',
                        valueField: 'name',
                        queryMode: 'local',
                        width: 100
                    }
                ]
            },
            {
                xtype: 'property-form',
                itemId: 'property-form',
                defaults: {
                    width: 325,
                    labelWidth: 260,
                    resetButtonHidden: true
                },
                width: 1000
            },
            {
                xtype: 'combobox',
                itemId: 'fwc-campaign-allowed-comtask',
                name: 'calendarUploadComTask',
                store: 'Fwc.firmwarecampaigns.store.ComTasksForSendCalendar',
                fieldLabel: Uni.I18n.translate(
                    'general.calendarUploadComTask',
                    'FWC',
                    'Send calendar communication task'
                ),
                required: true,
                allowBlank: false,
                forceSelection: true,
                emptyText: Uni.I18n.translate(
                    'general.calendarUploadComTask.empty',
                    'FWC',
                    'Select communication task ...'
                ),
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                margin: '30 0 10 0',
                hidden: true,
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                itemId: 'fwc-campaign-send-connection-strategy-container',
                hidden: true,
                fieldLabel: Uni.I18n.translate(
                    'general.connectionMethodStrategy',
                    'FWC',
                    'Connection method strategy'
                ),
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'fwc-campaign-send-connection-strategy',
                        name: 'calendarUploadConnectionStrategy',
                        store: 'Fwc.firmwarecampaigns.store.ConnectionStrategy',
                        queryMode: 'local',
                        displayField: 'name',
                        margin: '0 10 0 0',
                        valueField: 'id'
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'fwc-campaign-send-connection-strategy-reset',
                        handler: function() {
                            this.down('[name=calendarUploadConnectionStrategy]').reset();
                        },
                        scope: me,
                        margin: '0 0 0 10',
                        hidden: false
                    }
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'fwc-campaign-validation-comtask',
                name: 'validationComTask',
                store: 'Fwc.firmwarecampaigns.store.ComTasksForValidate',
                fieldLabel: Uni.I18n.translate(
                    'general.validationComTask',
                    'FWC',
                    'Validation communication task'
                ),
                hidden: true,
                disabled: true,
                required: true,
                allowBlank: false,
                forceSelection: true,
                emptyText: Uni.I18n.translate(
                    'general.validationComTask.empty',
                    'FWC',
                    'Select communication task ...'
                ),
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id'
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                itemId: 'fwc-campaign-validation-strategy-container',
                hidden: true,
                disabled: true,
                fieldLabel: Uni.I18n.translate(
                    'general.validationMethodStrategy',
                    'FWC',
                    'Validation method strategy'
                ),
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'fwc-campaign-validation-connection-strategy',
                        name: 'validationConnectionStrategy',
                        store: 'Fwc.firmwarecampaigns.store.ConnectionStrategy',
                        queryMode: 'local',
                        displayField: 'name',
                        margin: '0 10 0 0',
                        valueField: 'id'
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'fwc-campaign-validation-connection-strategy-reset',
                        handler: function() {
                            this.down('[name=validationConnectionStrategy]').reset();
                        },
                        scope: me,
                        margin: '0 0 0 10',
                        hidden: false
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-add-firmware-campaign',
                        text: Uni.I18n.translate('general.add', 'FWC', 'Add'),
                        ui: 'action',
                        action: me.action
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-cancel-add-firmware-campaign',
                        text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                        ui: 'link',
                        action: 'cancel',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onDeviceTypeChange: function (combo, newValue) {
        var me = this,
            counter = 2,
            onFieldsUpdate = function () {
                counter--;
                if (!counter) {
                    if (!me.skipLoadingIndication) {
                        me.setLoading(false);
                    } else {
                        me.fireEvent('fwc-deviceTypeChanged');
                    }
                }
            };

        if (combo.findRecordByValue(newValue)) {
            me.down('#property-form').loadRecord(Ext.create('Fwc.firmwarecampaigns.model.FirmwareManagementOption'));
            if (!me.skipLoadingIndication) {
                me.setLoading();
            }
            Ext.ModelManager.getModel('Fwc.firmwarecampaigns.model.FirmwareManagementOption').getProxy().setUrl(newValue);
            me.updateFirmwareType(newValue, onFieldsUpdate);
            me.updateManagementOptions(newValue, onFieldsUpdate, combo.isDisabled());
            me.updateComTasksComponents(newValue);
        }
    },

    updateComTasksComponents: function(deviceTypeId){
        var me = this;
        var sendComtaskField = me.down("[name=calendarUploadComTask]");
        me.down('#fwc-campaign-send-connection-strategy-container').show();
        sendComtaskField.show();
        sendComtaskField.getStore().getProxy().setUrl(deviceTypeId);
        sendComtaskField.getStore().load();
        var validationComTask = me.down("[name=validationComTask]");
        validationComTask.getStore().getProxy().setUrl(deviceTypeId);
        validationComTask.getStore().load();
    },

    updateFirmwareType: function (deviceTypeId, callback) {
        var me = this;

        Ext.getStore('Fwc.firmwarecampaigns.store.FirmwareTypes').load({
            params: {
                deviceType: deviceTypeId
            },
            callback: function (records) {
                callback();
                me.down('#firmware-type').showOptions(records, {
                    isRecord: true,
                    conditionCheck: 'meter',
                    showOnlyLabelForSingleItem: true
                });
            }
        })
    },

    updateManagementOptions: function (deviceTypeId, callback, deviceTypeComboDisabled) {
        var me = this,
            firmwareManagementOptions = Ext.ModelManager.getModel('Fwc.model.FirmwareManagementOptions');

        firmwareManagementOptions.getProxy().setUrl(deviceTypeId);
        firmwareManagementOptions.getProxy().extraParams = {};
        firmwareManagementOptions.load(1, {
            success: function (record) {
                me.down('#firmware-management-option').showOptions(record.get('allowedOptions'), {
                    showDescription: true,
                    showOnlyLabelForSingleItem: true,
                    disabled: deviceTypeComboDisabled
                });
            },
            callback: callback
        });
    },

    onManagementOptionChange: function (radiogroup, newValue) {
        var me = this,
            firmwareManagementOption = Ext.ModelManager.getModel('Fwc.firmwarecampaigns.model.FirmwareManagementOption'),
            recurrenceTypeCombo = me.down('#period-combo'),
            periodValues = me.down('#period-values');

        if (newValue && newValue.managementOption) {
            if (!me.skipLoadingIndication) {
                me.setLoading();
            }
            if (newValue.managementOption === "activate" || newValue.managementOption === "activateOnDate"){
                me.down('[name=validationComTask]').show();

                me.down('#fwc-campaign-validation-strategy-container').show();
                if (!me.campaignRecordBeingEdited) {
                    me.down('[name=validationComTask]').setDisabled(false);
                    me.down('#fwc-campaign-validation-strategy-container').setDisabled(false);
                }
            }else{
                me.down('[name=validationComTask]').hide();
                me.down('[name=validationComTask]').setDisabled(true);
                me.down('#fwc-campaign-validation-strategy-container').hide();
                me.down('#fwc-campaign-validation-strategy-container').setDisabled(true);
            }

            firmwareManagementOption.load(newValue.managementOption, {
                success: function (record) {
                    me.down('#property-form').loadRecord(record);
                    periodValues.show();
                    if(!recurrenceTypeCombo.getValue()){
                        recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(1));
                    }
                },
                callback: function () {
                    if (!me.skipLoadingIndication) {
                        me.setLoading(false);
                    } else {
                        me.fireEvent('fwc-propertiesInitialized');
                    }
                }
            });
        }
    },

    onFirmwareTypeChange: function (radiogroup, newValue) {
        var me = this,
            firmwareManagementOption = Ext.ModelManager.getModel('Fwc.firmwarecampaigns.model.FirmwareManagementOption'),
            option = me.down('#firmware-management-option').getValue();

        firmwareManagementOption.getProxy().setExtraParam('firmwareType', newValue.firmwareType);
        me.onManagementOptionChange(null, option);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);
        me.down('property-form').loadRecord(record);
    },

    updateRecord: function () {
        var me = this,
            propertyForm = me.down('property-form');

        me.callParent(arguments);

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            var properties = propertyForm.getRecord().properties();
            me.getRecord().propertiesStore = properties;
        }
    },

    loadRecordForEdit: function(campaignRecord) {
        var me = this,
            taskRunner = new Ext.util.TaskRunner(),
            deviceTypeCombo = me.down('#firmware-campaign-device-type'),
            firmwareTypeRadioGroup = me.down('#firmware-type'),
            deviceGroupComboContainer = me.down('#firmware-campaign-device-group-field-container'),
            deviceGroupCombo = me.down('#firmware-campaign-device-group'),
            managementOptionRadioGroup = me.down('#firmware-management-option'),
            periodCombo = me.down('#period-combo'),
            periodNumber = me.down('#period-number'),
            periodValues = me.down('#period-values'),
            deviceTypeId = campaignRecord.get('deviceType').id,
            hideDeviceGroupComboAndSetDeviceType = function() {
                deviceGroupCombo.allowBlank = true;
                deviceGroupComboContainer.allowBlank = true;
                deviceGroupComboContainer.hide();
                deviceTypeCombo.setDisabled(true);
                deviceTypeCombo.setValue(deviceTypeId);
            },
            setOptions = function() {
                firmwareTypeRadioGroup.setValue({
                    firmwareType : campaignRecord.get('firmwareType').id
                });
                firmwareTypeRadioGroup.setDisabled(true);
                managementOptionRadioGroup.setValue({
                    managementOption : campaignRecord.get('managementOption').id
                });
                managementOptionRadioGroup.setDisabled(true);
                periodValues.setDisabled(true);
                var validationTimeout = campaignRecord.get('validationTimeout');
                if(validationTimeout){
                    periodCombo.setRawValue(periodCombo
                        .getStore().findRecord('name',validationTimeout.timeUnit).get('displayValue'));
                    periodNumber.setValue(validationTimeout.count);
                }
                var calendarUploadComTask = campaignRecord.get('calendarUploadComTask');
                var validationComTask = campaignRecord.get('validationComTask');
                var calendarUploadConnectionStrategy = campaignRecord.get('calendarUploadConnectionStrategy');
                var validationConnectionStrategy = campaignRecord.get('validationConnectionStrategy');

                me.getForm().setValues({
                    calendarUploadComTask: calendarUploadComTask && calendarUploadComTask.id,
                    validationComTask: validationComTask && validationComTask.id,
                    calendarUploadConnectionStrategy: calendarUploadConnectionStrategy && calendarUploadConnectionStrategy.id,
                    validationConnectionStrategy: validationConnectionStrategy && validationConnectionStrategy.id
                });
                me.down('#fwc-campaign-allowed-comtask').setDisabled(true);
                me.down('#fwc-campaign-send-connection-strategy-container').setDisabled(true);

            },
            setProperties = function() {
                me.down('#property-form').setPropertiesAndDisable(campaignRecord.propertiesStore.getRange());
                me.setLoading(false);
                me.skipLoadingIndication = false;
            },
            setPropertiesTask = taskRunner.newTask({
                run: setProperties,
                scope: me,
                fireOnStart: false,
                interval: 20,
                repeat: 1
            });

        me.campaignRecordBeingEdited = campaignRecord;
        me.skipLoadingIndication = true;
        me.on('fwc-deviceTypeChanged', setOptions);
        me.on('fwc-propertiesInitialized', function() {
            setPropertiesTask.start();
        });
        me.loadRecord(campaignRecord);
        hideDeviceGroupComboAndSetDeviceType();
    }
});