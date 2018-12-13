/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.view.TimeOfUsePreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-tou-preview-form',
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
                    xtype: 'uni-form-empty-message',
                    itemId: 'commandWillNotBePickedUp',
                    hidden: true,
                    margin: '5 0 5 0'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                    itemId: 'nameField',
                    name: 'name',
                    value: '-'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.lastVerified', 'MDC', 'Last verified'),
                    itemId: 'lastVerifiedDisplayField',
                    value: '-'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.startOfCalculations', 'MDC', 'Start of calculations'),
                    itemId: 'startYear',
                    value: '-'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.periods', 'MDC', 'Periods'),
                    itemId: 'periodField',
                    value: '-'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.dayTypes', 'MDC', 'Day types'),
                    itemId: 'dayTypesField',
                    value: '-'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.tariffs', 'MDC', 'Tariffs'),
                    itemId: 'tariffsField',
                    value: '-'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.passiveCalendars', 'MDC', 'Passive calendar(s)'),
                    itemId: 'passiveField',
                    //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.supportsPassive,
                    value: '-'
                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (record) {
        var me = this,
            calendarRecord = record.getActiveCalendar(),
            passiveCalendars = record.get('passiveCalendars'),
            counter = 0;
        Ext.suspendLayouts();

        if (calendarRecord !== null) {

            if (record.get('activeIsGhost')) {
                me.down('#nameField').setValue(calendarRecord.get('name') + ' (' + Uni.I18n.translate('calendars.ghost', 'MDC', 'Ghost') + ')');
                me.down('#startYear').hide();
            } else {
                me.down('#nameField').setValue(calendarRecord.get('name'));
                me.down('#startYear').setValue(calendarRecord.get('startYear'));
            }

            if (record.get('lastVerified') && record.get('lastVerified') !== 0) {
                var creationTime = record.get('lastVerified');
                me.down('#lastVerifiedDisplayField').setValue(Uni.DateTime.formatDateTime(new Date(creationTime), Uni.DateTime.LONG, Uni.DateTime.SHORT));
            } else {
                me.down('#lastVerifiedDisplayField').setValue('-');
            }
        }

        me.down('#periodField').removeAll();
        me.down('#periodField').setVisible(!record.get('activeIsGhost'));
        calendarRecord.periods().each(function (record) {
            counter++;
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.fillEmpty(counter, '#periodField');
        counter = 0;

        me.down('#dayTypesField').removeAll();
        me.down('#dayTypesField').setVisible(!record.get('activeIsGhost'));
        calendarRecord.dayTypes().each(function (record) {
            counter++;
            me.down('#dayTypesField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + me.getDays(calendarRecord, record.get('id')),
                    margin: '0 0 -10 0'
                }
            );
        });
        me.fillEmpty(counter, '#dayTypesField');
        counter = 0;

        me.down('#tariffsField').removeAll();
        me.down('#tariffsField').setVisible(!record.get('activeIsGhost'));
        calendarRecord.events().each(function (record) {
            counter++;
            me.down('#tariffsField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + record.get('code') + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.fillEmpty(counter, '#tariffsField');
        counter = 0;

        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    fillEmpty: function (counter, itemId) {
        var me = this;
        if (counter <= 0) {
            me.down(itemId).add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: '-',
                    margin: '0 0 -10 0'
                }
            );
        }
    },

    fillPassiveCalendars: function(passiveCalendars) {
        var me = this;
        if(!me.down('#passiveField')) {
            return;
        }
        me.down('#passiveField').removeAll();
        if (passiveCalendars) {
            Ext.Array.each(passiveCalendars, function (passiveCalendar) {
                value = passiveCalendar.name;
                if (passiveCalendar.ghost) {
                    value += ' (' + Uni.I18n.translate('calendars.ghost', 'MDC', 'Ghost') + ')';
                }
                me.down('#passiveField').add(
                    {
                        xtype: 'displayfield',
                        fieldLabel: undefined,
                        value: value,
                        margin: '0 0 -10 0'
                    }
                );
            });
        } else {
            me.down('#passiveField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: '-',
                    margin: '0 0 -10 0'
                }
            );
        }
    },

    fillWithDashes: function() {
        var me = this,
            dash = {
                xtype: 'displayfield',
                fieldLabel: undefined,
                value: '-',
                margin: '0 0 -10 0'
            };
        me.down('#tariffsField').add(dash);
        me.down('#dayTypesField').add(dash);
        me.down('#periodField').add(dash);

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

    calculateDate: function (month, day) {
        var date = new Date();
        date.setMonth(month - 1);
        date.setDate(day);

        return Ext.util.Format.date(date, 'j F')
    }

});