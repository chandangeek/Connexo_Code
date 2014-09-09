Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'deviceGroup', type: 'auto' },
        { name: 'state', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'deviceType', type: 'auto' }
//        'startedBetween', 'finishedBetween',
//        {name: 'from', mapping: 'startedBetween.from'}
    ],
//    associations: [
//        { name: 'startedBetween', type: 'hasOne', model: 'Dsh.model.DateTimeRange', associationKey: 'startedBetween' },
//        { name: 'finishedBetween', type: 'hasOne', model: 'Dsh.model.DateTimeRange', associationKey: 'finishedBetween' }
//    ],
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', {root: 'filter'})
});