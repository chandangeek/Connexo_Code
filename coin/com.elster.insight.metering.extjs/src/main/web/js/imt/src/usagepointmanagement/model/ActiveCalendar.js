Ext.define('Imt.usagepointmanagement.model.ActiveCalendar', {
    extend: 'Ext.data.Model',
    requires: ['Uni.model.timeofuse.Calendar'],
    fields: [
        {name: 'fromTime',type: 'date'},
        {name: 'toTime',type: 'date'},
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