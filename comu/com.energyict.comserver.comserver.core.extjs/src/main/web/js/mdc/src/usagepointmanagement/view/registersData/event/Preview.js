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
            readingType = me.register.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined,
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
                            fieldLabel: Uni.I18n.translate('general.register.eventDate', 'MDC', 'Event date'),
                            name: 'eventDate',
                            renderer: function (value) {
                                return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                            }
                        },
                        {
                            itemId: 'collectedValue-field',
                            fieldLabel: Uni.I18n.translate('general.collectedValue', 'MDC', 'Collected value'),
                            name: 'collectedValue',
                            renderer: function (value) {
                                return value + ' ' + unit;
                            }
                        }
                    ]
                }
            }
        ];

        if (me.register.get('registerType') === 'EVENT_BILLING_VALUE') {
            me.items[0].items.items.unshift(
                {
                    itemId: 'measurement-period-field',
                    fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                    name: 'measurementPeriod',
                    htmlEncode: false,
                    renderer: function (value) {
                        if (!Ext.isEmpty(value)) {
                            var endDate = new Date(value.end);
                            if (value.start && value.end) {
                                var startDate = new Date(value.start);
                                return Uni.DateTime.formatDateTimeLong(startDate) + ' - ' + Uni.DateTime.formatDateTimeLong(endDate);
                            } else {
                                return Uni.DateTime.formatDateTimeLong(endDate);
                            }
                        }
                        return '-';
                    }
                }
            );
        } else {
            me.items[0].items.items.splice(1, 0,
                {
                    itemId: 'measurement-time-field',
                    fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                    name: 'measurementTime',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                    }
                }
            );
        }

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;
        if (me.register.get('registerType') === 'EVENT_BILLING_VALUE') {
            var interval = record.get('measurementPeriod'),
                title = Uni.I18n.translate(
                    'general.dateAtTime', 'MDC', '{0} at {1}',
                    [Uni.DateTime.formatDateTimeShort(new Date(interval.end)), Uni.DateTime.formatDateTimeShort(new Date(interval.end))], false);
        } else {
            var time = record.get('measurementTime'),
                title = time ? Uni.DateTime.formatDateTimeShort(new Date(time)) : '-';
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