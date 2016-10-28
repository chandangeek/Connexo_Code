Ext.define('Imt.usagepointmanagement.view.calendars.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.activeCalendarPreview',
    frame: false,
    layout: {
        type: 'column'
    },

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'calendar-preview-action-button',
                //  hidden: me.hideAction,
                //  privileges: Imt.privileges.UsagePoint.admin,
                menu: {
                    xtype: 'calendarActionMenu',
                    itemId: 'calendar-action-menu-id',
                    //      type: me.type
                }
            }
        ];

        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'fieldset',
                    title: '<H2>' + Uni.I18n.translate('general.current', 'IMT', 'Current') + '</H2>',
                    border: false,
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
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
                },
                {
                    xtype: 'fieldset',
                    title: '<H2>'+ Uni.I18n.translate('general.planned', 'IMT', 'Planned') +'</H2>',
                    border: false,
                    itemId: 'planned-calendars-fieldset',
                    defaults: {
                        labelWidth: 250
                    }
                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (calendarRecord) {
        var me = this;
        Ext.suspendLayouts();

        me.down('form').loadRecord(calendarRecord);

        me.down('#periodField').removeAll();
        calendarRecord.periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'IMt', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
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

        this.down('#tariffsField').removeAll();
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
    },

    loadRecord: function (calendar) {
        Ext.suspendLayouts();
        this.fillFieldContainers(calendar.getCalendar());
        var temp = calendar;
        while(!Ext.isEmpty(temp.getNext())){
            temp = temp.getNext();
            this.down('#planned-calendars-fieldset').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.calendar', 'IMT', 'Calendar'),
                    value: temp.getCalendar().get('name')
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                    value: Uni.DateTime.formatDateTimeShort(temp.getCalendar().get('toTime'))
                }
            )
        }
        Ext.resumeLayouts(true);
    }
});