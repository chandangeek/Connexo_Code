/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.ActiveCalendar', {
    extend: 'Ext.data.Model',
    requires: ['Uni.model.timeofuse.Calendar'],
    fields: [
        {name: 'fromTime', dateFormat: 'time', type: 'date'},
        {name: 'toTime', dateFormat: 'time', type: 'date'},
        {name: 'usagePointId'},
        {name: 'next'},
        {name: 'calendar'}
    ],

    associations: [
        {
            type: 'hasOne',
            name: 'calendar',
            model: 'Uni.model.timeofuse.Calendar',
            associationKey: 'calendar',
            getterName: 'getCalendar'
        },
        {
            type: 'hasOne',
            name: 'next',
            model: 'Imt.usagepointmanagement.model.ActiveCalendar',
            associationKey: 'next',
            getterName: 'getNext'
        }
    ]
});