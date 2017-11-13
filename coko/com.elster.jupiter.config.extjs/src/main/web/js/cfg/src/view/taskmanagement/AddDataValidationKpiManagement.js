/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.taskmanagement.AddDataValidationKpiManagement', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-data-validation-kpi-add-mgm',
    requires: [
        'Uni.view.form.ComboBoxWithEmptyComponent'
    ],
    defaults: {
        labelWidth: 250
    },
    items: [
        {
            xtype: 'comboboxwithemptycomponent',
            fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
            itemId: 'cmb-device-group',
            config: {
                name: 'deviceGroup',
                emptyText: Uni.I18n.translate('datavalidationkpis.selectDeviceGroup', 'CFG', 'Select a device group...'),
                store: 'Cfg.store.DataValidationGroups',
                queryMode: 'local',
                displayField: 'name',
                noObjectsText: Uni.I18n.translate('general.noDeviceGroup', 'CFG', 'No device group defined yet'),
                valueField: 'id',
                required: true,
                allowBlank: false,
                editable: false,
                width: 600,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            }
        },
        {
            xtype: 'displayfield',
            itemId: 'device-group-field',
            fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
            required: true,
            htmlEncode: false,
            hidden: true
        },
        {
            xtype: 'combobox',
            name: 'frequency',
            emptyText: Uni.I18n.translate('datavalidationkpis.selectCalculationFrequency', 'CFG', 'Select a calculation frequency...'),
            itemId: 'cmb-frequency',
            fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
            store: 'Cfg.store.DataValidationKpiFrequency',
            queryMode: 'local',
            editable: false,
            displayField: 'name',
            valueField: 'id',
            allowBlank: false,
            required: true,
            width: 600
        }
    ]
});
