/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.notCumulative.Preview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.register-data-noCumulative-preview',

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
                            itemId: 'collected-value-field',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.collectedValue', 'MDC', 'Collected value'),
                            name: 'collectedValue',
                            renderer: function (value) {
                                return value ? value + ' ' + unit : '-';
                            }
                        }
                    ]
                }
            }
        ];

        me.items[0].items.items.unshift(
            (function () {
                if (me.register.get('registerType') === 'NOT_CUMULATIVE_BILLING_VALUE') {
                    return {
                        itemId: 'measurement-period-field',
                        fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'MDC', 'Measurement period'),
                        name: 'measurementPeriod',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
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
                } else {
                    return {
                        itemId: 'measurement-time-field',
                        fieldLabel: Uni.I18n.translate('general.measurementTime', 'MDC', 'Measurement time'),
                        name: 'measurementTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    }
                }
            })()
        );


        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;
        if (me.register.get('registerType') === 'NOT_CUMULATIVE_BILLING_VALUE') {
            var interval = record.get('measurementPeriod'),
                title = interval ? interval.start ? Uni.I18n.translate(
                    'general.dateAtTime', 'MDC', '{0} at {1}',
                    [Uni.DateTime.formatDateTimeShort(new Date(interval.start)), Uni.DateTime.formatDateTimeShort(new Date(interval.end))], false) :
                    Uni.DateTime.formatDateTimeShort(new Date(interval.end)) : '-';
        } else {
            var time = record.get('measurementTime'),
                title = time ? Uni.DateTime.formatDateTimeShort(new Date(time)) : '-'
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