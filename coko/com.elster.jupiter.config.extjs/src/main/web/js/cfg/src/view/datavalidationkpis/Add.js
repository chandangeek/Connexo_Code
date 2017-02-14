/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.datavalidationkpis.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-data-validation-kpi-add',
    content: [
        {
            xtype: 'form',
            itemId: 'frm-data-validation-kpi-add',
            title: ' ',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true,
                    width: 600
                },
                {
                    xtype: 'combobox',
                    name: 'deviceGroup',
                    emptyText: Uni.I18n.translate('datavalidationkpis.selectDeviceGroup', 'CFG', 'Select a device group...'),
                    itemId: 'cmb-device-group',
                    fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
                    store: 'Cfg.store.DataValidationGroups',
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
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'add',
                            itemId: 'create-add-button'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            action: 'cancel-add-button'
                        }
                    ]
                }
            ]
        }
    ]

});
