/**
 * @class Uni.model.timeofuse.Ragne
 */
Ext.define('Uni.model.timeofuse.Range', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'event', type: 'number'},
        {name: 'fromHour', type: 'number'},
        {name: 'fromMinute', type: 'number'},
        {name: 'fromSecond', type: 'number'}
    ]
});