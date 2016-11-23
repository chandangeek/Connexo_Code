Ext.define('Imt.usagepointmanagement.view.calendars.HistoryCalendarPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.historyCalendarPreview',
    frame: true,
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
                            fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                            itemId: 'fromUntil',
                            htmlEncode: false
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('general.startOfCalculations', 'IMT', 'Start of calculations'),
                            name: 'startYear'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('general.periods', 'IMT', 'Periods'),
                            itemId: 'periodField'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('general.dayTypes', 'IMT', 'Day types'),
                            itemId: 'dayTypesField'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('general.eventTypes', 'IMT', 'Event types'),
                            itemId: 'tariffsField'
                        }
                    ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (calendarRecord) {
        var me = this;
        Ext.suspendLayouts();

        me.down('form').loadRecord(calendarRecord.getCalendar());
        me.down('#periodField').removeAll();
        calendarRecord.getCalendar().periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'IMT', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.down('#dayTypesField').removeAll();
        calendarRecord.getCalendar().dayTypes().each(function (record) {
            me.down('#dayTypesField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + me.getDays(calendarRecord, record.get('id')),
                    margin: '0 0 -10 0'
                }
            );
        });

        this.down('#tariffsField').removeAll();
        calendarRecord.getCalendar().events().each(function (record) {
            me.down('#tariffsField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + record.get('code') + ')',
                    margin: '0 0 -10 0'
                }
            );
        });

            var from = calendarRecord.get('fromTime'),
                to = calendarRecord.get('toTime');
            var fromTo = to ? Uni.I18n.translate('general.period.fromUntil', 'IMT', 'From {0} until {1}', [
                Uni.DateTime.formatDateTimeShort(from),
                Uni.DateTime.formatDateTimeShort(to)
            ])
                : Uni.I18n.translate('general.period.from', 'IMT', 'From {0}', [
                Uni.DateTime.formatDateTimeShort(from)
            ]);
        this.down('#fromUntil').setValue(fromTo);
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    getDays: function (record, id) {
        var days = record.getCalendar().daysPerType().findRecord('dayTypeId', id).get('days'),
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
    },

    loadRecord: function (calendar) {
        this.fillFieldContainers(calendar);
        this.setTitle(calendar.getCalendar().get('name'));
    }
});