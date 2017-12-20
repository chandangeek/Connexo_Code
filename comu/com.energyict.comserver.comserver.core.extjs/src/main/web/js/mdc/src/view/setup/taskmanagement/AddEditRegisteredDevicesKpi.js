/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.taskmanagement.AddEditRegisteredDevicesKpi', {
    extend: 'Ext.form.Panel',
    alias: 'widget.registered-devices-kpi-addedit-tgm',
    requires: [
        'Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies',
        'Mdc.registereddevices.store.AvailableDeviceGroups',
        'Mdc.registereddevices.store.AllTasks'
    ],
    defaults: {
        labelWidth: 250,
        validateOnChange: false,
        validateOnBlur: false
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'combobox',
                name: 'deviceGroup',
                emptyText: Uni.I18n.translate('general.selectADeviceGroup', 'MDC', 'Select a device group...'),
                itemId: 'mdc-registered-devices-kpi-add-device-group-combo',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                store: 'Mdc.registereddevices.store.AvailableDeviceGroups',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                width: 600,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'displayfield',
                itemId: 'mdc-registered-devices-kpi-add-device-group-displayField',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                required: true,
                htmlEncode: false,
                hidden: true
            },
            {
                xtype: 'combobox',
                name: 'frequency',
                emptyText: Uni.I18n.translate('datacollectionkpis.selectCalculationFrequency', 'MDC', 'Select a calculation frequency...'),
                itemId: 'mdc-registered-devices-kpi-add-frequency-combo',
                fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                store: 'Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                width: 600
            },
            {
                xtype: 'numberfield',
                name: 'target',
                required: true,
                allowDecimals: false,
                fieldLabel: Uni.I18n.translate('general.target', 'MDC', 'Target'),
                itemId: 'mdc-registered-devices-kpi-add-target',
                // margin: '0 5 0 10',
                width: 335,
                value: 95,
                minValue: 0,
                maxValue: 100,
                listeners: {
                    blur: function (field) {
                        if (field.getValue() < 0) {
                            field.setValue(0);
                        } else if (field.getValue() > 100) {
                            field.setValue(100);
                        }
                    }
                }
            },
            {
                xtype: 'combobox',
                itemId: 'followedBy-combo',
                fieldLabel: Uni.I18n.translate('general.followedBy', 'MDC', 'Followed by'),
                name: 'nextRecurrentTasks',
                width: 600,
                multiSelect: true,
                queryMode: 'local',
                store: 'Mdc.registereddevices.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('datacollectionkpis.taskSelectorPrompt', 'MDC', 'Select a task ...'),
                displayField: 'displayName',
                valueField: 'id'
            }
        ];
        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            deviceGroup = record.get('deviceGroup'),
            frequency = record.get('frequency'),
            target = record.get('target'),
            deviceGroupCombo = me.down('[name=deviceGroup]'),
            nextRecurrentTasks = record.get('nextRecurrentTasks');

        me.getForm().loadRecord(record);
        if (deviceGroup) {
            deviceGroupCombo.setRawValue(deviceGroup.name);
        }
        if (frequency) {
            me.down('[name=frequency]').setValue(frequency.every.count + frequency.every.timeUnit);
        }

        if (nextRecurrentTasks) {
            var selectedTasks = [];
            Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                selectedTasks.push(nextRecurrentTask.id);
            });
            me.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
        }
    }

});

