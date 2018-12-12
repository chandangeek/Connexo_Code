/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommandStep2', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-command-step2',
    ui: 'large',

    requires: [
        'Uni.util.FormInfoMessage',
        'Uni.util.FormErrorMessage',
        'Uni.form.field.DateTime',
        'Mdc.commands.store.CommandCategoriesForDeviceGroup'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                padding: '0 0 0 0',
                margin: '0 0 0 0',
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('add.command.step2.info', 'MDC',
                            'Only commands that are enabled on all devices in the selected device group and for which you have sufficient privileges on the device configuration are available.')
                    }
                ]
            },
            {
                xtype: 'uni-form-error-message',
                itemId: 'mdc-add-command-step2-error',
                width: 550,
                hidden: true
            },
            {
                xtype: 'combobox',
                name: 'commandCategory',
                fieldLabel: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                itemId: 'mdc-add-command-step2-category-combo',
                emptyText: Uni.I18n.translate('general.selectACommandCategory', 'MDC', 'Select a command category...'),
                store: 'Mdc.commands.store.CommandCategoriesForDeviceGroup',
                editable: false,
                required: true,
                allowBlank: false,
                valueField: 'id',
                displayField: 'name',
                labelWidth: 200,
                queryMode: 'local',
                width: 550,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'combobox',
                name: 'command',
                fieldLabel: Uni.I18n.translate('deviceCommand.add.command', 'MDC', 'Command'),
                itemId: 'mdc-add-command-step2-command-combo',
                emptyText: Uni.I18n.translate('general.selectACommand', 'MDC', 'Select a command...'),
                displayField: 'name',
                valueField: 'id',
                required: true,
                allowBlank: false,
                editable: false,
                disabled: true,
                queryMode: 'local',
                labelWidth: 200,
                width: 550
            },
            {
                xtype: 'date-time',
                name: 'releaseDate',
                itemId: 'mdc-add-command-step2-releaseDate',
                layout: 'hbox',
                required: true,
                fieldLabel: Uni.I18n.translate('general.releaseDate', 'MDC', 'Release date'),
                labelWidth: 200,
                width: 550,
                value: new Date(),
                dateConfig: {
                    width: 128,
                    flex: 2,
                    minValue: new Date(),
                    allowBlank: false,
                    editable: false,
                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
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
            },
            {
                itemId: 'mdc-add-command-step2-property-header',
                margin: '25 0 0 0'
            },
            {
                xtype: 'property-form',
                itemId: 'mdc-add-command-step2-property-form',
                margin: '20 0 0 0',
                defaults: {
                    labelWidth: 200,
                    resetButtonHidden: false,
                    width: 336 // To be aligned with the above
                }
            }
        ];
        me.callParent(arguments);
    }
});