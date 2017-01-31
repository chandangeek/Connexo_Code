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
        'Fwc.firmwarecampaigns.model.FirmwareManagementOption'
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
                required: true
            },
            {
                xtype: 'combobox',
                itemId: 'firmware-campaign-device-type',
                name: 'deviceType',
                fieldLabel: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                required: true,
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
                layout: 'hbox',
                width: 650,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'firmware-campaign-device-group',
                        name: 'deviceGroup',
                        store: 'Fwc.store.DeviceGroups',
                        forceSelection: true,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
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
                name: 'timeBoundaryStart',
                itemId: 'timeBoundaryStart',
                required: true
            },
            {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: Uni.I18n.translate('general.timeBoundaryEnd', 'FWC', 'Time boundary end'),
                name: 'timeBoundaryEnd',
                itemId: 'timeBoundaryEnd',
                required: true
            },
            {
                xtype: 'dynamic-radiogroup',
                itemId: 'firmware-management-option',
                name: 'managementOption',
                fieldLabel: Uni.I18n.translate('firmware.campaigns.firmwareManagementOption', 'FWC', 'Firmware management option'),
                required: true,
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
                hidden: true,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onFirmwareTypeChange, me)
                    }
                }
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
        }
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
            firmwareManagementOption = Ext.ModelManager.getModel('Fwc.firmwarecampaigns.model.FirmwareManagementOption');

        if (newValue && newValue.managementOption) {
            if (!me.skipLoadingIndication) {
                me.setLoading();
            }
            firmwareManagementOption.load(newValue.managementOption, {
                success: function (record) {
                    me.down('#property-form').loadRecord(record);
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
            me.getRecord().propertiesStore = propertyForm.getRecord().properties();
        }
    },

    loadRecordForEdit: function(campaignRecord) {
        var me = this,
            taskRunner = new Ext.util.TaskRunner(),
            deviceTypeCombo = me.down('#firmware-campaign-device-type'),
            firmwareTypeRadioGroup = me.down('#firmware-type'),
            deviceGroupComboContainer = me.down('#firmware-campaign-device-group-field-container'),
            managementOptionRadioGroup = me.down('#firmware-management-option'),
            deviceTypeId = campaignRecord.get('deviceType').id,
            hideDeviceGroupComboAndSetDeviceType = function() {
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