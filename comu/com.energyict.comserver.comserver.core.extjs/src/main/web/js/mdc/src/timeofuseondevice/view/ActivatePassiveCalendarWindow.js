/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.view.ActivatePassiveCalendarWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.activate-passive-calendar-window',
    modal: true,
    title: Uni.I18n.translate('timeofuse.activatePassiveCalendar', 'MDC', 'Activate passive calendar'),
    record: null,
    deviceId: null,
    requires: [
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'activate-passive-window-form',
            padding: 0,
            deviceId: me.deviceId,
            defaults: {
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.activationDate', 'MDC', 'Activation date'),
                    itemId: 'activate-passive-calendar-container',
                    required: true,
                    layout: 'hbox',
                    items: [
                        {
                            itemId: 'activate-passive-calendar',
                            xtype: 'radiogroup',
                            columns: 1,
                            vertical: true,
                            width: 100,
                            defaults: {
                                name: 'activateCalendar',
                                style: {
                                    overflowX: 'visible',
                                    whiteSpace: 'nowrap'
                                }
                            },
                            listeners: {
                                change: function (field, newValue, oldValue) {
                                    me.down('#activation-date-values').setDisabled(newValue.activateCalendar !== 'on-date-activation');
                                }
                            },
                            items: [
                                {
                                    itemId: 'now-activation-passive-date',
                                    boxLabel: Uni.I18n.translate('general.now', 'MDC', 'Now'),
                                    inputValue: 'immediate-activation',
                                    checked: true
                                },
                                {
                                    itemId: 'on-activation-passive-date',
                                    boxLabel: Uni.I18n.translate('general.on', 'MDC', 'On'),
                                    inputValue: 'on-date-activation'
                                }
                            ]
                        },
                        {
                            itemId: 'activation-date-values',
                            xtype: 'fieldcontainer',
                            name: 'activationDateValues',
                            margin: '30 0 10 -40',
                            disabled: true,
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'date-time',
                                    itemId: 'activation-on',
                                    layout: 'hbox',
                                    name: 'activationOn',
                                    dateConfig: {
                                        allowBlank: true,
                                        value: new Date(),
                                        editable: false,
                                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                    },
                                    hoursConfig: {
                                        width: 55,
                                        value: new Date().getHours()
                                    },
                                    minutesConfig: {
                                        width: 55,
                                        value: new Date().getMinutes()
                                    }
                                }
                            ]
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
                            itemId: 'activate-passive-calendar-button',
                            text: Uni.I18n.translate('general.send', 'MDC', 'Send'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
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