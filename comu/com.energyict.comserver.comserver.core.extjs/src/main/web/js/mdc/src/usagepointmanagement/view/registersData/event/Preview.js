/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.event.Preview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.register-data-event-preview',
    requires: [
        'Cfg.view.field.ReadingQualities'
    ],

    router: null,
    register: null,

    initComponent: function () {
        var me = this,
            defaults = {
                xtype: 'displayfield',
                labelWidth: 200
            };

        me.items = [
            {
                title: Uni.I18n.translate('general.general', 'MDC', 'General'),
                items: {
                    itemId: 'general-tab',
                    xtype: 'form',
                    frame: true,
                    defaults: defaults,
                    items: [
                        {
                            itemId: 'eventDate-field',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.eventDate', 'MDC', 'Event date'),
                            name: 'eventDate',
                            renderer: function (value) {
                                return value ? Uni.DateTime.formatDateLong(new Date(value)) : '-';
                            }
                        },
                        {
                            itemId: 'collectedValue-field',
                            fieldLabel: Uni.I18n.translate('device.dataValidation.collectedValue', 'MDC', 'Collected value'),
                            name: 'collectedValue'
                        }
                    ]
                }
            }
        ];

        me.items[0].items.items.unshift(
            (function () {
                if (me.register.get('registerType') === 'EVENT_BILLING_VALUE') {
                    return {
                        itemId: 'measurement-period-field',
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement period'),
                        name: 'measurementPeriod',
                        htmlEncode: false,
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                var endDate = new Date(value.end);
                                if (value.start && value.end) {
                                    var startDate = new Date(value.start);
                                    return Uni.DateTime.formatDateTimeShort(startDate) + ' - ' + Uni.DateTime.formatDateTimeShort(endDate);
                                } else {
                                    return Uni.DateTime.formatDateTimeShort(endDate);
                                }
                            }
                            return '-';
                        }
                    }
                } else {
                    return {
                        itemId: 'measurement-time-field',
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                        name: 'measurementTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(new Date(value)) : '-';
                        }
                    }
                }
            })()
        );

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;
        if (me.register.get('registerType') === 'EVENT_BILLING_VALUE') {
            var interval = record.get('measurementPeriod'),
                title = Uni.I18n.translate(
                'general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(new Date(interval.end)), Uni.DateTime.formatTimeLong(new Date(interval.end))], false);
        } else {
            var time = record.get('measurementTime'),
                title = time ? Uni.DateTime.formatDateLong(new Date(time)) : '-';
        }
        me.record = record;
        Ext.suspendLayouts();
        Ext.Array.each(me.query('form'), function (form) {
            form.setTitle(title);
            form.loadRecord(record);
        });
        Ext.resumeLayouts(true);
    }
});