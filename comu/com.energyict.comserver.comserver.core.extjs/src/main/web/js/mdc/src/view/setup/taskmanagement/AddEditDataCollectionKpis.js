/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis', {
    extend: 'Ext.form.Panel',
    alias: 'widget.data-collection-kpi-addedit-tgm',

    requires: [
        'Mdc.view.setup.datacollectionkpis.KpiFieldContainer',
        'Mdc.store.AllTasks'
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
                name: 'collectionType',
                emptyText: Uni.I18n.translate('datacollectionkpis.selectCollectionType', 'MDC', 'Select a collection type...'),
                itemId: 'cmb-collectionType',
                fieldLabel: Uni.I18n.translate('datacollectionkpis.collectionType', 'MDC', 'Collection type'),
                store: 'Mdc.store.DataCollectionKpiType',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                width: 600
            },
            {
                xtype: 'combobox',
                name: 'deviceGroup',
                emptyText: Uni.I18n.translate('general.selectADeviceGroup', 'MDC', 'Select a device group...'),
                itemId: 'cmb-device-group',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                store: 'Mdc.store.AvailableDeviceGroups',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                disabled: true,
                width: 600,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'displayfield',
                itemId: 'devicegroupDisplayField',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                required: true,
                htmlEncode: false,
                hidden: true
            },
            {
                xtype: 'combobox',
                name: 'frequency',
                emptyText: Uni.I18n.translate('datacollectionkpis.selectCalculationFrequency', 'MDC', 'Select a calculation frequency...'),
                itemId: 'cmb-frequency',
                fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                store: 'Mdc.store.DataCollectionKpiFrequency',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                width: 600,
                afterSubTpl: '<div id="frequencySubTpl"/>'
            },
            {
                xtype: 'combobox',
                name: 'displayRange',
                emptyText: Uni.I18n.translate('datacollectionkpis.selectDisplayRange', 'MDC', 'Select a display range...'),
                itemId: 'cmb-display-range',
                fieldLabel: Uni.I18n.translate('datacollectionkpis.displayRange', 'MDC', 'Display range'),
                store: 'Mdc.store.DataCollectionKpiRange',
                queryMode: 'local',
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                required: true,
                disabled: true,
                lastQuery: '',
                width: 600,
                afterSubTpl: '<div id="displayRangeSubTpl"/>'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('datacollectionkpis.kpiTarget', 'MDC', 'KPI target'),
                labelWidth: 250,
                layout: 'hbox',
                required: true,
                items: [
                    {
                        xtype: 'numberfield',
                        name: 'target',
                        itemId: 'kpi-target',
                        margin: '0 5 0 0',
                        value: 0,
                        width: 70,
                        minValue: 0,
                        maxValue: 100,
                        listeners: {
                            blur: function (field) {
                                if (field.getValue() < 0 || field.getValue() > 100) {
                                    field.setValue(0);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'displayfield',
                        value: '%'
                    }]

            },
            {
                xtype: 'combobox',
                itemId: 'followedBy-combo',
                fieldLabel: Uni.I18n.translate('general.followedBy', 'MDC', 'Followed by'),
                name: 'nextRecurrentTasks',
                width: 600,
                multiSelect: true,
                queryMode: 'local',
                store: 'Mdc.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('estimationtasks.taskSelectorPrompt', 'MDC', 'Select a task ...'),
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
            displayRange = record.get('displayRange'),
            deviceGroupCombo = me.down('[name=deviceGroup]');

        me.getForm().loadRecord(record);
        if (deviceGroup) {
            deviceGroupCombo.setRawValue(deviceGroup.name);
        }
        if (frequency) {
            me.down('[name=frequency]').setValue(frequency.every.count + frequency.every.timeUnit);
        }
        if (displayRange) {
            me.down('[name=displayRange]').setValue(displayRange.count + displayRange.timeUnit);
        }

    }
});
