/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.registersData.cumulative.Preview', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.register-data-cumulative-preview',

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
                        },
                        {
                            itemId: 'collected-value-field',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.collectedValue', 'MDC', 'Collected value'),
                            name: 'collectedValue',
                            renderer: function (value) {
                                return value ? value + ' ' + unit : '-';
                            }
                        },
                        {
                            itemId: 'delta-value-field',
                            fieldLabel: Uni.I18n.translate('general.deltaValue', 'MDC', 'Delta value'),
                            name: 'deltaValue',
                            renderer: function (value) {
                                return value ? value + ' ' + unit : '-';
                            }
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            interval = record.get('measurementPeriod'),
            title = interval ? interval.start ? Uni.I18n.translate(
                'general.dateAtTime', 'MDC', '{0} at {1}',
                [Uni.DateTime.formatDateTimeShort(new Date(interval.start)), Uni.DateTime.formatDateTimeShort(new Date(interval.end))], false) :
                Uni.DateTime.formatDateTimeShort(new Date(interval.end)) : '-';

        me.record = record;
        Ext.suspendLayouts();
        Ext.Array.each(me.query('form'), function (form) {
            form.setTitle(title);
            form.loadRecord(record);
        });
        Ext.resumeLayouts(true);
    }
});