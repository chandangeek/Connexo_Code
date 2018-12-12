/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommandStep1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.add-command-step1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.store.DeviceGroupsNoPaging'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'mdc-add-command-step1-error',
                width: 550,
                hidden: true
            },
            {
                xtype: 'combobox',
                name: 'deviceGroup',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                itemId: 'mdc-add-command-step1-deviceGroup-combo',
                emptyText: Uni.I18n.translate('general.selectADeviceGroup', 'MDC', 'Select a device group...'),
                store: 'Mdc.store.DeviceGroupsNoPaging',
                required: true,
                editable: false,
                displayField: 'name',
                valueField: 'id',
                allowBlank: false,
                labelWidth: 200,
                width: 550,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                required: true,
                itemId: 'mdc-add-command-step1-noDeviceGroup-msg',
                hidden: true,
                labelWidth: 200,
                width: 550,
                renderer: function(value){
                    return '<div style=" color:#FF0000; margin:6px 10px 6px 0px; ">' + Uni.I18n.translate('general.noDeviceGroup', 'MDC', 'No device group defined yet.') + '</div>';
                }
            }
        ];
        me.callParent(arguments);
    }
});