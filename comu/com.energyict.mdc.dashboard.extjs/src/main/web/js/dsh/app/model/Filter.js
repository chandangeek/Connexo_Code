Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', { root: 'filter' }),
    fields: [
        { name: 'deviceGroup', type: 'auto' },
        { name: 'state', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        { name: 'startedBetween', type: 'auto' },
        { name: 'finishedBetween', type: 'auto' }
    ]
});