Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Dsh.model.DateRange'
    ],
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', { root: 'filter' }),
    fields: [
        { name: 'state', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'deviceType', type: 'auto' }
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'startedBetween',
            instanceName: 'startedBetween',
            associatedName: 'startedBetween',
            associationKey: 'startedBetween',
            getterName: 'getStartedBetween',
            setterName: 'setStartedBetween'
        },
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'finishedBetween',
            instanceName: 'finishedBetween',
            associatedName: 'finishedBetween',
            associationKey: 'finishedBetween',
            getterName: 'getFinishedBetween',
            setterName: 'setFinishedBetween'
        }
    ]
});