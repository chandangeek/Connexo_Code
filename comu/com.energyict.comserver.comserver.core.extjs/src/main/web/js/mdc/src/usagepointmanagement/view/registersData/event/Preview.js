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

        me.unit = me.register.get('readingType').names.unitOfMeasure;

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
                            // htmlEncode: false,
                            // renderer: function (value) {
                            //     return Uni.DateTime.formatDateLong(value);
                            // }
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

        me.items[0].items.unshift(
            (function () {
                if (true) {
                    return {
                        itemId: 'measurement-period-field',
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement period'),
                        name: 'measurementPeriod',
                        htmlEncode: false,
                        renderer: function (interval) {
                            return Uni.I18n.translate(
                                'general.dateAtTime', 'MDC', '{0} at {1}',
                                [Uni.DateTime.formatDateLong(new Date(interval.start)), Uni.DateTime.formatTimeLong(new Date(interval.end))], false);
                        }
                    }
                } else {
                    return {
                        itemId: 'measurement-time-field',
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                        name: 'measurementTime',
                        htmlEncode: false,
                        renderer: function (value) {
                            return Uni.DateTime.formatDateLong(value);
                        }
                    }
                }
            })()
        );

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            interval = record.get('interval'),
            title = Uni.I18n.translate(
                'general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateLong(new Date(interval.end)), Uni.DateTime.formatTimeLong(new Date(interval.end))], false);

        me.record = record;
        Ext.suspendLayouts();
        Ext.Array.each(me.query('form'), function (form) {
            form.setTitle(title);
            form.loadRecord(record);
        });
        Ext.resumeLayouts(true);
    }
});