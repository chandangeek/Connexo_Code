/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.widget.CommandForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.key-renewal-edit-form',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.DeviceMessageCategories'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        xtype: 'combobox',
        labelWidth: 250,
        maxWidth: 600,
        allowBlank: false,
        validateOnBlur: false,
        required: true
    },


    initComponent: function () {

        this.items = [
            {
                fieldLabel: Uni.I18n.translate('deviceCommand.add.command', 'MDC', 'Key renewal command'),
                name: 'key renewal command',
                itemId: 'mdc-key-renewal-commands-combo',
                emptyText: Uni.I18n.translate('general.selectACommand', 'MDC', 'Select a command...'),
                displayField: 'name',
                valueField: 'id',
                editable: false,
                disabled: true,
                queryMode: 'local',
                blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
            },
        ];

        this.callParent(arguments);
    }
});




