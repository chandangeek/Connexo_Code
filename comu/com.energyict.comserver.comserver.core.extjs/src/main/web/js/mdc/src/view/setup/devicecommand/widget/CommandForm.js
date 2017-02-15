/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommand.widget.CommandForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-command-add-form',
    requires: [
        'Mdc.widget.DateTimeField',
        'Uni.form.field.DateTime'
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

        this.items =  [
            {
                fieldLabel: Uni.I18n.translate('deviceCommand.add.commandCategorys', 'MDC', 'Command categories'),
                name: 'commandCategory',
                itemId: 'mdc-addCommand-categories-combo',
                store: 'Mdc.store.DeviceMessageCategories',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
            },
            {
                fieldLabel: Uni.I18n.translate('deviceCommand.add.command', 'MDC', 'Command'),
                name: 'command',
                itemId: 'mdc-addCommand-commands-combo',
                displayField: 'name',
                valueField: 'id',
                editable: false,
                disabled: true,
                queryMode: 'local',
                blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
            },
            {
                xtype: 'date-time',
                name: 'releaseDate',
                itemId: 'releaseDate',
                layout: 'hbox',
                required: true,
                fieldLabel: Uni.I18n.translate('deviceCommand.add.releaseDate', 'MDC', 'Release date'),
                dateConfig: {
                    width: 128,
                    flex: 2,
                    minValue: new Date()
                },
                hoursConfig: {
                    width: 60,
                    flex: 1
                },
                minutesConfig: {
                    width: 60,
                    flex:1
                },
                dateTimeSeparatorConfig: {
                    html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                    margin: '0 6 0 6'
                }
            }
        ];

        this.callParent(arguments);
        this.down('date-time[name=releaseDate]').setValue(new Date().getTime())
    }
});




