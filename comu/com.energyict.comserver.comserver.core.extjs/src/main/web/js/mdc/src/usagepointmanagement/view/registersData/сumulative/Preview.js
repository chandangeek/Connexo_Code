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
                            htmlEncode: false,
                            renderer: function (value) {
                                return Uni.DateTime.formatDateLong(value.end);
                            }
                        },
                        {
                            itemId: 'collected-value-field',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                            name: 'collectedValue',
                            htmlEncode: false,
                            renderer: function (value) {
                                return Uni.DateTime.formatDateLong(value);
                            }
                        },
                        {
                            itemId: 'delta-value-field',
                            fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'MDC', 'Data validated'),
                            name: 'dataValidated'
                        }
                    ]
                }
            }
        ];

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