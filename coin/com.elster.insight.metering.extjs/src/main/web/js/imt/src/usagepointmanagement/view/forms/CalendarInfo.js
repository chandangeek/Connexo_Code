/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.CalendarInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.calendar-info-form',
    requires: [
        'Imt.usagepointmanagement.view.StepDescription'
    ],

    usagePoint: null,

    defaults: {
        labelWidth: 260
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'step-description',
                itemId: 'calendar-step-description',
                text: Uni.I18n.translate('usagepoint.wizard.calendar.description', 'IMT', 'Select the calendars for this usage point.')
            },
            {
                itemId: 'calendar-transition-info-warning',
                xtype: 'uni-form-error-message',
                width: 595,
                hidden: true
            }
        ];

        me.callParent(arguments);
    },



    getRecord: function () {
        var me = this,
            combos = me.query('combobox'),
            calendars = [];
        Ext.each(combos,function(combobox){
            var activateField = me.down('#activate-calendar-'+ combobox.calendarType);
            if(combobox.getValue()){
                calendars.push({
                    calendar: {
                        id: combobox.getValue()
                    },
                    immediately: activateField.down('radiogroup').getValue()['activateCalendar' + combobox.calendarType] === 'immediate-activation',
                    fromTime: activateField.down('radiogroup').getValue()['activateCalendar' + combobox.calendarType] === 'on-date-activation'?activateField.down('date-time').getValue():null
                });
            }
        });

        return calendars;
    }
});