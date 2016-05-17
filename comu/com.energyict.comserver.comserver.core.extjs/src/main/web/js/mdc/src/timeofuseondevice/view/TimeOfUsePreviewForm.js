Ext.define('Mdc.timeofuseondevice.view.TimeOfUsePreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-tou-preview-form',
    layout: {
        type: 'column'
    },

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                    itemId: 'nameField',
                    name: 'name',
                    value: '-'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.lastVerifief', 'MDC', 'Last verified'),
                    itemId: 'lastVefirifiedDisplayField',
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
            counter = 0,
            value;
        Ext.suspendLayouts();

        me.down('#nameField').setValue(calendarRecord.get('name'));
        if(record.get('lastVerified')) {
            var creationTime = record.get('lastVerified');
            me.down('#lastVefirifiedDisplayField').setValue(Uni.DateTime.formatDateLong(new Date(creationTime)));
        } else {
            me.down('#lastVefirifiedDisplayField').setValue('-');
        }

        me.down('#periodField').removeAll();
        calendarRecord.periods().each(function (record) {
            counter ++;
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
        calendarRecord.dayTypes().each(function (record) {
            counter ++;
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
        calendarRecord.events().each(function (record) {
            counter ++;
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

        this.down('#passiveField').removeAll();
        if(passiveCalendars) {
            Ext.Array.each(passiveCalendars, function (passiveCalendar) {
                value = passiveCalendar.name;
                if(passiveCalendar.ghost) {
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
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    fillEmpty: function(counter, itemId) {
        var me = this;
        if(counter <= 0) {
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
        date.setMonth(month);
        date.setDate(day);

        return Ext.util.Format.date(date, 'j F')
    }

});