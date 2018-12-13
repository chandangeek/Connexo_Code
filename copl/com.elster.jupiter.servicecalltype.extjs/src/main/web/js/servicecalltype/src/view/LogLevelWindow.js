/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.LogLevelWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.log-level-window',
    modal: true,
    title: Uni.I18n.translate('general.selectLogLevel', 'SCT', 'Select log level'),
    record: null,
    itemId: 'log-level-window',
    store: null,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'change-log-level-form',
            padding: 0,
            defaults: {
                width: 418,
                labelWidth: 150
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'combobox',
                    itemId: 'log-level-field',
                    name: 'logLevel',
                    fieldLabel: Uni.I18n.translate('general.logLevel', 'SCT', 'Log level'),
                    required: true,
                    editable: false,
                    store: me.store,
                    valueField: 'id',
                    displayField: 'displayValue',
                    queryMode: 'local',
                    margin: '10 0 0 0',
                    value: me.record ? me.record.get('logLevelName') : "WARNING",
                    emptyText: Uni.I18n.translate('general.selectALogLevel', 'SCT', 'Select a log level...')
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'save-log-level-button',
                            text: Uni.I18n.translate('general.save', 'SCT', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'SCT', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});