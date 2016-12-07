Ext.define('Uni.view.calendar.TimeOfUsePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.timeOfUsePreview',
    record: null,
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
                    fieldLabel: Uni.I18n.translate('general.startOfCalculations', 'UNI', 'Start of calculations'),
                    name: 'startYear'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.period', 'UNI', 'Period'),
                    itemId: 'periodField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.dayTypes', 'UNI', 'Day types'),
                    itemId: 'dayTypesField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.eventTypes', 'UNI', 'Event types'),
                    itemId: 'tariffsField'
                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (calendarRecord) {
        var me = this;
        Ext.suspendLayouts();
        me.setTitle(Ext.String.htmlEncode(calendarRecord.get('name')));
        me.down('form').loadRecord(calendarRecord);
        me.setTitle(Ext.String.htmlEncode(calendarRecord.get('name')));
        me.down('#periodField').removeAll();
        calendarRecord.periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'UNI', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
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
                    value: record.get('name') + ' (' + me.getDaysForType(calendarRecord, record) + ')',
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

    calculateDate: function (month, day) {
        var date = new Date();
        date.setMonth(month - 1);
        date.setDate(day);

        return Ext.util.Format.date(date, 'j F')
    },

    getDaysForType: function (calendarRecord, record) {
        var me = this,
            i,
            dayTypeId = record.get('id'),
            dayArray = calendarRecord.get('weekTemplate'),
            response = '';
        for (i = 1; i < dayArray.length; i++) {
            if (dayArray[i].type === dayTypeId) {
                response += dayArray[i].name + ', '
            }
        }

        response = response.substr(0, response.lastIndexOf(', '));

        return response;
    }

});