Ext.define('CSMonitor.model.performance.PoolsPerformance', {
    extend: 'Ext.data.Model',
    requires: [
        'CSMonitor.model.performance.Pool'
    ],
    fields: [
        'priority'
    ],

    associations: [
        {
            type: 'hasMany',
            model: 'CSMonitor.model.performance.Pool',
            associationKey: 'pools',
            name: 'pools'
        }
    ]
});