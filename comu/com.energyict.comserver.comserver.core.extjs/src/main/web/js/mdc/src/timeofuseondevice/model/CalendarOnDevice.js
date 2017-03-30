/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.model.CalendarOnDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Calendar',
        'Mdc.timeofuseondevice.model.NextPassiveCalendar'
    ],
    fields: [
        {name: 'passiveCalendars', type: 'auto', defaultValue: null},
        {name: 'lastVerified', type: 'number', defaultValue: null},
        {name: 'activeIsGhost', type: 'boolean'}

    ],

    associations: [
        {
            name: 'activeCalendar',
            type: 'hasOne',
            model: 'Uni.model.timeofuse.Calendar',
            getterName: 'getActiveCalendar',
            setterName: 'setActiveCalendar',
            associationKey: 'activeCalendar',
            foreignKey: 'activeCalendar',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Calendar';
            }
        },
        {
            name: 'nextPassiveCalendar',
            type: 'hasOne',
            model: 'Mdc.timeofuseondevice.model.NextPassiveCalendar',
            getterName: 'getNextPassiveCalendar',
            setterName: 'setNextPassiveCalendar',
            associationKey: 'nextPassiveCalendar',
            foreignKey: 'nextPassiveCalendar',
            getTypeDiscriminator: function(node) {
                return 'Mdc.timeofuseondevice.model.NextPassiveCalendar'
            }
        }
    ]

});
