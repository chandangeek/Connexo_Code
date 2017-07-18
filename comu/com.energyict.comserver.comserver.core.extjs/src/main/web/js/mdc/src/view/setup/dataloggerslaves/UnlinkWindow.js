/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.UnlinkWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.dataloggerslave-unlink-window',
    modal: true,
    dataLoggerSlaveRecord:null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this,
            minimalUnlinkDate = 0;

        if (me.dataLoggerSlaveRecord && me.dataLoggerSlaveRecord.get('linkingTimeStamp')) {
            var momentOfDate = moment(me.dataLoggerSlaveRecord.get('linkingTimeStamp'));
            momentOfDate.startOf('day');
            minimalUnlinkDate = momentOfDate.unix() * 1000;
        }

        me.items = {
            xtype: 'form',
            itemId: 'mdc-dataloggerslave-unlink-window-form',
            padding: 0,
            defaults: {
                width: 500,
                labelWidth: 175
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
                    xtype: 'fieldcontainer',
                    margin: '10 0 10 0',
                    fieldLabel: Uni.I18n.translate('general.unlinkOn', 'MDC', 'Unlink on'),
                    itemId: 'mdc-unlink-date-container',
                    required: true,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'date-time',
                            valueInMilliseconds: false,
                            itemId: 'mdc-dataloggerslave-unlink-window-date-picker',
                            layout: 'hbox',
                            labelAlign: 'left',
                            value: new Date(),
                            style: {
                                border: 'none',
                                padding: 0,
                                marginBottom: '10px'
                            },
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault),
                                minValue: new Date(minimalUnlinkDate),
                                width: 155
                            },
                            hoursConfig: {
                                width: 60
                            },
                            minutesConfig: {
                                width: 60
                            }
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-dataloggerslave-unlink-window-unlink',
                            text: Uni.I18n.translate('general.unlink', 'MDC', 'Unlink'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-dataloggerslave-unlink-window-cancel',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
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
