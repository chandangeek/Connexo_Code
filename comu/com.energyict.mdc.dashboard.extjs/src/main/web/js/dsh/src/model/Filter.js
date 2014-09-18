Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Dsh.model.DateRange'
    ],
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', { root: 'filter' }),
    fields: [
        { name: 'currentStates', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResults', type: 'auto' },
        { name: 'comPortPools', type: 'auto' },
        { name: 'connectionTypes', type: 'auto' },
        { name: 'deviceTypes', type: 'auto' }
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'startedBetween',
            instanceName: 'startedBetween',
            associationKey: 'startedBetween',
            getterName: 'getStartedBetween',
            setterName: 'setStartedBetween'
        },
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'finishedBetween',
            instanceName: 'finishedBetween',
            associationKey: 'finishedBetween',
            getterName: 'getFinishedBetween',
            setterName: 'setFinishedBetween'
        }
    ]
});