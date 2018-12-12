/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.devicetype-tou-preview-form',
    layout: {
        type: 'column'
    },
    requires: [
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.period', 'MDC', 'Period'),
                    itemId: 'periodField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.dayTypes', 'MDC', 'Day types'),
                    itemId: 'dayTypesField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.tariffs', 'MDC', 'Tariffs'),
                    itemId: 'tariffsField'
                },
                {
                    xtype: 'uni-form-empty-message',
                    text: Uni.I18n.translate('timeofuse.calendarIsGhostMessage', 'MDC', "No information available due to status as 'ghost'."),
                    itemId: 'ghostStatusMessage',
                    hidden: true

                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (calendarRecord) {
        var me = this;
        Ext.suspendLayouts();
        me.down('#periodField').show();
        me.down('#dayTypesField').show();
        me.down('#tariffsField').show();
        me.down('#ghostStatusMessage').hide();

        me.down('#periodField').removeAll();
        calendarRecord.periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.down('#dayTypesField').removeAll();
        calendarRecord.dayTypes().each(function (record) {
            me.down('#dayTypesField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + me.getDays(calendarRecord, record.get('id')),
                    margin: '0 0 -10 0'
                }
            );
        });

        me.down('#tariffsField').removeAll();
        calendarRecord.events().each(function (record) {
            me.down('#tariffsField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + record.get('code') + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    calculateDate: function (month, day) {
        var date = new Date();
        date.setMonth(month - 1);
        date.setDate(day);

        return Ext.util.Format.date(date, 'j F')
    },

    getDays: function (record, id) {
        var days = record.daysPerType().findRecord('dayTypeId', id).get('days'),
            response = "";
        if (days.length === 0) {
            return response;
        } else {
            response = ' (';
            Ext.Array.each(days, function (day) {
                response += day + ', '
            });
            response = response.substr(0, response.lastIndexOf(', '));
            response += ')';
            return response;
        }
    },

    showEmptyMessage: function () {
        var me = this;

        me.down('#periodField').hide();
        me.down('#dayTypesField').hide();
        me.down('#tariffsField').hide();
        me.down('#ghostStatusMessage').show();
    }

});